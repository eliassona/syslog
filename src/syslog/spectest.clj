(ns syslog.testspec
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.repl :refer [doc source]])
  )

(comment
  see rfc
  https://tools.ietf.org/html/rfc5424
  )

(defn not-empty? [s] (not (empty? s)))
(defn no-space? [s] (< (.indexOf s " ") 0))

(def ctrl-chars (into #{} (map (comp str char) (range 0 32))))

(defn no-chars-below-space? [s]
  (let [bs (.getBytes s)]
    (if (= (count bs) 1)
      (>= (first bs) 32)
      true)))
      

(defn no-ctrl-chars? [s the-set]
  (reduce (fn [v1 v2] (and v1 v2)) true (map #(and (no-chars-below-space? %) (< (.indexOf s %) 0)) the-set)))

(defn no-ctrl-chars-sd-name? [s]
  (no-ctrl-chars? s #{"=", "\"", "]", " ",  "\\"}))
  
(defn no-ctrl-chars-sd-value? [s]
  (no-ctrl-chars? s #{"]", "\"", "\\"}))


(s/def ::str-ascii (s/with-gen string? gen/string-ascii))
(s/def ::str-utf-8 (s/with-gen string? gen/string))
(s/def ::str-ascii-no-ctrl-chars (s/and ::str-ascii no-ctrl-chars-sd-name?))
(s/def ::str-utf-8-no-ctrl-chars (s/and ::str-utf-8 no-ctrl-chars-sd-value?))

(s/def ::str-or-nil (s/with-gen (s/or :nil nil? :str string?) gen/string))

(s/def ::str-no-space (s/and ::str-ascii no-space?))

(s/def ::str-no-space-or-nil (s/or :nil nil? :str ::str-no-space))

(s/def ::facility (s/and integer? #(>= % 0) #(<= % 23)))
(s/def ::severity (s/and integer? #(>= % 0) #(<= % 7)))
(s/def ::rfc-5424-version (s/and integer? #(= % 1)))
(s/def ::rfc-3164-version (s/and integer? zero?))
(s/def ::rfc-5424-timestamp ::str-no-space)
(s/def ::rfc-3164-timestamp (s/and string? #(= (.length %) 15)))
(s/def ::rfc-5424-host-name ::str-no-space-or-nil)
(s/def ::rfc-3164-host-name ::str-no-space)
(s/def ::rfc-5424-app-name ::str-no-space-or-nil) 
(s/def ::rfc-5424-proc-id ::str-no-space-or-nil)
(s/def ::rfc-5424-msg-id ::str-no-space-or-nil)
(s/def ::sd-id (s/and ::str-ascii-no-ctrl-chars not-empty?))
(s/def ::sd-param-name (s/and ::str-ascii-no-ctrl-chars not-empty?))
(s/def ::sd-param-value (s/and ::str-utf-8-no-ctrl-chars not-empty?))
(s/def ::sd-param-map (s/and (s/map-of ::sd-param-name ::sd-param-value) not-empty?))
(s/def ::sd-id-map (s/and (s/map-of ::sd-id ::sd-param-map) not-empty?))
(s/def ::structured-data (s/or :nil nil? :map ::sd-id-map))
(s/def ::msg (s/or :st-utf-8 (s/and ::str-utf-8 #(not (empty? %))) :nil nil?))


(s/def ::rfc-5424-syslog-msg (s/keys :req [::facility 
                                  ::severity 
                                  ::rfc-5424-version 
                                  ::rfc-5424-timestamp
                                  ::rfc-5424-host-name
                                  ::rfc-5424-app-name
                                  ::rfc-5424-proc-id
                                  ::rfc-5424-msg-id
                                  ::structured-data
                                  ::msg]))

(s/def ::rfc-3164-syslog-msg (s/keys :req [::facility 
                                  ::severity 
                                  ::rfc-3164-version 
                                  ::rfc-3164-timestamp
                                  ::rfc-3164-host-name
                                  ::msg]))

(s/def ::syslog-msg (s/or :rfc-5424 ::rfc-5424-syslog-msg, :rfc-3164 ::rfc-3164-syslog-msg))
(s/explain ::rfc-5424-syslog-msg {::facility 1, 
                                  ::severity 1, 
                                  ::rfc-5424-version 1,
                                  ::rfc-5424-timestamp "timestamp",
                                  ::rfc-5424-host-name nil,
                                  ::rfc-5424-app-name nil,
                                  ::rfc-5424-proc-id nil,
                                  ::rfc-5424-msg-id nil
                                  ::structured-data nil
                                  ::msg "a msg"
                                  })