(ns round-the-rabbit.core-test
  (:use midje.sweet
        [clojure.pprint :only [pprint cl-format]])
  (:require [round-the-rabbit.core :as core])
  (:import java.io.IOException))


(facts "retries when connection fails"
  (prerequisites
   (core/sleep anything) => anything)

  (let [config {:max-reconnect-attempts 100
                     :on-connection (fn [state] (println "Connected!"))}
        state (atom {:config config
                         :connection true})]

    (fact "tries connecting until connected"
      (core/connect config) => anything
      (provided
        (core/connect-once! anything) =streams=> [(atom {}) (atom {}) state]
          :times 3))

    (let [state (atom {:config (assoc config
                                 :ms-between-restarts (iterate inc 100))
                       :connection true})]
      (fact "sleeps the desired number of times between reconnects"
        (core/connect-with-state! state) => anything
        (provided
          (core/connect-once! anything) =streams=> [(atom {}) (atom {}) state]
          (core/sleep 100) => anything
          (core/sleep 101) => anything)))))

(facts
  (fact
    (core/ensure-sequential []) => [])

  (fact
    (core/ensure-sequential {}) => [{}]
    (core/ensure-sequential [{}]) => [{}]))