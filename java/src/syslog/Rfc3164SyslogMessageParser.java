package syslog;

import static syslog.SyslogMessageImpl.facilityOf;
import static syslog.SyslogMessageImpl.parseErrorMsg;
import static syslog.SyslogMessageImpl.severityOf;

public class Rfc3164SyslogMessageParser {
	public static SyslogMessage parseNoPri(final String syslogMsg, final byte[] data) {
		return new SyslogMessageImpl(0, 
				 0, 
				 0, 
				 null, 
				 null, 
				 null, 
				 null, 
				 null, 
				 null, 
				 syslogMsg, 
				 null, 
				 data);
	}

	public static SyslogMessage parseAfterPri(final String syslogMsg, final byte[] data, final ATuple<Integer> priTuple) {
		final ATuple<String> timestampTuple = timestampTupleOf(syslogMsg, priTuple.index);
		final ATuple<String> hostNameTuple = hostNameOf(syslogMsg, timestampTuple.index, "hostname");
		return new SyslogMessageImpl(facilityOf(priTuple), 
									 severityOf(priTuple), 
									 0, 
									 timestampTuple.value, 
									 hostNameTuple.value, 
									 null, 
									 null, 
									 null, 
									 null, 
									 msgOf(syslogMsg, hostNameTuple.index), 
									 null, 
									 data);
	}

	private static ATuple<String> hostNameOf(final String syslogMsg, final int index, final String string) {
		final int nextSpace = syslogMsg.indexOf(' ', index);
		if (nextSpace < 0) {
			return new ATuple<String>(syslogMsg.length(), syslogMsg.substring(index));
		}
		return new ATuple<String>(nextSpace + 1, syslogMsg.substring(index, nextSpace));
	}

	private static String msgOf(final String syslogMsg, final int index) {
		if (index < syslogMsg.length()) {
			return syslogMsg.substring(index);
		}
		return null;
	}
	public static final int TIMESTAMP_LENGTH = 15;
	private static ATuple<String> timestampTupleOf(final String syslogMsg, final int index) {
		try {
			final int endIx = index + TIMESTAMP_LENGTH;
			if (syslogMsg.charAt(endIx) != ' ') {
				throw new ParseException(String.format(parseErrorMsg, "timestamp", syslogMsg));
			}
			return new ATuple<>(endIx + 1, syslogMsg.substring(index, endIx));
		} catch (final ParseException e) {
			throw e;
		} catch (final Exception e) {
			throw new ParseException(String.format(parseErrorMsg, "timestamp", syslogMsg));
		}
	}
}
