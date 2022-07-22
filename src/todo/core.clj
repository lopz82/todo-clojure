(ns todo.core
  (:require [todo.db]
            [reitit.ring :as ring]
            [reitit.coercion.schema]
            [schema.core :as s]
            [reitit.ring.coercion :as rrc]
            [todo.handlers :as handlers]))

(def Todo-request
  {:todos/name s/Str
   :todos/done s/Bool})

(def Todo-response
  (assoc Todo-request :todos/id s/Int))

(def router
  (ring/router
    [["/debug" {:get identity}]
     ["/todos"
      {:post
       {:summary    "Adds a new todo"
        :handler    handlers/save-todo
        :parameters {:body Todo-request}
        :responses  {201 {:body nil}}}
       :get
       {:summary   "Retrieves all todos"
        :handler   handlers/all-todos
        :responses {200 {:body [Todo-response]}}}
       :delete
       {:summary   "Deletes all todos"
        :handler   handlers/delete-all-todos
        :responses {204 nil}}}]]
    {:data {:coercion   reitit.coercion.schema/coercion
            :middleware [rrc/coerce-exceptions-middleware
                         rrc/coerce-request-middleware
                         rrc/coerce-response-middleware]}}))

(def default-errors
  (ring/create-default-handler
    {:not-found          (constantly {:status 404, :body ""})
     :method-not-allowed (constantly {:status 405, :body ""})
     :not-acceptable     (constantly {:status 406, :body ""})}))

(def app
  (ring/ring-handler router
                     default-errors))

(defn -main
  ([port]
   (jetty/run-jetty #'app {:port  (Integer. port)
                           :join? true}))
  ([]
   (-main 3000)))
