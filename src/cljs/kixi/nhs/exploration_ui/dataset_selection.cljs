(ns kixi.nhs.exploration-ui.dataset-selection
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs.core.async :refer [<! >! chan put!]]))


(defn dataset-row [cursor owner]
  (om/component
   (html
    [:tr {:on-click (fn [e]
                      (put! (om/get-shared owner :event-chan)
                            {:event :dataset-details :value cursor}))}
     [:td (:title cursor)]])))

(defn selection [cursor owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (.addEventListener js/window
                         "resize"
                         (fn [] (om/refresh! owner))))
    om/IRender
    (render [_]
      (html
       [:div
        [:h3 "Datasets"]
        [:div.panel.panel-default
         [:div.panel-body {:style {:height 600 :overflow "scroll"
                                   :font-size "80%" :padding 0}}
          [:table.table.table-hover.table-condensed
           [:thead
            [:tr
             [:th "Name"]]]
           [:tbody {:style {:overflow-y "auto"}}
            (om/build-all dataset-row (:all cursor) {:opts {:key :id}})]]]]]))))
