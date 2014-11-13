(ns life-in-cljs.core
  (:require
    [figwheel.client :as fw]
    [cljs.core.async :as a]
    [life-in-cljs.full-matrix :as fm]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]))

(enable-console-print!)

(defonce sim-height 200)

(defonce sim-width 200)

(defn random-cells [width height n]
  (repeatedly n (fn [] [(rand-int width) (rand-int height)])))


;; Om Components

(defn first-life [data owner]
  (reify om/IRender
    (render [_]
      (dom/div #js {:id "first-sim"}
               (dom/canvas #js {:id "first-canvas"
                                :height sim-height
                                :width sim-width
                                :ref "first-canvas-ref"})))
    om/IInitState
    (init-state [_]
      (fm/init 100 100 (random-cells 100 100 50)))))

(om/root first-life {} {:target (. js/document (getElementById "app"))})

;; define your app data so that it doesn't get over-written on reload
;; (defonce app-data (atom {}))

(fw/watch-and-reload
 :jsload-callback (fn []
                    ;; (stop-and-start-my app)
                    ))
