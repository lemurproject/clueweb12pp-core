;;;; API to get dates in Clueweb12++ records. The initial approach I
;;;; used was with the NER taggers but Natty has a wide base of
;;;; surface representations and is quick enough for us to use.

(ns clueweb12pp-core.page-times
  (:gen-class)
  (:require [clj-time.core :as core-time]
            [clj-time.coerce :as coerce-time]
            [clojure.tools.reader.edn :as edn])
  (:import (com.mdimension.jchronic Chronic)
           (com.mdimension.jchronic.tags Pointer)
           (com.mdimension.jchronic.utils Span Time)
           (com.joestelmach.natty Parser DateGroup)
           (net.htmlparser.jericho Source TextExtractor Config LoggerProvider HTMLElementName)))

(def *parser* (new Parser))

;;; This is needed since clj-time wraps JodaTime that can't work with
;;; a java date
(defn convert-to-clj-time-format
  [a-date-obj]
  (coerce-time/from-long
   (.getTime a-date-obj)))

(defn dates-in-text
  [text]
  (map
   convert-to-clj-time-format
   (flatten
    (map
     (fn [x] (into [] x))
     (map
      (fn [group] (.getDates group))
      (try (.parse *parser* text)
           (catch Exception e [])))))))
