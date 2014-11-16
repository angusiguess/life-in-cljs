(ns life-in-cljs.full-matrix)

(defn neighbors [x y]
  (for [dx [-1 0 1]
        dy (if (zero? dx) [-1 1] [-1 0 1])]
    [(+ dx x) (+ dy y)]))


(defn toggle-cell [state [x y]]
  (update-in state [x y] not))

(defn get-cell [state [x y]]
  (get-in state [x y]))

(defn neighbor-count [world [x y]]
  (->> (neighbors x y)
       (map #(get-cell world %))
       (filter #(= true %))
       count))

(defn cells [width height]
  (for [x (range 0 width)
        y (range 0 height)]
    [x y]))

(defn live-cells [state]
  (let [height (count state)
        width (-> state
                  first
                  count)
        cells (cells width height)]
    (filter #(true? (get-cell state %)) cells)))

(defn init [width height cells]
  (let [state (->> (repeat height (into []
                                        (repeat width false)))
                   (into []))]
    (reduce (fn [acc x]
              (toggle-cell acc x)) state cells)))

(defn step [state]
  (let [width (count state)
        height (-> state first count)
        locs (cells width height)]
    (reduce (fn [acc loc]
              (let [neighbors (neighbor-count state loc)]
                (cond (and (false? (get-cell state loc))
                           (= 3 neighbors))
                      (toggle-cell acc loc)
                      (and (true? (get-cell state loc))
                           (or (< neighbors 2)
                               (> neighbors 3)))
                      (toggle-cell acc loc)
                      :else acc))) state locs)))

(defn render [context state]
  (let [height (count state)
        width (-> state first count)]
    (set! (.-fillStyle context) "#FFFFFF")
    (.fillRect context 0 0 (* 2 width) (* 2 height))
    (set! (.-fillStyle context) "#000000")
    (for [x (range width)
          y (range height)]
      (when (true? (get-cell x y))
                   (.fillRect context (* x 2) (* y 2) 2 2)))))
