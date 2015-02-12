(ns kixi.nhs.exploration-ui.core
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [kixi.charts.line-chart :as line]))

(def app-state (atom {:chart {:data [{:year 2001 :value 3}
                                     {:year 2002 :value 4}
                                     {:year 2003 :value 5}
                                     {:year 2004 :value 1}
                                     {:year 2005 :value 6}]}}))

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
     [:h3 "Chart"]
     [:div
      (om/build line/simple-line-chart (:chart cursor) {:opts {:id "chart"
                                                               :style {:margin {:top 5 :right 5 :bottom 5 :left 5}
                                                                       :width 200 :height 150
                                                                       :axis-opts {:x-field :year :x-orientation :bottom
                                                                                   :x-axis-title "Year"
                                                                                   :y-field :value :y-orientation :left
                                                                                   :y-axis-title "Indicator Values"}}}})]])))

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
