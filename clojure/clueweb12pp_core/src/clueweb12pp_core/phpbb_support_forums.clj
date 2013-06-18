;;;; This is code to process the phpbb support group's member list
;;;; We got blocked doing this. We will continue further

(ns clueweb12pp_core.phpbb_support_forums
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [net.cgrand.enlive-html :as html]
            [warc-clojure.core :as warc])
  (:import [org.jwat.warc WarcRecord WarcReader WarcReaderFactory]))

(defn handle-warc-file
  [warc-file]
  (let [process-page          (fn [page-stream]
                                (html/select
                                 (html/html-resource page-stream)
                                 [:table :td.info :a]))
        get-contact-pages     (fn [page-stream]
                                (map
                                 (fn [tag]
                                   (-> tag
                                      :attrs
                                      :href))
                                 (process-page page-stream)))
        get-https-records-seq (fn [warc-file]
                                (filter
                                 (fn [record]
                                   (re-find #"^https:" (:target-uri-str record)))
                                 (warc/get-response-records-seq
                                  (warc/get-warc-reader warc-file))))]
    (doseq [record (get-https-records-seq warc-file)]
      (doseq [link (get-contact-pages (:payload-stream record))]
        (println link)))))

(defn -main
  [& args]
  (let [[optional [job-directory] banner] (cli/cli args)]
    (doseq [warc-file (filter
                       (fn [dir-file] (re-find #".*warc.gz.*" (.getAbsolutePath dir-file)))
                       (file-seq (clojure.java.io/file job-directory)))]
      (handle-warc-file (.getAbsolutePath warc-file)))))

