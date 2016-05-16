(ns syslog.core
  (:require [clojure.core.async :refer [chan go go-loop >! <! >!! <!! timeout thread dropping-buffer]]
            [instaparse.core :as insta])
  (:import [java.net DatagramSocket DatagramPacket InetAddress]))


(def parser (insta/parser "/Users/anderse/source/syslog/etc/rfc5424.txt" :input-format :abnf))


(def rfc5424-msg 
  "<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"] BOMAn application event log entry...")

(defprotocol ISyslogParser
  (parse [msg]))

(extend-protocol ISyslogParser
  String 
  (parse [s] (parser s))
  )

(defn syslog [log-fn]
  (let [sl (SyslogServer/getInstance "udp")
        t (Thread. sl)]
    (-> sl .getConfig (.addEventHandler (handler-of log-fn)))
    (.setThread sl t)
    (.start t)
    sl
    ))
  


(defn server [] 
   (let [serverSocket (DatagramSocket. 9876)
         receiveData  (byte-array 1024)]
      (while true
          (let [receivePacket (DatagramPacket. receiveData (count receiveData))]
            (.receive serverSocket receivePacket)
            (-> receivePacket .getData String. parse println)))))



(defn client []
  (let [clientSocket (DatagramSocket.)
        ip-address (InetAddress/getByName "localhost")
        sendData  (.getBytes sample-msg)]
    (while true 
      (let [sendPacket  (DatagramPacket. sendData, (count sendData), ip-address, 9876)]
        (.send clientSocket sendPacket)
        (Thread/sleep 10000)))))




  

