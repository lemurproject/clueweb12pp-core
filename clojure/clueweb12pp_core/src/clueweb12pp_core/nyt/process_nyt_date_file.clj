;;;; The New York Times corpus in Clueweb12++ was downloaded keeping
;;;; each day in mind. This routine provides an interface for reading
;;;; a day's file

(ns clueweb12pp-core.nyt.process-nyt-date-file
  (:gen-class :main true)
  (:require [clueweb12pp-core.core :as core]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.tools.cli :as cli]
            [clojure.walk :as walk])
  (:import [java.io InputStreamReader]
           [java.util.zip GZIPInputStream]))

(defn nyt-date-file-contents
  [nyt-date-file]
  (let [date-file-reader (-> nyt-date-file
                            io/input-stream
                            GZIPInputStream.
                            io/reader)]
    (map
     (fn [line] (walk/keywordize-keys (json/read-str line)))
     (line-seq date-file-reader))))

(defn process-nyt-date-file
  [nyt-date-file]
  (doseq [line (nyt-date-file-contents nyt-date-file)]
    (println line)))

(defn -main
  [& args]
  (let [[optional [nyt-date-file] banner] (cli/cli args)]
    (process-nyt-date-file nyt-date-file)))