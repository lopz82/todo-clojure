(ns todo.handlers
  (:require [todo.db :as db]))

(defn created
  []
  {:status 201
   :body   ""})

(defn ok
  ([body]
   {:status 200
    :body   body})
  ([]
   (ok "")))

(defn no-content
  []
  {:status 204})

(defn save-todo
  [{:keys [body-params]}]
  (db/insert-todo (vals body-params))
  (created))

(defn all-todos
  [_]
  (ok (db/get-all-todos)))

(defn delete-all-todos
  [_]
  (db/delete-all)
  (no-content))
