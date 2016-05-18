package syslog;

import java.util.Map;

public class SimpleSyslogMessage implements SyslogMessage {

	private final String msg;
	private final int facility;
	private final int severity;
	private int version;
	private String timestamp;
	
	//"<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8"
	
	
	public SimpleSyslogMessage(final String msg) {
		this.msg = msg;
		final int closingPriIx = msg.indexOf('>', 1);
		final int pri = Integer.valueOf(msg.substring(1, closingPriIx));
		this.facility = pri & 0x7;
		this.severity = (pri & 0xf8) >> 3;
		this.version = msg.charAt(closingPriIx + 1) - 48;
		final int startOfTimestamp = closingPriIx + 3;
		final int spaceAfterTimestamp = msg.indexOf(' ', startOfTimestamp);
		this.timestamp = msg.substring(startOfTimestamp, spaceAfterTimestamp);
	}

	@Override
	public String getMsg() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Map<String, String>> getStructuredData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DateTime getTimestamp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSeverity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFacility() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		return String.format("severity %s, facility %s, version %s, timestamp %s", severity, facility, version, timestamp);
	}
	
}
