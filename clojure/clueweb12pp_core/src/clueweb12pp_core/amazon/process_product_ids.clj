;;;; Process the amazon node pages, extract links to product reviews
;;;; and emit them to a file.

(ns clueweb12pp-core.amazon.process-product-ids
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [net.cgrand.enlive-html :as html]
            [warc-clojure.core :as warc]
            (org.bovinegenius [exploding-fish :as uri])))

(defn handle-record
  [record]
  (filter
   uri/absolute?
   (distinct
    (map
     (fn [product-match] product-match)
     (filter
      identity
      (map
       (fn [a-link]
         (try
           (re-find #".*product-reviews.*" a-link)
           (catch Exception e nil)))
       (map
        (fn [an-a-tag]
          (-> an-a-tag
             :attrs
             :href))
        (html/select
         (html/html-resource (:payload-stream record))
         [:a]))))))))

(defn -main
  [& args]
  (let [[optional [& amazon-pages-warc-files] banner] (cli/cli args)]
    (doseq [warc-file amazon-pages-warc-files]
      (doseq [record (warc/skip-get-response-records-seq
                      (warc/get-warc-reader warc-file))]
        (doseq [link (handle-record record)]
          (println link)
          (flush))))))
