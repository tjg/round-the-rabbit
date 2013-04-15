(ns round-the-rabbit.wait-test
  (:use midje.sweet
        [clojure.pprint :only [pprint cl-format]])
  (:require [round-the-rabbit.wait :as wait]))


(fact
  (take 3 (wait/fixed+random 10 anything)) => [11 12 13]
  (provided
    (rand anything) =streams=> [1 2 3]))

(fact
  (wait/bounded-exponential-backoff 1 10) => [1 2 4 8])