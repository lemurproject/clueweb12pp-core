(ns clueweb12pp_core.nabble.nabble_posts_dates
  (:gen-class :main true)
  (:require [clueweb12pp-core.core :as core]
            [clj-time.core :as core-time]
            [clj-time.coerce :as coerce-time]
            [clojure.tools.cli :as cli]
            [net.cgrand.enlive-html :as html])
  (:import [net.htmlparser.jericho Source TextExtractor Config LoggerProvider HTMLElementName]))

(set! (. Config LoggerProvider) LoggerProvider/DISABLED)

(defn dates-in-record
  [record]
  [(:target-uri-str record)
   (filter
    identity
    (map
     (fn [x] (last (re-find
                   #".*new Date\((\d+)\).*"
                   (.toString (.getContent x)))))
     (.getAllElements
      (Source. (:payload-stream record))
      HTMLElementName/SCRIPT)))])

(defn process-warc-file
  [warc-file]
  (filter
   (fn [[link dates]] (not (empty? dates)))
   (for [record (filter
                 (fn [record] (re-find #".*-td.*html" (:target-uri-str record)))
                 (core/non-meta-warc-file-walk warc-file identity))]
     (dates-in-record record))))

(defn -main
  [& args]
  (let [[optional [& job-directories] banner] (cli/cli args)
        dates-in-clueweb-time-range?          (fn [epoch-num]
                                                (core-time/within?
                                                 (core-time/interval core/clueweb12pp-time-start
                                                                     core/clueweb12pp-time-end)
                                                 (coerce-time/from-long (java.lang.Long/parseLong epoch-num))))]
    (doseq [job-directory job-directories]
      (doseq [warc-file (core/job-warc-files job-directory)]
        (doseq [[post dates] (process-warc-file warc-file)]
          (when (some dates-in-clueweb-time-range? dates)
            (println post)
            (flush)))))))