(defproject org.lemurproject.clueweb12pp/clueweb12pp-core "0.2.0"
  :description "Code for clueweb12++ crawl"
  :url "http://www.cs.cmu.edu/~callan/Projects/IIS-1160862/"
  :license {:name "The BSD License"
            :url "http://opensource.org/licenses/BSD-2-Clause"}
  :dependencies [[clj-time "0.5.1"]
                 [enlive "1.1.1"]
                 [org.bovinegenius/exploding-fish "0.3.3"]
                 [org.lemurproject.clueweb12pp/warc-clojure "0.3.1"]
                 [org.clojure/clojure "1.4.0"]
                 [org.clojure/data.json "0.2.2"]
                 [org.clojure/tools.cli "0.2.2"]
                 [net.htmlparser.jericho/jericho-html "3.3"]]
  :aot  :all
  :jvm-opts ["-Dfile.encoding=utf-8" "-Xmx2000M" "-Xss5000K" "-XX:PermSize=256M" "-XX:MaxPermSize=512M"])
