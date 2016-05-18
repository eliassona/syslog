(ns syslog.core
  (:require [clojure.core.async :refer [chan go go-loop >! <! >!! <!! timeout thread dropping-buffer]]
            [instaparse.core :as insta])
  (:import [java.net DatagramSocket DatagramPacket InetAddress]
           [java.util.concurrent TimeUnit]))

(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
     x#))
(def parser (insta/parser "https://raw.githubusercontent.com/eliassona/syslog/master/resources/rfc5424.txt" :input-format :abnf))


(defprotocol ISyslogParser
  (msg->ast [msg]))

(extend-protocol ISyslogParser
  String 
  (msg->ast [s] (parser s))
  )

(def apply-str (partial apply str))

(defn apply-str-w-nil-check [args]
  (let [s (apply str args)] 
    (if (= s "-")
      nil
      s)))


(def ast->data-map
  {:OCTET identity,
   :PRINTUSASCII identity
   :NONZERO-DIGIT identity
   :DIGIT identity
   :PRIVAL (comp read-string apply-str)
   :PRI (fn [_ pri _] [:facility (bit-and pri 7), :severity (-> pri (bit-and 0xf8) (bit-shift-right 3))])
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
   :HOSTNAME (fn [& args] [:hostname  (apply-str-w-nil-check args)])
   :PROCID (fn [& args] [:proc-id  (apply-str-w-nil-check args)])
   :APP-NAME (fn [& args] [:app-name  (apply-str-w-nil-check args)])
   :MSGID (fn [& args] [:msg-id  (apply-str-w-nil-check args)])
   :HEADER (fn [pri version _ timestamp _ hostname _ app-name _ proc-id _ msg-id] [pri version timestamp hostname app-name proc-id msg-id])
   :MSG-ANY apply-str
   :SD-NAME apply-str
   :UTF-8-STRING apply-str
   :PARAM-NAME identity
   :PARAM-VALUE identity
   :SD-PARAM (fn [n _ _ v _]  [n v])
   :SD-ID identity
   :SD-ELEMENT (fn [_ sd-id _ & params] [sd-id (apply hash-map (flatten (map first (partition 2 params))))])
   :STRUCTURED-DATA (comp (partial apply hash-map) concat) 
   :SYSLOG-MSG (fn [[pri version timestamp hostname app-name proc-id msg-id] & [_ & [sd _ msg]]] {:header (apply hash-map (concat pri version timestamp hostname app-name proc-id msg-id)) :structured-data sd, :msg msg})
   :MSG identity
   :NILVALUE (fn [_] nil)
   :TIME-NUMOFFSET (fn [sign hours _ minutes] {:sign sign, :hours (read-string hours), :min (read-string minutes)})
   })

(defn ast->data [ast]
  (insta/transform
    ast->data-map 
    ast))


(defn parse [msg]
  (-> msg msg->ast ast->data))


(defprotocol ITimeOffset
  (time-offset-in-secs-of [this]))
  
(extend-protocol ITimeOffset
  String
  (time-offset-in-secs-of [s] (if (= s "Z") 0 (throw (IllegalStateException. s))))
  java.util.Map
  (time-offset-in-secs-of [m]
    (cond 
      (contains? m :time-offset)
      (->  m :time-offset time-offset-in-secs-of)
      (= (into #{} (keys m)) #{:sign :hours :min}) 
      ((-> m :sign read-string eval) (.toSeconds (TimeUnit/MINUTES) (+ (.toMinutes (TimeUnit/HOURS) (:hours m)) (:min m))))
      :else
      (->  m :header time-offset-in-secs-of)))
  nil
  (time-offset-in-secs-of [m] (throw (IllegalStateException. "cannot contain null"))
  ))
  
  ;;tests


  ;; --------------------------------------- test code for servers and clients ----------------------------------------------
  (comment 
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
            sendData  (.getBytes "<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8")]
        (while true 
          (let [sendPacket  (DatagramPacket. sendData, (count sendData), ip-address, 9876)]
            (.send clientSocket sendPacket)
            #_(Thread/sleep 10000))))))




