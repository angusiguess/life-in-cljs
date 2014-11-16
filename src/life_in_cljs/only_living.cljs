(ns life-in-cljs.only-living)

(defn neighbours
  "Given a cell's coordinates, returns the coordinates of its neighbours."
  [[x y]]
  (for [dx [-1 0 1] dy (if (zero? dx) [-1 1] [-1 0 1])]
    [(+ dx x) (+ dy y)]))

(def neighbours-memo (memoize neighbours))

(defn step
  "Given a set of living cells, computes the new set of living cells."
  [cells]
  (set (for [[cell n] (frequencies (mapcat neighbours-memo cells))
             :when (or (= n 3) (and (= n 2) (cells cell)))]
         cell)))
