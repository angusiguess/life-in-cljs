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

(def init-state (fm/init 100 100 [[1 1] [1 2] [1 3]]))

;; Om Components

(defn first-life [data owner]
  (reify om/IRender))

;; define your app data so that it doesn't get over-written on reload
;; (defonce app-data (atom {}))

(fw/watch-and-reload
 :jsload-callback (fn []
                    ;; (stop-and-start-my app)
                    ))
