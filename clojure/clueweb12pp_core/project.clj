(defproject org.lemurproject.clueweb12pp/clueweb12pp-core "0.2.0"
  :description "Code for clueweb12++ crawl"
  :url "http://www.cs.cmu.edu/~callan/Projects/IIS-1160862/"
  :license {:name "The BSD License"
            :url "http://opensource.org/licenses/BSD-2-Clause"}
  :dependencies [[org.clojure/data.xml "0.0.7"]
                 [commons-codec/commons-codec "1.3"]
                 [clj-http "0.7.5"]
                 [clj-time "0.5.1"]
                 [enlive "1.1.1"]
                 [me.raynes/fs "1.4.4"]
                 [org.bovinegenius/exploding-fish "0.3.3"]
                 [org.lemurproject.clueweb12pp/warc-clojure "0.3.1"]
                 [org.clojure/clojure "1.4.0"]
                 [org.clojure/data.json "0.2.2"]
                 [org.clojure/tools.cli "0.2.2"]
                 [org.clojure/tools.reader "0.7.4"]
                 [org.clojure/java.jdbc "0.0.6"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [net.htmlparser.jericho/jericho-html "3.3"]
                 [com.rubiconproject.oss/jchronic "0.2.6"]
                 [com.joestelmach/natty "0.6"]]
  :dev-dependencies [[org.clojure/java.jdbc "0.0.6"]]
  :aot  :all
  :jvm-opts ["-Dfile.encoding=utf-8" "-Xmx20G" "-Xss25G"]
  :java-source-paths ["src/main/java"])
