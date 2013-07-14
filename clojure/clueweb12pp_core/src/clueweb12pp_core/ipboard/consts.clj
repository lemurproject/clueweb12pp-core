;;;; Some global constants (magic strings etc.)

(ns clueweb12pp-core.ipboard.consts
  (:gen-class :main true)
  (:require [clueweb12pp-core.core :as core]))


;;; IP-Board topic url styles
(def ipboard-unfriendly-topics-regex #".*\?showtopic.*")
(def ipboard-windows-friendly-topics-regex #".*index.php\?/topic/.*")
(def ipboard-apache-friendly-topics-regex #".*index.php/topic/.*")
(def ipboard-mod-rewrite-friendly-topics-regex #".*/topic/.*")


