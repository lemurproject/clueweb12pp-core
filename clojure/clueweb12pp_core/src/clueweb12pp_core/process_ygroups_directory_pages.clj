(ns clueweb12pp_core.process_ygroups_directory_pages
  (:gen-class :main true)
  (require [warc-clojure.core :as warc]
           [clojure.tools.cli :as cli]
           [net.cgrand.enlive-html :as html]
           [org.bovinegenius.exploding-fish :as uri]))

(defn process-record
  "Extracts public forum links from a page on ygroups"
  [page-record]
  (let [page-uri-hostname (uri/host (:target-uri-str page-record))
        page-stream (:payload-stream page-record)
        page-forums-list (html/select 
                          (html/html-resource page-stream) 
                          [:table.datatable :tr :a])]
    (map
      (fn [public-group-tag]
        (.toString (uri/scheme  (uri/host
                                  (uri/uri (-> public-group-tag
                                               :attrs
                                               :href))
                                  page-uri-hostname)
                                "http")))
      (filter
        (fn [page-a-tags]
          (some #(= "Public" %)
                (-> page-a-tags
                    :content)))
        page-forums-list))))

(defn process-records
  [warc-file]
  (for [record (warc/get-http-records-seq
                   (warc/get-warc-reader warc-file))]
    (process-record record)))

(defn -main
  [& args]
  (let [[optional-args [warc-gz-file] banner] (cli/cli args)
        forums-list (flatten (process-records warc-gz-file))]
    (doseq [forum forums-list]
      (println forum))))