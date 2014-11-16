(ns life-in-cljs.components.only-changed
  (:require [cljs.core.async :as a]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [life-in-cljs.only-changed :as oc])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]))

(defonce sim-height 200)

(defonce sim-width 200)

(defn random-cells [width height n]
  (repeatedly n (fn [] [(rand-int width) (rand-int height)])))

;; Rendering functions

(defn render-cells [context state]
    (set! (.-fillStyle context) "#FFFFFF")
    (.fillRect context 0 0 sim-height sim-width)
    (set! (.-fillStyle context) "#000000")
    (doseq [[x y] state] 
      (.fillRect context (* 2 x) (* 2 y) 2 2)))

(defn update [owner dom-node-ref state]
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
                 (om/set-state! owner [:benchmark] (str "Frames created:" count " FPS:" (/ count 60)))
                 (do (om/update-state! owner [:state] oc/step)
                     (recur (a/timeout 10) (inc count))))))))

(defn step [owner]
  (.profile js/console "step")
  (om/update-state! owner [:state] oc/step)
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
      (dom/div #js {:id "third-sim"}
               (dom/canvas #js {:id "third-canvas"
                                :height sim-height
                                :width sim-width
                                :ref "third-canvas-ref"})
               (make-benchmark-button (om/get-state owner [:comm]) owner)
               (make-step-button (om/get-state owner [:comm]) owner)
               (dom/h4 nil (om/get-state owner [:benchmark]))))
    om/IWillMount
    (will-mount [_]
      (let [chan (a/chan)]
        (om/set-state! owner :comm chan)
        (go (while true
              (let [event (a/<! chan)]
                (handle-event owner event))))))
    om/IDidMount
    (did-mount [_]
      (update owner "third-canvas-ref" (om/get-state owner [:state :cells])))
    om/IDidUpdate
    (did-update [_ _ _]
      (update owner "third-canvas-ref" (om/get-state owner [:state :cells])))
    om/IInitState
    (init-state [_]
      (let [cells (set (random-cells 100 100 5000))]
        {:state {:cells cells
                 :changed cells}
         :benchmark "Click benchmark"}))))
