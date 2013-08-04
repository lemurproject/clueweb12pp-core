;;;; The reddit SQLITE db model

(ns clueweb12pp-core.reddit.model
  (:gen-class)
  (:require [clojure.java.jdbc :as db]))

(defn get-db
  [db-subname]
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     db-subname})
