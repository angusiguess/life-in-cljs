(ns life-in-cljs.components.full-matrix
  (:require [cljs.core.async :as a]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [life-in-cljs.full-matrix :as fm])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]))

(defonce sim-height 200)

(defonce sim-width 200)

(defn random-cells [width height n]
  (repeatedly n (fn [] [(rand-int width) (rand-int height)])))

;; Rendering functions

(defn render-cells [context state]
  (let [height (count state)
        width (-> state first count)
        coords (for [x (range 0 width)
                     y (range 0 height)] [x y])]
    (set! (.-fillStyle context) "#FFFFFF")
    (.fillRect context 0 0 sim-height sim-width)
    (set! (.-fillStyle context) "#000000")
    (doseq [[x y :as coord] coords] 
      (when (fm/get-cell state coord)
        (.fillRect context (* 2 x) (* 2 y) 2 2)))))

(defn update-fm [owner dom-node-ref state]
  (let [canvas (om/get-node owner dom-node-ref)
        context (.getContext canvas "2d")]
    (render-cells context state)))

;; Dispatch function

(defn benchmark [owner]
  (let [duration (a/timeout 60000)]
    (go-loop [frame (a/timeout 10)
              count 0]
             (let [[v ch] (a/alts! [duration frame])]
               (if (= ch duration)
                 (do (println "Frames created:" count)
                     (println "FPS:" (/ count 60)))
                 (do (om/update-state! owner [:state] fm/step)
                     (recur (a/timeout 10) (inc count))))))))

(defn step [owner]
  (.profile js/console "step")
  (om/update-state! owner [:state] fm/step)
  (.profileEnd js/console))

(defn handle-event [owner [type value]]
  (case type
    :benchmark (benchmark owner)
    :step (step owner)))


;; Om Components

(defn make-benchmark-button [comm owner]
  (dom/button
    #js {:id "benchmark"
         :onClick #(a/put! comm [:benchmark nil])}
    "Benchmark"))

(defn make-step-button [comm owner]
  (dom/button
    #js {:id "step"
         :onClick #(a/put! comm [:step nil])}
    "Step"))

(defn component [data owner]
  (reify om/IRender
    (render [_]
      (dom/div #js {:id "first-sim"}
               (dom/canvas #js {:id "first-canvas"
                                :height sim-height
                                :width sim-width
                                :ref "first-canvas-ref"})
               (make-benchmark-button (om/get-state owner [:comm]) owner)
               (make-step-button (om/get-state owner [:comm]) owner)))
    om/IWillMount
    (will-mount [_]
      (let [chan (a/chan)]
        (om/set-state! owner :comm chan)
        (go (while true
              (let [event (a/<! chan)]
                (handle-event owner event))))))
    om/IDidMount
    (did-mount [_]
      (update-fm owner "first-canvas-ref" (om/get-state owner [:state])))
    om/IDidUpdate
    (did-update [_ _ _]
      (update-fm owner "first-canvas-ref" (om/get-state owner [:state])))
    om/IInitState
    (init-state [_]
      {:state (fm/init 100 100 (random-cells 100 100 500))})))
