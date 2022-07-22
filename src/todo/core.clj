(ns todo.core
  (:require [muuntaja.core :as m]
            [reitit.coercion.schema]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as rrc]
            [reitit.ring.middleware.muuntaja :as rrmm]
            [ring.adapter.jetty :as jetty]
            [schema.core :as s]
            [todo.db]
            [todo.handlers :as handlers]))

(def Todo-request
  {:name s/Str
   :done s/Bool})

(def Todo-response
  (assoc Todo-request :id s/Int))

(def Todo-patch
  {(s/optional-key :name) s/Str
   (s/optional-key :done) s/Bool})

(def router
  (ring/router
    [["/debug" {:get identity}]
     ["/debug/:id" {:get identity}]
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
        :responses {204 nil}}}]
     ["/todos/:id" {:parameters {:path {:id s/Int}}
                    :get
                    {:summary   "Get info about one task"
                     :handler   handlers/get-todo
                     :responses {200 {:body Todo-response}}}
                    :put
                    {:summary    "Replace one task"
                     :handler    handlers/modify-todo
                     :parameters {:body Todo-request}
                     :responses  {200 {:body Todo-response}}}
                    :patch
                    {:summary "Updates one task"
                     :handler handlers/modify-todo
                     :parameters {:body Todo-patch}
                     :responses {200 {:body Todo-response}}}
                    :delete
                    {:summary "Deletes one task"
                     :handler handlers/delete-todo
                     :parameters {:body nil}
                     :responses {204 nil}}}]]
    {:data {:muuntaja   m/instance
            :coercion   reitit.coercion.schema/coercion
            :middleware [;; rrmm/format-middleware             ;; Remove to get data instead of a stream when working in the REPL
                         rrc/coerce-exceptions-middleware
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
