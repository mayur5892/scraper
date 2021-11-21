(defproject scraper "0.1.0-SNAPSHOT"
  :description "scrapper engine"
  :main scraper.core
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/data.csv "1.0.0"]
                 [http-kit "2.5.3"]
                 [cheshire "5.10.1"]]
  :repl-options {:init-ns scraper.core})
