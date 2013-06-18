(ns clueweb12pp_core.page_times
  (:gen-class)
  (:require [clj-time.core :as core-time]
            [clj-time.format :as format-time]
            [clj-time.coerce :as coerce-time])
  (:import [edu.stanford.nlp.pipeline StanfordCoreNLP Annotation]
           [edu.stanford.nlp.ling CoreAnnotations 
                                  CoreAnnotations$TokensAnnotation 
                                  CoreAnnotations$NamedEntityTagAnnotation
                                  CoreAnnotations$DocDateAnnotation]
           [edu.stanford.nlp.time TimeAnnotations$TimexAnnotation])

;;;; Takes a record and obtains the date information

