(ns cljplygrnd.barbershop
  (:require [clojure.core.async :as async :refer :all])
  )



(defonce waiting-room (chan (dropping-buffer 4)))


(defn add-new-customer! [customer-name]
  (println "Adding customer " customer-name)
   (try 
     (go (>! waiting-room customer-name))
     (catch Exception e (println (.getMessage e))))
)

(defn add-baber! [barber-name]
  (thread 
    (while true (let [customer (<!! (go (<! waiting-room)))]
    (Thread/sleep 2000)
    (println (str "Barber " barber-name "finished dealing with customer: " customer))
    )
      )
   )
 )


(defn start-barbershop! []
  (add-baber! "barber1")
  (add-new-customer! "customer1")
  (add-new-customer! "customer2")
  (add-new-customer! "customer3")
  (Thread/sleep 1000)
  (add-new-customer! "customer4")
  (Thread/sleep 1000)
  (add-new-customer! "customer5")
  (add-new-customer! "customer6")
  (add-new-customer! "customer7")
  (add-new-customer! "customer8")
  (add-new-customer! "customer9")
  (add-new-customer! "customer10")
  (add-new-customer! "customer11")
  )