(ns lackd.core
  (:require [clojure.java.io :as io]
            [lackd.tuple :refer [seq->tuple tuple->seq]])
  (:import [com.sleepycat.je Environment EnvironmentConfig Database DatabaseConfig DatabaseEntry]
           [com.sleepycat.bind.tuple StringBinding TupleBinding]))

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

(defmulti put-entry! (fn [db key value] (if (sequential? value)
                                          :sequence
                                          :string)))
(defmethod put-entry! :string
  [^Database db key value]
  (sync db
        (let [key-entry (new DatabaseEntry)
              value-entry (new DatabaseEntry)]
          (StringBinding/stringToEntry key key-entry)
          (StringBinding/stringToEntry value value-entry)
          (.put db nil key-entry value-entry))))

(defmethod put-entry! :sequence
  [^Database db key sequence]
  (sync db
        (let [key-entry (new DatabaseEntry)
              value-entry (new DatabaseEntry)]
          (StringBinding/stringToEntry key key-entry)
          (StringBinding/outputToEntry (seq->tuple sequence) value-entry)
          (.put db nil key-entry value-entry))))

(defn get-entry!
  [^Database db key]
  (sync db
        (let [key-entry (new DatabaseEntry)
              value-entry (new DatabaseEntry)]
          (StringBinding/stringToEntry key key-entry)
          (.get db nil key-entry value-entry nil)
          (StringBinding/entryToString value-entry))))

(defn get-sequence!
  [^Database db key]
  (sync db
        (let [key-entry (new DatabaseEntry)
              value-entry (new DatabaseEntry)]
          (StringBinding/stringToEntry key key-entry)
          (.get db nil key-entry value-entry nil)
          (tuple->seq (TupleBinding/entryToInput value-entry)))))
