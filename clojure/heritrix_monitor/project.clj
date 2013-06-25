(defproject org.lemurproject/heritrix_monitor "0.1.0-SNAPSHOT"
  :description "Code to monitor a heritrix crawl"
  :url "http://boston.lti.cs.cmu.edu"
  :license {:name "The BSD License"
            :url "http://opensource.org/licenses/BSD-2-Clause"}
  :dependencies [[clj-time "0.5.1"]
                 [org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.cli "0.2.2"]
                 [incanter "1.2.3-SNAPSHOT"]]
  :aot :all)
