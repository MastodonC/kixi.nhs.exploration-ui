(ns kixi.nhs.exploration-ui.core
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]))

(def app-state (atom {}))

(defn navbar [cursor owner]
  (om/component
   (html
    [:nav.navbar.navbar-default.navbar-fixed-top
     [:div.container-fluid
      [:div.row.header
       [:h1.col-span-8.col-sm-offset-1.header-text "Exploration UI"]]]])))

(defn dataset-selection [cursor owner]
  (om/component
   (html
    [:div
     [:h3 "Datasets"]
     ])))

(defn side-bar [cursor owner]
  (om/component
   (html
    [:div.col-sm-3.side-menu
     (om/build dataset-selection (:all-datasets cursor))])))

(defn charting-area [cursor owner]
  (om/component
   (html
    [:div.col-sm-9.col-sm-offset-3.main
     [:h3 "Chart"]])))

(defn main []
  (om/root
    (fn [app owner]
      (reify
        om/IRender
        (render [_]
          (html
           [:div
            (om/build navbar app)
            [:div.container-fluid
             [:div.row.exploration-ui
              (om/build side-bar app)
              (om/build charting-area app)]]]))))
    app-state
    {:target (. js/document (getElementById "app"))}))
