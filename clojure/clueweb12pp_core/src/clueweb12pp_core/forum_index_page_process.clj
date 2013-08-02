;;;; Process an index page and produce topic links
;;;; These topic links are (supposed to be) from our time range.
;;;; For now let us just spit out a list of all links on the page
;;;; Yuku and zetaboards regex patterns included in the program -
;;;; specify as flag on cmd-line.


(ns clueweb12pp-core.forum-index-page-process
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [clueweb12pp-core.ipboard.consts :as ipboard-consts]
            [clueweb12pp-core.page-times :as page-times]
            [org.bovinegenius.exploding-fish :as uri]
            [warc-clojure.core :as warc])
  (:import [net.htmlparser.jericho Source TextExtractor Config LoggerProvider HTMLElementName]))

(set! (. Config LoggerProvider) LoggerProvider/DISABLED)

(defn links-in-source
  [jericho-source regex]
  (filter
   (fn [a-link] (try (re-find regex a-link)
                    (catch Exception e false)))
   (map
    (fn [a-tag] (.getAttributeValue a-tag "href"))
    (.getAllElements
     jericho-source
     HTMLElementName/A))))

(defn dates-on-page
  [jericho-source]
  (try (page-times/dates-in-text
        (.toString
         (TextExtractor. jericho-source)))
       (catch Exception e [])))

(defn handle-record
  ([record regex]
     (let [source          (try (Source. (:payload-stream record))
                                (catch Exception e nil))
           links           (try (links-in-source source regex)
                                (catch Exception e []))
           dates           (try (dates-on-page source)
                                (catch Exception e []))
           processed-links (map
                            (fn [a-link]
                              (if (uri/absolute? a-link)
                                a-link
                                (core/uri-resolve-path-query
                                 (:target-uri-str record)
                                 a-link)))
                            links)]
       (list processed-links dates)))
  ([record] (handle-record record #".*")))

(defn handle-warc-file
  [warc-file regex]
  (binding [*out* (java.io.FileWriter.
                           (clojure.string/join "-"
                                                (list
                                                 (last (clojure.string/split warc-file #"/"))
                                                     "-showthread-liks")))]
   (doseq [record (warc/skip-get-response-records-seq
                   (warc/get-warc-reader warc-file))]
     (let [[links dates] (handle-record record regex)]
       (when (some (fn [a-date] (core/in-clueweb12pp-time-range? a-date))
                   dates)
         (doseq [link links]
           (println link)
           (flush)))))))