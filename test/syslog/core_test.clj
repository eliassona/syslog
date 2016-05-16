(ns syslog.core-test
  (:require [criterium.core :refer [bench]])
  (:require [clojure.test :refer :all]
              [syslog.core :refer :all]
              [instaparse.core :as insta]))

;;test examples taken from rfc5424

(deftest rfc5424-compliance
  (is (= {:header {:min "14", :day "11", :hour "22", :facility 2, :month "10", :sec-frac ".003", :year "2003", :severity 4, :sec "15", :version 1, :time-offset "Z"}, :structured-data {}, :msg "BOM'su root' failed for lonvick on /dev/pts/8"} 
         (parse "<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8")))
  
  (is (= {:header {:min "14", :day "24", :hour "05", :facility 5, :month "08", :sec-frac ".000003", :year "2003", :severity 20, :sec "15", :version 1, :time-offset {:sign "-", :hours 7, :min 0}}, :structured-data {}, :msg "%% It's time to make the do-nuts."} 
         (parse "<165>1 2003-08-24T05:14:15.000003-07:00 192.0.2.1 myproc 8710 - - %% It's time to make the do-nuts.")))
  
  (is (= {:header {:min "14", :day "11", :hour "22", :facility 5, :month "10", :sec-frac ".003", :year "2003", :severity 20, :sec "15", :version 1, :time-offset "Z"}, :structured-data {"exampleSDID@32473" {"eventID" "1011", "iut" "3", "eventSource" "Application"}}, :msg "BOMAn application event log entry..."} 
         (parse "<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"] BOMAn application event log entry...")))
  
  (is (= {:header {:min "14", :day "11", :hour "22", :facility 5, :month "10", :sec-frac ".003", :year "2003", :severity 20, :sec "15", :version 1, :time-offset "Z"}, :structured-data {"exampleSDID@32473" {"eventID" "1011", "iut" "3", "eventSource" "Application"}, "examplePriority@32473" {"class" "high"}}, :msg nil}
         (parse "<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"][examplePriority@32473 class=\"high\"]")))
  
  (is (= {"exampleSDID@32473" {"iut" "3", "eventSource=\"Application\"eventID" "1011"}} 
         (ast->data (parser "[exampleSDID@32473 iut=\"3\" eventSource=\"Application\"eventID=\"1011\"]" :start :STRUCTURED-DATA))))
  
  (is (= {"exampleSDID@32473" {"iut" "3", "eventSource=\"Application\"eventID" "1011"}, "examplePriority@32473" {"class" "high"}} 
         (ast->data (parser "[exampleSDID@32473 iut=\"3\" eventSource=\"Application\"eventID=\"1011\"][examplePriority@32473 class=\"high\"]" :start :STRUCTURED-DATA))))
  
  
  (is (insta/failure? (msg->ast "[exampleSDID@32473 iut=\"3\" eventSource=\"Application\"eventID=\"1011\"] [examplePriority@32473 class=\"high\"]")))

  (is (insta/failure? (msg->ast "[exampleSDID@32473 iut=\"3\" eventSource=\"Application\"eventID=\"1011\"] [examplePriority@32473 class=\"high\"]")))
  
  (is (insta/failure? (msg->ast "[ exampleSDID@32473 iut=\"3\" eventSource=\"Application\"eventID=\"1011\"][examplePriority@32473 class=\"high\"]")))
  
  )
