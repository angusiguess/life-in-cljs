(ns life-in-cljs.only-changed
  (:require [clojure.set :as s]))

(defn neighbors [[x y]]
  (for [dx [-1 0 1] 
        dy (if (zero? dx) [-1 1] [-1 0 1])]
    [(+ dx x) (+ dy y)]))

(defn changed-cells [[x y]]
  (for [dx [-1 0 1] 
        dy  [-1 0 1]]
    [(+ dx x) (+ dy y)]))

(def changed-cells-memo (memoize changed-cells))


(def neighbors-memo (memoize neighbors))

(defn step [state]
  (let [{:keys [cells changed]} state
        changed-neighborhood (set (mapcat changed-cells-memo changed))
        changed-and-alive (s/intersection changed-neighborhood cells)
        changed-cells (frequencies (mapcat neighbors-memo changed-and-alive))
        live-counts (reduce (fn [acc loc] (assoc acc loc 0)) {} changed-and-alive)
        changed-cells (merge live-counts changed-cells)
        state (assoc state :changed #{})]
    (reduce (fn [acc [loc n]]
              (cond (and (cells loc) (or (< n 2) (> n 3)))
                    (-> acc
                        (update-in [:cells] disj loc)
                        (update-in [:changed] conj loc))
                    (and (nil? (cells loc)) (= n 3))
                    (-> acc
                        (update-in [:cells] conj loc)
                        (update-in [:changed] conj loc))
                    :else acc)) state changed-cells)))
