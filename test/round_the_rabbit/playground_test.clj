(ns round-the-rabbit.playground-test
  (:use midje.sweet
        [clojure.pprint :only [pprint cl-format]])
  (:require [round-the-rabbit.playground :as playground])
  (:import java.io.IOException))


(facts "retries when connection fails"
  (prerequisites
   (playground/sleep anything) => anything)

  (let [test-config {:max-reconnect-attempts 100
                     :on-connection (fn [state] (println "Connected!"))}
        new-state (atom {:config test-config})]

    (fact "tries connecting until connected"
      (playground/connect! test-config) => anything
      (provided
        (playground/connect-once! anything) =streams=> [nil nil new-state]
          :times 3))))

(facts
  (fact
    (playground/ensure-seq []) => [])

  (fact
    (playground/ensure-seq {}) => [{}]
    (playground/ensure-seq [{}]) => [{}]))