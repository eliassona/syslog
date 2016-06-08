(ns syslog.core-test
  (:require [criterium.core :refer [bench]])
  (:require [clojure.test :refer :all]
              [syslog.core :refer :all]
              [instaparse.core :as insta]
              [clojure.core.async :refer [chan go go-loop >! <! >!! <!! timeout thread dropping-buffer close!]])
  (:import [java.net DatagramSocket DatagramPacket InetAddress]
           [java.util.concurrent TimeUnit]))

;;test examples taken from rfc5424

(deftest rfc5424-compliance
  (let 
    [r (parse "<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8")]
     (is (= {:header {:min "14", :day "11", :proc-id nil, :hour "22", :msg-id "ID47", :facility 2, :month "10", :sec-frac ".003", :app-name "su", :hostname "mymachine.example.com", :year "2003", :severity 4, :sec "15", :version 1, :time-offset "Z"}, :structured-data {}, :msg "BOM'su root' failed for lonvick on /dev/pts/8"} 
            r))
     (is (= 0 (time-offset-in-secs-of r))))
  
  (let [r (parse "<165>1 2003-08-24T05:14:15.000003-07:00 192.0.2.1 myproc 8710 - - %% It's time to make the do-nuts.")]
    (is (= {:header {:min "14", :day "24", :proc-id "8710", :hour "05", :msg-id nil, :facility 5, :month "08", :sec-frac ".000003", :app-name "myproc", :hostname "192.0.2.1", :year "2003", :severity 20, :sec "15", :version 1, :time-offset {:sign "-", :hours 7, :min 0}}, :structured-data {}, :msg "%% It's time to make the do-nuts."} 
           r))
    (is (= -25200 (time-offset-in-secs-of r))))
  
  (is (= {:header {:min "14", :day "11", :proc-id nil, :hour "22", :msg-id "ID47", :facility 5, :month "10", :sec-frac ".003", :app-name "evntslog", :hostname "mymachine.example.com", :year "2003", :severity 20, :sec "15", :version 1, :time-offset "Z"}, :structured-data {"exampleSDID@32473" {"eventID" "1011", "iut" "3", "eventSource" "Application"}}, :msg "BOMAn application event log entry..."} 
         (parse "<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"] BOMAn application event log entry...")))
  
  (is (= {:header {:min "14", :day "11", :proc-id nil, :hour "22", :msg-id "ID47", :facility 5, :month "10", :sec-frac ".003", :app-name "evntslog", :hostname "mymachine.example.com", :year "2003", :severity 20, :sec "15", :version 1, :time-offset "Z"}, :structured-data {"exampleSDID@32473" {"eventID" "1011", "iut" "3", "eventSource" "Application"}, "examplePriority@32473" {"class" "high"}}, :msg nil}
         (parse "<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"][examplePriority@32473 class=\"high\"]")))
  
  (is (= {"exampleSDID@32473" {"iut" "3", "eventSource=\"Application\"eventID" "1011"}} 
         (ast->data (parser "[exampleSDID@32473 iut=\"3\" eventSource=\"Application\"eventID=\"1011\"]" :start :STRUCTURED-DATA))))
  
  (is (= {"exampleSDID@32473" {"iut" "3", "eventSource=\"Application\"eventID" "1011"}, "examplePriority@32473" {"class" "high"}} 
         (ast->data (parser "[exampleSDID@32473 iut=\"3\" eventSource=\"Application\"eventID=\"1011\"][examplePriority@32473 class=\"high\"]" :start :STRUCTURED-DATA))))
  
  
  (is (insta/failure? (msg->ast "[exampleSDID@32473 iut=\"3\" eventSource=\"Application\"eventID=\"1011\"] [examplePriority@32473 class=\"high\"]")))

  (is (insta/failure? (msg->ast "[exampleSDID@32473 iut=\"3\" eventSource=\"Application\"eventID=\"1011\"] [examplePriority@32473 class=\"high\"]")))
  
  (is (insta/failure? (msg->ast "[ exampleSDID@32473 iut=\"3\" eventSource=\"Application\"eventID=\"1011\"][examplePriority@32473 class=\"high\"]")))
  
  )




 ;; --------------------------------------- test code for servers and clients ----------------------------------------------
#_(defn server [] 
     (let [serverSocket (DatagramSocket. 9876)
           receiveData  (byte-array 1024)]
        (while true
            (let [receivePacket (DatagramPacket. receiveData (count receiveData))]
              (.receive serverSocket receivePacket)
              (-> receivePacket .getData String. parse println)))))

(use 'pacer.core)

(defn client [c]
  (let [clientSocket (DatagramSocket.)
        ip-address (InetAddress/getByName "localhost")]
    (go-loop 
      [n 0]
      (if-let [s (<! c)]
        (let [data (.getBytes s)]
          (.send clientSocket (DatagramPacket. data, (count data), ip-address, 9876))
          (recur (inc n)))
        (do
          (println (format "Nr of packets sent %s" n))
          (println "closing socket")
          (.close clientSocket))))))


(defn client-direct []
  (let [run (atom true)
        clientSocket (DatagramSocket.)
        ip-address (InetAddress/getByName "localhost")
;        data (.getBytes "<34>Oct 11 22:14:15 mymachine su: 'su root' failed for lonvick on /dev/pts/8")
;        data (.getBytes "<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"] BOMAn application event log entry...")
;        data (.getBytes "<11>1 2003-10-11T22:14:15.003Z mymachine.example.com myappname procid17 ID47 - A message")
        ;data (.getBytes "<11>1 x x x x")
        data (.getBytes "<165>1 2009-11-12T21:35:53.45-08:00 hulahoop-macbook-pro.local BANKING - Transfer [Hula@12293 Amount=\"55.00\" FromAccount=\"12345601\" ToAccount=\"12345602\"][Hoop@12293 timezone=\"America/Los_Angeles\" bcId=\"DI4448\" sessionId=\"TestUser\" userId=\"User1\" hostName=\"MyHost\" companyId=\"Company1\" userProduct=\"Banking\" ipAddress=\"10.200.10.5\"] Transfer successful")
        ]
    (go-loop
      [n 0]
      (if @run
         (do 
           (.send clientSocket (DatagramPacket. data (count data) ip-address, 9876))
           (recur (inc n)))
         (println n)
         ))
    run))

(comment
  (def c (chan))
  (def p 
    (pacer 
      100000 
      1E6 
      (fn [v] "<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8") c))
  (client c)
  #_(set-tps! 100000 p)
  )


"<165>1 2009-11-12T21:35:53.45-08:00 hulahoop-macbook-pro.local BANKING - Transfer [Hula@12293 Amount=\"55.00\" FromAccount=\"12345601\" ToAccount=\"12345602\"][Hoop@12293 timezone=\"America/Los_Angeles\" bcId=\"DI4448\" sessionId=\"TestUser\" userId=\"User1\" hostName=\"MyHost\" companyId=\"Company1\" userProduct=\"Banking\" ipAddress=\"10.200.10.5\"] Transfer successful"