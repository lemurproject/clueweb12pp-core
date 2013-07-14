;;;; Code specific to processing ipboard index pages

(ns clueweb12pp-core.ipboard.process-mod-rewrite-friendly-index-pages
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [clueweb12pp-core.forum-index-page-process :as index-page-process]
            [clueweb12pp-core.ipboard.consts :as consts]
            [warc-clojure.core :as warc]))


(defn -main
  [& args]
  (let [[optional [warc-files-list] banner] (cli/cli args)]
    (doseq [warc-file (index-page-process/process-warc-files-list warc-files-list)]
      (doseq [record (warc/skip-get-response-records-seq
                      (warc/get-warc-reader warc-file))]
        (try (let [[links dates] (index-page-process/handle-record
                                  record
                                  consts/ipboard-mod-rewrite-friendly-topics-regex)]
               (when (some (fn [x] (core/in-clueweb12pp-time-range? x))
                           dates)
                 (doseq [link links]
                   (println link)
                   (flush))))
             (catch Exception e nil))))))