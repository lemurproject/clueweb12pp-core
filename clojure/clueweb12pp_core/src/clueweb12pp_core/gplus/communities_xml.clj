;;;; Navigate the links to the public communities in the robots.txt
;;;; file at http://plus.google.com/robots.txt

(ns clueweb12pp-core.gplus.communities-xml
  (:gen-class :main true)
  (:require [clojure.data.xml :as xml]
            [clojure.java.io :as io]
            [clojure.tools.cli :as cli]
            [net.cgrand.enlive-html :as html]))


(defn -main
  [& args]
  (let [[optional [communities-file] banner] (cli/cli args)]
    (with-open [rdr (io/reader communities-file)]
      (doseq [link (map
                    #(-> %
                        :content
                        first)
                    (-> (xml/parse rdr)
                       (html/select [:sitemapindex :sitemap :loc])))]
        (println link)))))