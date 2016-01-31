(ns lackd.util
  (:import [java.nio ByteBuffer]))

(def ^:const buffer-size (/ Long/SIZE Byte/SIZE))

(defn ^bytes long->bytes
  [^Long val]
  (.array (.putLong (ByteBuffer/allocate buffer-size) val)))

(defn ^Long bytes->long
  [^bytes val]
  (.getLong (ByteBuffer/wrap val)))
