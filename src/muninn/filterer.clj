(ns muninn.filterer
  "Filter text using regular expression and basic NLP"
  (:require [clj-fuzzy.stemmers :refer [lancaster lovins]]
            [clojure.string :as str]))

(defn match-stem?
  "Take a `stemmer` function, a `word-a` and `word-b` strings.
  State if `word-a` and `word-b` share the same stem."
  [stemmer word-a word-b]
  (= (stemmer word-a) (stemmer word-b)))

(defn similar?
  "State if `word-a` and `word-b` are similar. Two words are similar if they are
  equal or share the same stem."
  [word-a word-b]
  (or (= word-a word-b)
      (match-stem? lancaster word-a word-b)
      (match-stem? lovins word-a word-b)))

(defn has-similar?
  "State if a sequence of `words` contains at least one word similar to `word`."
  [word words]
  (some (partial similar? word) words))

(defn sentence->words
  "Split a `sentence` string into a list of words"
  [sentence]
  (str/split (str/trim sentence) #"\s{1,}"))

(defn words->sentence
  "Co-morphisme of `sentence->words`"
  [seq]
  (str/trim (str/join " " seq)))

(defn line->sentences
  "Split a `line` of text into a list of sentences."
  [line]
  (str/split line #"(\.\.\.|[\.\?!…])"))

(defn paragraph->lines
  "Split a `paragraph` string into lines"
  [paragraph]
  (str/split-lines paragraph))

(defn text->sentences
  "Extract a list of sentences out of `text`."
  [text]
  (mapcat line->sentences (paragraph->lines text)))

(defn sentences-matching
  "Extract all sentences from `text` and return all ones having at least one word
  similar to `word`."
  [word text]
  (->> (text->sentences text)
       (map sentence->words)
       (filter (partial has-similar? word))
       (map words->sentence)))
