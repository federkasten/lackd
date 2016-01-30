(ns lackd.core
  (:require [clojure.java.io :as io]
            [lackd.entry :as entry])
  (:import [java.io Serializable]
           [java.util Comparator]
           [java.math BigInteger]
           [com.sleepycat.je Environment EnvironmentConfig Database DatabaseConfig DatabaseEntry LockMode OperationStatus]))

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
  (let [comparator (proxy [Comparator Serializable] []
                       (compare [^bytes k1 ^bytes k2]
                                (.compareTo (new BigInteger k1)
                                            (new BigInteger k2))))
        config (doto (new DatabaseConfig)
                 (.setAllowCreate true)
                 (.setDeferredWrite true)
                 (.setBtreeComparator comparator))]
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

;;; Queue

(defn- ^BigInteger next-queue-key
  [^Database queue]
  (let [last-key-entry (new DatabaseEntry)
        last-value-entry (new DatabaseEntry)]
    (with-open [cursor (.openCursor queue nil nil)]
      (.getLast cursor last-key-entry last-value-entry LockMode/RMW)
      (if (.getData last-key-entry)
        (-> last-key-entry
            .getData
            BigInteger.
            (.add BigInteger/ONE))
        (BigInteger/valueOf 0)))))

(defn push-item!
  [^Database queue value]
  (sync queue
        (let [key-entry (new DatabaseEntry (.toByteArray (next-queue-key queue)))
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
            (when-not (nil? (.getData value-entry))
              (.delete cursor)
              (entry/decode value-entry))))))
