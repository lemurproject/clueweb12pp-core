;;;; Parse the xenforo support forums

(ns clueweb12pp-core.xenforo.parse-support-forums
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [warc-clojure.core :as warc])
  (:import (net.htmlparser.jericho Source TextExtractor Config LoggerProvider HTMLElementName)))

(defn handle-record
  [record]
  (let [source        (Source. (:payload-stream record))
        info-sections (.getAllElementsByClass source "pairsColumns aboutPairs")]
    (map
     (fn [an-attribute] (.getValue an-attribute))
     (flatten
      (map
       #(into [] %)
       (flatten
        (map
         (fn [x]
           (map
            (fn [a-tag] (.getURIAttributes a-tag))
            (.getAllElements x HTMLElementName/A)))
         info-sections)))))))

(defn -main
  [& args]
  (let [[optional [job-directory] banner] (cli/cli args)]
    (doseq [warc-file (core/job-warc-files job-directory)]
      (doseq [record (warc/skip-get-response-records-seq (warc/get-warc-reader warc-file))]
        (doseq [link (handle-record record)]
          (println link))))))
