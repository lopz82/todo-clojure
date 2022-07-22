(ns todo.handlers
  (:require [todo.db :as db]))

(defn ok
  ([body]
   {:status 200
    :body   body})
  ([]
   (ok "")))

(defn header
  [response name value]
  (assoc-in response [:headers name] (str value)))

(defn add-location-header
  [response uri id]
  (->> id
       (format "%s/%s" uri)
       (header response "Location")))

(defn created [] {:status 201})
(defn no-content [] {:status 204})
(defn not-found [] {:status 404})

(defn save-todo
  [{:keys [body-params uri]}]
  (->> (db/insert-todo body-params)
       :id
       (add-location-header (created) uri)))

(defn all-todos
  [_]
  (ok (db/get-all-todos)))

(defn delete-all-todos
  [_]
  (db/delete-all)
  (no-content))

(defn get-todo
  [{{:keys [id]} :path-params}]
  (let [result (-> id Integer. db/get-todo first)]
    (cond
      (empty? result) (not-found)
      :else (ok result))))

(defn modify-todo
  [{:keys        [body-params]
    {:keys [id]} :path-params}]
  (let [result (-> id Integer. (db/modify-todo body-params) first)]
    (cond
      (empty? result) (not-found)
      :else (ok))))

(defn delete-todo
  [{{:keys [id]} :path-params}]
  (let [result (-> id Integer. db/delete-todo first)]
    (cond
      (empty? result) (not-found)
      :else (no-content))))