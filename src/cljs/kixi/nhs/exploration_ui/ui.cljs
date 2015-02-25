(ns kixi.nhs.exploration-ui.ui
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs.core.async :refer [<! >! chan put!]]
            [kixi.charts.line-chart :as line]
            [kixi.nhs.exploration-ui.dataset-details :as dd]
            [kixi.nhs.exploration-ui.dataset-selection :as ds]
            [kixi.nhs.exploration-ui.data :as data]
            [clojure.string :as str]))

(def app-state (atom {:datasets {:all []
                                 :selected {:resources []
                                            :preview {:data []
                                                      :id nil}}}
                      :chart {:data []
                              :fields []}}))

(defn navbar [cursor owner]
  (om/component
   (html
    [:nav.navbar.navbar-default.navbar-fixed-top
     [:div.container-fluid
      [:div.row.header
       [:div.col-span-4
        [:img {:src "images/NHS-England-Logo.png"
               :style {:position "absolute" :left 20 :top 50 :z-index 4 :height 40}}]]]]])))

(defn side-bar [cursor owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (data/get-all-datasets [:datasets :all] cursor))
    om/IRender
    (render [_]
      (html
       [:div.col-sm-3.side-menu
        (om/build ds/selection (:datasets cursor))]))))

(defn charting-area [cursor owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [data fields]} cursor]
        (html
         [:div.col-sm-9.col-sm-offset-3.main
          [:h3 "Chart"]
          [:div {:id "chart" :style {:width "100%" :height 400}}
           (when (seq (-> cursor :data))
             (om/build line/simple-line-chart cursor {:opts {:id "chart"
                                                             :style {:margin {:top 20 :right 40 :bottom 50 :left 40}
                                                                     :axis-opts {:x-field (:x-axis fields) :x-orientation :bottom
                                                                                 :x-axis-title (:x-axis fields)
                                                                                 :y-field (:y-axis fields)
                                                                                 :y-orientation :left
                                                                                 :y-axis-title (:y-axis fields)}}}}))]])))))

(defmulti event-handler (fn [event value cursor] event))

(defmethod event-handler :dataset-details [_ value cursor]
  (om/update! cursor [:datasets :selected :preview :data] [])
  (om/update! cursor [:datasets :selected :preview :id] nil)
  (om/update! cursor [:chart :data] [])
  (om/update! cursor [:chart :fields] {})
  (om/update! cursor [:datasets :selected :resources] (into [] (:resources value))))

(defmethod event-handler :resource-details [_ value cursor]
  (om/update! cursor [:datasets :selected :preview :data] [])
  (om/update! cursor [:datasets :selected :preview :id] nil)
  (om/update! cursor [:datasets :selected :preview :id] (:id value))
  (data/get-resource-data (:id value) [:datasets :selected :preview :data] cursor :limit 5))

(defmethod event-handler :field-selection [_ value cursor]
  (let [x-axis (-> value :x-axis :field-name keyword)
        y-axis (-> value :y-axis first :field-name keyword)] ;; TODO plot multiple line chart
    (om/update! cursor [:chart :fields] {:x-axis x-axis :y-axis y-axis})
    (data/get-resource-data (-> @cursor :datasets :selected :preview :id) [:chart :data] cursor)))

(defn exploration-ui [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [event-chan (om/get-shared owner :event-chan)
              {:keys [event value]} (<! event-chan)]
          (event-handler event value cursor)
          (recur))))
    om/IRenderState
    (render-state [_ state]
      (html
       [:div
        (om/build navbar cursor)
        [:div.container-fluid
         [:div.row.exploration-ui
          (om/build side-bar cursor)
          (when (empty? (-> cursor :chart :data))
            (om/build dd/dataset-preview (-> cursor :datasets :selected)))
          (when (seq (-> cursor :chart :data))
            (om/build charting-area (:chart cursor)))]]]))))

(defn main []
  (om/root exploration-ui app-state {:target (. js/document (getElementById "app"))
                                     :shared {:event-chan (chan)}}))
