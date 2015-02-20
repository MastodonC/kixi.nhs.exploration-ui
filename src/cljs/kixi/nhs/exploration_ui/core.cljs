(ns kixi.nhs.exploration-ui.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs.core.async :refer [<! >! chan put!]]
            [kixi.charts.line-chart :as line]
            [kixi.nhs.exploration-ui.dataset-details :as dd]
            [ajax.core :refer (GET POST)]
            [clojure.walk :as walk]
            [clojure.string :as str]))

(def app-state (atom {:datasets {:all []
                                 :selected {}}
                      :chart {:data []}}))

(defn date-parser [date]
  (let [date-str (-> date (.match #"\d{4}"))]
    (new js/Date date-str)))

(defn get-url
  "Build url string by joining site url with params."
  [& api-method]
  (let [hostname "http://54.154.11.196/api/3/action/"]
    (apply str hostname api-method)))

(defn get-all-datasets [k cursor]
  (GET (get-url "current_package_list_with_resources")
       {:handler (fn [response]
                   (om/update! cursor k (-> (get response "result")
                                            (js->clj)
                                            walk/keywordize-keys)))
        :headers {"Accept" "application/json"}
        :response-format :json}))

(defn str-vals->num-vals [data]
  (map #(-> %
            (update-in [:indicator_value] js/parseInt)
            (update-in [:year] date-parser)) data))

(defn scrub-keywords [data]
  (->> data
       (map #(->> %
                 (map (fn [[k v]] {(-> k
                                       (str/replace " " "_")
                                       (str/lower-case)) v}))
                 (apply merge)))
       walk/keywordize-keys))

(defn get-resource-data [resource_id k cursor]
  (GET (get-url "datastore_search?offset=" 0 "&resource_id=" resource_id)
       {:handler (fn [response]
                   (om/update! cursor k (-> (get response "result")
                                            (get "records")
                                            js->clj
                                            scrub-keywords
                                            str-vals->num-vals)))
        :headers {"Accept" "application/json"}
        :response-format :json}))

(defn navbar [cursor owner]
  (om/component
   (html
    [:nav.navbar.navbar-default.navbar-fixed-top
     [:div.container-fluid
      [:div.row.header
       [:div.col-span-4
        [:img {:src "images/NHS-England-Logo.png"
               :style {:position "absolute" :left 20 :top 50 :z-index 4 :height 40}}]]
       ;;[:h1.col-span-8.col-sm-offset-3.header-text "Exploration UI"]
       ]]])))

(defn dataset-row [cursor owner]
  (om/component
   (html
    [:tr {:on-click (fn [e]
                      (put! (om/get-shared owner :selected-dataset-chan) cursor))}
     [:td (:title cursor)]])))

(defn dataset-selection [cursor owner]
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

(defn side-bar [cursor owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (get-all-datasets [:datasets :all] cursor))
    om/IRender
    (render [_]
      (html
       [:div.col-sm-3.side-menu
        (om/build dataset-selection (:datasets cursor))]))))

(defn charting-area [cursor owner]
  (reify
    om/IRender
    (render [_]
      (println "chart: " (->  cursor :chart :data))
      (html
       [:div.col-sm-9.col-sm-offset-3.main
        [:h3 "Chart"]
        [:div {:id "chart" :style {:width "100%" :height 400}}
         (when (seq (-> cursor :chart :data))
           (om/build line/simple-line-chart (:chart cursor) {:opts {:id "chart"
                                                                    :style {:margin {:top 20 :right 40 :bottom 50 :left 40}
                                                                            :axis-opts {:x-field :year :x-orientation :bottom
                                                                                        :x-axis-title "Year"
                                                                                        :y-field :indicator_value :y-orientation :left
                                                                                        :y-axis-title "Value"}}}}))]]))))

(defn exploration-ui [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [selected-dataset-chan (om/get-shared owner :selected-dataset-chan)
              row                   (<! selected-dataset-chan)]
          (om/update! cursor [:datasets :selected] row)
          (recur))))
    om/IRenderState
    (render-state [_ state]
      (html
       [:div
        (om/build navbar cursor)
        [:div.container-fluid
         [:div.row.exploration-ui
          (om/build side-bar cursor)
          (om/build dd/dataset-preview (-> cursor :datasets :selected))
          ;;(om/build charting-area cursor)
          ]]]))))

(defn main []
  (om/root exploration-ui app-state {:target (. js/document (getElementById "app"))
                                     :shared {:selected-dataset-chan (chan)}}))
