# round-the-rabbit

Simple way to use RabbitMQ:
* auto-reconnect
* declarative interface
* light wrapper atop Langohr, so you can use all its powers


## TODO

* Multiple addresses (waiting on a patch to Langohr, to not complicate this lib)
* Exotic queue declarations
* Type hints (warn on reflection)
* How to deal with unrecoverable exceptions, like changing exchange type?

## Positive quirks

If a queue's name is a keyword, it's server-assigned. You can use this
keyword in bindings.

## Usage

Make a couple exchanges and queues (`queue-1`'s simple declaration
just gives it the default config):

```clojure
(def conn-state
  (connect!
   {:declare-exchanges [{:name "exchange-1" :type "fanout"}
                        {:name "exchange-2" :type "topic"}]
    :declare-queues ["queue-1"
                     {:name "queue-2" :durable true :auto-delete true}]
    :bindings [{:exchange "exchange-1" :queue "queue-1"}]}))
```

If you declare one exchange or one queue, you don't need to put it in a list:

```clojure
(def conn-state
  (connect! {:declare-queues "queue-10"}))
```

There's a bunch of knobs

```clojure
(def conn-state
  (connect!
   {:declare-queues "queue-1"

    :bindings [{:exchange "exchange-1", :queue "queue-1"}]
    :on-connection (fn [conn-state] (println "Connected!" conn-state))
    :on-new-connection-fail (fn [conn-state ex] (.printStackTrace ex))
    :max-reconnect-attempts 10
    :ms-between-restarts 1

    :addresses [{:host "example.com" :port 5566}]
    :login {:username "my-username" :password "my-password"}
    :vhost "/my-vhost"}))
```


## License

Copyright © 2013 Tj Gabbour

Distributed under the Eclipse Public License, the same as Clojure.
