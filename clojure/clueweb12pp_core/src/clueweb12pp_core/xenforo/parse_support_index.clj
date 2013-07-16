;;;; To work around the member page restrictions, we downloaded the
;;;; index pages from xenforo support.

(ns clueweb12pp-core.xenforo.parse-support-index
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.forum-index-page-process :as index-page-process]
            [warc-clojure.core :as warc])
  (:import [net.htmlparser.jericho Source TextExtractor Config LoggerProvider HTMLElementName]))

(set! (. Config LoggerProvider) LoggerProvider/DISABLED)

(defn handle-record
  [record]
  (index-page-process/links-in-source
   (Source. (:payload-stream record))
   #".*members/.+"))

(defn -main
  [& args]
  (let [[optional [warc-files-list] banner] (cli/cli args)]
    (doseq [warc-file (index-page-process/process-warc-files-list warc-files-list)]
      (doseq [record (warc/skip-get-response-records-seq
                      (warc/get-warc-reader warc-file))]
        (doseq [link (handle-record record)]
          (println (clojure.string/join
                    "/"
                    (list "http://xenforo.com/community"
                          link))))))))