(ns round-the-rabbit.playground-test
  (:use midje.sweet)
  (:require [round-the-rabbit.playground :as playground])
  (:import java.io.IOException))


(facts "retries on connection failure"
  (prerequisites
   (playground/sleep anything) => anything)

  (let [test-config
        {:on-connection (fn [connection] (println "Connected!" connection))
         :max-reconnect-attempts 100}]

    (fact "tries connecting until it connects"
      (playground/connect! test-config) => anything
      (provided
        (playground/connect-once! anything) =streams=> [false false true] :times 3))))

(facts
  (fact
    (playground/ensure-seq []) => [])

  (fact
    (playground/ensure-seq {}) => [{}]
    (playground/ensure-seq [{}]) => [{}]))