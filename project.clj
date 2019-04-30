(defproject muninn "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [enlive "1.1.6"]
                 [clj-http "3.9.1"]
                 [clj-fuzzy "0.4.1"]
                 [org.clojure/tools.logging "0.4.1"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [ch.qos.logback/logback-core "1.2.3"]]
  :jvm-opts ["-Dlogback.configurationFile=logback.xml"]
  :main ^:skip-aot muninn.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot          :all
                       :uberjar-name "muninn.jar"}})
