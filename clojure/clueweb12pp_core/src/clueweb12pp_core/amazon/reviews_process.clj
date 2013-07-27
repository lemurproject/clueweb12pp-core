;;;; Process a review page on AMZN and extract reviews in the 2012
;;;; range.

(ns clueweb12pp-core.amazon.reviews-process
  (:gen-class :main true)
  (:require [clj-time.core :as core-time]
            [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [clueweb12pp-core.utils :as utils]
            [net.cgrand.enlive-html :as html]
            [warc-clojure.core :as warc]))

(defn handle-record
  [record]
  (let [product-reviews-table (first
                               (filter
                                (fn [a-table]
                                  (= (-> a-table
                                        :attrs
                                        :id)
                                     "productReviews"))
                                (html/select
                                 (html/html-resource
                                  (:payload-stream record))
                                 [:table])))
        
        reviews               (html/select
                               product-reviews-table
                               [:div])

        parse-amzn-date       (fn [a-date-str]
                                (let [[mon-str date-str yr-str]
                                      (rest
                                       (re-find #"(.*) (.*), (.*)" a-date-str))]
                                  (core-time/date-time
                                   (java.lang.Integer/parseInt yr-str)
                                   (utils/month-full-name->int mon-str)
                                   (java.lang.Integer/parseInt date-str))))
        
        date-of-review        (fn [a-review]                                
                                (html/text
                                 (first
                                  (html/select
                                   a-review
                                   [:nobr]))))]
    (do
      (when product-reviews-table
        (map
         #(println %)
         reviews))
      (map
       (fn [a-review]
         (println (date-of-review a-review)))
       reviews))))

(defn handle-job
  [job-directory]
  (doseq [warc-file (core/job-warc-files job-directory)]
    (doseq [record (warc/skip-get-response-records-seq
                    (warc/get-warc-reader warc-file))]
      (handle-record record))))

(defn -main
  [& args]
  (let [[optional [amazon-reviews-job] banner] (cli/cli args)]
    (handle-job amazon-reviews-job)))
