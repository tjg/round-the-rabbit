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

(def my-rabbitmq-connection
  (connect!
   {:addresses {:host "localhost" :port 5672}
    :login {:username "guest" :password "foobar}
    :declare-queues "my-queue"}))

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
