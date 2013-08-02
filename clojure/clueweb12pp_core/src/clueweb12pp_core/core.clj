(ns clueweb12pp-core.core
  (:gen-class)
  (require [clj-time.core :as ctime-core]
           [clojure.java.io :as io]
           [clojure.set]
           [net.cgrand.enlive-html :as html]
           [org.bovinegenius.exploding-fish :as uri]
           [warc-clojure.core :as warc]))

(def clueweb12pp-time-start (ctime-core/date-time 2012 01 01))
(def clueweb12pp-time-end (ctime-core/date-time 2012 06 30))

;;;; Is the specified time in the clueweb12pp time range
(defn in-clueweb12pp-time-range?
  [a-time]
  (ctime-core/within? (ctime-core/interval clueweb12pp-time-start clueweb12pp-time-end)
                      a-time))

;;;; Applies f to every record in the supplied warc.gz file
(defn warc-file-reduce
  [f init warc-file-path]
  (reduce
   f
   init
   (warc/get-http-records-seq (warc/get-warc-reader warc-file-path))))

;;;; Walk over a warc file
(defn warc-file-walk
  [warc-file-path f]
  (for [record (warc/get-http-records-seq
                (warc/get-warc-reader warc-file-path))]
    (f record)))

(defn skip-warc-file-walk
  [warc-file-path f]
  (for [record (warc/skip-get-http-records-seq
                (warc/get-warc-reader warc-file-path))]
    (f record)))

;;;; Compute a set of links visited in the warc-file
(defn warc-src-links
  [warc-file-path]
  (warc-file-reduce
   (fn [acc record]
     (cons (:target-uri-str record) acc))
   '()
   warc-file-path))

;;;; Compute a set of body-links in the file
(defn warc-body-links
  [warc-file-path]
  (let [record-links (fn [record]
                       (map
                        (fn [a-tag]
                          (-> a-tag
                             :attrs
                             :href))
                        (html/select
                         (html/html-resource (:payload-stream record))
                         [:a])))]
    (flatten
     (warc-file-walk warc-file-path record-links))))

;;;; Only traverse non robot.txt response links.
(defn non-meta-warc-file-walk
  [warc-file-path f]
  (for [record (filter
                (fn [record]
                  (not (re-find #"robots.txt" (:target-uri-str record))))
                (warc/get-http-records-seq
                 (warc/get-warc-reader warc-file-path)))]
    (f record)))

(defn skip-non-meta-warc-file-walk
  [warc-file-path f]
  (for [record (filter
                (fn [record]
                  (not (re-find #"robots.txt" (:target-uri-str record))))
                (warc/skip-get-response-records-seq
                 (warc/get-warc-reader warc-file-path)))]
    (f record)))

;;;; Provides a list of full paths of warc files
;;;; in a heritrix job direcotry
(defn job-warc-files
  [job-directory]
  (map
   (fn [x]
     (.getAbsolutePath x))
   (filter
    (fn [file-obj]
      (and (re-find #".*warc.gz.*" (.getAbsolutePath file-obj))
         (not (re-find #".*latest.*" (.getAbsolutePath file-obj))))) ; this needed since a currently running job with get two of these
    (file-seq (io/file job-directory)))))

(defn job-closed-warc-files
  "Warc files without a .open at the end"
  [job-directory]
  (filter
   (fn [warc-file] (re-find #".*.open" warc-file))
   (job-warc-files job-directory)))

;;;; We compute the crawled links and then the
;;;; links pointed to in the body. The set difference
;;;; is the outlinks list.
;;;; Args:
;;;;  - warc-files : list of files to compute outlinks from
;;;; Output:
;;;;  - list of outlinks
(defn job-outlinks
  [warc-files]
  (let [body-links (set (flatten (for [warc-file warc-files]
                                   (warc-body-links warc-file))))
        src-links  (set (flatten (for [warc-file warc-files]
                                   (warc-src-links warc-file))))]
    (clojure.set/difference body-links src-links)))

;;;; Sample the warc files to collect <num-samples> records
(defn sample-warc-files
  [warc-files-list num-samples]
  (for [i (range 0 num-samples)]
    (rand-nth (non-meta-warc-file-walk
               (rand-nth warc-files-list) identity))))

(defn content-warc?
  [warc-payload]
  (let [first-line (first (clojure.string/split-lines warc-payload))]
    (re-find "200" first-line)))

(defn process-text-file
  [text-file]
  (clojure.string/split
   (slurp text-file)
   #"\n"))

(defn warc-process-engine
  "Framework to process a heritrix job.
   Args:
    heritrix-directory : /path/to/job/directory
    process-save-file : where to save list of processed warc files
    warc-file-handler : where to save list"
  [heritrix-directory process-save-file warc-file-handler]

  (let [processed-warc-files (set
                             (clojure.string/split
                              (slurp process-save-file)
                              #"\n"))
        unprocessed-warcs    (filter
                              (fn [warc-file]
                                (not (contains? processed-warc-files warc-file)))
                              (job-closed-warc-files heritrix-directory))]
    (doseq [warc-file unprocessed-warcs]
      (warc-file-handler warc-file))
    (spit process-save-file (clojure.string/join "\n" unprocessed-warcs))))

(defn uri-resolve-path-query
  [src target]
  "uri/resolve-path doesn't carry the target uri over"
  (if (uri/absolute? target)
    target
    (uri/query
     (uri/resolve-path src target)
     (uri/query target))))