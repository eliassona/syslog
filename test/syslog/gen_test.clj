(ns syslog.gen_test
  (:use [clojure.pprint])
  (:import [syslog SyslogMessageImpl SyslogEncoder Rfc5424SyslogMessageParser ParseException])
  (:require [clojure.spec.gen :as gen]
            [clojure.spec :as s]
            [clojure.test :refer :all]
            [syslog.syslogspec :refer :all]
            [clojure.data :refer [diff]]
            )
  )

(defn syslog-obj-of [the-map]
  (let [{facility :syslog.syslogspec/facility 
         severity :syslog.syslogspec/severity 
         structured-data :syslog.syslogspec/structured-data 
         msg :syslog.syslogspec/msg} the-map]
  (cond 
    (contains? the-map :syslog.syslogspec/rfc-5424-version)
    (SyslogMessageImpl. 
      facility 
      severity
      (:syslog.syslogspec/rfc-5424-version the-map) 
      (:syslog.syslogspec/rfc-5424-timestamp the-map)
      (:syslog.syslogspec/rfc-5424-host-name the-map)
      (:syslog.syslogspec/rfc-5424-app-name the-map)
      (:syslog.syslogspec/rfc-5424-proc-id the-map)
      (:syslog.syslogspec/rfc-5424-msg-id the-map)
      structured-data
      msg
      nil
      nil)
    (contains? the-map :syslog.syslogspec/rfc-3164-version)
    (SyslogMessageImpl. 
      facility 
      severity
      (:syslog.syslogspec/rfc-3164-version the-map) 
      (:syslog.syslogspec/rfc-3164-timestamp the-map)
      (:syslog.syslogspec/rfc-3164-host-name the-map)
      nil
      nil
      nil
      nil
      msg
      nil
      nil))))



(defn map-of [o]
  (if (= (.getVersion o) 1)
    {:syslog.syslogspec/facility (.getFacility o)  
     :syslog.syslogspec/severity (.getSeverity o)
     :syslog.syslogspec/rfc-5424-version (.getVersion o)
     :syslog.syslogspec/rfc-5424-timestamp (.getTimestamp o)
     :syslog.syslogspec/rfc-5424-host-name (.getHostName o)
     :syslog.syslogspec/rfc-5424-app-name (.getAppName o)
     :syslog.syslogspec/rfc-5424-proc-id (.getProcId o)
     :syslog.syslogspec/rfc-5424-msg-id (.getMsgId o)
     :syslog.syslogspec/structured-data (.getStructuredData o)
     :syslog.syslogspec/msg (.getMsg o)}
    {:syslog.syslogspec/facility (.getFacility o)  
     :syslog.syslogspec/severity (.getSeverity o)
     :syslog.syslogspec/rfc-3164-version (.getVersion o)
     :syslog.syslogspec/rfc-3164-timestamp (.getTimestamp o)
     :syslog.syslogspec/rfc-3164-host-name (.getHostName o)
     :syslog.syslogspec/msg (.getMsg o)}))

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
		         (swap! errors conj {:expected expected, :encoded encoded, :exception e})
           )))
      (catch clojure.lang.ExceptionInfo e
        :do-nothing)))
  errors)

;TODO run it as test.check test
(deftest test-parse
  (do-parse :syslog.syslogspec/syslog-msg 1000))

