(ns cljplygrnd.messaging.core
  (:require [langohr.core :as rmq]
            [langohr.channel :as lch]
            [langohr.queue :as lq]
            [langohr.consumers :as lc]
            [langohr.basic :as lb]))

(def ^{:const true}
  default-exchange-name "exchange1")

(defn message-handler
  [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
  (println (format "[consumer] Received a message: %s, delivery tag: %d, content type: %s, type: %s"
                   (String. payload "UTF-8") delivery-tag content-type type)))


(defn try-it [& args]
  (let [conn (rmq/connect {:host "turtle.rmq.cloudamqp.com" :username "xxx" :password "xxx" :vhost "xxx"})
        ch (lch/open conn)
        qname "queue1"]
    (println (format "[main] Connected. Channel id: %d" (.getChannelNumber ch)))
    (lq/declare ch qname :exclusive false :auto-delete true)
    (lc/subscribe ch qname message-handler :auto-ack true)
    (lb/publish ch default-exchange-name qname "Hello!" :content-type "text/plain" :type "greetings.hi")
    (Thread/sleep 2000)
    (println "[main] Disconnecting...")
    (rmq/close ch)
    (rmq/close conn)))