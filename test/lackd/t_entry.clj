(ns lackd.t-entry
  (:require [midje.sweet :refer :all]
            [lackd.entry :as entry])
  (:import [com.sleepycat.je DatabaseEntry]))

(defrecord TestRecord [data])

(def stress-data
  {:nil nil
   :true true
   :false false
   :char \あ
   :str-short "日本語"
   :str-long (apply str (range 1000))
   :kw :kw
   :kw-ns ::kw-ns
   :sym 'foo
   :sym-ns 'foo/bar
   :list '(1 2 3 (4 5 (((6 7) 8 () 9) 10)) 11 ((12)))
   :vector [1 2 3 [4 5 [[[6 7] 8 [] 9] 10]] 11 [[12]]]
   :map {:a 1 :b {:c {:d 2 :e 3} :f 4 :g {}} :h 6}
   :sorted-map (sorted-map :b 2 :a 1 :d 4 :c 3)
   :set #{1 2 3 #{4 5 #{} 6 #{7 8}} 9}
   :sorted-set (sorted-set 1 2 3 4 5)
   :meta (with-meta {:a :b} {:c :d})
   :nested [(list '() [] {} #{})
            ['() [] {} #{}]
            {:l '() :v [] :m {} :s #{}}
            #{[] {} #{}}]
   :lazy-seq (repeatedly 1000 rand)
   :lazy-seq-empty (map identity nil)
   :short (short 13)
   :int (int 13)
   :long (long 13)
   :bigint (bigint 31415926535897932384626433832795)
   :float (float 3.14)
   :double (double 3.14)
   :bigdec (bigdec 3.1415926535897932384626433832795)
   :ratio 5/3
   :uuid (java.util.UUID/randomUUID)
   :date (java.util.Date.)
   :record (TestRecord. "test")
   ;; Unsupported objects
   ;; :byte (byte 13)
   ;; :throwable (Throwable. "throwable")
   ;; :exception (try (/ 1 0) (catch Exception e e))
   ;; :ex-info (ex-info "ex-info" {:a 1 :b 2})
   ;; :atom (atom 1)
   })

(def sd-bytes (byte-array (map byte [1 16 -5])))
(def sd-shorts (short-array (map short [1 16 -5])))
(def sd-ints (int-array (map int [1 16 -5])))
(def sd-longs (long-array (map long [1 16 -5])))
(def sd-chars (char-array (map char [64 65 66])))
(def sd-floats (float-array (map float [1.4 16.6 -5.3])))
(def sd-doubles (double-array (map double [1.4 16.6 -5.3])))
(def sd-booleans (boolean-array [true false true]))
(def sd-regex #"<([A-Za-z][0-9A-Za-z]*)\b[^>]*>(.*?)</\1>")

(fact "lackd.entry"
  (doseq [[k v] stress-data]
    [k (entry/decode (entry/encode v))] => [k v])
  (seq (entry/decode (entry/encode sd-bytes))) => (seq sd-bytes)
  (seq (entry/decode (entry/encode sd-ints))) => (seq sd-ints)
  (seq (entry/decode (entry/encode sd-longs))) => (seq sd-longs)
  (seq (entry/decode (entry/encode sd-floats))) => (seq sd-floats)
  (seq (entry/decode (entry/encode sd-doubles))) => (seq sd-doubles)
  (seq (entry/decode (entry/encode sd-booleans))) => (seq sd-booleans)
  (str (entry/decode (entry/encode sd-regex))) => (str sd-regex)
  ;; Unsupported arrays
  ;; (seq (entry/decode (entry/encode sd-shorts))) => (seq sd-shorts)
  ;; (seq (entry/decode (entry/encode sd-chars))) => (seq sd-chars)
  (type (entry/encode nil)) => DatabaseEntry)
