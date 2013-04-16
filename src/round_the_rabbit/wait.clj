(ns round-the-rabbit.wait
  (:use [clojure.pprint :only [pprint cl-format]]))


(defn fixed+random [init scale-of-randomness]
  (map (fn [fixed] (+ fixed (rand scale-of-randomness)))
       (repeat init)))

(defn truncated-exponential-backoff [init maximum]
  (->> init
       (iterate (partial * 2)) ;; keep doubling
       (take-while #(< % maximum))
       (map rand)))