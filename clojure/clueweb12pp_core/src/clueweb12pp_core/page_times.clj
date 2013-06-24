;;; Faster JChronic approach used for detecting dates.
;;; Hopefully this works
;;; This is because the NER tagger is slow as fuck

(ns clueweb12pp_core.page_times
  (:gen-class)
  (:require [clj-time.core :as clj-time]
            [clj-time.format :as clj-time-format]
            [clj-time.coerce :as coerce-time]
            [warc-clojure.core :as warc]))

