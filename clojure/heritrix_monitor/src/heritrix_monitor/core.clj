;;;; Code to inspect a heritrix crawl job's output and generate some
;;;; pretty graphs

(ns heritrix-monitor.core
  (:gen-class :main true)
  (:require [clj-time.coerce :as coerce-time]
            [clj-time.core :as core-time]
            [clj-time.format :as format-time]
            [clojure.java.io :as io]
            [clojure.tools.cli :as cli]
            clojure.set
            [clojure.string :as string]
            [incanter.core :as incanter]
            [incanter.charts :as charts]))

(defn process-crawl-log
  [crawl-log-file]
  (with-open [rdr (io/reader crawl-log-file)]
    (reduce
     (fn [prev line]
       (let [[_ resp _ _ _ _ _ _ _ _ _ _] (string/split line #"\s+")]
         (if (= resp "200")
           (+' prev 1)
           prev)))
     0
     (line-seq rdr))))

;;; Obtain the crawl log files for a heritrix job
(defn job-crawl-log-files
  [job-directory]
  (filter
   (fn [a-file-obj] (and (re-find #"crawl.log" (.getAbsolutePath a-file-obj))
                      (not (re-find #"latest" (.getAbsolutePath a-file-obj)))
                      (not (re-find #"crawl.log.lck" (.getAbsolutePath a-file-obj)))))
   (file-seq (io/file job-directory))))

(defn write-to-csv-file
  [csv-file datum]
  (with-open [wrtr (io/writer csv-file :append true)]
    (.write wrtr (format "%s,%d,\n" (.toString (core-time/now)) datum))))

(defn plot-graph
  [csv-file png-file]
  (let [time-count (map
                    (fn [x] (string/split x #","))
                    (string/split
                     (slurp csv-file)
                     #"\n"))
        times      (map
                    (fn [x] (coerce-time/to-long (format-time/parse x)))                    
                    (map first time-count))
        counts     (map second time-count)]
    (println times)
    (incanter/save
     (charts/time-series-plot
      times
      (map (fn [x] (java.lang.Long/parseLong x)) counts)
      :x-label "Time"
      :y-label "Number of pages")
     png-file)))

(defn -main
  [& args]
  (let [[optional [job-directory csv-file png-file] banner] (cli/cli args)]
    (write-to-csv-file
     csv-file
     (reduce
      (fn [prev crawl-log-file]
        (+' prev (process-crawl-log crawl-log-file)))
      0
      (job-crawl-log-files job-directory)))
    (plot-graph csv-file png-file)))