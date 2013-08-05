;;;; Code specific to processing ipboard index pages

(ns clueweb12pp-core.ipboard.process-index-pages
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [clueweb12pp-core.forum-index-page-process :as index-page-process]
            [clueweb12pp-core.ipboard.consts :as consts]
            [warc-clojure.core :as warc]))


