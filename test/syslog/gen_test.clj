(ns syslog.gen_test
  (:use [clojure.pprint])
  (:import [syslog SyslogMessageImpl SyslogEncoder Rfc5424SyslogMessageParser ParseException])
  (:require [clojure.spec.gen :as gen]
            [clojure.spec :as s]
            [clojure.test :refer :all]
            [syslog.testspec :refer :all]
            [clojure.data :refer [diff]]
            )
  )

(defn syslog-obj-of [the-map]
  (let [{facility :syslog.testspec/facility 
         severity :syslog.testspec/severity 
         structured-data :syslog.testspec/structured-data 
         msg :syslog.testspec/msg} the-map]
  (cond 
    (contains? the-map :syslog.testspec/rfc-5424-version)
    (SyslogMessageImpl. 
      facility 
      severity
      (:syslog.testspec/rfc-5424-version the-map) 
      (:syslog.testspec/rfc-5424-timestamp the-map)
      (:syslog.testspec/rfc-5424-host-name the-map)
      (:syslog.testspec/rfc-5424-app-name the-map)
      (:syslog.testspec/rfc-5424-proc-id the-map)
      (:syslog.testspec/rfc-5424-msg-id the-map)
      structured-data
      msg
      nil
      nil)
    (contains? the-map :syslog.testspec/rfc-3164-version)
    (SyslogMessageImpl. 
      facility 
      severity
      (:syslog.testspec/rfc-3164-version the-map) 
      (:syslog.testspec/rfc-3164-timestamp the-map)
      (:syslog.testspec/rfc-5424-host-name the-map)
      nil
      nil
      nil
      nil
      msg
      nil
      nil))))



(defn map-of [o]
  (if (= (.getVersion o) 1)
    {:syslog.testspec/facility (.getFacility o)  
     :syslog.testspec/severity (.getSeverity o)
     :syslog.testspec/rfc-5424-version (.getVersion o)
     :syslog.testspec/rfc-5424-timestamp (.getTimestamp o)
     :syslog.testspec/rfc-5424-host-name (.getHostName o)
     :syslog.testspec/rfc-5424-app-name (.getAppName o)
     :syslog.testspec/rfc-5424-proc-id (.getProcId o)
     :syslog.testspec/rfc-5424-msg-id (.getMsgId o)
     :syslog.testspec/structured-data (.getStructuredData o)
     :syslog.testspec/msg (.getMsg o)}
    {:syslog.testspec/facility (.getFacility o)  
     :syslog.testspec/severity (.getSeverity o)
     :syslog.testspec/rfc-3164-version (.getVersion o)
     :syslog.testspec/rfc-3164-timestamp (.getTimestamp o)
     :syslog.testspec/host-name (.getHostName o)
     :syslog.testspec/msg (.getMsg o)}))

(def errors (atom []))
(defn do-parse [rfc-spec n]
  (dotimes [i n]
    (try 
      (let [expected (-> rfc-spec s/gen gen/generate)
            encoded (-> expected syslog-obj-of SyslogEncoder/encode)]
         (try 
           (let 
              [actual (-> encoded Rfc5424SyslogMessageParser/parse map-of)
              res (= expected actual)]
		       (when (not res)
		         (swap! errors conj {:expected expected, :actual actual, :encoded encoded, :diff (diff expected actual)}))
         (is res))
           (catch ParseException e 
		         (swap! errors conj {:expected expected, :encoded encoded})
           )))
      (catch clojure.lang.ExceptionInfo e
        :do-nothing)))
  errors)

(deftest test-parse
  (do-parse 100000))

(defn test-sub-sd [e]
  (let [buf (StringBuilder.)]
    (SyslogEncoder/addToSdStr (key e) (val e) buf)
    (.toString buf)))
  

(defn test-sd [m]
  (map test-sub-sd (-> m :syslog.testspec/structured-data)))

