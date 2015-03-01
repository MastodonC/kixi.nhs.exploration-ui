(ns test-runner
  (:require [cljs.test :refer-macros [run-tests]]
            [kixi.nhs.exploration-ui.data-test]))

(enable-console-print!)

(defn runner []
  (println "Runner starts")
  (if (cljs.test/successful?
        (run-tests
         'kixi.nhs.exploration-ui.data-test))
    0
    1))
