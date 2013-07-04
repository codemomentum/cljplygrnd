(ns cljplygrnd.cassandra.play
(:require [clojurewerkz.cassaforte.client :as client])
  (:use clojurewerkz.cassaforte.cql
        clojurewerkz.cassaforte.query))

; ;; Will connect to 3 nodes in a cluster
; (client/connect! ["127.0.0.1"])

; ;; Default session is used for the query
; (insert :users {:name "Alex" :city "Munich"})

