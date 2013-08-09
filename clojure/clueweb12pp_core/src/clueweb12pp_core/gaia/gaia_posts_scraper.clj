;;;; Scrape GAIA Online post pages

(ns clueweb12pp-core.gaia.gaia_posts_scraper
  (:gen-class :main true)
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [clueweb12pp-core.utils :as utils]
            [net.cgrand.enlive-html :as html]
            [org.bovinegenius.exploding-fish :as uri])
  (:import java.util.zip.GZIPOutputStream))

(defn download
  "Downloads and returns the page html content"
  [page-uri]
  (utils/try-times
   10
   (when page-uri
     (:body (client/get
             page-uri
             {:headers {"User-Agent" core/clueweb12pp-crawler}})))))

(defn next-page
  "Follows the next link on a post page"
  [page-content]
  (when page-content
    (-> (first
        (filter
         (fn [a-page-jump]
           (= "next page"
              (-> a-page-jump
                 :attrs
                 :title)))
         (html/select
          (html/html-resource
           (java.io.StringReader. page-content))
          [:a.page_jump])))

       :attrs
       :href)))

(defn dump-page
  [page-uri page-content output-file]
  (with-open [output (clojure.java.io/writer output-file :append true)]
    (binding [*out* output]
      (println
       (json/write-str
        {:uri     page-uri
         :content page-content})))))

(defn download-post
  "Starts from the post seed and downloads the entire page"
  [post-seed output-file visited]
  (let [post-seed-page (download post-seed)]
    (do
      (println post-seed)
      (flush)
      (dump-page post-seed post-seed-page output-file)
      (. Thread (sleep 3000))
      (let [next (next-page post-seed-page)]
       (when (and next (not (contains? visited next)))
         (recur (uri/resolve-path
                 post-seed
                 next)
                output-file
                (conj visited next)))))))

(defn -main
  [& args]
  (let [[optional [post-seeds output-file] banner] (cli/cli args)]
    (doseq [post-seed (core/process-text-file post-seeds)]
      (download-post post-seed output-file (set [])))))
