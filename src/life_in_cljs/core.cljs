(ns life-in-cljs.core
  (:require
    [figwheel.client :as fw]
    [om.core :as om :include-macros true]
    [life-in-cljs.components.full-matrix :as om-fm])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]))

(enable-console-print!)



(om/root om-fm/component {} {:target (. js/document (getElementById "app"))})

;; define your app data so that it doesn't get over-written on reload
;; (defonce app-data (atom {}))

(fw/watch-and-reload
 :jsload-callback (fn []
                    ;; (stop-and-start-my app)
                    ))
