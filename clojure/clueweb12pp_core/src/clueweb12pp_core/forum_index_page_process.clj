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

;;; Routine to check whether a job has already been completed.
;;; Essentially the last line shouldn't be a RECORD-URI. This is
;;; obviously not a sound strategy since a natural completion will
;;; also have gotten this far. The idea is that in case we screwed up
;;; on the last process, will catch it, else it will restart by
;;; skipping over the offending warc file.
(defn handle-warc-file
  [warc-file regex]
  (let [out-file         (clojure.string/join
                          "-"
                          (list
                           (last (clojure.string/split warc-file #"/"))
                           "-showthread-liks"))
        
        last-read-record (core/restart-warc-file out-file)
        
        warc-seq         (utils/warc-skip-to-record
                          (warc/get-response-records-seq
                           (warc/get-warc-reader warc-file))
                          last-read-record)]

    (binding [*out* (java.io.FileWriter. out-file true)]
     (doseq [record warc-seq]
       (println "RECORD-URI:" (:target-uri-str record)) ; print this for
                                        ; progress checking
       (let [[links dates] (handle-record record regex)]
         (when (some (fn [a-date] (core/in-clueweb12pp-time-range? a-date))
                     dates)
           (doseq [link links]
             (println link)
             (flush))))))))

(defn process-warc-files-list
  [text-file]
  (clojure.string/split
   (slurp text-file)
   #"\n"))