(ns clueweb12pp-core.core
  (:gen-class)
  (require [clj-time.core :as ctime-core]
           [clj-http.client :as client]
           [clojure.java.io :as io]
           [clojure.set]
           [clueweb12pp-core.utils :as utils]
           [net.cgrand.enlive-html :as html]
           [org.bovinegenius.exploding-fish :as uri]
           [warc-clojure.core :as warc]))

(def clueweb12pp-time-start (ctime-core/date-time 2012 01 01))

(def clueweb12pp-time-end (ctime-core/date-time 2012 06 30))

(def target-uri-marker "RECORD-URI:")

(def clueweb12pp-crawler
  "Mozilla/5.0 (compatible; mandalay admin@lemurproject.org; +http://boston.lti.cs.cmu.edu/crawler/clueweb12pp/")

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
   (fn [warc-file] (not (re-find #".*.open" warc-file)))
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
    (pmap
     (fn [warc-file] (warc-file-handler warc-file))
     unprocessed-warcs)
    (spit process-save-file (clojure.string/join "\n" unprocessed-warcs))))

(defn uri-resolve-path-query
  [src target]
  "uri/resolve-path doesn't carry the target uri over"
  (if (uri/absolute? target)
    target
    (uri/query
     (uri/resolve-path src target)
     (uri/query target))))

(defn warc-file-integrity
  "Tests the integrity of a warc file by iterating through it"
  [warc-file]
  (doseq [record (warc/skip-get-response-records-seq
                  (warc/get-warc-reader warc-file))]
    (println (:target-uri-str record))
    (flush)))

;;; Check if a warc file was unfinished and return which position to
;;; restart from. This has happened to us as a result of a bug in the
;;; Natty library. As a result, we prefix our dumps using RECORD-URI
;;; and place the target-string-uri term in front. This is the
;;; offending warc that needs to be skipped (essentially).
(defn restart-warc-file
  [path-to-output]
  (if (utils/file-exists? path-to-output)
    (let [record-uri-regex #"RECORD-URI:(.*)"]
      (count
       (filter
        identity
        (map
         (fn [line]
           (second (re-find record-uri-regex line)))
         (utils/lines path-to-output)))))
    0))

(defn job-crawl-log-files
  "Identify crawl.log files in a heritrix job directory"
  [job-directory]
  (map
   #(.getAbsolutePath %)
   (filter
    (fn [a-file]
      (re-find #"crawl.log" (.getAbsolutePath a-file)))
    (file-seq (io/file job-directory)))))

(defn search-crawl-log
  [crawl-log-file regex]
  (doall
   (let [rdr (io/reader crawl-log-file)]
     (filter
      (fn [a-link]
        (re-find regex a-link))
      (map
       (fn [a-line]
         (let [[_ _ _ target _ src _ _ _ _ _ _] (clojure.string/split a-line #"\s+")]
           target))
       (line-seq rdr))))))

(defn search-crawl-logs
  "Go through a heritrix job's crawl directory
and apply the regex provided to the URLs crawled"
  [job-directory regex]
  (flatten
   (map
    #(search-crawl-log % regex)
    (job-crawl-log-files job-directory))))

(defn do-to-warc-file
  "Apply handle-record to a warc file with enough info to
re-bootstrap a hung job. This function returns nil."
  [warc-file handle-record output-file]
  (let [restart-from (restart-warc-file output-file)]
    (with-open
        [wrtr (io/writer output-file :append true)]
      (binding [*out* wrtr]
       (doseq [record (drop
                       restart-from
                       (warc/skip-get-response-records-seq
                        (warc/get-warc-reader warc-file)))]
         (println target-uri-marker (:target-uri-str record))
         (flush)
         (handle-record record))))))

(defn warc-records-seq
  "Returns a seq of records in a warc. Saves you 1 call"
  [warc-file]
  (warc/skip-get-response-records-seq
   (warc/get-warc-reader warc-file)))

(defn html-resource
  "This function exists since enlive's html-resource
doesn't allow us to specify a User-Agent string"
  [url]
  (-> (client/get url {:headers {"User-Agent" clueweb12pp-crawler}})
     :body
     java.io.StringReader.
     html/html-resource))