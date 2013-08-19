;;;; Code to download a newsgroup from narkive.
;;;; We are using a non-heritrix setup here.
;;;; The idea is that if we binary search for the time-range needed,
;;;; then we can speed up the narkive crawl and keep the narkive folk
;;;; happy as well

(ns clueweb12pp-core.narkive.download-newsgroup
  (:gen-class :main true)
  (:require [clj-http.client :as client]
            [clj-time.core :as core-time]
            [clj-time.format :as format-time]
            [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [net.cgrand.enlive-html :as html]))

(defn dates-on-narkive-page
  "Get the max date on the page."
  [page-src]
  (let [split-rev (fn [re str] (clojure.string/split str re))]
   (map
    (fn [a-span]
      (->> a-span
         :attrs
         :title
         (split-rev #"\+")
         first
         (format-time/parse
          (format-time/formatters :date-hour-minute-second))))
    (-> page-src
       (html/select [:span.timeago])))))

(defn scope-newsgroup
  "Scoping is where we get some stats about how
many pages there are overall"
  [newsgroup-link]
  (let [page-src  (-> newsgroup-link
                     core/html-resource)
        num-pages (first
                   (filter
                    identity
                    (map
                     (fn [a-div]
                       (-> (->> a-div
                             html/text
                             (re-find #"Page (\d+) of (\d+)"))
                          (nth 2)))
                     (-> page-src
                        (html/select [:div])))))]
    num-pages))

(defn page-url
  "The pagination info is only available on page two of the results"
  [newsgroup-link page-num]
  (clojure.string/join "" (list newsgroup-link
                                "p/"
                                (str page-num))))

(defn span-neighborhood
  [page-url]
  (println "PAGE IDENTIFIED!!!!" page-url))

(defn search-start-time-range
  [narkive-link start end]
  (println narkive-link start end)
  (. Thread sleep 3000)
  (let [mid               (int (/ (+ start end) 2))
        
        mid-page-src      (-> narkive-link
                             (page-url mid)
                             core/html-resource)
        
        dates-on-mid-page (-> mid-page-src
                             dates-on-narkive-page)]
    
    (when (not (or (= start mid)
                (= end mid)))
      (cond (some core/in-clueweb12pp-time-range?
                  dates-on-mid-page)
            (span-neighborhood (page-url narkive-link mid)) ; identified time-range posts

            (every?
             (fn [date] (core-time/after? date core/clueweb12pp-time-end))
             dates-on-mid-page)
            (recur narkive-link mid end) ; ended up beyond the time-range

            (every?
             (fn [date] (core-time/before? date core/clueweb12pp-time-start))
             dates-on-mid-page)
            (recur narkive-link start mid)   ; ended up short of the
                                        ; time range

            :else                          ; no post in time range
            nil))))

(defn -main
  [& args]
  (let [[optional [newsgroup-link] banner] (cli/cli args)]
    (println (search-start-time-range
              newsgroup-link
              1
              (java.lang.Integer/parseInt
               (scope-newsgroup (page-url newsgroup-link 2)))))))
