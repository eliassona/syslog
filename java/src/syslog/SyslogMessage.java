package syslog;

import java.util.Map;

public interface SyslogMessage {
	String getMsg();
	String getStructuredData();
	String getTimestamp();
	int getVersion();
	int getSeverity();
	int getFacility();
	Map<String, Map<String, String>> getStructuredDataMap();
	DateTime getTimestampObj();
}
