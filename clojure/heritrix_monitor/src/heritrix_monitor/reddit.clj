(ns heritrix-monitor.reddit
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
           [heritrix-monitor.core :as core]))

(defn -main
  [& args]
  (let [[optional [reddit-log-file pages-csv-file pages-png-file size-csv-file size-png-file] banner] (cli/cli args)]
    (with-open [rdr (clojure.java.io/reader reddit-log-file)]
      (core/write-to-csv-file pages-csv-file
                              size-csv-file
                              [(count (line-seq rdr))
                               (count (line-seq rdr))]))
    (core/plot-graph pages-csv-file pages-png-file "Timestamp" "Number of posts" identity)))
