(defproject syslog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha5"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [instaparse "1.4.2"]
                 [criterium "0.4.4"]
                 [pacer "0.1.0-SNAPSHOT"]
                 [org.clojure/test.check "0.9.0"]]
  :java-source-paths ["java/src"]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]]}})
