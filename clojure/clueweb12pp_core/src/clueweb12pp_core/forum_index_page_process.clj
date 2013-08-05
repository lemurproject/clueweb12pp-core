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
            [clueweb12pp-core.utils :as utils]
            [org.bovinegenius.exploding-fish :as uri]
            [warc-clojure.core :as warc])
  (:import [net.htmlparser.jericho Source TextExtractor Config LoggerProvider HTMLElementName]))

(set! (. Config LoggerProvider) LoggerProvider/DISABLED)

(defn links-in-source
  "Retuns a sequence of links in a jericho source matching a regex.
   Jericho source is obtained by doing running: (Source. page-stream)"
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
  "On an index page, dates are stored in tables.
   So we parse the table content for dates"
  [jericho-source]
  (let [text (.toString
              (TextExtractor. jericho-source))]
    (try (utils/with-timeout
           30000
           []
           (page-times/dates-in-text
            text))
         (catch Exception e []))))

(defn handle-record
  ([record regex]
     (let [source          (try (Source. (:payload-stream record))
                                (catch Exception e nil))
           links           (try (links-in-source source regex)
                                (catch Exception e []))
           dates           (utils/with-timeout
                             1000
                             []
                             (try (doall (dates-on-page source))
                                  (catch Exception e [])))
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
   (doseq [record (warc/get-response-records-seq
                   (warc/get-warc-reader warc-file))]
     (println "RECORD-URI:" (:target-uri-str record)) ; print this for
                                                      ; progress checking
     (let [[links dates] (handle-record record regex)]
       (when (some (fn [a-date] (core/in-clueweb12pp-time-range? a-date))
                   dates)
         (doseq [link links]
           (println link)
           (flush)))))))

(defn process-warc-files-list
  [text-file]
  (clojure.string/split
   (slurp text-file)
   #"\n"))