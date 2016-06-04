(ns syslog.gen_test
  (:import [syslog SyslogMessageImpl SyslogEncoder Rfc5424SyslogMessageParser])
  (:require [clojure.spec.gen :as gen]
            [clojure.spec :as s]
            [clojure.test :refer :all]
            [syslog.testspec :refer :all]
            )
  )
(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
     x#))
(defn syslog-obj-of [{facility :syslog.testspec/facility 
                      severity :syslog.testspec/severity 
                      version :syslog.testspec/rfc-5424-version
                      timestamp :syslog.testspec/rfc-5424-timestamp
                      host-name :syslog.testspec/host-name 
                      app-name :syslog.testspec/rfc-5424-app-name 
                      proc-id :syslog.testspec/rfc-5424-proc-id 
                      msg-id :syslog.testspec/rfc-5424-msg-id 
                      structured-data :syslog.testspec/structured-data 
                      msg :syslog.testspec/msg}]
  (SyslogMessageImpl. 
    facility 
    severity
    version 
    timestamp
    host-name
    app-name
    proc-id
    msg-id
    structured-data
    msg
    nil
    nil)
    
  )

(defn map-of [o]
  {:syslog.testspec/facility (.getFacility o)  
   :syslog.testspec/severity (.getSeverity o)
   :syslog.testspec/rfc-5424-version (.getVersion o)
   :syslog.testspec/rfc-5424-timestamp (.getTimestamp o)
   :syslog.testspec/host-name (.getHostName o)
   :syslog.testspec/rfc-5424-app-name (.getAppName o)
   :syslog.testspec/rfc-5424-proc-id (.getProcId o)
   :syslog.testspec/rfc-5424-msg-id (.getMsgId o)
   :syslog.testspec/structured-data (.getStructuredData o)
   :syslog.testspec/msg (.getMsg o)})

(def errors (atom []))
(defn do-parse [n]
  (dotimes [i n]
    (try 
      (let [expected (-> :syslog.testspec/rfc-5424-syslog-msg s/gen gen/generate)
            actual (-> expected syslog-obj-of SyslogEncoder/encode Rfc5424SyslogMessageParser/parse map-of)
            res (= expected actual)]
        (when (not res)
          (swap! errors conj {:expected expected, :actual actual}))
        (is res))
      (catch clojure.lang.ExceptionInfo e
        :do-nothing)))
  errors)

(deftest test-parse
  (do-parse 10000))
