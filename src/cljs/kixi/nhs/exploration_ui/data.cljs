(ns kixi.nhs.exploration-ui.data
  (:require [om.core :as om :include-macros true]
            [ajax.core :refer (GET POST)]
            [clojure.walk :as walk]
            [clojure.string :as str]))

(def parsers [#"\d{4}"
              #"\w* \d{4} to \w* \d{4}"
              #"\w* - \w* \d{4}"
              #"\d{4}\.\d"
              #"\d{4}-\d{2}"
              #"\d{4}/\d{2}"
              #"\d{1,2}/\d{1,2}/\d{4} to \d{1,2}/\d{1,2}/\d{4}"])

(def months
  ["January" "February" "March" "April" "May" "June" "July" "August"
   "September" "October" "November" "December"])

(def short-months
  ["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug"
   "Sep" "Oct" "Nov" "Dec"])

(defn index-of [coll x]
  (first (keep-indexed #(when (= %2 x) %1) coll)))

(defmulti s->timestamps (fn [s pattern] pattern))

;; January 2014 to February 2014
(defmethod s->timestamps "\\w* \\d{4} to \\w* \\d{4}" [s pattern]
  (let [[start-str _] (str/split s #" to ")
        start-date    (str (str/trim (first (.match start-str #"\d{4}"))) "/"
                           (inc (index-of months (str/trim (first (.match start-str #"\w*")))))
                           "/" "01")]
    (new js/Date start-date)))

;; 1/4/2013 to 31/3/2014
(defmethod s->timestamps "\\d{1,2}\\/\\d{1,2}\\/\\d{4} to \\d{1,2}\\/\\d{1,2}\\/\\d{4}" [s pattern]
  (let [[start-str _] (str/split s #" to ")
        start-date    (str/trim (first (.match start-str #"\d{1,2}/\d{1,2}/\d{4}")))]
    (new js/Date start-date)))

;; Oct - Dec 2013
(defmethod s->timestamps "\\w* - \\w* \\d{4}" [s pattern]
  (let [[start-str end-str] (str/split s #" - ")
        start-date          (str (str/trim (first (.match end-str #"\d{4}"))) "/"
                                 (inc (index-of short-months (str/trim start-str)))
                                 "/" "01")]
    (new js/Date start-date)))

;; 2004
(defmethod s->timestamps "\\d{4}" [s pattern]
  (new js/Date s))

;; 2004.0
(defmethod s->timestamps "\\d{4}\\.\\d" [s pattern]
  (new js/Date (str/trim (first (.match s #"\d{4}")))))

;; 2013-14
(defmethod s->timestamps "\\d{4}-\\d{2}" [s pattern]
  (let [start-str (str/trim (first (.match s #"\d{4}")))]
    (new js/Date start-str)))

;; 2013/14
(defmethod s->timestamps "\\d{4}\\/\\d{2}" [s pattern]
  (let [start-str (str/trim (first (.match s #"\d{4}")))]
    (new js/Date start-str)))

(defn parse [parser s]
  (let [date-str (.match s parser)]
    (when (= s (first date-str)) ;; we're looking for exact match
      (s->timestamps s (.-source parser)))))

(defn parse-date [s]
  (some (fn [p]
          (let [d (parse p s)]
            (when-not (nil? d)
              d))) parsers))

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

(defn scrub-dates [m field]
  (update-in m [field] parse-date))

(defn scrub-numerical-vals [m field]
  (update-in m [field] js/parseInt))

(defn scrub-data [fields data]
  (keep #(when-not (nil? (get % (:x fields)))
           (-> %
               (scrub-numerical-vals (:y fields))
               (scrub-dates (:x fields)))) data))

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
  [resource_id k cursor & {:keys [limit fields] :or {limit 10000}}]
  (GET (get-url "datastore_search?offset=" 0 "&resource_id=" resource_id "&limit=" limit)
       {:handler (fn [response]
                   (om/update! cursor k (-> (get response "result")
                                            (get "records")
                                            js->clj
                                            scrub-keywords
                                            (cond->> (seq fields) (scrub-data fields))
                                            vec)))
        :headers {"Accept" "application/json"}
        :response-format :json}))
