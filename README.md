# round-the-rabbit

A simple way to use RabbitMQ and Clojurewerk's excellent Langohr library.

Declare how you want to connect. (With a vocabulary faithful to AMQP
concepts.) This lib takes care of auto-reconnect; and it's still easy
to use Langohr directly, for more control.


## Status

**Not release-ready yet.**

## TODO

* Multiple addresses (waiting on a patch to Langohr, to not complicate this lib)
* Exotic queue declarations
* Type hints (warn on reflection)
* How to deal with unrecoverable exceptions, like changing exchange type?
* Tests
* `connect`s return value needs more accessors
* Documentation
* Not a single defn-? Mmm-hmm.
* Instead of throwing exceptions on bad input, provide an analyzer. It
  could even try to connect and check if there's conflicting exchange
  settings.
* Allow exchange types to be keywords?

## Positive quirks

If a queue's name is a keyword, it's server-assigned. You can use this
keyword in bindings.

## Usage

**Dealing with buggy declarations**

```clojure
(def conn-state
  (connect
   {:declare-exchanges [{:name "exchange-1" :type "foo"}
                        {:name "exchange-2" :type "topic"}]
    :declare-queues "queue-1"
    :bindings [{:exchange "exchange-1" :queue "queue-1"}]

    :on-connection (fn [conn-state] (println "Connected!" conn-state))
    :on-new-connection-fail (fn [conn-state ex]
                              (println (bean ex))
                              (.printStackTrace ex))
    :max-connect-attempts 1}))
```

What are we looking at? We:
* declared an invalid exchange (type foo). It won't work.
* declared just one queue, with default settings. (No auto-delete, not durable.)
* generally want debug output, or at least stats, on failure.
* don't want it to reconnect on failure. We're just testing.

(If you're running this using nrepl, the stacktrace may print to the
`*nrepl server*` buffer, while println goes to your REPL. Confusing
behavior, though at least a nightmare of repeated stacktraces won't
make your REPL too unusable.)


**Simple working examples**

If we just have a single exchange or queue, then we don't need to put
it in a vector. But we can, if we want:

```clojure
(def conn-state
  (connect
   {:declare-exchanges [{:name "exchange-1" :type "fanout"}]
    :declare-queues [{:name "queue-1" :durable true :auto-delete true}]
    :bindings [{:exchange "exchange-1" :queue "queue-1"}]}))
```

Often, you'll want to subscribe to a queue:

```clojure
(def conn-state-10
  (connect {:declare-queues "queue-10"
             :consumers [{:queue "queue-10"
                          :handler (fn [channel metadata ^bytes payload]
                                     (println (String. payload "UTF-8")))}]}))
```

There's a bunch of knobs:

```clojure
(def conn-state
  (connect
   {:declare-queues "queue-1"

    :on-connection (fn [conn-state] (println "Connected!" conn-state))
    :on-new-connection-fail (fn [conn-state ex] (.printStackTrace ex))
    :max-connect-attempts 10
    :ms-between-restarts 1

    :addresses [{:host "example.com" :port 5566}]
    :login {:username "my-username" :password "my-password"}
    :vhost "/my-vhost"}))
```


## License

Copyright © 2013 Tj Gabbour

Distributed under the Eclipse Public License, the same as Clojure.
