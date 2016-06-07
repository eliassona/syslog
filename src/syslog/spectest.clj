(ns syslog.testspec
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen])
  )
(defn not-empty? [s] (not (empty? s)))
(defn no-space? [s] (< (.indexOf s " ") 0))

(s/def ::str-or-nil (s/or :nil nil? :str string?))

(s/def ::str-no-space (s/and string? no-space?))

(s/def ::str-no-space-or-nil (s/or :nil nil? :str ::str-no-space))

(s/def ::facility (s/and integer? #(>= % 0) #(<= % 23)))
(s/def ::severity (s/and integer? #(>= % 0) #(<= % 7)))
(s/def ::rfc-5424-version (s/and integer? #(= % 1)))
(s/def ::rfc-3164-version (s/and integer? zero?))
(s/def ::version (s/or :rfc-5424 ::rfc-5424-version :rfc-3164 ::rfc-3164-version)) 
(s/def ::rfc-5424-timestamp ::str-no-space)
(s/def ::rfc-3164-timestamp (s/and string? #(= (.length %) 15)))
(s/def ::timestamp (s/or :rfc-5424 ::rfc-5424-timestamp :rfc-3164 ::rfc-3164-timestamp))
(s/def ::host-name ::str-no-space-or-nil)
(s/def ::rfc-5424-app-name ::str-no-space-or-nil) 
(s/def ::rfc-5424-proc-id ::str-no-space-or-nil)
(s/def ::rfc-5424-msg-id ::str-no-space-or-nil)
(s/def ::rfc-3164-app-name nil?) 
(s/def ::rfc-3164-proc-id nil?)
(s/def ::rfc-3164-msg-id nil?)
(s/def ::app-name (s/or :rfc-5424 ::rfc-5424-app-name :rfc-3164 ::rfc-3164-app-name))
(s/def ::proc-id (s/or :rfc-5424 ::rfc-5424-proc-id :rfc-3164 ::rfc-3164-proc-id))
(s/def ::msg-id (s/or :rfc-5424 ::rfc-5424-msg-id :rfc-3164 ::rfc-3164-msg-id))
(s/def ::sd-id (s/and string? not-empty?))
(s/def ::sd-param-name (s/and string? not-empty?))
(s/def ::sd-param-value (s/and string? not-empty?))
(s/def ::sd-param-map (s/and (s/map-of ::sd-param-name ::sd-param-value) not-empty?))
(s/def ::sd-id-map (s/and (s/map-of ::sd-id ::sd-param-map) not-empty?))
(s/def ::structured-data (s/or :nil nil? :map ::sd-id-map))
(s/def ::msg (s/or :string (s/and string? #(not (empty? %))) :nil nil?))


(s/def ::rfc-5424-syslog-msg (s/keys :req [::facility 
                                  ::severity 
                                  ::rfc-5424-version 
                                  ::rfc-5424-timestamp
                                  ::host-name
                                  ::rfc-5424-app-name
                                  ::rfc-5424-proc-id
                                  ::rfc-5424-msg-id
                                  ::structured-data
                                  ::msg]))

(s/def ::rfc-3164-syslog-msg (s/keys :req [::facility 
                                  ::severity 
                                  ::rfc-3164-version 
                                  ::rfc-3164-timestamp
                                  ::host-name
                                  ::rfc-3164-app-name
                                  ::rfc-3164-proc-id
                                  ::rfc-3164-msg-id
                                  ::msg]))

(s/def ::syslog-msg (s/or :rfc-5424 ::rfc-5424-syslog-msg, :rfc-3164 ::rfc-3164-syslog-msg))
(s/explain ::rfc-5424-syslog-msg {::facility 1, 
                                  ::severity 1, 
                                  ::rfc-5424-version 1,
                                  ::rfc-5424-timestamp "timestamp",
                                  ::host-name nil,
                                  ::rfc-5424-app-name nil,
                                  ::rfc-5424-proc-id nil,
                                  ::rfc-5424-msg-id nil
                                  ::structured-data nil
                                  ::msg "a msg"
                                  })