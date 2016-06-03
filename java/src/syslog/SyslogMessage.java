package syslog;

import java.util.Map;

public interface SyslogMessage {
	String getMsg();
	Map<String, Map<String, String>> getStructuredData();
	String getTimestamp();
	int getVersion();
	int getSeverity();
	int getFacility();
	String getHostName();
	String getAppName();
	String getProcId();
	String getMsgId();
	DateTime getTimestampObj();
	byte[] getRawData();
	String getClientHostName();
}
