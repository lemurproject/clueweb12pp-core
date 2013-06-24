;;;; Generate seeds for livejournal communities download.
;;;; The step to grab these urls is:
;;;;  - Visit the calendar for 2012
;;;;  - Visit the posts for our timeframe
;;;;  - Visit and download
;;;;  - Not sure what to do with the expanding of posts. This has to
;;;; be confirmed by jamie.

(ns clueweb12pp_core.livejournal.livejournal_seeds
  (:gen-class :main true)
  (:require [clueweb12pp-core.core :as core]
            [clojure.java.io :as io]
            [clojure.tools.cli :as cli]))

(defn prepare-2012-url
  [url]
  (clojure.string/join "" (list url "2012")))

(defn -main
  [& args]
  (let [[optional [communities-list] banner] (cli/cli args)]
    (with-open [rdr (io/reader communities-list)]
      (doseq [line (line-seq rdr)]
        (println (prepare-2012-url line))))))
