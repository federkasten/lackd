(defproject lackd "0.1.0-SNAPSHOT"
  :description "Lightweight embedded data storage for Clojure"
  :url "https://github.com/federkasten/lackd"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :min-lein-version "2.5.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.sleepycat/je "5.0.73"]]
  :profiles {:dev {:global-vars {*warn-on-reflection* true
                                 *assert* true}}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.0"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}})
