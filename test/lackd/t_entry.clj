(ns lackd.t-entry
  (:require [midje.sweet :refer :all]
            [lackd.entry :as entry]
            [taoensso.nippy :as nippy])
  (:import [com.sleepycat.je DatabaseEntry]))

;;; :stress-record :date :lazy-seq-empty :true :long :double :lazy-seq :short
;;; :meta :str-long :bigint :sym-ns :queue :float :sorted-set :nested
;;; :queue-empty :lotsa-small-keywords :map-empty :set-empty :false :list-empty
;;; :vector :kw :sym :str-short :ex-info :integer :list :ratio :byte :bigdec
;;; :nil :sorted-map :bytes :regex :exception :uuid :set :list-quoted
;;; :throwable :vector-empty :lotsa-small-strings :kw-ns :map
;;; :lotsa-small-numbers :char
(def stress-data
  (dissoc nippy/stress-data
          ;; Unsupported by data.fressian
          :queue :queue-empty :byte
          :ex-info :exception :throwable
          ;; Supported by data.fressian, but could not to compare directly
          :bytes :regex))

(def stress-data-bytes (:bytes nippy/stress-data))
(def stress-data-regex (:regex nippy/stress-data))

(fact "lackd.entry"
  (let [v1 nil
        v2 [1 [2 3 [4] [[[[5 6]]] 7] 8] nil :a "bb" 'c :d ::e :f/g true false]
        v3 {:a {:b {:c "d" :e/f 'g} :h 123}
            ::key ::val
            "str-key" :val
            [[["struct-key"]]] :val}
        v4 #{1 #{2 3 #{4 5 6} 7} 8 nil :a "bbb"}
        v5 '(1 ((((2 3 (4) 5 6) 7 8) 9)) :a :b/c d e + fn)
        v6 `(foo bar baz)
        v7 (atom "abc")]
    (entry/decode (entry/encode v1)) => v1
    (entry/decode (entry/encode v2)) => v2
    (entry/decode (entry/encode v3)) => v3
    (entry/decode (entry/encode v4)) => v4
    (entry/decode (entry/encode v5)) => v5
    (entry/decode (entry/encode v6)) => v6
    ;; @(entry/decode (entry/encode v7)) => @v7
    (entry/decode (entry/encode stress-data)) => stress-data
    (seq (entry/decode (entry/encode stress-data-bytes))) => (seq stress-data-bytes)
    (str (entry/decode (entry/encode stress-data-regex))) => (str stress-data-regex)
    (type (entry/encode nil)) => DatabaseEntry))
