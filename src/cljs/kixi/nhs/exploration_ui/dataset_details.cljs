(ns kixi.nhs.exploration-ui.dataset-details
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs.core.async :refer [<! >! chan put!]]
            [clojure.string :as str]
            [kixi.nhs.exploration-ui.data :as data]))

(defn data-row
  "Renders a single row of data."
  [cursor owner]
  (om/component
   (html
    [:tr {:on-click (fn [_] )}
     (for [[k v] cursor]
       [:td v])])))

(defn resource-preview
  "Displays preview of selected resource
  (dataset may have more than one resouce)
  and allows the user to select fields for
  charting."
  [cursor owner]
  (reify
    om/IRender
    (render [_]
      (let [headers (->> cursor first keys (map name))]
        (html
         [:div.panel.panel-default
          [:div.panel-body
           [:h4 "Data Preview"]
           [:div.table-responsive {:style {:overflow "auto"}}
            [:table.table.table-hover.table-condensed
             [:thead
              [:tr
               (for [th headers]
                 [:th th])]]
             [:tbody
              ;; Preview 5 first rows of the data
              (om/build-all data-row (into [] cursor))]]]]])))))


(defn resource-item
  "Renders a single resource."
  [cursor owner]
  (om/component
   (let [{:keys [id description name created]} cursor]
     (html
      [:tr {:on-click (fn [_] (put! (om/get-shared owner :event-chan)
                                    {:event :resource-details :value cursor}))}
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

(defmulti validate-selection (fn [owner header axis] axis))

(defmethod validate-selection :none [owner header _]
  (when (= header (:field-name (om/get-state owner :x_axis)))
    (om/set-state! owner :x_axis {})
    (om/set-state! owner :y_axis (vec (remove #(= (:field %) header) (om/get-state owner :y_axis))))))

(defmethod validate-selection :x_axis [owner header _]
  (let [current-selection (om/get-state owner :x_axis)]
    (if (seq current-selection)
      (om/set-state! owner :error {:message "You can only select a single field to plot on X axis."})
      (om/set-state! owner :x_axis {:field-name header}))))

(defmethod validate-selection :y_axis [owner header _]
  (om/set-state! owner :y_axis (conj (om/get-state owner :y_axis) {:field-name header})))

(defn on-change [owner header e]
  (let [v (.-value (aget (.-options (.-target e)) (.-selectedIndex (.-options (.-target e)))))]
    (om/set-state! owner :error {})
    (validate-selection owner header (data/scrub-keyword v))))

(defn select-fields [owner event-chan]
  (let [{:keys [x_axis y_axis]} (om/get-state owner)]
    (if (and (seq x_axis) (seq y_axis))
      (put! event-chan {:event :field-selection :value {:x-axis x_axis :y-axis y_axis}}) ;; TODO refactor the underscore!
      (om/set-state! owner :error {:message "Please select a single field to plot on X axis and one or more fields to plot on Y axis."}))))

(defn fields-selection
  "Allows the user to select which fields should
  be plotted on x and y axes."
  [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:x_axis {}
       :y_axis []
       :error {}})
    om/IRenderState
    (render-state [_ {:keys [error y-axis x-axis] :as state}]
      (html
       [:div
        (when (seq cursor)
          [:div [:h5 "Select fields that you would like to plot on the chart:"]
           (let [headers (->> cursor first keys (map name))
                 options ["None" "X Axis" "Y Axis"]]
             ;; TODO make scrollable horizontally
             [:div.table-responsive {:style {:overflow-x "scroll"}}
              [:table.table.table-borderless.table-condensed
               [:tbody
                [:tr
                 (for [h headers]
                   [:td {:style {:min-width "80px"}}
                    [:div h]
                    [:div [:select.form-control {:default-value "None"
                                                 :on-change #(on-change owner h %)}
                           (for [o options]
                             [:option o])]]])]]]])
           [:div (if (seq error) [:p {:style {:color "red"}} (:message error)])]
           [:div {:style {:padding-bottom "10px"}}
            [:button.btn.btn-primary {:on-click #(select-fields owner (om/get-shared owner :event-chan))} "Select fields" ]]])]))))

(defn dataset-preview
  "Displays all resources within the selected dataset,
  and allows the user to preview their data and select
  the fields for plotting on the chart."
  [cursor owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-sm-9.col-sm-offset-3.main
        (if-not (empty? cursor)
          [:div
           [:h3 "Detaset Details"]
           (om/build resources-list (:resources cursor))
           (om/build fields-selection (-> cursor :preview :data))
           (om/build resource-preview (-> cursor :preview :data))]
          [:p {:style {:padding-top "25px"}} "Please select dataset on the left."])]))))
