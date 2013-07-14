(ns heritrix-monitor.disqus-post
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [heritrix-monitor.core :as core]))

(defn -main
  [& args]
  (let [[optional [post-counts-file pages-csv pages-png] banner] (cli/cli args)
        posts (java.lang.Integer/parseInt
               (slurp post-counts-file))]
    (core/write-to-csv-file pages-csv pages-csv [posts posts])
    (core/plot-graph pages-csv pages-png "Timestamp" "Number of Post-Ids" identity)))