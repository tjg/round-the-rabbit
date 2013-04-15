(ns round-the-rabbit.wait
  (:use [clojure.pprint :only [pprint cl-format]]))


(defn fixed+random [init scale-of-randomness]
  (map (fn [fixed] (+ fixed (rand scale-of-randomness)))
       (repeat init)))

(defn bounded-exponential-backoff [init maximum]
  (take-while #(< % maximum) (iterate #(* 2 %) init)))