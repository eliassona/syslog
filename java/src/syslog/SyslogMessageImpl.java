package syslog;

import java.util.Map;


public final class SyslogMessageImpl implements SyslogMessage {
	public static final String parseErrorMsg = "Can't parse %s in syslog message: '%s'";
	private final int facility;
	private final int severity;
	private final int version;
	private final String timestamp;
	private final String hostName;
	private final String appName;
	private final String procId;
	private final String msgId;
	private final Map<String, Map<String, String>> structuredData;
	private final String msg;
	private final byte[] rawData;
	private String clientHostName;
	
	
	public SyslogMessageImpl(final int facility, 
			   final int severity, 
			   final int version, 
			   final String timestamp, 
			   final String hostName, 
			   final String appName, 
			   final String procId,
			   final String msgId, 
			   final Map<String, Map<String, String>> structuredData, 
			   final String msg, 
			   final String clientHostName, 
			   final byte[] data) {
	this.facility = facility;
	this.severity = severity;
	this.version = version;
	this.timestamp = timestamp;
	this.hostName = hostName;
	this.appName = appName;
	this.procId = procId;
	this.msgId = msgId;
	this.structuredData = structuredData;
	this.msg = msg;
	this.clientHostName = clientHostName;
	this.rawData = data;

}



	@Override
	public String getMsg() {
		return msg;
	}

	@Override
	public Map<String, Map<String, String>> getStructuredData() {
		return structuredData;
	}

	@Override
	public String getTimestamp() {
		return timestamp;
	}

	@Override
	public int getVersion() {
		return version;
	}

	@Override
	public int getSeverity() {
		return severity;
	}

	@Override
	public int getFacility() {
		return facility;
	}

	@Override
	public String toString() {
		return String.format("severity %s, facility %s, version %s, timestamp %s, hostname %s, app-name %s, proc-id %s, msg-id %s, sd %s, msg %s", severity, facility, version, timestamp, hostName, appName, procId, msgId, structuredData, msg);
	}


	@Override
	public DateTime getTimestampObj() {
		//Not implememented here due to performance
		return null;
	}

	@Override
	public String getHostName() {
		return hostName;
	}

	@Override
	public String getAppName() {
		return appName;
	}

	@Override
	public String getProcId() {
		return procId;
	}

	@Override
	public String getMsgId() {
		return msgId;
	}
	
	@Override
	public byte[] getRawData() {
		return rawData;
	}




	@Override
	public String getClientHostName() {
		return clientHostName;
	}

	
	public static final int severityOf(final ATuple<Integer> priTuple) {
		return priTuple.value & 0x7;
	}


	public static final int facilityOf(final ATuple<Integer> priTuple) {
		return (priTuple.value & 0xf8) >> 3;
	}
	
	public static final ATuple<String> nextStr(final String msg, final int offset, final String name) {
		try {
			final int spaceAfter = msg.indexOf(' ', offset);
			if (spaceAfter == offset) {
				throw new ParseException(String.format(parseErrorMsg, name, msg));
			}
			return new ATuple<>(spaceAfter + 1, msg.substring(offset, spaceAfter));
		} catch (final Exception e) {
			throw new ParseException(String.format(parseErrorMsg, name, msg));
		}
	}
	
	public static final ATuple<String> nextStr(final String msg, final int offset, final char ch, final String name) {
		try {
			final int spaceAfter = msg.indexOf(ch, offset);
			return new ATuple<>(spaceAfter + 1, msg.substring(offset, spaceAfter));
		} catch (final Exception e) {
			throw new ParseException(String.format(parseErrorMsg, name, msg));
		}
	}


	
	
}

