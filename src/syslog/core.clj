(ns syslog.core
  (:require [clojure.core.async :refer [chan go go-loop >! <! >!! <!! timeout thread dropping-buffer]]
            [instaparse.core :as insta])
  (use [criterium.core])
  (:import [java.net DatagramSocket DatagramPacket InetAddress]))


(def parser (insta/parser "resources/rfc5424.txt" :input-format :abnf))


(defprotocol ISyslogParser
  (parse [msg]))

(extend-protocol ISyslogParser
  String 
  (parse [s] (parser s))
  )

(def apply-str (partial apply str))

(def ast->data-map
  {:OCTET identity,
   :PRINTUSASCII identity
   :NONZERO-DIGIT identity
   :DIGIT identity
   :PRIVAL (fn [& args] args)
   :PRI (fn [_ pri _] [:pri (map read-string pri)])
   :VERSION (fn [v] [:version (read-string v)])
   :DATE-FULLYEAR apply-str
   :DATE-MONTH apply-str
   :DATE-MDAY apply-str
   :FULL-DATE (fn [year _ month _ day] [:year year, :month month, :day day])
   :TIME-HOUR apply-str
   :TIME-MINUTE apply-str
   :TIME-SECOND apply-str
   :TIME-SECFRAC apply-str
   :TIME-OFFSET (fn [z] [:time-offset z])
   :FULL-TIME (fn [t to] (concat t to))
   :TIMESTAMP (fn [d _ t] (concat d t))
   :PARTIAL-TIME (fn [hour _ min _ sec sec-frac] [:hour hour, :min min, :sec sec, :sec-frac sec-frac])
   :HEADER (fn [pri version _ timestamp hostname & args] [pri version timestamp hostname])
   :MSG-ANY apply-str
   :SD-NAME apply-str
   :UTF-8-STRING apply-str
   :PARAM-NAME identity
   :PARAM-VALUE identity
   :SD-PARAM (fn [n _ _ v _]  [n v])
   :SD-ID identity
   :SD-ELEMENT (fn [_ sd-id _ & params] [sd-id (apply hash-map (flatten (map first (partition 2 params))))])
   :STRUCTURED-DATA (partial apply hash-map) 
   :SYSLOG-MSG (fn [[pri version timestamp] & [_ & [sd _ msg]]] {:header (apply hash-map (concat pri version timestamp)) :structured-data sd, :msg msg})
   :MSG identity
   })

(defn ast->data [ast]
  (insta/transform
    ast->data-map 
    ast))


;;tests


;;example message
(def rfc5424-msg 
  "<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"] BOMAn application event log entry...")



;; --------------------------------------- test code for servers and clients ----------------------------------------------
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
        sendData  (.getBytes rfc5424-msg)]
    (while true 
      (let [sendPacket  (DatagramPacket. sendData, (count sendData), ip-address, 9876)]
        (.send clientSocket sendPacket)
        (Thread/sleep 10000)))))




