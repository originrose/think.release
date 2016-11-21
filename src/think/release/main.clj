(ns think.release.main
  (:gen-class))

(defn print-help
  []
  (println "possible arguments are:
version bump-type project.clj-path
update version-str [project-top-dir]

version will attempt to output the version bumped by bump type which must be one of:
\t[major,minor,release].
update will attempt to update all project.clj files lying at or below a given directory
\tso that the are exactly the given version str and any projects depending on them are
\tthe given version-str"))


(defn -main
  [& args]
  (when (< 2 (count args))
    (print-help))

  (let [command (keyword command)]
    (condp = command
      :version )
    ))
