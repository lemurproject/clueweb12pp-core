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

;;; Returns a list of lines in a (reasonably-small) file
(defn lines
  [filename]
  (filter
   #(not (clojure.string/blank? %))
   (clojure.string/split (try
                           (slurp filename)
                           (catch Exception e nil))
                         #"\n")))

;;; Used to skip past a record that has caused a processing job to
;;; crash. We drop the offending record as well
(defn warc-skip-to-record
  [warc-response-records-seq skip-target-uri]
  (drop skip-target-uri warc-response-records-seq))

(defn try-times*
  "Executes thunk. If an exception is thrown, will retry. At most n retries
  are done. If still some exception is thrown we just return nil."
  [n thunk]
  (loop [n n]
    (if-let [result (try
                      [(thunk)]
                      (catch Exception e
                        (Thread/sleep 1000)
                        (when (zero? n)
                          nil)))]
      (result 0)
      (recur (dec n)))))

(defmacro try-times
  "Executes body. If an exception is thrown, will retry. At most n retries
  are done. If still some exception is thrown it is bubbled upwards in
  the call chain."
  [n & body]
  `(try-times* ~n (fn [] ~@body)))

(defn fs-join
  "Joins directory and object to get directory/object"
  [dir-path obj-path]
  (if (= (last dir-path) \/)
    (clojure.string/join "" (list dir-path obj-path))
    (clojure.string/join "/" (list dir-path obj-path))))

(defn file-exists?
  [file-path]
  (.exists
   (java.io.File. file-path)))