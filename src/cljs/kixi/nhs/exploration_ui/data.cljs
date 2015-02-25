(ns kixi.nhs.exploration-ui.data
  (:require [om.core :as om :include-macros true]
            [ajax.core :refer (GET POST)]
            [clojure.walk :as walk]
            [clojure.string :as str]))

;; TODO replace with a proper date parser
(defn date-parser [date]
  (let [date-str (-> date (.match #"\d{4}"))]
    (new js/Date date-str)))

(defn ckan-host []
  "http://54.154.11.196/api/3/action/")

(defn get-url
  "Build url string by joining site url with params."
  [& api-method]
  (let [hostname (ckan-host)]
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

(defn scrub-keyword [k]
  (-> k
      (str/replace " " "_")
      (str/lower-case)
      keyword))

(defn scrub-keywords [data]
  (->> data
       (map #(->> %
                 (map (fn [[k v]] {(scrub-keyword k) v}))
                 (apply merge)))))

(defn get-resource-data
  "Search data in a resource and return results.
  If limit is not passed in, the default of 10000 is used."
  [resource_id k cursor & {:keys [limit] :or {limit 10000}}]
  (GET (get-url "datastore_search?offset=" 0 "&resource_id=" resource_id "&limit=" limit)
       {:handler (fn [response]
                   (om/update! cursor k (-> (get response "result")
                                            (get "records")
                                            js->clj
                                            scrub-keywords
                                            vec)))
        :headers {"Accept" "application/json"}
        :response-format :json}))
