(ns muninn.filterer
  (:require [clj-fuzzy.stemmers :refer [lancaster lovins]]
            [clojure.string :as str]))

(defn match-stem? [stemmer word-a word-b]
  (= (stemmer word-a) (stemmer word-b)))

(defn match? [word-a word-b]
  (or (= word-a word-b)
      (match-stem? lancaster word-a word-b)
      (match-stem? lovins word-a word-b)))

(defn has-match? [word seq]
  (some (partial match? word) seq))

(defn sentence->word-seq [s]
  (str/split s #"\s{1,}"))

(defn line->sentences [s]
  (str/split s #"(\.\.\.|[\.\?!…])"))

(defn paragraph->lines [s]
  (str/split-lines s))

(defn all-sentences [s]
  (mapcat line->sentences (paragraph->lines s)))

(defn word-seq->sentence [seq]
  (str/trim (str/join " " seq)))

(defn sentences-matching [word s]
  (->> (map sentence->word-seq (all-sentences s))
       (filter (partial has-match? word))
       (map word-seq->sentence)))


(comment
  (def _text "Feugiat nisl pretium fusce id velit ut tortor pretium viverra suspendisse potenti nullam ac tortor vitae purus faucibus ornare suspendisse. Sapien et ligula ullamcorper malesuada proin libero nunc, consequat interdum... Aliquet risus feugiat in ante metus, dictum at tempor commodo, ullamcorper a lacus vestibulum sed arcu non odio euismod lacinia! Mattis rhoncus, urna neque viverra justo, nec ultrices dui sapien?
              Malesuada fames ac turpis egestas maecenas pharetra! Id eu nisl nunc mi ipsum, faucibus vitae aliquet nec, ullamcorper sit amet risus nullam eget felis eget nunc lobortis mattis aliquam faucibus.")
  (sentences-matching "consequences" _text)
  
  (has-match? (lancaster "work") ["working" "worker" "workaround"]))
