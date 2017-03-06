(ns lackd.t-util
  (:require [midje.sweet :refer :all]
            [lackd.util :as util]))

(fact "lackd.util"
  (seq (util/long->bytes (long 1234567890))) => [0 0 0 0 73 -106 2 -46]
  (seq (util/long->bytes (long -1))) => [-1 -1 -1 -1 -1 -1 -1 -1]
  (type (util/long->bytes (long 1))) => (type (byte-array 0))
  (util/bytes->long (byte-array [1 2 3 4 5 6 7 8])) => 72623859790382856
  (util/bytes->long (byte-array [-1 -2 -3 -4 -5 -6 -7 -8])) => -283686952306184
  (type (util/bytes->long (byte-array [0 0 0 0 0 0 0 0]))) => (type (long 0)))
