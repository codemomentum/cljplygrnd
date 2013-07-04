(ns cljplygrnd.stormspout
(:use
   [lamina.core]
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.callbacks.handlers]
   [twitter.api.streaming])
  (:require
   [backtype.storm [clojure :refer [defspout spout emit-spout!]]]
   [clojure.java.io :as io]
   [clojure.data.json :as json]
   [http.async.client :as ac])
  (:import
   [twitter.callbacks.protocols AsyncStreamingCallback]
   [java.io PushbackReader]))

(def twitter-conf
  {:consumer-token      "kIATR2cgQXt2nGl4mFDP1PveMl3M2NehzOkJelQMEc"
   :consumer-key        "w5DH0EfPkRhXSSWgXbVBdQ"
   :access-token        "748991593-020BCLeT4yQ5cCaEGI3IjdMDSwTvfgCYOroULcsg"
   :access-token-secret "c0H6qIckHwVh5HMOR2pCl6BzgmhPpo5uoE19EXr0LY"})

(def oath-creds
  (make-oauth-creds
    (twitter-conf :consumer-key)
    (twitter-conf :consumer-token)
    (twitter-conf :access-token)
    (twitter-conf :access-token-secret))
  )


(defspout twitter-spout ["tweet"]
  [conf context collector]

  (let [tweet-channel (channel)
        connection    (user-stream :oauth-creds oath-creds)
        callback      (AsyncStreamingCallback.
                       (fn on-body-part [response byte-stream]
                         (->> byte-stream
                              str
                              json/read-json
                              (enqueue tweet-channel)))
                       (fn on-failure [response]
                         (println response))
                       (fn on-exception [response]
                         (println response)))]

    (statuses-filter :params {:track "bigdata"}
                     :oauth-creds oath-creds
                     :callbacks callback)

    (spout
     (nextTuple []
                (emit-spout! collector [@(read-channel tweet-channel)]))
     (ack [id]
          ;; You only need to define this method for reliable spouts
          ;; (such as one that reads off of a queue like Kestrel)
          ;; This is an unreliable spout, so it does nothing here
          ))))