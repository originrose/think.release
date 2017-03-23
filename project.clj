(defproject thinktopic/think.release "0.1.0-2017-03-23-08-26"
  :description "Library to perform releases clojure releases on a project or a directory of
  projects."
  :url "http://github.com/thinktopic/think.release"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [thinktopic/think.config "0.2.4"]
                 [org.clojure/tools.cli "0.3.5"]]

  :main         think.release.main

  :profiles { :uberjar {:aot :all
                        :uberjar-name "think.release.jar"}}

  :plugins [[lein-environ "1.0.0"]
            [s3-wagon-private "1.3.0"]]



  :repositories  {"snapshots"  {:url "s3p://thinktopic.jars/snapshots/"
                                :no-auth true
                                :releases false
                                :sign-releases false}
                  "releases"  {:url "s3p://thinktopic.jars/releases/"
                               :no-auth true
                               :snapshots false
                               :sign-releases false}}
  :aliases {"release"      ["run" "-m" "think.release.main"]})
