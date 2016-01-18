(ns rackd.tuple
  (:import [com.sleepycat.bind.tuple TupleInput TupleOutput]))

(defn ^TupleOutput seq->tuple
  [sequence]
  (let [tuple (new TupleOutput)]
    (doseq [v sequence]
      (.writeString tuple (str v)))
    tuple))

(defn tuple->seq
  [^TupleInput tuple]
  (letfn [(try-to-read [^TupleInput t]
            (try
              (.readString t)
              (catch ArrayIndexOutOfBoundsException e nil)))]
    (if (zero? (.available tuple))
      []
      (loop [v (try-to-read tuple)
             res []]
        (if-not v
          res
          (recur (try-to-read tuple)
                 (conj res v)))))))
