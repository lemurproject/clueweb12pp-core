;;;; Code to compute outlinks in nabble

(ns clueweb12pp_core.nabble.nabble_outlinks
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]))

(defn warc-files-list
  [nabble-post-jobs]
  (flatten (map core/job-warc-files nabble-post-jobs)))

(defn -main
  [& args]
  (let [[optional [& nabble-post-directories] banner] (cli/cli args)]
    (doseq [link (core/job-outlinks (warc-files-list nabble-post-directories))]
      (println link))))
