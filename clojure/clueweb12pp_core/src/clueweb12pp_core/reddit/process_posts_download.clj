;;;; The reddit posts download happen across our infrastructure
;;;; (Boston-Cluster, AWS1, AWS2, Lenox). The download destinations
;;;; are into a SQLITE database. The db has 2 tables. One for the post
;;;; hierarchy, the other for the post content itself. This format was
;;;; chosen since the API returned the comments in a tree format and
;;;; extracting the full tree required multiple function calls.

;;;; This chunk of code exists to process a reddit SQLITE db and
;;;; extract info/content

(ns clueweb12pp-core.reddit.process-posts-download
  (:gen-class :main true)
  (:require [clojure.java.jdbc :as db]
            [clojure.tools.cli :as cli]
            [clueweb12pp-core.reddit.model :as reddit-model]))

;;; Access to the hierarchy table
;;; Args:
;;;  path-to-db : /path/to/database
;;;  f : filtering routine on the results
(defn reddit-hierarchy
  ([path-to-db filter-by]
     (let [reddit-db (reddit-model/get-db path-to-db)]
       (db/with-connection
         reddit-db
         (db/with-query-results rs ["SELECT parent, child FROM hierarchy"]
           (doall (filter filter-by rs))))))
  ([path-to-db]
     (reddit-hierarchy path-to-db identity)))

;;; Access to the stats of the hierarchy table
;;; This routine provides you the results of a count operation
;;; on the parents column
(defn reddit-hierarchy-stats
  
  ([path-to-db filter-by]
     (let [reddit-db (reddit-model/get-db path-to-db)]
       (db/with-connection
         reddit-db
         (db/with-query-results rs ["SELECT parent, COUNT(*) FROM hierarchy LIMIT 10"]
           (doall (filter filter-by rs))))))
  
  ([path-to-db]
     (reddit-hierarchy-stats path-to-db identity)))

;;; Walk the comment tree.
;;; Args:
;;;  submission-id : an entry that has an id that looks like t3_###
;;;  comment-graph : a tree of comments
(defn reddit-thread-walk
  [start-id comment-graph f nil-return]
  (if (nil? (comment-graph start-id))
    nil-return
    (let [y (map
             (fn [x]
               (reddit-thread-walk x comment-graph f nil-return))
             (comment-graph start-id))]
      (f y))))

;;; Walk the comment tree for a submission and compute its size
(defn reddit-thread-size
  [submission-id comment-graph]
  (reddit-thread-walk submission-id
                      comment-graph
                      (fn [subtree-results] (+ 1 (apply max (flatten subtree-results))))
                      0))

;;; Builds a comment-tree from a dump of the hierarchy table
;;; We originally have:
;;;  {:parent p1 :child p1_c1
;;;   :parent p1 :child p1_c2
;;;   :parent p1 :child p1_c3
;;;   .
;;;   .}
;;;
;;; We now get:
;;;  {p1 [p1_c2 p1_c2 p1_c3]
;;;   .
;;;   .
;;;   }
(defn dump->tree
  [hierarchy-dump]
  (reduce
   (fn [acc parent-child]
     (merge-with
      concat
      acc
      {(:parent parent-child)
       [(:child parent-child)]}))
   {}
   hierarchy-dump))

;;; Computes stats on comment tree sizes on reddit
(defn reddit-tree-sizes
  [path-to-db]
  (let [reddit-hierarchy-dump (reddit-hierarchy path-to-db)
        
        submissions           (filter
                               (fn [parent-id]
                                 (re-find #"t3_" parent-id))
                               (map
                                (fn [parent-child] (-> parent-child
                                                     :parent))
                                reddit-hierarchy-dump))

        comment-graph         (dump->tree reddit-hierarchy-dump)

        sub-threads           (flatten
                               (map (fn [submission]
                                      (comment-graph submission))
                                    submissions))]
    
    (map
     (fn [sub-thread]
       {:thread-id   sub-thread
        :thread-size (reddit-thread-size sub-thread comment-graph)})
     sub-threads)))

(defn -main
  [& args]
  (let [[optional [path-to-db] banner] (cli/cli args
                                                ["--top-level-posts-count" :flag true]
                                                ["--thread-sizes" :flag true])
        
        top-level-parents-only         (fn [parent-child-map]
                                         (re-find
                                          #"t3_.*"
                                          (-> parent-child-map
                                             :parent)))]
    
    (when (:top-level-posts-count optional)
     (println (count
               (reddit-hierarchy path-to-db top-level-parents-only))))

    (when (:thread-sizes optional)
      (clojure.pprint/pprint (reddit-tree-sizes path-to-db)))))