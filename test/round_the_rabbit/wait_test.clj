(ns round-the-rabbit.wait-test
  (:use midje.sweet
        [clojure.pprint :only [pprint cl-format]])
  (:require [round-the-rabbit.wait :as wait]))


(fact
  (take 3 (wait/fixed+random 10 anything)) => [11 12 13]
  (provided
    (rand anything) =streams=> [1 2 3]))


(facts "truncated exponential backoff works"
  (fact "if we didn't randomize the results, it'd just all be doubled"
    (with-redefs [rand identity]
      (wait/truncated-exponential-backoff 1 10) => [1 2 4 8]))

  (fact "randomizes each result"
    (with-redefs [rand (partial * 2)]
      (wait/truncated-exponential-backoff 1 10) => [2 4 8 16])))