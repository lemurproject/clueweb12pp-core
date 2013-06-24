;;;; Code to process the ipboard support directories

(ns clueweb12pp_core.ipboard.ipboard_support
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [net.cgrand.enlive-html :as html]
            [clueweb12pp-core.core :as core]))

(defn handle-warc-file
  [warc-file]
  (let [user-page?            (fn [record]
                                (re-find #".*/user/.*" (:target-uri-str record)))
        process-page          (fn [page-stream]
                                (html/select
                                 (html/html-resource page-stream)
                                 [:div.ipsLayout_content :span.row_data :a]))
        get-contact-page      (fn [page-stream]
                                (map
                                 (fn [link]
                                   (-> link
                                      :attrs
                                      :href))
                                 (process-page page-stream)))
        get-user-pages-seq    (fn [warc-file]
                                (filter
                                 (fn [record] (user-page? record))
                                 (core/warc-file-walk warc-file identity)))
        website?              (fn [link]
                                (or (re-find #"http:" link)
                                   (re-find #"https:" link)))
        empty-website?        (fn [link]
                                (= "http://" link))]
    (doseq [record (get-user-pages-seq warc-file)]
      (doseq [link (filter
                    (fn [link] (and (website? link)
                                 (not (empty-website? link))))
                    (get-contact-page (:payload-stream record)))]
        (println link)))))

(defn -main
  [& args]
  (let [[optional [job-directory] banner] (cli/cli args)]
    (doseq [warc-file (core/job-warc-files job-directory)]
      (handle-warc-file warc-file))))
