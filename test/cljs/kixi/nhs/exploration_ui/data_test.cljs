(ns kixi.nhs.exploration-ui.data-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [kixi.nhs.exploration-ui.data :as data]))

(deftest s->timestamp-test
  (testing "Testing parsing of dates."
    (is (= (new js/Date "2013")
           (data/s->timestamps "2013-14" "\\d{4}-\\d{2}")))
    (is (= (new js/Date "2013")
           (data/s->timestamps "2013/14" "\\d{4}\\/\\d{2}")))
    (is (= (new js/Date "2013")
           (data/s->timestamps "2013.0" "\\d{4}\\.\\d")))
    (is (= (new js/Date "2013")
           (data/s->timestamps "2013" "\\d{4}")))
    (is (= (new js/Date "2013/10/01")
           (data/s->timestamps "Oct - Dec 2013" "\\w* - \\w* \\d{4}")))
    (is (= (new js/Date "1/4/2013")
           (data/s->timestamps "1/4/2013 to 31/3/2014" "\\d{1,2}\\/\\d{1,2}\\/\\d{4} to \\d{1,2}\\/\\d{1,2}\\/\\d{4}")))
    (is (= (new js/Date "2014/1/1")
           (data/s->timestamps "January 2014 to February 2014" "\\w* \\d{4} to \\w* \\d{4}")))))

(deftest parse-date-test
  (testing "Testing parse-date."
    (is (= (new js/Date "2013")
           (data/parse-date "2013-14")))
    (is (= (new js/Date "2013")
           (data/parse-date "2013/14")))
    (is (= (new js/Date "2013")
           (data/parse-date "2013.0")))
    (is (= (new js/Date "2013")
           (data/parse-date "2013")))
    (is (= (new js/Date "2013/10/01")
           (data/parse-date "Oct - Dec 2013")))
    (is (= (new js/Date "1/4/2013")
           (data/parse-date "1/4/2013 to 31/3/2014")))
    (is (= (new js/Date "2014/1/1")
           (data/parse-date "January 2014 to February 2014")))))
