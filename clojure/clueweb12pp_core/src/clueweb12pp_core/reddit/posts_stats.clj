;;;; Statistics on the post crawl

(ns clueweb12pp-core.reddit.posts-stats
  (:gen-class :main true)
  (:require [clojure.java.jdbc :as db]
            [clojure.tools.cli :as cli]
            [clueweb12pp-core.reddit.model :as reddit-model]
            [clueweb12pp-core.reddit.process-posts-download :as process-posts-download]))

(defn top-level-posts
  [path-to-db]
  (let [reddit-hierarchy   (process-posts-download/reddit-hierarchy path-to-db)

        submissions        (filter
                            (fn [parent-child] (re-find #"t3_" (-> parent-child
                                                               :parent)))
                            reddit-hierarchy)]
    (map
     (fn [parent-child] (-> parent-child
                          :child))
     submissions)))

(defn top-level-posts-with-discussion
  [path-to-db top-level-post-ids]
  (let [reddit-hierarchy (process-posts-download/reddit-hierarchy path-to-db)
        hierarchy-tree   (process-posts-download/dump->tree reddit-hierarchy)]
    (filter
     #(hierarchy-tree %)
     top-level-post-ids)))

(defn -main
  [& args]
  (let [[optional [path-to-db] banner]
        (cli/cli
         args
         ["--num-top-level"
          "Computes the number of top-level posts"
          :flag true])]

    (when (-> optional :num-top-level)
      (let [top-level-post-ids (top-level-posts path-to-db)]
        (println "Number of top-level posts:" (count top-level-post-ids))
        (println "Top level posts with underlying discussion"
                 (count (top-level-posts-with-discussion path-to-db top-level-post-ids)))))))