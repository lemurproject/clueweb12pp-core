(ns clueweb12pp-core.main
  (:gen-class :main true)
  (require [clueweb12pp-core.core :as core]))

(defn -main
  [& args]
  (println (first (core/warc-body-links (first args)))))