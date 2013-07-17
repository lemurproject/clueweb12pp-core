;;;; Partial implementation of the AWS Product Search API. We only
;;;; have those methods that we care about

(ns clueweb12pp-core.amazon.product-search-api
  (:gen-class :main true)
  (:require  [clj-http.client :as client]
             [clojure.tools.cli :as cli]
             [clojure.xml :as xml]
             [clueweb12pp-core.utils :as utils]
             [net.cgrand.enlive-html :as html])
  (:import (com.amazon.advertising.api.sample SignedRequestsHelper)))

(def *endpoint* "ecs.amazonaws.com")

;; The itemlookup operation
(def item-lookup-operation "ItemLookup")
(def item-search-operation "ItemSearch")
(def browse-node-operation "BrowseNodeLookup")

;; Standard params of all operations
(def request-common-params {"Service"       "AWSECommerceService"
                            "Version"       "2009-03-31"})

(defn get-signed-request-helper
  [aws-key secret-key]
  (SignedRequestsHelper/getInstance *endpoint* aws-key secret-key))

(defn build-browse-node-params
  [node-id]
  {"BrowseNodeId" node-id})

(defn build-request
  [signed-request-helper params]
  (.sign
   signed-request-helper
   (java.util.HashMap. params)))

(defn build-browse-node-lookup-request
  [signed-request-helper associate-tag browse-node-id]
  (let [params (merge request-common-params
                      {"Operation"    "BrowseNodeLookup"
                       "BrowseNodeId" browse-node-id
                       "AssociateTag" associate-tag})]
    (build-request signed-request-helper params)))

(defn browse-node-lookup-response
  [response]
  (distinct
   (flatten
    (map
     (fn [x] (-> x :content))
     (html/select
      response
      [:BrowseNodeLookupResponse :BrowseNodeId])))))

(defn get-node-tree
  [root-node aws-key secret-key associate-tag]
  (let [signed-request-helper (get-signed-request-helper aws-key secret-key)
        a-signed-url (build-browse-node-lookup-request signed-request-helper associate-tag root-node)
        results (first
                 (map
                  browse-node-lookup-response
                  (-> (client/get a-signed-url)
                     :body
                     utils/zip-str)))]
    (doseq [id (rest results)]
      (println id))
    (Thread/sleep 1000) ; amazon usage limits
    (map
     (fn [node-id]
       (get-node-tree node-id aws-key secret-key associate-tag))
     (rest results))))

;;; the main method is mostly here for me to run some simple tests on
;;; the API
(defn -main
  [& args]
  (let [[optional [aws-key secret-key associate-tag root-id] banner] (cli/cli args)]
    (get-node-tree root-id aws-key secret-key associate-tag)))
