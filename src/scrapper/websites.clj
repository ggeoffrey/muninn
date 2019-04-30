(ns scrapper.websites
  (:require [net.cgrand.enlive-html :as html]
            [scrapper.browser :as browser]
            [scrapper.parser :as parser]
            [clojure.string :as str]))

(defn- get-website-content! [url]
  (:body (browser/fetch-url url)))

(defn- paragraphs [html-tree]
  (html/select html-tree [:p]))

(defn- extract-paragraphs-text [html-tree]
  (map html/text (paragraphs html-tree)))

(defn extract-paragraphs! [url]
  (-> (get-website-content! url)
      (parser/to-html-tree)
      (extract-paragraphs-text)))

(defn extract-text! [url]
  (str/join " " (extract-paragraphs! url)))

(comment
  (extract-paragraphs! "https://pitchdeck.improvepresentation.com/what-is-a-pitch-deck"))
