(ns kixi.nhs.exploration-ui.dataset-details
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs.core.async :refer [<! >! chan put!]]
            [kixi.charts.line-chart :as line]
            [ajax.core :refer (GET POST)]
            [clojure.walk :as walk]
            [clojure.string :as str]))


(defn resource-preview
  "Displays preview of selected resource
  (dataset may have more than one resouce)
  and allows the user to select fields for
  charting."
  [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {})
    om/IRenderState
    (render-state [_ state]
      (html
       [:div
        ]))))


(defn resource-item
  "Renders a single resource."
  [cursor owner]
  (om/component
   (let [{:keys [id description name created]} cursor]
     (html
      [:tr
       [:td name]
       [:td description]
       [:td created]]))))


(defn resources-list
  "Displays a list of available resources
  for selected dataset and allows the user
  to select one to preview."
  [cursor owner]
  (om/component
   (html
    [:div.panel.panel-default
     [:div.panel-body
      [:h4 "All Available Resources:"]
      [:table.table.table-hover.table-condensed
       [:thead
        [:tr
         [:th "Name"] [:th "Description"] [:th "Created"]]]
       [:tbody
        (om/build-all resource-item cursor {:key :id})]]]])))

(defn dataset-preview
  ""
  [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {})
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-sm-9.col-sm-offset-3.main
        (if-not (empty? cursor)
          [:div
           [:h3 "Detaset Details"]
           (om/build resources-list (:resources cursor))]
          [:p {:style {:padding-top "25px"}} "Please select dataset on the left."])]))))
