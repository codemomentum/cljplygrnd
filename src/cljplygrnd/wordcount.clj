(ns cljplygrnd.wordcount
  (:import [backtype.storm StormSubmitter LocalCluster]
           [lamina.core])
  (:use [backtype.storm clojure config]))

(defspout sentence-spout ["sentence"]
  [conf context collector]
  (let [sentences ["a little brown dog"
                   "the man petted the dog"
                   "four score and seven years ago"
                   "an apple a day keeps the doctor away"]]
    (spout
      (nextTuple []
        (Thread/sleep 100)
        (emit-spout! collector [(rand-nth sentences)])
        )
      (ack [id]
        ;; You only need to define this method for reliable spouts
        ;; (such as one that reads off of a queue like Kestrel)
        ;; This is an unreliable spout, so it does nothing here
        ))))

(defbolt split-sentence ["word"] [tuple collector]
  (let [words (.split (.getString tuple 0) " ")]
    (doseq [w words]
      (emit-bolt! collector [w] :anchor tuple))
    (ack! collector tuple)
    ))

(defbolt word-count ["word" "count"] {:prepare true}
  [conf context collector]
  (.println System/out "-------------COUNTER RESTART-----------------")
  (let [counts (atom {})]
    (bolt
      (execute [tuple]
        (let [word (.getString tuple 0)]
          (swap! counts (partial merge-with +) {word 1})
          (emit-bolt! collector [word (@counts word)] :anchor tuple)
          (.println System/out (str "-------------" word (@counts word) "-----------------"))
          (ack! collector tuple)
          )))))

(defn word-count-top
  []
  (topology
    {"sentence-emiter" (spout-spec sentence-spout)}
    {"wordiser" (bolt-spec
                  {"sentence-emiter" :shuffle}
                  split-sentence :p 5)
     "counter" (bolt-spec
                 {"wordiser" ["word"]}
                 word-count :p 5)}))

(defn run! []
  (let [cluster (LocalCluster.)]
    (.submitTopology cluster "word-count" {TOPOLOGY-DEBUG true} (word-count-top))
    (Thread/sleep 10000)
    (.shutdown cluster)
    ))
