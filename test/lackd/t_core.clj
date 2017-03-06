(ns lackd.t-core
  (:require [midje.sweet :refer :all]
            [lackd.core :as core]
            ;[lackd.t-fixture :as t-fixture]
            ;[lackd.t-common :rat t-common]
            [clojure.java.io :as io]))

(with-state-changes [(before :facts (do
                                      ;; TODO
                                      nil))
                     (after :facts (do
                                     ;; TODO
                                     nil))]
  (fact "lackd.core"
    (let []
      ;; TODO
      123 => 123
      )))
