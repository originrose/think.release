(ns think.release.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.pprint :as pprint]
            [think.config.core :as config])
  (:import [java.io File]
           [java.util Date]
           [java.text SimpleDateFormat]))


(defn ^:private recurse-find-project-files
  [dirname]
  (let [all-files (.listFiles ^File (io/file dirname))
        all-dirs (filter #(and (.isDirectory ^File %)
                               (not= "checkouts" (.getName ^File %))) all-files)
        all-projects (filter #(= "project.clj" (.getName ^File %)) all-files)]
    (concat all-projects (mapcat recurse-find-project-files all-dirs))))

(def ^:private version-regex #"(\w+)\.(\w+)\.(\w+)(.*)")

(def ^:private snapshot-indicator "-SNAPSHOT")

(defn ^:private parse-long
  [part-name data-str]
  (try
    (Long/parseLong data-str)
    (catch Throwable e
      (throw (RuntimeException. (format "Failed to parse version part %s" part-name)
                                e)))))

(defn ^:private parse-version
  [^String version-string]
  (let [snapshot? (.endsWith version-string snapshot-indicator)
        version-string (if snapshot?
                         (.substring version-string 0 (- (.length version-string)
                                                         (.length ^String snapshot-indicator)))
                         version-string)
        matcher (re-matcher version-regex version-string)
        groups (vec (drop 1 (re-find matcher)))]
    (when-not (seq groups)
      (throw (ex-info "Failed to parse project version string"
                      {:version-string version-string})))
    {:major (parse-long :major (get groups 0 "0"))
     :minor (parse-long :minor (get groups 1 "0"))
     :bugfix (parse-long :bugfix (get groups 2 "0"))
     :snapshot? snapshot?
     :rest (get groups 3)}))


(defn ^:private crap-parse-project-file
  "Pull first line out of project file and return the third thing in that line space delimited"
  [proj-file]
  (let [file-lines (string/split (slurp proj-file) #"\n")
        first-line (first file-lines)
        rest-file (rest file-lines)]
    {:first-line-vec (string/split first-line #"\s+")
     :proj-data rest-file}))


(defn ^:private get-version-from-project
  [parsed-project]
  (parse-version (read-string (nth (:first-line-vec parsed-project) 2))))


(defn ^:private version->string
  [version]
  (format "%s.%s.%s%s%s" (:major version) (:minor version) (:bugfix version)
          (:rest version) (if (:snapshot? version) "-SNAPSHOT" "")))


(defn ^:private write-version-to-project
  [parsed-project version snapshot?]
  (update-in parsed-project [:first-line-vec 2]
             (constantly (version->string version))))


(def ^:private dependency-regex #"\[([\w\.\-/]+)\s+\"([^\"]+)\"\]")

(def ^:private lein-dependency-regex #"(\d+)\.(\d+)\.(\d+)(?:-(?!SNAPSHOT)([^\-]+))?(?:-(SNAPSHOT))?")


(defn ^:private re-update-in
  "Recursive function to do updates according to a regex within a string.
group-fn receives a vector of the regex groups, the first of which always
contains the entire string matched."
  [s re group-fn]
  (let [matcher (re-matcher re s)]
    (if (.find matcher)
      (string/join
       [(.substring s 0 (.start matcher))
        (group-fn (re-groups matcher))
        (re-update-in (.substring s (.end matcher)) re group-fn)])
      s)))


(defn ^:private set-project-dependencies
  "If a dependency of the project is in the map, update it
to the version specified in the dependency map.  Relies implicitly
on the version being on the same line as the dependency"
  [project-file-data dependency-map]
  (update-in project-file-data [:proj-data]
             #(mapv (fn [data-line]
                      (re-update-in
                       data-line dependency-regex
                       (fn [[_ proj version-string]]
                         (format "[%s \"%s\"]" proj
                                 (get dependency-map proj version-string)))))
                    %)))


(defn ^:private set-project-group-to-version
  [version-string project-file-seq]
  (let [project-dep-map (->> project-file-seq
                             (map crap-parse-project-file)
                             (map (fn [proj]
                                    [(get-in proj [:first-line-vec 1]) version-string]))
                             (into {}))]
    (doseq [proj-file project-file-seq]
      (let [updated-proj (-> (crap-parse-project-file proj-file)
                             (update-in [:first-line-vec 2] (constantly (format "\"%s\""
                                                                                version-string)))
                             (set-project-dependencies project-dep-map))
            proj-str (string/join "\n"
                                  (concat [(string/join " " (:first-line-vec updated-proj))]
                                          (:proj-data updated-proj)))]
        (spit proj-file (str proj-str "\n"))))))

(defn ^:private project-directory
  []
  (let [proj-dir (or (config/unchecked-get-config :project-directory)
                     "")]
    (if (= proj-dir "")
      (System/getProperty "user.dir")
      proj-dir)))


(defn ^:private project-list
  []
  (recurse-find-project-files (project-directory)))


(defn ^:private bump-project-version
  [version bump-type]
  (cond
    (and (= bump-type :release)
         (get version :snapshot?))
    (assoc version
           :snapshot? false
           :rest (if (config/get-config :date-version)
                   (str "-"
                        (.format (SimpleDateFormat. "yyyy-MM-dd-hh-mm") (Date.)))
                   ""))
    (and (= bump-type :snapshot)
         (not (get version :snapshot?)))
    (-> (if (= "" (or (get version :rest) ""))
          (update version :bugfix #(inc (or % 0)))
          (assoc version :rest ""))
        (assoc :snapshot? true))
    :else
    version))



(defn ^:private project-version
  [project-list bump-type]
  (let [project-version (or (config/unchecked-get-config :project-version)
                            "")]
    (if (= project-version "")
      (-> (crap-parse-project-file (first project-list))
          (get-version-from-project)
          (bump-project-version bump-type)
          version->string)
      project-version)))


(defn list-projects
  "List the projects found under the project directory."
  [& args]
  (clojure.pprint/pprint [(project-directory)
                          (project-list)]))


(defn show-release-version
  "Show the pending release version."
  [& args]
  (println (project-version (project-list) :release)))


(defn show-snapshot-version
  "Show the pending snapshot version."
  [& args]
  (println (project-version (project-list) :snapshot)))


(defn show-current-version
  "Show the current project version"
  [& args]
  (println (project-version (project-list) :current)))


(defn ^:private set-version
  [bump-type]
  (let [project-dir (project-directory)]
    (if-let [project-list (-> (recurse-find-project-files project-dir)
                              seq)]
      (set-project-group-to-version (project-version project-list bump-type)
                                    project-list)
      (throw (ex-info "Failed to find any projects!"
                      {:project-directory (project-directory)})))))


(defn set-release-version
  "Set the project to the next release version if it is a snapshot version."
  [& args]
  (set-version :release))


(defn set-snapshot-version
  "Set the project to the next snapshot version if it is a non-snapshot version."
  [& args]
  (set-version :snapshot))
