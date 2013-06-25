;;;; Process the gaia online index pages downloaded and extract the
;;;; posts from them

(ns clueweb12pp-core.gaia.posts
  (:gen-class :main true)
  (:require [clueweb12pp-core.core :as core]
            [clueweb12pp-core.utils :as utils]
            [clojure.tools.cli :as cli]
            [warc-clojure.core :as warc]
            [clj-time.core :as cljt]
            [clj-time.coerce :as cljc]
            (org.bovinegenius [exploding-fish :as uri]))
  (:import  [net.htmlparser.jericho Source TextExtractor Config LoggerProvider HTMLElementName]))

(set! (. Config LoggerProvider) LoggerProvider/DISABLED) ;; turn off
;; notifications thrown by this file


(defn gaia-online-time-format
  [updated-time-str]
  (let
      [[_ date mon-str yr _ _] (clojure.string/split updated-time-str #"\s+")]
    (cljt/date-time
     (java.lang.Integer/parseInt yr)
     (utils/month->int mon-str)
     (java.lang.Integer/parseInt date))))

(defn get-unique-topic
  [topic-links]
  (first
   (distinct (map
              (fn [topic-link] (second (re-find #"(.*/t.\d+).*" topic-link)))
              topic-links))))

(defn handle-record
  [record]
  (let [;; rows on page
        rows            (.getAllElements
                         (Source. (:payload-stream record))
                         HTMLElementName/TR) ;; rows on page

        ;; css class of the element
        element-class       (fn [element]
                         (try
                           (.getValue (.getAttributes element) "class")
                           (catch Exception e nil)))

        ;; rows in page containing topic links
        topic-link-rows (filter
                         (fn [row] (and row
                                     (element-class row)
                                     (or (re-find #"rowon" (element-class row))
                                        (re-find #"rowoff" (element-class row)))))
                         rows)

        ;; get topic link and topic date td elements from a topic row
        topic-elements  (fn [row]
                          (filter
                           (fn [td-element]
                             (or (re-find #"topic" (element-class td-element))
                                (re-find #"lastupdated" (element-class td-element))))
                           (.getAllElements
                            row
                            HTMLElementName/TD)))

        ;; from the topic link td element extract base topic link
        topic-link      (fn [topic-link-element]
                          (map
                           (fn [a-tag]
                             (.toString
                              (uri/path
                               (uri/uri (:target-uri-str record))
                               (.getValue
                                (.getAttributes a-tag)
                                "href"))))
                           (.getAllElements
                            topic-link-element
                            HTMLElementName/A)))

        ;; from the topic-date element extract the date information
        topic-updated   (fn [topic-updated-element]
                          (try
                           (gaia-online-time-format
                            (.getValue
                             (.getAttributes
                              (first
                               (.getAllElements
                                topic-updated-element
                                HTMLElementName/ABBR)))
                             "title"))
                           (catch Exception e (println "FUCK UP:" (:target-uri-str record)))))]
    (for [row topic-link-rows]
      (let [[topic-link-ele topic-updated-ele] (topic-elements row)]
        (list (topic-link topic-link-ele)
              (topic-updated topic-updated-ele))))))

(defn -main
  [& args]
  (let [[optional [job-directory] banner] (cli/cli args)]
    (doseq [warc-file (core/job-warc-files job-directory)]
      (doseq [record (warc/skip-get-response-records-seq
                      (warc/get-warc-reader warc-file))]
        (try (doseq [[topic-links topic-time] (handle-record record)]
               (when (core/in-clueweb12pp-time-range? topic-time)
                 (println (get-unique-topic topic-links))))
             (catch Exception e "FUCK UP:" (:target-uri-str record)))))))