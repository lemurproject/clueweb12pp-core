(ns clueweb12pp_core.yuku.yuku_index_pages
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [clj-time.core :as core-time]
            [clj-time.format :as format-time]
            [clj-time.coerce :as coerce-time]
            [clueweb12pp-core.core :as core]
            [clueweb12pp-core.page-times :as page-times]
            [warc-clojure.core :as warc]
            (org.bovinegenius [exploding-fish :as uri]))
  (:import [net.htmlparser.jericho Source TextExtractor Config LoggerProvider HTMLElementName]))

(set! (. Config LoggerProvider) LoggerProvider/DISABLED)

(defn date-in-range?
  [date]
  (core-time/within? (core-time/interval core/clueweb12pp-time-start
                                         core/clueweb12pp-time-end)
                     date))

