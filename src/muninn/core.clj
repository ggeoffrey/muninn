(ns muninn.core
  "This is the main app entry point."
  (:gen-class)
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [muninn.google :as google]
            [muninn.text :as text]
            [muninn.websites :as website]))

(defn sentences-matching
  "Given a `query` string and some `text`, find all sentences matching the query."
  [query text]
  (->> (text/sentence->words query)
       (mapcat #(text/sentences-matching % text))
       (distinct)))

(defn contextualise
  "Given a `url` string and a list of `sentences` strings, produce a map
  {:link, :sentences}, indicating where these sentences came from."
  [url sentences]
  {:link    url
   :summary sentences})

(defn format-one-occurence
  "We don't want to output JSON or EDN to the output, we want simple text. This
  function take a context map and format it to text."
  [{:keys [link summary]}]
  (str "link:" link "\n\n"
       "summary:\n"
       (str/join "\n\n" summary)))

(defn format-summary
  "Given all returned `results`, format it to a text summary."
  [results]
  (str/join "\n\n\n" (map format-one-occurence results)))

(defn write-summary!
  "Take a `query` text and search results. Will create a `reports/<query>.txt`
  file and write a summary to it. Write to disk."
  [query results]
  (let [file-name (str "./reports/" query ".txt")]
    (io/make-parents file-name)
    (spit file-name (format-summary results))
    (log/info "Wrote " (str query ".txt"))))

(defn search-in-page!
  "Given a `query` string and a `url`, search in the page text for matching
  sentences. Return all matching one associated with the `url`, so we we can
  keep track where they come from."
  [query url]
  (log/info "Scrapping " url " …")
  (try
    (->> (website/extract-text! url)
         (sentences-matching query)
         (contextualise url))
    (catch Throwable t
      (log/error "Unable to scrapp website: " url ". Cause" (.getCause t))
      (contextualise url ["ERROR: Unable to scrap this website, please check logs for more info."]))))

(defn search-and-save-to-file!
  "Given a `query` text, search on Google, explore each result, find sentences
  matching `query` into each one of them, and spit out the result to a <query>.txt
  file in reports/ folder."
  [query]
  (try
    (->> (google/search! query)
         (pmap (partial search-in-page! query))
         (write-summary! query))
    (catch Throwable t
      (log/error t "Unable to query google for query: " query)
      nil)))

(defn load-config!
  "Given a `file-name`, read parse and return it"
  [file-name]
  (let [config (->> file-name io/resource slurp edn/read-string)]
    (log/info "Loaded config" config)
    config))

(defn build-queries
  "Given a `user-input` string, and a configuration `config`, produce a list of
  strings with configuration keywords appended.
  Eg: 'vegan food' ['trends', 'brand'] => ('vegan food trends' 'vegan food brand')"
  [user-input config]
  (map #(str user-input " " %) (:query-suffix config)))

(defn -main [& args]
  (let [config     (load-config! "config.edn")
        user-input (str/join " " args)]
    (log/info "Searching for: " user-input)
    (doall (map search-and-save-to-file! (build-queries user-input config)))
    (log/info "Done.")
    (System/exit 0)))

(comment
  (-main "almond milk france"))
