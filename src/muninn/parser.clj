(ns muninn.parser
  (:require [clojure.java.io :as io]
            [net.cgrand.enlive-html :as html]))

(defn to-html-tree
  "Given an `html-string`, parse it to a recursive tree of html nodes."
  [html-string]
  (html/html-resource (io/input-stream (.getBytes html-string))))
