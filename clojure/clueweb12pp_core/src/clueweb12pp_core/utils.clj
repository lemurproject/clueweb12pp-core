;;;; Simple utilities like literal date mapping etc.

(ns clueweb12pp-core.utils
  (:gen-class)
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]))

(def month-TLA
  ["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"])

(defn month->int
  [mon-str]
  (+ 1 (.indexOf month-TLA mon-str)))

(defn str-replace-month->int
  [a-str]
  (clojure.string/join
   " "
   (map
    (fn [s] (if (>= (.indexOf month-TLA s) 0)
             (month->int s)
             s))
    (clojure.string/split a-str #"\s+"))))

(defn zip-str
  [s]
  (zip/xml-zip
   (xml/parse
    (java.io.ByteArrayInputStream. (.getBytes s)))))