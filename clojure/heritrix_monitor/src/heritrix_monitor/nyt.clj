(ns heritrix-monitor.nyt
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [heritrix-monitor.core :as core]))

(defn -main
  [& args]
  (let [[optional [nyt-output-dir pages-csv size-csv pages-png size-png] banner] (cli/cli args)
        days (count (file-seq (clojure.java.io/file nyt-output-dir)))
        size (reduce
              +
              0
              (map
               (fn [x] (.length x))
               (.listFiles (clojure.java.io/file nyt-output-dir))))]
    (println days size)
    (core/write-to-csv-file pages-csv size-csv [days size])
    (core/plot-graph pages-csv pages-png "Timestamp" "Days Finished" identity)
    (core/plot-graph size-csv size-png "Timestamp" "Size" (fn [x] (/ x 1e9)))))