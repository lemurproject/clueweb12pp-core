;;;; We need to know how many articles and consequently the number of
;;;; discussions in the NYT corpus. Here we print out a sequence of
;;;; article urls.

(ns clueweb12pp-core.nyt.nyt-compute-stats
  (:gen-class :main true)
  (:require [clojure.java.io :as io]
            [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [clueweb12pp-core.nyt.process-nyt-date-file :as process-date-file]))

(defn -main
  [& args]
  (let [[optional [nyt-jobs-directory] banner] (cli/cli args)
        directory-seq (rest (file-seq (io/file nyt-jobs-directory)))
        article-comment-counts {}]
    (doseq [date-file directory-seq]
      (doseq [datum (process-date-file/nyt-date-file-contents date-file)]
        (println (:articleURL datum))))))
