(ns clueweb12pp_core.nabble_posts_process
  (:gen-class :main true)
  (:require [clj-time.core :as ctime-core]
            [clj-time.coerce :as ctime-coerce]
            [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [net.cgrand.enlive-html :as html]))

(defn process-nabble-page
  [nabble-post-record]
  (let [get-date-from-js       (fn [line]
                                 (ctime-coerce/from-long (java.lang.Long/parseLong
                                                          (last (re-find #".*new Date\((\d+)\).*" line)))))
        in-clueweb-time-range  (fn [a-date]
                                 (ctime-core/within? (ctime-core/interval core/clueweb12pp-time-start
                                                                          core/clueweb12pp-time-end)
                                                     a-date))]
    {:uri   (:target-uri-str nabble-post-record)
     
     :dates (filter
             in-clueweb-time-range
             (map
              get-date-from-js
              (map
               html/text
               (html/select
                (html/html-resource (:payload-stream nabble-post-record))
                [:div.classic-subject-line :span.post-date]))))}))

(defn process-nabble-post-directory
  [post-directory]
  (for [warc-file (core/job-warc-files post-directory)]
    (filter
     (fn [x] (not (empty? (:dates x))))
     (core/non-meta-warc-file-walk warc-file process-nabble-page))))

(defn -main
  [& args]
  (let [[optional [& nabble-post-directories-list] banner] (cli/cli args)]
    (doseq [nabble-post-directory nabble-post-directories-list]
      (doseq [warc-file-data (process-nabble-post-directory nabble-post-directory)]
        (doseq [url-dates warc-file-data]
          (println (:uri url-dates)))))))