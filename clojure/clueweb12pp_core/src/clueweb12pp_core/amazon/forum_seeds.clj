;;;; Amazon.com has standard discussion forums as well.
;;;; These discussion forums underwent a KBA scoping crawl.
;;;; We process the job crawl log.

(ns clueweb12pp-core.amazon.forum-seeds
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]))

(defn -main
  [& args]
  (let [[optional [job-directory] banner] (cli/cli args)]
    (doseq [link (core/search-crawl-logs job-directory #"www.amazon.com/forum")]
      (println link))))
