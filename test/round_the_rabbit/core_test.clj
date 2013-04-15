(ns round-the-rabbit.core-test
  (:use midje.sweet
        [clojure.pprint :only [pprint cl-format]])
  (:require [round-the-rabbit.core :as core])
  (:import java.io.IOException))


(facts "retries when connection fails"
  (prerequisites
   (core/sleep anything) => anything)

  (let [test-config {:max-reconnect-attempts 100
                     :on-connection (fn [state] (println "Connected!"))}
        new-state (atom {:config test-config})]

    (fact "tries connecting until connected"
      (core/connect test-config) => anything
      (provided
        (core/connect-once! anything) =streams=> [nil nil new-state]
          :times 3))))

(facts
  (fact
    (core/ensure-sequential []) => [])

  (fact
    (core/ensure-sequential {}) => [{}]
    (core/ensure-sequential [{}]) => [{}]))