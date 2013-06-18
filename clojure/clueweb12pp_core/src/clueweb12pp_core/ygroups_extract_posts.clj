;;;; Extract posts from the yahoo groups index pages download
;;;; We also store some metadata (index page that led to this crawl)

(ns clueweb12pp_core.ygroups_extract_posts
  (:gen-class :main true)
  (:require [clj-time.core :as time-core]
            [clojure.tools.cli :as cli]
            [clojure.data.json :as json]
            [clueweb12pp-core.core :as core]
            [clueweb12pp_core.utils :as utils]
            [net.cgrand.enlive-html :as html]
            (org.bovinegenius [exploding-fish :as uri])))

;;;; routine in the walk done over the warc file.
;;;; collects posts in the clueweb12pp time frame
(defn process-index-page-record
  [record]
  (let
      [post-in-time-range        (fn [[post post-date]]
                                   (time-core/within? (time-core/interval
                                                       core/clueweb12pp-time-start
                                                       core/clueweb12pp-time-end)
                                                      post-date))

       junk-date                 (time-core/date-time 2015 01 01)
       
       make-date                 (fn [[mon-str yr-str]]
                                   (if (and (not mon-str) (not mon-str))
                                     junk-date
                                     (time-core/date-time (java.lang.Integer/parseInt yr-str)
                                                          (utils/month->int mon-str)
                                                          1)))
       
       ygroups-date-match-regex  #"(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec).*(\d\d\d\d).*"

       get-msg-absolute-url      (fn [msg-url]
                                   (.toString
                                    (uri/scheme
                                     (uri/host (uri/uri msg-url) (uri/host (:target-uri-str record)))
                                     (uri/scheme (:target-uri-str record)))))
       
       process-post-and-date     (fn [[ message date]]
                                   (list
                                    (get-msg-absolute-url
                                     (-> (first
                                         (html/select
                                          (:content message)
                                          [:noscript :a]))
                                        :attrs
                                        :href))
                                    (make-date
                                     (rest
                                      (re-find
                                       ygroups-date-match-regex
                                       (clojure.string/trim
                                        (first
                                         (filter
                                          (fn [s] (not (clojure.string/blank? s)))
                                          (map
                                           html/text
                                           (-> date
                                              :content))))))))))

       posts-on-page             (fn [page-stream]
                                   (map
                                    first
                                    (filter
                                     post-in-time-range
                                     (map
                                      process-post-and-date
                                      (partition
                                       2
                                       (html/select
                                        (html/html-resource page-stream)
                                        #{[:table.datatable :td.message] [:table.datatable :td.date]}))))))]
    
    {:index-page (:target-uri-str record) :posts (posts-on-page (:payload-stream record))}))

;;;; Called once per warc-file in job directory
(defn process-index-page-warc-file
  [warc-file]
  (for [index-page-posts
        (filter
         (fn [x] (not (empty? (:posts x))))
         (for [record (filter
                       (fn
                         [record]
                         (re-find #"tidx" (:target-uri-str record)))
                       (core/non-meta-warc-file-walk warc-file identity))]
           (process-index-page-record record)))]
    (:posts index-page-posts)))

;;;; Expected command line args:
;;;;  - index-page-directories : a file containing a list of
;;;;     (line-separated) directories containing posts
(defn -main
  [& args]
  (let [[optional [index-page-job-directory] banner] (cli/cli args)]
    (doseq [posts
            (filter
             (fn [x] (not (empty? x)))
             (for [warc-file (core/job-warc-files index-page-job-directory)]
               (process-index-page-warc-file warc-file)))]
      (doseq [post posts]
        (doseq [p post]
          (println p))))))