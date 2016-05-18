package syslog;

import java.util.Map;

public final class SimpleSyslogMessage implements SyslogMessage {
	private final int facility;
	private final int severity;
	private final int version;
	private final String timestamp;
	private final String hostName;
	private final String appName;
	private final String procId;
	private final String msgId;
	private final String struturedData;
	private final String msg;
	
	
	
	public static SyslogMessage parse(final String syslogMsg) {
		final ATuple<Integer> priTuple = priOf(syslogMsg);
		final ATuple<Integer> verTuple = versionOf(syslogMsg, priTuple.index);
		final ATuple<String> timestampTuple = nextStr(syslogMsg, verTuple.index);
		final ATuple<String> hostNameTuple = nextStr(syslogMsg, timestampTuple.index);
		final ATuple<String> appNameTuple = nextStr(syslogMsg, hostNameTuple.index);
		final ATuple<String> procIdTuple = nextStr(syslogMsg, appNameTuple.index);
		final ATuple<String> msgIdTuple = nextStr(syslogMsg, procIdTuple.index);
		final ATuple<String> struturedDataTuple = nextStr(syslogMsg, msgIdTuple.index);
		return new SimpleSyslogMessage(
				priTuple.value & 0x7, 
				(priTuple.value & 0xf8) >> 3,
				verTuple.value,
				timestampTuple.value,
				checkNull(hostNameTuple.value),
				checkNull(appNameTuple.value),
				checkNull(procIdTuple.value),
				checkNull(msgIdTuple.value),
				checkNull(struturedDataTuple.value),
				msgIdTuple.value);
		
		
	}

	public SimpleSyslogMessage(final int facility, 
							   final int severity, 
							   final int version, 
							   final String timestamp, 
							   final String hostName, 
							   final String appName, 
							   final String procId,
							   final String msgId, 
							   final String structuredData, 
							   final String msg) {
		this.facility = facility;
		this.severity = severity;
		this.version = version;
		this.timestamp = timestamp;
		this.hostName = hostName;
		this.appName = appName;
		this.procId = procId;
		this.msgId = msgId;
		this.struturedData = structuredData;
		this.msg = msg;
		
	}




	//"<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8"
	
	public static String checkNull(final String value) {
		if (value.equals("-")) {
			return null;
		}
		return value;
	}

	static ATuple<Integer> priOf(final String msg) {
		final int closingPriIx = msg.indexOf('>', 1);
		return new ATuple<>(closingPriIx, Integer.valueOf(msg.substring(1, closingPriIx)));
	}
	static ATuple<Integer> versionOf(final String msg, final int offset) {
		return  new ATuple<>(offset + 3, msg.charAt(offset + 1) - 48);
	}
	static ATuple<String> nextStr(final String msg, final int offset) {
		final int spaceAfterTimestamp = msg.indexOf(' ', offset);
		return new ATuple<>(spaceAfterTimestamp + 1, msg.substring(offset, spaceAfterTimestamp));
	}

	@Override
	public String getMsg() {
		return msg;
	}

	@Override
	public String getStructuredData() {
		return struturedData;
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
		return String.format("severity %s, facility %s, version %s, timestamp %s, hostname %s, app-name %s, proc-id %s, msg-id %s, sd %s, msg %s", severity, facility, version, timestamp, hostName, appName, procId, msgId, struturedData, msg);
	}

	@Override
	public Map<String, Map<String, String>> getStructuredDataMap() {
		//Not implememented here due to performance
		return null;
	}

	@Override
	public DateTime getTimestampObj() {
		//Not implememented here due to performance
		return null;
	}
	
}

final class ATuple<T> {
	final int index;
	final T value;

	ATuple(final int index, final T value) {
		this.index = index;
		this.value = value;
		
	}
}
