(ns clueweb12pp-core.gmane.gmane-process-downloads
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [warc-clojure.core :as warc])
  (:import [net.htmlparser.jericho Source TextExtractor Config LoggerProvider]))

;;;; Turn verbose error messages off
(set! (. Config LoggerProvider) LoggerProvider/DISABLED)

;;;; Turn this nonsense off as well
(defn get-post-titles
  [record]
  (map
   (fn [x] (.toString (TextExtractor. x)))
   (.getAllElementsByClass
    (Source. (:payload-stream record))
    "title")))

;;;;                                                                                                
(defn -main
  [& args]
  (let [[optional [gmane-job-directory] banner] (cli/cli args)]
    (doseq [warc-file (core/job-warc-files gmane-job-directory)]
      (doseq [record (warc/skip-get-response-records-seq
                      (warc/get-warc-reader warc-file))]
        (when (re-find #"2012" (:target-uri-str record))
          (doseq [title (get-post-titles record)]
            (println title)
            (flush)))))))