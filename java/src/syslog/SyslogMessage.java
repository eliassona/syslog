package syslog;

import java.util.Date;
import java.util.Map;

public interface SyslogMessage {
	String getMsg();
	Map<String, Map<String, String>> getStructuredData();
	Date getTimestamp();
	int getVersion();
	int getSeverity();
	int getFacility();
}
