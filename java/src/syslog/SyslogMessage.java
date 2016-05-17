package syslog;

import java.util.Map;

public interface SyslogMessage {
	String getMsg();
	Map<String, Map<String, String>> getStructuredData();
	DateTime getTimestamp();
	int getVersion();
	int getSeverity();
	int getFacility();
}
