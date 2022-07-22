(ns todo.db
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]))

(def db {:dbtype "postgres" :dbname "todos" :host "localhost" :port 65432 :user "postgres" :password "pass"})
(def ds (jdbc/get-datasource db))

(defn query
  "Helper function to execute the SQL statement."
  [q]
  (jdbc/execute! ds q))

(defn get-todo
  "Returns a todo depending on the column and value specified. Defaults to id column."
  ([col val]
   (query (sql/format {:select [:*], :from [:todos], :where [:= col val]})))
  ([val]
   (query (sql/format {:select [:*], :from [:todos], :where [:= :id val]}))))

(defn get-all-todos
  "Returns all todos from the table."
  []
  (query (sql/format {:select [:*], :from [:todos]})))

(defn insert-todo
  "Inserts a new todo."
  [vals]
  (query (sql/format {:insert-into :todos :columns [:name :done] :values [vals]})))

(defn delete-todo
  "Deletes one todo."
  [id]
  (query (sql/format {:delete-from [:todos] :where [:= :id id]})))

(defn delete-all
  "Deletes all todos."
  []
  (query (sql/format {:delete-from [:todos]})))

(defn modify-todo
  "Modifies a todo."
  [id new-vals]
  (query (sql/format {:update :todos :set new-vals :where [:= :id id] :returning [:*]})))