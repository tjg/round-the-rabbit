(ns round-the-rabbit.playground
  (:require [langohr.core      :as rmq]
            [langohr.basic     :as rmq-basic]
            [langohr.channel   :as rmq-channel]
            [langohr.consumers :as rmq-consumers]
            [langohr.queue     :as rmq-queue])
  (:use [clojure.pprint :only [pprint cl-format]])
  (:import [com.rabbitmq.client ConnectionFactory]
           java.io.IOException))



(defn ensure-seq [obj]
  (if (and (coll? obj)
           (not (map? obj)))
    obj
    [obj]))

(defn ensure-repeating [aseq]
  (lazy-seq
   (cond (empty? aseq)
         (repeat nil)

         (empty? (rest aseq))
         (cons (first aseq) (ensure-repeating (repeat (first aseq))))

         :else
         (cons (first aseq) (ensure-repeating (rest aseq))))))

(def ensure-repeating-seq (comp ensure-repeating ensure-seq))

(defn sleep [ms]
  (Thread/sleep ms))

(def default-config
  {:addresses [{:host "localhost" :port ConnectionFactory/DEFAULT_AMQP_PORT}]
   :login {:username "guest" :password "guest"}
   :vhost "/"
   :declare-exchanges []
   :declare-queues []
   :bindings []
   ;; Number or seq. If finite seq, eventually use last element
   :ms-between-restarts 1
   :max-reconnect-attempts nil
   :on-connection (constantly nil)
   :on-new-connection-fail (constantly nil)
   :on-connection-close (constantly nil)
   :on-channel-close (constantly nil)
   :channel-restart-strategy :restart-connection})

(defn declare-queue [channel queue-config]
  (let [queue (:name queue-config)
        queue-args (dissoc queue-config :name)]
    (apply rmq-queue/declare channel queue queue-args)))

(defn bind [channel binding-config]
  (let [{:keys [queue exchange]} binding-config
        binding-args (dissoc :queue :exchange)]
    (apply rmq-queue/bind channel queue exchange binding-args)))

(defn connect-once! [config]
  (try
    (let [conn (rmq/connect (merge (:login config) (first (:addresses config))))
          setup-channel (rmq-channel/open conn)
          queues   (map declare-queue (ensure-seq (:declare-queues config)))
          bindings (map bind          (ensure-seq (:bindings config)))]
      conn)
    (catch IOException e
      ((:on-new-connection-fail config) e))))

(defn connect! [config]
  (let [config (merge default-config config)]
    (loop [attempt-count 0]
      (when-not (= attempt-count (:max-reconnect-attempts config))
        (let [connection (connect-once! config)]
          (if connection
            (do
              ((:on-connection config) connection)
              connection)
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

