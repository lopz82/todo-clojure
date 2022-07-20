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
    (is (= (handlers/created) {:status 201
                               :body   ""}))))
