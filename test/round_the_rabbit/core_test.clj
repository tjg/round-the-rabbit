(ns round-the-rabbit.core-test
  (:use midje.sweet
        [clojure.pprint :only [pprint cl-format]])
  (:require [round-the-rabbit.core :as core]
            [langohr.core      :as rmq]
            [langohr.basic     :as rmq-basic]
            [langohr.channel   :as rmq-channel]
            [langohr.exchange  :as rmq-exchange]
            [langohr.queue     :as rmq-queue])
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


  (fact "obeys :max-reconnect-attempts"
    (core/connect-with-state!
      (atom {:config {:max-connect-attempts 2}})) => anything
    (provided
      (core/connect-once! anything) =streams=> [(atom {}) (atom {}) (atom {})]
        :times 2)))

(future-facts "sets up RabbitMQ connection"
  (prerequisites
   (rmq/connect anything) => anything
   (rmq-channel/open anything) => anything
   (rmq-exchange/declare anything anything anything) => anything
   (rmq-exchange/declare anything anything anything anything anything) => anything
   (rmq-queue/declare anything anything) => anything
   (rmq-queue/declare anything anything anything anything) => anything
   (rmq-queue/bind anything anything anything) => anything
   (rmq-queue/bind anything anything anything anything anything) => anything
   (rmq/shutdown-listener anything) => anything
   (core/subscribe-to-queue anything anything) => anything
   (core/add-shutdown-listener anything anything) => anything
   (core/remove-shutdown-listener anything anything) => anything)

  (core/connect {}) => anything

  (fact "sets up connection")
  (fact "sets up channels")
  (fact "sets up exchanges")
  (fact "sets up queues")
  (fact "sets up bindings")
  (fact "sets up queue consumers"))
