;;;; Access the posts in Stackoverflow

(ns clueweb12pp-core.stackoverflow.process-posts
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clojure.data.xml :as xml]
            [clueweb12pp-core.core :as core]))


(defn count-posts
  [posts-file]
  (doseq [x (xml/parse posts-file)]
    (println x)
    (flush)))


(defn -main
  [& args]
  (let [[optional [post-file] banner] (cli/cli
                                       args
                                       ["-c"
                                        "--count-posts"
                                        "Count the number of posts in the Clueweb time range"
                                        :flag true])]
    (when (:count-posts optional)
      (with-open [rdr (java.io.FileReader. post-file)]
        (println (xml/parse rdr))))))
