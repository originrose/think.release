(defproject thinktopic/gpu-compute "0.1.0-2017-03-23-08-26"
  :description "Gpu backend for thinktopic's compute library"
  :url "http://thinktopic.com/cortex"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.bytedeco.javacpp-presets/cuda "8.0-1.2"]
                 [thinktopic/compute ""0.1.0-2017-03-22-02-09""]])
