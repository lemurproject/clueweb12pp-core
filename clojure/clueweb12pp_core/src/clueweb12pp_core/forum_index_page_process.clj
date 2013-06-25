;;;; Process an index page and produce topic links
;;;; These topic links are (supposed to be) from our time range.
;;;; For now let us just spit out a list of all links on the page
;;;; Yuku and zetaboards regex patterns included in the program -
;;;; specify as flag on cmd-line.


(ns clueweb12pp-core.forum-index-page-process
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [clueweb12pp-core.page-times :as page-times]
            [org.bovinegenius.exploding-fish :as uri]
            [warc-clojure.core :as warc])
  (:import [net.htmlparser.jericho Source TextExtractor Config LoggerProvider HTMLElementName]))

(set! (. Config LoggerProvider) LoggerProvider/DISABLED)

(def yuku-topic-regex #".*/topic/.*")
(def zeta-topic-regex #".*/topic/.*")


(defn handle-record
  ([record] (handle-record record #".*"))
  ([record regex]
     (let [source (Source. (:payload-stream record))]
       (when (some (fn [x] (core/in-clueweb12pp-time-range? x))
                   (page-times/dates-in-text
                    (.toString
                     (TextExtractor. source))))
         (filter
          #(re-find regex %)
          (map
           (fn [an-a-tag]
             (.toString
              (clojure.string/replace
               (let [a-link (uri/uri (.getValue (.getAttributes an-a-tag) "href"))]
                 (if (uri/absolute? a-link)
                   a-link
                   (uri/path
                    (uri/uri (:target-uri-str record))
                    a-link)))
               #"\?.*"
               "")))
           (.getAllElements
            source
            HTMLElementName/A)))))))

(defn -main
  [& args]
  (let [[optional [job-directory] banner] (cli/cli args
                                                   ["-y" "--yuku" :flag true]
                                                   ["-z" "--zeta" :flag true])]
    (doseq [warc-file (core/job-warc-files job-directory)]
      (doseq [record (warc/skip-get-response-records-seq
                      (warc/get-warc-reader warc-file))]
        (cond (-> optional :yuku)
              (doseq [link (handle-record record yuku-topic-regex)]
                (println link))
              (-> optional :zeta)
              (doseq [link (handle-record record zeta-topic-regex)]
                (println link))
              :else (doseq [link (handle-record record)]
                      (println link)))))))
