;;;; Process the vbulletin.org support forums.
;;;; We just grab the contact links and process them by downloading
;;;; each
;;;; First do one hop from there to possibly get to the forum page

(ns clueweb12pp_core.vbulletin.vbulletin_org_support
  (:gen-class :main true)
  (:require [clojure.tools.cli :as cli]
            [net.cgrand.enlive-html :as html]
            [warc-clojure.core :as warc]))

(defn handle-warc-file
  [warc-file]
  (let [non-empty?          (fn [x] (not (empty? x)))
        process-member-page (fn [page-stream]
                              (html/select
                               (html/html-resource page-stream)
                               [:div.panel :div.fieldset :a]))
        get-contact-page    (fn [page-stream]
                              (map
                               (fn [contact-detail]
                                 (-> contact-detail
                                    :attrs
                                    :href))
                               (process-member-page page-stream)))
        member-page?        (fn [record]
                              (re-find
                               #"member.php"
                               (:target-uri-str record)))]
    (doseq [contact-page  (filter
                           non-empty?
                           (for
                               [member-page (filter
                                             member-page?
                                             (warc/get-http-records-seq (warc/get-warc-reader warc-file)))]
                             (get-contact-page (:payload-stream member-page))))]
      (println (first contact-page)))))

(defn -main
  [& args]
  (let [[optional [job-directory] banner] (cli/cli args)]
    (doseq [warc-file (filter
                       (fn [dir-file] (re-find #".*warc.gz.*" (.getAbsolutePath dir-file)))
                       (file-seq (clojure.java.io/file job-directory)))]
      (handle-warc-file (.getAbsolutePath warc-file)))))
