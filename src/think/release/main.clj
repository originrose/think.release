(ns think.release.main
  (:require [clojure.tools.cli :refer [parse-opts]]
            [think.config.core :refer [with-config get-config get-configurable-options]]
            [think.release.core :as release]
            [clojure.string :as s])
  (:gen-class))

(def docs
  {:project-version "project-version version to set the project to.  If empty then perform:
if first project.clj version contains snapshot
 - remove snapshot, (if --date-version add date).

else
 - if dated remove
 - else increment last integer
 - add snapshot"
   :project-directory "The top level directory to recursively search for projects."})

(defn- print-help
  []
  (println "commands:
set-version: Set all projects in directory to version x.
show-project-version: show the new version set-version will enact.
list-projects: show the list of projects set-project will affect
--

Arguments:
")
  (->> docs
       (map (fn [[k v]]
              (println k v)))
       dorun))

(defn build-command-map-from-namespace
  [ns-sym]
  (->> (ns-publics ns-sym)
       (map (fn [[k v]]
              [(keyword (name k)) v]))
       (into {})))


(defn cli-options
  [doc-map]
  (concat
  (for [c (get-configurable-options)]
    [nil (str "--" (name c) " "  (s/upper-case (name c))) (get doc-map c)
     :default (get-config c)
     :id c])
  [["-h" "--help"]]))


(defn auto-main
  "Find any public symbols in core-ns and those become the commands.
Uses config system to parse command line arguments."
  [help-fn core-ns-sym doc-map args]
  (require core-ns-sym)
  (let [{:keys [options arguments errors summary]} (parse-opts args (cli-options doc-map))
        arguments (map #(if (and (string? %) (= (first %) \:))
                          (keyword (subs % 1))
                          %)
                       arguments)]

    (with-config (apply concat (into {} options))
      (try
        (if-let [fn-val (-> (build-command-map-from-namespace core-ns-sym)
                            (get (keyword (first arguments))))]
          (do (fn-val (rest arguments))
              0)
          (do
            (help-fn)
            -1))
        (catch Throwable e
          (binding [*out* *err*]
            (clojure.pprint/pprint ["Main failure"
                                    {:error e
                                     :main-ns core-ns-sym
                                     :options (->> options
                                                   (mapv (fn [[k v]]
                                                           [k (get-config k)]))
                                                   (into {}))
                                     :arguments arguments}]))
          -1)))))


(defn -main
  [& args]
  (-> (auto-main print-help 'think.release.core docs args)
      (System/exit)))
