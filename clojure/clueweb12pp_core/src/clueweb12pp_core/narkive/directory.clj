;;;; Code to scrape and obtain a list of Narkive newsgroups
;;;; Printed to STDOUT:
;;;;  <newsgroup name> <newsgroup url>
;;;;  .
;;;;  .
;;;;  .

(ns clueweb12pp-core.narkive.directory
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [net.cgrand.enlive-html :as html]))

(defn handle-record
  [record]
  (doall
   (map
    (fn [group-link] (println (-> group-link
                                html/text) 
                             (-> group-link
                                :attrs
                                :href)))
    (-> record
       :payload-stream
       html/html-resource
       (html/select [:div#thread_lister :a])))))

(defn handle-directory-job
  [job-directory]
  (doseq [warc-file (core/job-warc-files job-directory)]
    (doseq [record (core/warc-records-seq warc-file)]
      (handle-record record))))

(defn -main
  [& args]
  (let [[optional [narkive-directory] banner] (cli/cli args)]
    (handle-directory-job narkive-directory)))