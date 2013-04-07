# round-the-rabbit

Not done yet.

## TODO

* Multiple addresses (waiting on a patch to Langohr, to not complicate this lib)
* Exotic queue declarations
* Declaring exchanges
* Simple queue declaration, where it's just a string.

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
