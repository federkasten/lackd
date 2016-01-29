(ns lackd.entry
  (:require [clojure.data.fressian :as fressian])
  (:import [com.sleepycat.je DatabaseEntry]
           [com.sleepycat.bind ByteArrayBinding]))

(defonce ^ByteArrayBinding byte-array-bind (new ByteArrayBinding))

(defn ^DatabaseEntry encode
  [o]
  (let [entry (new DatabaseEntry)]
    (.objectToEntry byte-array-bind
                    (-> o
                        fressian/write
                        .array)
                    entry)
    entry))

(defn decode
  [^DatabaseEntry entry]
  (let []
    (fressian/read (.entryToObject byte-array-bind entry))))
