(ns lackd.core
  (:require [clojure.java.io :as io]
            [lackd.entry :as entry]
            [lackd.util :refer [long->bytes bytes->long]])
  (:import [com.sleepycat.je Environment EnvironmentConfig Database DatabaseConfig DatabaseEntry LockMode OperationStatus]
           [lackd SimpleByteComparator]))

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
                 (.setAllowCreate true)
                 (.setDeferredWrite true))]
    (.openDatabase env nil name config)))

(defn open-queue!
  [^Environment env name]
  (let [config (doto (new DatabaseConfig)
                 (.setAllowCreate true)
                 (.setDeferredWrite true)
                 (.setBtreeComparator SimpleByteComparator))]
    (.openDatabase env nil name config)))

(defn close-db!
  [^Database db]
  (.close db))

(defn close-queue!
  [^Database db]
  (.close db))

(defn size
  [^Database db]
  (.count db))

;;; Key-Value

(defn put-entry!
  [^Database db ^String key value]
  (sync db
        (let [key-entry (entry/encode key)
              value-entry (entry/encode value)]
          (= OperationStatus/SUCCESS
             (.put db nil key-entry value-entry)))))

(defn get-entry!
  [^Database db ^String key]
  (sync db
        (let [key-entry (entry/encode key)
              value-entry (new DatabaseEntry)]
          (.get db nil key-entry value-entry nil)
          (when-not (nil? (.getData value-entry))
            (entry/decode value-entry)))))

(defn delete-entry!
  [^Database db ^String key]
  (sync db
        (let [key-entry (entry/encode key)]
          (= OperationStatus/SUCCESS
             (.delete db nil key-entry)))))

(defn update-entry!
  [^Database db ^String key f]
  (sync db
        (let [key-entry (entry/encode key)
              value-entry (new DatabaseEntry)]
          (.get db nil key-entry value-entry nil)
          (let [value (when-not (nil? (.getData value-entry))
                        (entry/decode value-entry))
                new-value-entry (entry/encode (f value))]
            (= OperationStatus/SUCCESS
               (.put db nil key-entry new-value-entry))))))

;;; Queue

(defn- ^Long next-queue-key
  [^Database queue]
  (let [last-key-entry (new DatabaseEntry)
        last-value-entry (new DatabaseEntry)]
    (with-open [cursor (.openCursor queue nil nil)]
      (.getLast cursor last-key-entry last-value-entry LockMode/RMW)
      (if (.getData last-key-entry)
        (-> last-key-entry
            .getData
            bytes->long
            inc)
        (Long/valueOf 0)))))

(defn- ^Long previous-queue-key
  [^Database queue]
  (let [first-key-entry (new DatabaseEntry)
        first-value-entry (new DatabaseEntry)]
    (with-open [cursor (.openCursor queue nil nil)]
      (.getFirst cursor first-key-entry first-value-entry LockMode/RMW)
      (if (.getData first-key-entry)
        (-> first-key-entry
            .getData
            bytes->long
            dec)
        (Long/valueOf 0)))))

(defn insert-item!
  [^Database queue value]
  (sync queue
        (let [key-entry (new DatabaseEntry (long->bytes (previous-queue-key queue)))
              value-entry (entry/encode value)]
          (= OperationStatus/SUCCESS
             (.put queue nil key-entry value-entry)))))

(defn push-item!
  [^Database queue value]
  (sync queue
        (let [key-entry (new DatabaseEntry (long->bytes (next-queue-key queue)))
              value-entry (entry/encode value)]
          (= OperationStatus/SUCCESS
             (.put queue nil key-entry value-entry)))))

(defn pop-item!
  [^Database queue]
  (sync queue
        (let [key-entry (new DatabaseEntry)
              value-entry (new DatabaseEntry)]
          (with-open [cursor (.openCursor queue nil nil)]
            (.getFirst cursor key-entry value-entry LockMode/RMW)
            (when (.getData value-entry)
              (.delete cursor)
              (entry/decode value-entry))))))
