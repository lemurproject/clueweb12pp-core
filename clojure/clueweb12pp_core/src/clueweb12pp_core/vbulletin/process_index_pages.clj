;;;; Code specific to processing ipboard index pages


(ns clueweb12pp-core.vbulletin.process-index-pages
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.core :as core]
            [clueweb12pp-core.forum-index-page-process :as index-page]
            [clueweb12pp-core.page-times :as page-times]
            [clueweb12pp-core.vbulletin.consts :as consts]))

(def *process-save-file* "/bos/tmp19/spalakod/clueweb12pp/data/vbulletin-index-page-warcs.txt")

(defn -main
  [& args]
  (let [[optional [warc-file] banner] (cli/cli args)]
    (index-page/handle-warc-file
     warc-file
     consts/unfriendly-topics-regex)))