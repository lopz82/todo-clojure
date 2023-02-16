(ns todo.handlers
  (:require [todo.db :as db]))


(defn header
  "Adds a header to the response."
  [response name value]
  (assoc-in response [:headers name] (str value)))

(defn add-location-header
  "Adds a location header to the response using the base uri."
  [response uri id]
  (header response "Location" (format "%s/%s" uri id)))

(defn ok [body] {:status 200 :body body})
(defn created [_] {:status 201})
(defn no-content [_] {:status 204})
(defn not-found [_] {:status 404})

(defn save-todo
  [{:keys [body-params uri]}]
  (->> (db/insert-todo body-params)
       :id
       (add-location-header (created "") uri)))

(defn all-todos [_] (ok (db/get-all-todos)))

(defn delete-all-todos
  [_]
  (db/delete-all)
  (no-content ""))

(defn check-db-result
  "Checks if the result of the query has been added to the request map
  and responds according its successful result or failure."
  [request success fail]
  (let [result (:db request)]
    (cond
      (empty? result) (fail result)
      :else (success result))))

(defn ok-or-not-found [request] (check-db-result request ok not-found))
(defn not-content-or-not-found [request] (check-db-result request no-content not-found))

(defn database-query-middleware [handler op ks]
  (fn [request]
    (let [values (vec (map #(get-in request %) ks))]
      (handler (assoc request :db (apply op values))))))
