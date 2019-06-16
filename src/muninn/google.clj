(ns muninn.google
  "Perform queries to Google and extract links from it.
  This namespace rely on some transducers."
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [muninn.browser :as browser]
            [muninn.parser :as parser]
            [net.cgrand.enlive-html :as html]))

(defn query-url
  "Given a `query` string, return a fetchable google.com url."
  [query]
  (str "http://www.google.ca/search?hl=en&num=10&lr=lang_en&ft=i&cr=&safe=images"
       "&q=" (browser/encode query)))

(def xf-external-links
  "Transducer filtering all meaningfull links on a sequence of links comming from
  google page. @see https://clojure.org/reference/transducers"
  (filter #(and (= :a (:tag %))
                (not (str/includes? (:href (:attrs %)) "webcache.googleusercontent"))
                (str/starts-with? (:href (:attrs %)) "http"))))

(defn keep-only-external-links
  "Given a list of `links` (html :a nodes), return all the ones pointing to actual
  websites outside of google.com."
  [links]
  (sequence xf-external-links links))

(defn result-links
  "Given some `html` tree comming from google.com, extract all meaningfull links."
  [html]
  (->> (html/select html [:div.rc :div.r :a])
       (keep-only-external-links)))

(def xf-extract-hrefs
  "Transducer extracting 'href' attribute out of a sequence of links.
  @see https://clojure.org/reference/transducers"
  (map #(get-in % [:attrs :href])))

(defn search!
  "Given a `query` string (like 'Vegan food'), fetch google and extract all links
  from the first page."
  [query]
  (let [links (some->> (query-url query)
                       (browser/fetch-url)
                       (:body)
                       (parser/to-html-tree)
                       (result-links)
                       (sequence xf-extract-hrefs))]
    (log/info "Found " (count links) " on Google for query: " query)
    links))
