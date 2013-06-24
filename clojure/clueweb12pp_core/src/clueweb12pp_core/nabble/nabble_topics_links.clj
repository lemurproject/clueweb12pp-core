;;;; Code to process nabble topic links and identify the index pages
;;;; that led to them.

(ns clueweb12pp-core.nabble.nabble-topics-links
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [warc-clojure.core :as warc])
  (:import  [net.htmlparser.jericho Source TextExtractor Config LoggerProvider HTMLElementName]))

(set! (. Config LoggerProvider) LoggerProvider/DISABLED)

(defn record-links
  [record]
  (filter
   (fn [x] (re-find #".*-tp.*html" x))
   (filter
    identity
    (map
     (fn [x] (.getAttributeValue x "href"))
     (.getAllElements
      (Source. (:payload-stream record))
      HTMLElementName/A)))))

(defn -main
  [& args]
  (let [[optional [& positional] banner] (cli/cli args)
        topic-links-file                 (first positional)
        nabble-index-page-jobs           (rest positional)
        topic-links                      (clojure.string/split
                                          (slurp topic-links-file)
                                          #"\n")]
    (doseq [job-directory nabble-index-page-jobs]
      (doseq [warc-file (core/job-warc-files job-directory)]
        (doseq [record (warc/skip-get-response-records-seq
                        (warc/get-warc-reader warc-file))]
          (when (some (fn [x] (some #{x} topic-links))
                      (record-links record))
            (println (:target-uri-str record))
            (flush)))))))