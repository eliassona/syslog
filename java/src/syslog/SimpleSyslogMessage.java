package syslog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class SimpleSyslogMessage implements SyslogMessage {
	private static final char NULL_CHAR = '-';
	private static final String NULL_STR = new String(new char[]{NULL_CHAR});
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
	
	public static final Object[] createArray() {
		Object[] a = new Object[100];
		a[0] = "Anders1";
		a[1] = "Anders";
		a[2] = "Anders2";
		a[3] = "Anders";
		a[4] = "Anders3";
		a[5] = "Anders";
		a[6] = "Anders4";
		a[7] = "Anders";
		a[8] = "Anders5";
		a[9] = "Anders";
		a[10] = "Anders6";
		a[11] = "Anders";
		return a;
	}
	public static final List createList() {
		List a = new ArrayList(100);
		a.add("Anders1");
		a.add("Anders");
		a.add("Anders2");
		a.add("Anders");
		a.add("Anders3");
		a.add("Anders");
		a.add("Anders4");
		a.add("Anders");
		a.add("Anders5");
		a.add("Anders");
		a.add("Anders6");
		a.add("Anders");
		return a;
	}
	
	public static Map createMap() {
		HashMap m = new HashMap<>();
		m.put("Anders1", "Anders");
		m.put("Anders2", "Anders");
		m.put("Anders3", "Anders");
		m.put("Anders4", "Anders");
		m.put("Anders5", "Anders");
		m.put("Anders6", "Anders");
		return m;
	}

	public static SyslogMessage parse(final byte[] data) {
		return parse(new String(data));//TODO locale
	}
	
	/**
	 * This method has been written with performance in mind. It parses the syslog message from left to right increasing the
	 * index as it goes a long. As few string objects as possible are created during the parse.
	 * @param syslogMsg
	 * @return
	 */
	public static SyslogMessage parse(final String syslogMsg) {
		final ATuple<Integer> priTuple = priOf(syslogMsg);
		final ATuple<Integer> verTuple = versionOf(syslogMsg, priTuple.index);
		final ATuple<String> timestampTuple = nextStr(syslogMsg, verTuple.index, "timestamp");
		final ATuple<String> hostNameTuple = nextStr(syslogMsg, timestampTuple.index, "hostname");
		final ATuple<String> appNameTuple = nextStr(syslogMsg, hostNameTuple.index, "app-name");
		final ATuple<String> procIdTuple = nextStr(syslogMsg, appNameTuple.index, "proc-id");
		final ATuple<String> msgIdTuple = nextStr(syslogMsg, procIdTuple.index, "msg-id");
		final ATuple<Map<String, Map<String, String>>> structuredDataTuple = sdOf(syslogMsg, msgIdTuple.index);
		return new SimpleSyslogMessage(
				(priTuple.value & 0xf8) >> 3,
				priTuple.value & 0x7, 
				verTuple.value,
				timestampTuple.value,
				checkNull(hostNameTuple.value),
				checkNull(appNameTuple.value),
				checkNull(procIdTuple.value),
				checkNull(msgIdTuple.value),
				structuredDataTuple.value,
				msgOf(syslogMsg, structuredDataTuple.index));
		
		
	}
	
	public SimpleSyslogMessage(final int facility, 
			   final int severity, 
			   final int version, 
			   final String timestamp, 
			   final String hostName, 
			   final String appName, 
			   final String procId,
			   final String msgId, 
			   final Map<String, Map<String, String>> structuredData, 
			   final String msg) {
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

	/**
	 * @note slow, only use for testing
	 * @return
	 */
	
	public static final ATuple<Map<String, Map<String, String>>> sdOf(final String syslogMsg, final int index) {
		try {
			if (syslogMsg.charAt(index) == NULL_CHAR) {
				return new ATuple<>(index + 2, null);
			}
			final ATuple<Map<String, Map<String, String>>> sdTuple = subSdOf(syslogMsg, index);
			return new ATuple<>(sdTuple.index + 1, sdTuple.value);
		} catch (final Exception e) {
			throw new ParseException(String.format(parseErrorMsg, "structured-data", syslogMsg));
		}
	}

	public static final ATuple<Map<String, Map<String, String>>> subSdOf(final String syslogMsg, final int start) {
		final Map<String, Map<String, String>> m = new HashMap<>();
		int ix = start;
		do {
			final ATuple<String> idTuple = nextStr(syslogMsg, ix + 1, "id in structured data");
			final ATuple<Map<String, String>> sdTuple = kvSdOf(syslogMsg, idTuple.index);
			m.put(idTuple.value, sdTuple.value);
			ix = sdTuple.index;
		} while (shouldContinueSubSdSearch(syslogMsg, ix));
		return new ATuple<>(ix, m);
	}
	
	private static boolean shouldContinueSubSdSearch(final String syslogMsg, final int ix) {
		if (ix >= syslogMsg.length() - 1) {
			return false;
		}
		return syslogMsg.charAt(ix) == '[';
	}


	public static final ATuple<Map<String, String>> kvSdOf(final String syslogMsg, final int start) {
		final Map<String, String> m = new HashMap<>();
		int subStart = start;
		do {
			final ATuple<String> keyTuple = nextStr(syslogMsg, subStart, '=', "sd-key");
			final ATuple<String> valueTuple = nextStr(syslogMsg, keyTuple.index + 1, '"', "sd-value");
			m.put(keyTuple.value, valueTuple.value);
			subStart = valueTuple.index + 1;
		} while (syslogMsg.charAt(subStart - 1) != ']');
		return new ATuple<>(subStart, m);
	}


	public static final String msgOf(final String syslogMsg, final int index) {
		if (index < syslogMsg.length()) {
			return syslogMsg.substring(index);
		}
		return null;
	}

	public static final String checkNull(final String value) {
		if (value.equals(NULL_STR)) {
			return null;
		}
		return value;
	}

	public static final ATuple<Integer> priOf(final String msg) {
		try {
			final int closingPriIx = msg.indexOf('>', 1);
			return new ATuple<>(closingPriIx, checkPriRange(Integer.valueOf(msg.substring(1, closingPriIx))));
		} catch (final ParseException e) {
			throw e;
		} catch (final Exception e) {
			throw new ParseException(String.format(parseErrorMsg, "priority", msg));
		}
	}
	public static final int checkPriRange(final int priVal) {
		if (priVal >= 0 && priVal <= 191) return priVal; //according to rfc
		throw new ParseException(String.format(parseErrorMsg, "priority", "0 >= pri <= 191"));
	}

	public static final ATuple<Integer> versionOf(final String msg, final int offset) {
		try { 
			return  new ATuple<>(offset + 3, msg.charAt(offset + 1) - 48);
		} catch (final Exception e) {
			throw new ParseException(String.format(parseErrorMsg, "version", msg));
		}
	}
	public static final ATuple<String> nextStr(final String msg, final int offset, final String name) {
		return nextStr(msg, offset, ' ', name);
	}
	
	public static final ATuple<String> nextStr(final String msg, final int offset, final char ch, final String name) {
		try {
			final int spaceAfterTimestamp = msg.indexOf(ch, offset);
			return new ATuple<>(spaceAfterTimestamp + 1, msg.substring(offset, spaceAfterTimestamp));
		} catch (final Exception e) {
			throw new ParseException(String.format(parseErrorMsg, name, msg));
		}
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
