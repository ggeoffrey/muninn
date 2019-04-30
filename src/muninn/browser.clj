(ns muninn.browser
  (:require [clj-http.client :as http])
  (:import java.net.URLEncoder))

(def user-agent
  "User Agent for chrome on Mac."
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")

(defn fetch-url
  "Given a `url`, fetch it and return an HTTP response"
  [url]
  (http/get url {:headers {"User-Agent" user-agent}}))

(defn encode [s]
  (URLEncoder/encode s "UTF-8"))
