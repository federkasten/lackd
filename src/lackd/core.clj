(ns lackd.core
  (:require [clojure.java.io :as io]
            [lackd.entry :as entry])
  (:import [com.sleepycat.je Environment EnvironmentConfig Database DatabaseConfig DatabaseEntry]))

(defn ^Environment open-env!
  [path]
  (when-let [d (io/file path)]
    (when-not (.isDirectory d)
      (.mkdirs d))
    (let [config (doto (new EnvironmentConfig)
                   (.setAllowCreate true))]
      (new Environment (io/file path) config))))

(defn close-env!
  [^Environment env]
  (.close env))

(defn open-db!
  [^Environment env name]
  (let [config (doto (new DatabaseConfig)
                 (.setAllowCreate true))]
    (.openDatabase env nil name config)))

(defn close-db!
  [^Database db]
  (.close db))

(defn put-entry!
  [^Database db ^String key value]
  (sync db
        (let [key-entry (entry/encode key)
              value-entry (entry/encode value)]
          (.put db nil key-entry value-entry))))

(defn get-entry!
  [^Database db ^String key]
  (sync db
        (let [key-entry (entry/encode key)
              value-entry (new DatabaseEntry)]
          (.get db nil key-entry value-entry nil)
          (entry/decode value-entry))))

(defn delete-entry!
  [^Database db ^String key]
  (sync db
        (let [key-entry (entry/encode key)]
          (.delete db nil key-entry))))
