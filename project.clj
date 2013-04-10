(defproject org.pentaside/round-the-rabbit "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.json "0.2.1"]
                 [com.novemberain/langohr "1.0.0-beta13"]
                 [com.taoensso/timbre "1.5.2"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}}
  :warn-on-reflection true
  :jvm-opts ["-Xmx512m"])
