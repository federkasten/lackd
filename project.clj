(defproject lackd "0.2.1-SNAPSHOT"
  :description "Lightweight embedded data storage for Clojure"
  :url "https://github.com/federkasten/lackd"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :min-lein-version "2.5.0"
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :dependencies [[org.clojure/tools.logging "0.3.1"]
                 [org.clojure/data.fressian "0.2.1"]
                 [com.sleepycat/je "5.0.73"]]
  :profiles {:dev {:global-vars {*warn-on-reflection* true
                                 *assert* true}
                   :dependencies [[org.clojure/clojure "1.8.0"]]}
             :test {:plugins [[lein-cloverage "1.0.9"]
                              [lein-midje "3.2.1"]]
                    :dependencies [[midje "1.8.3"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.0"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}}
  :aliases {"test" ["with-profile" "+test" "midje"]}
  :signing {:gpg-key "me@tak.sh"})
