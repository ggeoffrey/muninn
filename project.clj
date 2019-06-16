(defproject muninn "0.1.0-SNAPSHOT"
  :url "https://github.com/ggeoffrey/muninn"
  :dependencies [[ch.qos.logback/logback-classic "1.2.3"] ;; logger backend
                 [ch.qos.logback/logback-core "1.2.3"] ;; logger interface
                 [clj-fuzzy "0.4.1"] ;; Little bits of NLP
                 [clj-http "3.9.1"] ;; perform HTTP requests
                 [enlive "1.1.6"] ;; Parse and manipulate xml
                 [org.clojure/clojure "1.10.0"]
                 ;; Clojure logging library
                 [org.clojure/tools.logging "0.4.1"]]
  :jvm-opts ["-Dlogback.configurationFile=logback.xml"]
  :main ^:skip-aot muninn.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot          :all
                       :uberjar-name "muninn.jar"}})
