(ns todo.handlers-test
  (:require [clojure.test :refer :all]
            [todo.handlers :as handlers]))

(deftest ok
  (testing "Ok returns 200 and honors the body"
    (is (= (handlers/ok) {:status 200
                          :body   ""}))
    (is (= (handlers/ok "OK") {:status 200
                               :body   "OK"}))))
(deftest created
  (testing "created returns 201 with empty body"
    (is (= (handlers/created "") {:status 201}))))

(deftest not-found
  (testing "not-found returns 404 with empty body"
    (is (= (handlers/not-found "") {:status 404}))))

(deftest ok-or-not-found
  (testing "returns ok if the result is not empty"
    (is (= (handlers/ok-or-not-found {:db {:some "data"}}) {:status 200 :body {:some "data"}})))
  (testing "returns 404 if the result is empty"
    (is (= (handlers/ok-or-not-found {:db []}) {:status 404}))
    (is (= (handlers/ok-or-not-found {:db nil}) {:status 404}))))

(deftest not-content-or-not-found
  (testing "returns ok if the result is not empty"
    (is (= (handlers/not-content-or-not-found {:db {:some "data"}}) {:status 204})))
  (testing "returns 404 if the result is empty"
    (is (= (handlers/not-content-or-not-found {:db []}) {:status 404}))
    (is (= (handlers/not-content-or-not-found {:db nil}) {:status 404}))))

(defn mock-operation [& values] {:values values})

(deftest database-query-middleware
  (let [body {:body-params {:value 2} :id 1}]
    (testing "nested keys"
      (let [k [[:id] [:body-params :value]]
            handler (handlers/database-query-middleware handlers/ok-or-not-found mock-operation k)]
        (is (= (handler body) {:status 200 :body {:values '(1 2)}}))))
    (testing "single key"
      (let [k [[:id]]
            handler (handlers/database-query-middleware handlers/ok-or-not-found mock-operation k)]
        (is (= (handler body) {:status 200 :body {:values '(1)}}))))))
