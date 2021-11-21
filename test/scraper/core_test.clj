(ns scraper.core-test
  (:require [clojure.test :refer :all]
            [scraper.core :refer :all]))


(defn- prepare-test-response [n price]
  (hash-map :items (for [i (range n)]
                     {:item_basic {:itemid i
                                   :name (str "name" i)
                                   :price price}})))

(deftest transform-response-test
  (testing "successfully transform the input"
    (let [test-data (prepare-test-response 1 9500000)
          expected-response (list [0 "name0" 95.0 1 2])
          actual-response (transform-response test-data {"newest" 60})]
      (is (= expected-response actual-response))))

  (let [test-data (prepare-test-response 2 9500000)
        expected-response (list [0 "name0" 95.0 1 2] [1 "name1" 95.0 2 2])
        actual-response (transform-response test-data {"newest" 60})]
    (is (= expected-response actual-response))))


