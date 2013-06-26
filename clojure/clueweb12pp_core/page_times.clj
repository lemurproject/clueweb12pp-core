;;;; API to get dates on pages. We need this since our current
;;;; implementation is quite quriky

(ns clueweb12pp-core.page-times
  (:gen-class :main true)
  (:import (com.mdimension.jchronic Chronic)
           (com.mdimension.jchronic.tags Pointer)
           (com.mdimension.jchronic.utils Span Time)))

(defn -main
  [& args]
  (println (Chronic/parse "2012-11-10")))