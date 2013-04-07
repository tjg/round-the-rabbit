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
   :consumers []
   ;; Number or seq. If finite seq, eventually use last element
   :ms-between-restarts 1
   :max-reconnect-attempts nil
   :on-connection (constantly nil)
   :on-new-connection-fail (constantly nil)
   :on-connection-shutdown (constantly nil)
   :on-channel-shutdown (constantly nil)
   :channel-restart-strategy :restart-connection})

(defn declare-queue [channel queue-config]
  (let [queue (:name queue-config)
        queue-args (dissoc queue-config :name)]
    (apply rmq-queue/declare channel queue queue-args)))

(defn bind [channel binding-config]
  (let [{:keys [queue exchange]} binding-config
        binding-args (dissoc :queue :exchange)]
    (apply rmq-queue/bind channel queue exchange binding-args)))

(declare connect-with-state!)

(defn make-on-connection-shutdown [state]
  (rmq/shutdown-listener
   (fn [cause]
     (connect-with-state! state))))

(defn make-on-channel-shutdown [conn]
  (rmq/shutdown-listener
   (fn [cause]
     (when-not (.isHardError cause)
       (try (rmq/close conn)
            (catch IOException e))))))

(defn connect-once! [state]
  (try
    (let [config (:config @state)
          conn (rmq/connect (merge (:login config) (first (:addresses config))))
          channel (rmq-channel/open conn)
          queues   (map #(declare-queue channel %)
                        (ensure-seq (:declare-queues config)))
          bindings (map #(bind channel %)
                        (ensure-seq (:bindings config)))]
      (reset! state {:connection conn :channel channel :config config})
      (.addShutdownListener conn    (make-on-connection-shutdown state))
      (.addShutdownListener channel (make-on-channel-shutdown conn))
      state)
    (catch IOException e
      ((:on-new-connection-fail (:config @state)) e)
      (reset! state {:connection nil :channel nil :config (:config @state)})
      state)))

(defn connect-with-state! [state]
  (loop [attempt-count 0]
    (when-not (= attempt-count (:max-reconnect-attempts (:config @state)))
      (let [new-state (connect-once! state)]
        (if new-state
          (do
            ((:on-connection (:config @new-state)) new-state)
            new-state)
          (do
            (sleep (nth (ensure-repeating-seq
                         (:ms-between-restarts (:config @state)))
                        attempt-count))
            (recur (inc attempt-count))))))))

(defn connect! [config]
  (let [config (merge default-config config)]
    (connect-with-state! (atom {:connection nil :channel nil :config config}))))

(defn fixed+random [init scale-of-randomness]
  (map (fn [fixed]
         (+ fixed (rand scale-of-randomness)))
       (repeat init)))

(defn bounded-exponential-backoff [init maximum]
  (take-while #(< % maximum) (iterate #(* 2 %) init)))

