(ns todo.handlers-test
  (:require [clojure.test :refer :all]
            [todo.handlers :as handlers]))

(deftest header
  (testing "adds header to a response"
    (is (= (handlers/header {:body {:content "response"} :status 200} "Test-Header" "Test value")
           {:body {:content "response"} :status 200 :headers {"Test-Header" "Test value"}}))))

(deftest add-location-header
  (testing "adds a location header to a response"
    (is (= (handlers/add-location-header {:body {:content "response"} :status 200} "fake/uri" 42)
           {:body {:content "response"} :status 200 :headers {"Location" "fake/uri/42"}}))
    (is (= (handlers/add-location-header {:body {:content "response"} :status 200 :headers {"Existing" "Header"}} "fake/uri" 42)
           {:body {:content "response"} :status 200 :headers {"Existing" "Header" "Location" "fake/uri/42"}}))))

(deftest ok
  (testing "Ok returns 200 and honors the body"
    (is (= (handlers/ok "OK")
           {:status 200 :body "OK"}))))
(deftest created
  (testing "created returns 201 with empty body"
    (is (= (handlers/created "")
           {:status 201}))))

(deftest no-content
  (testing "created returns 204 with empty body"
    (is (= (handlers/no-content "")
           {:status 204}))))

(deftest not-found
  (testing "not-found returns 404 with empty body"
    (is (= (handlers/not-found "")
           {:status 404}))))

(deftest check-db-result
  (testing "if database query is empty returns failure response"
    (is (= (handlers/check-db-result {:db []} handlers/ok handlers/not-found)
           {:status 404}))
    (is (= (handlers/check-db-result {:uri "fake-uri"} handlers/ok handlers/not-found)
           {:status 404})))
  (testing "if database query is contains a result returns success response"
    (is (= (handlers/check-db-result {:db {:id 1}} handlers/ok handlers/not-found)
           {:status 200 :body {:id 1}}))))

(deftest ok-or-not-found
  (testing "returns ok if the result is not empty"
    (is (= (handlers/ok-or-not-found {:db {:some "data"}})
           {:status 200 :body {:some "data"}})))
  (testing "returns 404 if the result is empty"
    (is (= (handlers/ok-or-not-found {:db []})
           {:status 404}))
    (is (= (handlers/ok-or-not-found {:db nil})
           {:status 404}))))

(deftest not-content-or-not-found
  (testing "returns ok if the result is not empty"
    (is (= (handlers/not-content-or-not-found {:db {:some "data"}})
           {:status 204})))
  (testing "returns 404 if the result is empty"
    (is (= (handlers/not-content-or-not-found {:db []})
           {:status 404}))
    (is (= (handlers/not-content-or-not-found {:db nil})
           {:status 404}))))

(defn mock-operation [& values] {:values values})

(deftest database-query-middleware
  (let [body {:body-params {:value 2} :id 1}]
    (testing "nested keys"
      (let [k [[:id] [:body-params :value]]
            handler (handlers/database-query-middleware handlers/ok-or-not-found mock-operation k)]
        (is (= (handler body)
               {:status 200 :body {:values '(1 2)}}))))
    (testing "single key"
      (let [k [[:id]]
            handler (handlers/database-query-middleware handlers/ok-or-not-found mock-operation k)]
        (is (= (handler body)
               {:status 200 :body {:values '(1)}}))))))
