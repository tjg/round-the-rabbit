(ns round-the-rabbit.playground
  (:require [langohr.core      :as rmq]
            [langohr.basic     :as rmq-basic]
            [langohr.channel   :as rmq-channel]
            [langohr.consumers :as rmq-consumers]
            [langohr.queue     :as rmq-queue])
  (:use [clojure.pprint :only [pprint cl-format]])
  (:import [com.rabbitmq.client ConnectionFactory]))



(defn ensure-seq [obj]
  (if (coll? obj)
    obj
    [obj]))

(defn sleep [ms]
  (Thread/sleep ms))

(def default-config
  {:addresses [{:host "localhost" :port ConnectionFactory/DEFAULT_AMQP_PORT}]
   :login {:username "guest" :password "guest"}
   :vhost "/"
   :declare-queues []
   :bindings []
   ;; Number or seq. If finite seq, eventually use last element
   :ms-between-restarts 1
   :max-reconnect-attempts nil
   :on-connection (constantly nil)
   :on-connection-close (constantly nil)
   :on-channel-close (constantly nil)
   :channel-restart-strategy :restart-connection})

(defn ensure-repeating [aseq]
  (lazy-seq
   (cond (empty? aseq)
         (repeat nil)

         (empty? (rest aseq))
         (cons (first aseq) (ensure-repeating (repeat (first aseq))))

         :else
         (cons (first aseq) (ensure-repeating (rest aseq))))))

(def ensure-repeating-seq (comp ensure-repeating ensure-seq))

(defn connect-once! [config]
  true)

(defn connect! [config]
  (let [config (merge default-config config)]
    (loop [attempt-count 0]
      (when-not (= attempt-count (:max-reconnect-attempts config))
        (let [connection (connect-once! config)]
          (if connection
            (do
              ((:on-connection config) connection)
              nil)
            (do
              (sleep (nth (ensure-repeating-seq (:ms-between-restarts config))
                          attempt-count))
              (recur (inc attempt-count)))))))))

(defn fixed+random [init scale-of-randomness]
  (map (fn [fixed]
         (+ fixed (rand scale-of-randomness)))
       (repeat init)))

(defn bounded-exponential-backoff [init maximum]
  (take-while #(< % maximum) (iterate #(* 2 %) init)))

