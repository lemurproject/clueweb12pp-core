;;;; Simple utilities like literal date mapping etc.

(ns clueweb12pp-core.utils
  (:gen-class)
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]))

(def month-TLA
  ["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"])

(def month-full-name
  ["January"
   "February"
   "March"
   "April"
   "May"
   "June"
   "July"
   "August"
   "September"
   "October"
   "November"
   "December"])

(defn month->int
  [mon-str]
  (+ 1 (.indexOf month-TLA mon-str)))

(defn month-full-name->int
  [mon-str]
  (+ 1 (.indexOf month-full-name mon-str)))

(defn str-replace-month->int
  [a-str]
  (clojure.string/join
   " "
   (map
    (fn [s] (if (>= (.indexOf month-TLA s) 0)
             (month->int s)
             s))
    (clojure.string/split a-str #"\s+"))))

(defn str-replace-month-full->int
  [a-str]
  (clojure.string/join
   " "
   (map
    (fn [s] (if (>= (.indexOf month-full-name s) 0)
             (month->int s)
             s))
    (clojure.string/split a-str #"\s+"))))

(defn zip-str
  [s]
  (zip/xml-zip
   (xml/parse
    (java.io.ByteArrayInputStream. (.getBytes s)))))


(defmacro with-timeout
  "Allows us to time-out on expressions.
   For example, Natty enters some long-winded periods of doing nothing.
   Now, we can just time out when it is confused by a string"
  [millis default-return & body]
    `(let [future# (future ~@body)]
      (try
        (.get future# ~millis java.util.concurrent.TimeUnit/MILLISECONDS)
        (catch java.util.concurrent.TimeoutException x# 
          (do
            (future-cancel future#)
            ~default-return)))))