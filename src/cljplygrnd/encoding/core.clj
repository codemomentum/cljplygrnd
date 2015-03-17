(ns cljplygrnd.encoding.core)

(defn run-rle 
  "will produce a collection like the following:

  [(A 3) (B 1) (C 5)]
  "
  [state-coll item]
                                        ;(println @state-coll)
                                        ;(println item)
  (let [last-entry (first @state-coll)]
    (if (nil? last-entry)
      (reset! state-coll [[item 1]])
      (do 
        (if (= item (first last-entry))
          (let [count (inc (last last-entry))]
            (reset! state-coll (cons [(first last-entry) count] (rest @state-coll))))
          (reset! state-coll (cons [item 1] @state-coll))))))
  )

(defn encode-rle 
  "returns a run length encoded version of the given message"
  [^String message]
  (let [coll-state (atom [])]
    (dorun  (map #(run-rle coll-state %) (seq message)))
    (reduce str (reverse @coll-state)))
  )



(encode-rle "AAAAVVVVBBBBCCCCQQWEQWEASAAAAAAAAAARRRRRRRRRRRRRRRRRR!!!!!!!!")

(encode-rle "AAB")

(encode-rle "AAABB")


