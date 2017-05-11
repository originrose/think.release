(defproject thinktopic/think.release "0.1.0-2017-05-11-14-13"
  :description "Library to perform releases clojure releases on a project or a directory of
  projects."
  :url "http://github.com/thinktopic/think.release"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [thinktopic/think.config "0.3.0"]
                 [org.clojure/tools.cli "0.3.5"]]

  :main         think.release.main

  :profiles { :uberjar {:aot :all
                        :uberjar-name "think.release.jar"}}

  :plugins [[lein-environ "1.0.0"]
            [s3-wagon-private "1.3.0"]]

  :aliases {"release" ["run" "-m" "think.release.main"]})
