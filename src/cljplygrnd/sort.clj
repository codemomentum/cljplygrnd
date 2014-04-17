(def x [4 6 1 2 3 8 0 5])


(defn insert [n coll]
  (cond
    (empty? coll) (list n)
    (> (first coll) n) (conj coll n)
    :else (conj (insert n (rest coll)) (first coll))))

(defn insertion-sort [coll]
  (loop [list coll result '()]
    (if (empty? list)
      result
      (recur (rest list) (insert (first list) result)))))



(defn move [coll]
  (loop [list coll
         result '()]
    (if (empty? list)
      result
    (recur (rest list) (cons (first list) result)))))
