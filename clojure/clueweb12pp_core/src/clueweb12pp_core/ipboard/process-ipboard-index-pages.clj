;;;; Code specific to processing ipboard index pages

(ns clueweb12pp-core.ipboard.process-index-pages
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [clueweb12pp-core.forum-index-page-process :as index-page-process]
            [clueweb12pp-core.ipboard.consts :as consts]))


(defn -main
  [& args]
  (let [[optional [warc-files-list] banner] (cli/cli args)]
    (doseq [warc-file (process-warc-files-list warc-files-list)]
      (doseq [record (warc/skip-get-response-records-seq
                      (warc/get-warc-reader warc-file))]
        (try (let [[links dates] (handle-record record consts/ipboard-unfriendly-topics-regex)]
               (when (some (fn [x] (core/in-clueweb12pp-time-range? x))
                           dates)
                 (doseq [link links]
                   (println link))))
             (catch Exception e nil))))))