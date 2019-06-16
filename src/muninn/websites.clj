(ns muninn.websites
  "Fetch html documents and extract text out of them."
  (:require [clojure.string :as str]
            [muninn.browser :as browser]
            [muninn.parser :as parser]
            [net.cgrand.enlive-html :as html]))

(defn- get-website-content!
  "Given a `url` string, HTTP GET it and return the HTML <body> tag."
  [url]
  (:body (browser/fetch-url url)))

(defn- paragraphs
  "Given an HTML tree structure (DOM), return all <p> tags."
  [html-tree]
  (html/select html-tree [:p]))

(defn- extract-paragraphs-text
  "Given an HTML tree structure (DOM), return inner text of all <p> tags"
  [html-tree]
  (map html/text (paragraphs html-tree)))

(defn- extract-paragraphs!
  "Given a `url` string, return text of all <p> tags in the HTML document. Will perform
  an HTTP request to fetch the document."
  [url]
  (-> (get-website-content! url)
      (parser/to-html-tree)
      (extract-paragraphs-text)))

(defn extract-text!
  "Given a `url` string, fetch it and extract all text out of it."
  [url]
  (str/join " " (extract-paragraphs! url)))
