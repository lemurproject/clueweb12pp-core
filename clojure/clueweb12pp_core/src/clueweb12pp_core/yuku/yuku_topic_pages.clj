(ns clueweb12pp-core.yuku.yuku-topic-pages
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [clueweb12pp-core.page-times :as page-times]
            [clueweb12pp-core.utils :as utils]
            [warc-clojure.core :as warc])
  (:import (net.htmlparser.jericho Source TextExtractor Config LoggerProvider)))

(defn handle-record
  [record]
  (let [text (.toString
              (TextExtractor.
               (Source. (:payload-stream record))))]
    (when (some
           core/in-clueweb12pp-time-range?
           (page-times/dates-in-text text))
      (println (:target-uri-str record))
      (flush))))

(defn -main
  [& args]
  (let [[optional [warc-file] banner] (cli/cli args)
        output-file (clojure.string/join
                     ""
                     (list
                      (last (clojure.string/split warc-file #"/"))
                      "-links.txt"))]
    (core/do-to-warc-file warc-file handle-record output-file)))