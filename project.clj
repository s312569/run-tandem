(defproject run-tandem "0.1.1-SNAPSHOT"
  :description "Simple utility to run X! Tandem."
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [me.raynes/fs "1.4.6"]
                 [clj-tandem "0.1.6"]
                 [org.clojars.hozumi/clj-commons-exec "1.2.0"]
                 [org.clojure/tools.cli "0.3.5"]]
  :main ^:skip-aot run-tandem.core
  :target-path "target/%s"
  :jvm-opts ["-Xmx2000m"]
  :profiles {:uberjar {:aot :all}})
