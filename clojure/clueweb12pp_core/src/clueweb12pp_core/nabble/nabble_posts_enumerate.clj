(ns clueweb12pp_core.nabble.nabble_posts_enumerate
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]))

(defn enumerate-links
  [warc-file]
  (filter
   (fn [url] (re-find #"-td\d*.html" url))
   (core/non-meta-warc-file-walk warc-file (fn [record] (:target-uri-str record)))))

(defn -main
  [& args]
  (let [[optional [& job-directories] banner] (cli/cli args)]
    (doseq [job-directory job-directories]
      (doseq [warc-file (core/job-warc-files job-directory)]
        (doseq [link (enumerate-links warc-file)]
          (println link)
          (flush))))))