(ns round-the-rabbit.core-test
  (:use midje.sweet
        [clojure.pprint :only [pprint cl-format]])
  (:require [round-the-rabbit.core :as core])
  (:import java.io.IOException))


(facts "retries when connection fails"
  (prerequisites
   (core/sleep anything) => anything)

  (fact "tries connecting until connected"
    (core/connect {:max-reconnect-attempts 100}) => anything
    (provided
      (core/connect-once! anything) =streams=> [(atom {}) (atom {})
                                                (atom {:connection true})] :times 3))


  (fact "sleeps the desired number of times between reconnects"
    (core/connect-with-state!
      (atom {:config {:ms-between-restarts (iterate inc 100)}})) => anything
    (provided
      (core/connect-once! anything) =streams=> [(atom {}) (atom {})
                                                (atom {:connection true})]
      (core/sleep 100) => anything
      (core/sleep 101) => anything
      (core/sleep 102) => anything :times 0))


  (fact "sleeps the desired number of times between reconnects"
    (core/connect-with-state!
      (atom {:config {:max-connect-attempts 2}})) => anything
    (provided
      (core/connect-once! anything) =streams=> [(atom {}) (atom {}) (atom {})]
      :times 2)))
