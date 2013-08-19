;;;; Code to process the seeds list and obtain a list of gmane
;;;; newsgroups. This is done so that we don't duplicate content
;;;; across our newsgroup crawls.

(ns clueweb12pp-core.gmane.gmane-newsgroups
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clueweb12pp-core.utils :as utils]))

(defn -main
  [& args]
  (let [[optional [gmane-seeds-list] banner] (cli/cli args)]
    (doall
     (map
      #(println
        (clojure.string/replace % #"http://blog.gmane.org/gmane." ""))
      (utils/lines gmane-seeds-list)))))
