(ns scraper.core
  (:require
    [clojure.data.csv :as csv]
    [clojure.java.io :as io]
    [org.httpkit.client :as http]
    [cheshire.core :as json]))

(def input-csv-location  "input-url.csv")
(def output-csv-location  "scrapped-data.csv")
(def csv-headers ["Item-id" "name" "price" "Order-in-page" "Page-number"])
(def api-base-url "https://shopee.sg/api/v4/search/search_items")
(def query-template {"by" "relevancy"
                     "limit" 60
                     "order" "desc"
                     "page_type" "search"
                     "scenario" "PAGE_OTHERS"
                     "version" 2
                     })

(defn- category-id-from-url [url]
  (last (clojure.string/split url #"\.")))

(defn- invoke-api [query-params]
  (let [{:keys [body status]}
        @(http/request
          {:url api-base-url
           :method :get
           :query-params query-params
           :throw-exceptions false})]
    (when (= status 200)
      (json/parse-string body keyword))))

(defn transform-response [response query-params]
  (let [page-number (inc (/ (query-params "newest") 60))]
    (->> response
      :items
      (map :item_basic)
      (map #(select-keys % [:itemid :name :price]))
      (map-indexed (fn [item-index item]
                     (-> (update item :price
                           #(double (/ % 100000)))
                       (assoc :order-in-page (inc item-index)))))
      (map (fn [{:keys [itemid name price order-in-page]}]
              [itemid name price order-in-page page-number])))))


(defn- scrap-url [url]
  (let [category-id (category-id-from-url url)
        request-queries (for [i (range 3)]
                         (merge query-template
                           {"match_id" category-id "newest" (* i 60)}))]
    (mapcat
      (fn [query-params]
        (when-let [response-body (invoke-api query-params)]
          (transform-response response-body query-params)))
      request-queries)))

(defn- sanitize [input]
  (->> input
    rest
    (map first)))

(defn- start-scarping-engine [sanitized-input]
  (->> sanitized-input
    (pmap scrap-url)
    (apply concat)))

(defn- write-to-csv [writer data]
  (->> (cons csv-headers data)
    vec
    (csv/write-csv writer )))


(defn- start-scarping []
  (with-open [reader (io/reader input-csv-location)
              writer (io/writer output-csv-location)]
    (->> (csv/read-csv reader)
      sanitize
      start-scarping-engine
      (write-to-csv writer))))

(defn -main []
  (start-scarping))

