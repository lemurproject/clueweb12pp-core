;;;; Given a node-id, we generate its link. This is the first step in
;;;; our AMZN crawl

(ns clueweb12pp-core.amazon.generate-hierarchy-pages
  (:gen-class :main true))


;; the base page we will be targeting.
(def base-uri "http://www.amazon.com/s?node=")

(defn -main
  [& args]
  (let [hierarchy-list (first args)]
    (with-open [rdr (clojure.java.io/reader hierarchy-list)]
      (doseq [id (line-seq rdr)]
        (println (clojure.string/join "" [base-uri id]))))))