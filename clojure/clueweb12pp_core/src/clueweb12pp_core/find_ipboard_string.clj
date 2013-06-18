;;;; Code to identify IPBoard seeds

(ns clueweb12pp-core.find-ipboard-string
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [net.cgrand.enlive-html :as html]
            [warc-clojure.core :as warc])
  (:import [net.htmlparser.jericho Source TextExtractor Config LoggerProvider]))

(def log-file "read.log")
(def log-file-handle (clojure.java.io/writer log-file))

(set! (. Config LoggerProvider) LoggerProvider/DISABLED)

(defn detect-ipboard-str
  [record]
  (try
    (let [payload-str (.toString
                       (TextExtractor.
                        (Source. (:payload-stream record))))]
      [(:target-uri-str record) (re-find #"IP.Board" payload-str)])
    (catch Exception e ["Fail" false])))


(defn -main
  [& args]
  (let
      [[optional [ipboard-support-job] banner] (cli/cli args)]
    (doseq [warc-file (core/job-warc-files ipboard-support-job)]
      (doseq [record (warc/skip-get-response-records-seq
                      (warc/get-warc-reader warc-file))]
        (let [[link state] (detect-ipboard-str record)]
          (when state
            (println link)))))))