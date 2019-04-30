(ns muninn.core
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.tools.logging :as log]
            [muninn.websites :as website]
            [muninn.filterer :as filterer]
            [muninn.google :as google]
            [clojure.java.io :as io])
  (:gen-class))

(defn query->words [query]
  (str/split (str/trim query) #"\s"))

(defn website->summary! [query url]
  (let [paragraphs (website/extract-text! url)]
    (->> (query->words query)
         (mapcat #(filterer/sentences-matching % paragraphs))
         (distinct))))

(defn google-search->summary! [query]
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

(defn summary->text-file [query result]
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

(defn search-and-save-to-file! [query]
  (log/info "Searching for: " query)
  (try
    (summary->text-file query (google-search->summary! query))
    (log/info "Wrote " (str query ".txt"))
    (catch Throwable t
      (log/error t "Unable to query google for query: " query)
      nil)))

(defn load-config! [config]
  (->> config io/resource slurp edn/read-string))

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
