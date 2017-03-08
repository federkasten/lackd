(ns lackd.t-core
  (:require [midje.sweet :refer :all]
            [lackd.core :as lackd]
            [lackd.entry :as entry]
            [clojure.java.io :as io])
  (:import [com.sleepycat.je Environment Database]))

(def database-path "/tmp/lackd-t-core")
(def db-name "lackd-t-core-db")
(def queue-name "lackd-t-core-queue")

(defn delete-dir! [dir-or-file]
  (let [dir-or-file (if (instance? java.io.File dir-or-file)
                      dir-or-file
                      (io/file dir-or-file))]
    (when (.isDirectory dir-or-file)
      (doseq [child (.listFiles dir-or-file)]
        (delete-dir! child)))
    (.delete dir-or-file)))

(defn- cleanup! []
  (delete-dir! database-path))

(with-state-changes [(before :facts (cleanup!))
                     (after :facts (cleanup!))]
  (fact "lackd.core"
    (let [env (atom nil)
          db (atom nil)
          queue (atom nil)
          test-data {"test1" {:test1 'abc}
                     "test2" {::test2 [[[[[:def/ghi]]]]]}
                     "test2-2" :test2-2
                     "test3" [1 2 3 4 5]}
          open! (fn []
                  (reset! env (lackd/open-env! database-path))
                  (reset! db (lackd/open-db! @env db-name))
                  (reset! queue (lackd/open-queue! @env queue-name)))
          close! (fn []
                   (swap! queue #(when %
                                   (lackd/close-queue! %)
                                   nil))
                   (swap! db #(when %
                                (lackd/close-db! %)
                                nil))
                   (swap! env #(when %
                                 (lackd/close-env! %)
                                 nil)))]
      (open!)
      ;; Type
      (type @env) => Environment
      (type @db) => Database
      (type @queue) => Database
      ;; For db
      (lackd/size @db) => 0
      (lackd/put-entry! @db "test1" (test-data "test1"))
      (lackd/size @db) => 1
      (lackd/put-entry! @db "test2" (test-data "test2"))
      (lackd/size @db) => 2
      (lackd/put-entry! @db "test2" (test-data "test2-2"))
      (lackd/size @db) => 2
      (lackd/put-entry! @db "test3" (test-data "test3"))
      (lackd/size @db) => 3
      (lackd/get-entry! @db "test1") => (test-data "test1")
      (lackd/get-entry! @db "test2") => (test-data "test2-2")
      (lackd/get-entry! @db "test3") => (test-data "test3")
      (lackd/get-entry! @db "not-exists") => nil
      (lackd/update-entry! @db "test3" #(map inc %))
      (lackd/get-entry! @db "test3") => (map inc (test-data "test3"))
      (lackd/update-entry! @db "not-exists" #(str "[" % "]"))
      (lackd/get-entry! @db "not-exists") => nil ; TODO: Should be "[]" ?
      (lackd/delete-entry! @db "not-exists")
      (lackd/delete-entry! @db "test1")
      (lackd/get-entry! @db "test1") => nil
      (lackd/size @db) => 2
      (lackd/delete-entry! @db "test1")
      (lackd/size @db) => 2
      (lackd/delete-entry! @db "test2")
      (lackd/size @db) => 1
      (lackd/delete-entry! @db "test3")
      (lackd/size @db) => 0
      ;; For queue
      (lackd/size @queue) => 0
      (lackd/pop-item! @queue) => nil
      (lackd/insert-item! @queue :a)
      (lackd/insert-item! @queue :b)
      (lackd/insert-item! @queue :c)
      (lackd/size @queue) => 3
      (lackd/pop-item! @queue) => :c
      (lackd/pop-item! @queue) => :b
      (lackd/pop-item! @queue) => :a
      (lackd/pop-item! @queue) => nil
      (lackd/size @queue) => 0
      (lackd/push-item! @queue :d)
      (lackd/push-item! @queue :e)
      (lackd/push-item! @queue :f)
      (lackd/size @queue) => 3
      (lackd/pop-item! @queue) => :d
      (lackd/pop-item! @queue) => :e
      (lackd/pop-item! @queue) => :f
      (lackd/pop-item! @queue) => nil
      (lackd/size @queue) => 0
      (lackd/insert-item! @queue :g)
      (lackd/push-item! @queue :h)
      (lackd/insert-item! @queue :i)
      (lackd/push-item! @queue :j)
      (lackd/insert-item! @queue :k)
      (lackd/size @queue) => 5
      (lackd/pop-item! @queue) => :k
      (lackd/pop-item! @queue) => :i
      (lackd/pop-item! @queue) => :g
      (lackd/pop-item! @queue) => :h
      (lackd/pop-item! @queue) => :j
      (lackd/pop-item! @queue) => nil
      (lackd/size @queue) => 0
      ;; TODO: Add test for limit of long (but very slow)
      ;; Permanence
      (lackd/put-entry! @db "test2" (test-data "test2"))
      (lackd/put-entry! @db "test3" (test-data "test3"))
      (lackd/put-entry! @db "test1" (test-data "test1"))
      (lackd/size @db) => 3
      (lackd/push-item! @queue :a)
      (lackd/insert-item! @queue :b)
      (lackd/push-item! @queue :c)
      (lackd/insert-item! @queue :d)
      (lackd/push-item! @queue :e)
      (lackd/size @queue) => 5
      (close!)
      (open!)
      (lackd/get-entry! @db "test1") => (test-data "test1")
      (lackd/get-entry! @db "test2") => (test-data "test2")
      (lackd/get-entry! @db "test3") => (test-data "test3")
      (lackd/size @db) => 3
      (lackd/size @queue) => 5
      (lackd/pop-item! @queue) => :d
      (lackd/pop-item! @queue) => :b
      (lackd/pop-item! @queue) => :a
      (lackd/pop-item! @queue) => :c
      (lackd/pop-item! @queue) => :e
      (lackd/pop-item! @queue) => nil
      (lackd/size @queue) => 0
      ;; Stress test
      ;; TODO
      ;; Shutdown
      (close!))))
