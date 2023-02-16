(ns todo.db
  (:require [honey.sql :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(def db {:dbtype "postgres" :dbname "todos" :host "localhost" :port 65432 :user "postgres" :password "pass"})
(def ds (jdbc/get-datasource db))

(def query-options {:return-keys true :builder-fn rs/as-unqualified-lower-maps})

(defn query
  "Helper function to execute the SQL statement."
  [q]
  (jdbc/execute! ds q query-options))

(defn query-one
  "Helper function to execute one SQL statement."
  [q]
  (jdbc/execute-one! ds q query-options))

(defn get-todo
  "Returns a todo depending on the column and value specified. Defaults to id column."
  ([col val]
   (query-one (sql/format {:select [:*], :from [:todos], :where [:= col (Integer/parseInt val)]})))
  ([val]
   (query-one (sql/format {:select [:*], :from [:todos], :where [:= :id (Integer/parseInt val)]}))))

(defn get-all-todos
  "Returns all todos from the table."
  []
  (query (sql/format {:select [:*], :from [:todos]})))

(defn insert-todo
  "Inserts a new todo."
  [data]
  (query-one (sql/format {:insert-into :todos :columns (keys data) :values [(vals data)] :returning [:id]})))

(defn delete-todo
  "Deletes one todo."
  [id]
  (query-one (sql/format {:delete-from [:todos] :where [:= :id (Integer/parseInt id)] :returning [:*]})))

(defn delete-all
  "Deletes all todos."
  []
  (query (sql/format {:delete-from [:todos]})))

(defn modify-todo
  "Modifies a todo."
  [id new-vals]
  (query-one (sql/format {:update :todos :set new-vals :where [:= :id (Integer/parseInt id)] :returning [:*]})))