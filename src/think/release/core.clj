(ns think.release.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.pprint :as pprint])
  (:import [java.io File]))


(defn recurse-find-project-files
  [dirname]
  (let [all-files (.listFiles ^File (io/file dirname))
        all-dirs (filter #(and (.isDirectory ^File %)
                               (not= "checkouts" (.getName ^File %))) all-files)
        all-projects (filter #(= "project.clj" (.getName ^File %)) all-files)]
    (concat all-projects (mapcat recurse-find-project-files all-dirs))))

(def version-regex #"(\w+)\.(\w+)\.(\w+)(.*)")

(def snapshot-indicator "-SNAPSHOT")

(defn parse-long
  [part-name data-str]
  (try
    (Long/parseLong data-str)
    (catch Throwable e
      (throw (RuntimeException. (format "Failed to parse version part %s" part-name)
                                e)))))

(defn parse-version
  [^String version-string]
  (let [snapshot? (.endsWith version-string snapshot-indicator)
        version-string (if snapshot?
                         (.substring version-string 0 (- (.length version-string)
                                                         (.length ^String snapshot-indicator))))
        matcher (re-matcher  version-string)
        groups (vec (drop 1 (re-find version-regex matcher)))]
    (when-not (seq groups)
      (throw (ex-info "Failed to parse project version string"
                      {:version-string version-string})))
    {:major (parse-long :major (get groups 0 "0"))
     :minor (parse-long :minor (get groups 1 "0"))
     :bugfix (parse-long :bugfix (get groups 2 "0"))
     :snapshot? snapshot?
     :rest (get groups 3)}))


(defn crap-parse-project-file
  "Pull first line out of project file and return the third thing in that line space delimited"
  [proj-file]
  (let [file-lines (string/split (slurp proj-file) #"\n")
        first-line (first file-lines)
        rest-file (rest file-lines)]
    {:first-line-vec (string/split first-line #"\s+")
     :proj-data rest-file}))


(defn get-version-from-project
  [parsed-project]
  (parse-version (read-string (nth (:first-line-vec parsed-project) 2))))


(defn write-version-to-project
  [parsed-project version snapshot?]
  (update-in parsed-project [:first-line-vec 2]
             (constantly
              (format "\"%s.%s.%s%s%s\"" (:major version) (:minor version) (:bugfix version)
                      (:rest version) (if snapshot? "-SNAPSHOT" "")))))



(defn bump-project-version
  [project-file-data bump-type snapshot?]
  (let [version (-> (get-version-from-project project-file-data)
                    (update-in [bump-type] inc))
        version (condp = bump-type
                  :major (-> version
                             (assoc :minor 0)
                             (assoc :bugfix 0))
                  :minor (assoc version :bugfix 0)
                  version)]
    (write-version-to-project version snapshot?)))

(def dependency-regex #"\[([\w\.\-/]+)\s+\"([^\"]+)\"\]")

(def lein-dependency-regex #"(\d+)\.(\d+)\.(\d+)(?:-(?!SNAPSHOT)([^\-]+))?(?:-(SNAPSHOT))?")


(defn re-update-in
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


(defn set-project-dependencies
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


(defn set-project-group-to-version
  [version-string project-file-seq]
  (let [project-dep-map (->> project-file-seq
                             (map crap-parse-project-file)
                             (map (fn [proj]
                                    [(get-in proj [:first-line-vec 1]) version-string]))
                             (into {}))]
    (doseq [proj-file project-file-seq]
      (let [updated-proj (-> (crap-parse-project-file proj-file)
                             (update-in [:first-line-vec 2] (constantly (format "\"%s\"" version-string)))
                             (set-project-dependencies project-dep-map))
            proj-str (string/join "\n"
                                  (concat [(string/join " " (:first-line-vec updated-proj))]
                                          (:proj-data updated-proj)))]
        (spit proj-file (str proj-str "\n"))))))
