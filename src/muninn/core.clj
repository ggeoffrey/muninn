(ns muninn.core
  "This is the main app entry point."
  (:gen-class)
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [muninn.filterer :as filterer]
            [muninn.google :as google]
            [muninn.websites :as website]))

(defn sentence->words
  "Take a `sentence` string, an return a list of words."
  [sentence]
  (str/split (str/trim sentence) #"\s"))

(defn website->summary!
  "Given a text `query` and a `url`, fetch that page and find into it sentences
  matching the query. Perform an HTTP request."
  [query url]
  (let [paragraphs (website/extract-text! url)]
    (->> (sentence->words query)
         (mapcat #(filterer/sentences-matching % paragraphs))
         (distinct))))

(defn google-search->summary!
  "Given a `query` text, like you would type on Google, get all links from
  Google's first page, and search into each pages for matching sentences. Return
  a map {:link <url>, :summary [<sentence 1> <sentence 2>]}. Pages are fetched
  and searched in parallel, one page per physical processor core. Perform N+1
  HTTP requests. One for Google search, and one for each returned link."
  [query]
  (let [links (google/get-links! query)]
    (log/info "Found " (count links) " on Google for query: " query)
    (->> links
         (pmap (fn [link]
                 (try
                   (log/info "Scrapping " link " …")
                   {:link    link
                    :summary (website->summary! query link)}
                   (catch Throwable t
                     (log/error "Unable to scrapp website: " link ". Cause" (.getCause t))
                     {:link    link
                      :summary ["ERROR: Unable to scrap this website, please check logs for more info."]})))))))

(defn summary->text-file!
  "Take a `query` text and a search result. Will create a `reports/<query>.txt`
  file and write summaries into it."
  [query result]
  (let [file-name (str "./reports/" query ".txt")]
    (io/make-parents file-name)
    (->> result
         (map (fn [{:keys [link summary]}]
                (str "link:" link "\n\n"
                     "summary:\n"
                     (str/join "\n\n" summary)
                     )))
         (str/join "\n\n\n")
         (spit file-name))))

(defn search-and-save-to-file!
  "Given a `query` text, search on Google, explore each result, find sentences
  matching `query` into each one of them, and spit out the result to a <query>.txt
  file in reports/ folder."
  [query]
  (log/info "Searching for: " query)
  (try
    (summary->text-file! query (google-search->summary! query))
    (log/info "Wrote " (str query ".txt"))
    (catch Throwable t
      (log/error t "Unable to query google for query: " query)
      nil)))

(defn load-config!
  "Given a `file-name`, read parse and return it"
  [file-name]
  (->> file-name io/resource slurp edn/read-string))

(defn -main
  [& args]
  (let [query                                            (str/join " " args)
        {:keys [query-suffix extra-keywords] :as config} (load-config! "config.edn")]
    (log/info "Loaded config" config)
    (->> query-suffix
         (map #(str query " " %))
         (map search-and-save-to-file!)
         (doall))
    (log/info "Done.")))

(comment
  (-main "almond milk france"))
