package syslog;


import java.util.HashMap;
import java.util.Map;



public final class Rfc5424SyslogMessageParser {
	private static final char VERSION = '1';
	private static final char NULL_CHAR = '-';
	private static final String NULL_STR = new String(new char[]{NULL_CHAR});
	public static final char BACKSLASH = 92;
	public static SyslogMessage parse(final byte[] rawData, final int length) {
		return parse(new String(rawData, 0, length), rawData);//TODO locale;
	}


	public static SyslogMessage parse(final byte[] rawData) {
		return parse(new String(rawData), rawData);//TODO locale
	}
	
	/**
	 * Only for testing
	 * @param syslogMsg
	 * @return
	 */
	public static SyslogMessage parse(final String syslogMsg) {
		return parse(syslogMsg, syslogMsg.getBytes());
	}
	
	/**
	 * This method has been written with performance in mind. It parses the syslog message from left to right increasing the
	 * index as it goes a long. As few string objects as possible are created during the parse.
	 * @param syslogMsg
	 * @return
	 */
	public static SyslogMessage parse(final String syslogMsg, final byte[] data) {
		return priParse(syslogMsg, data);
	}


	private static SyslogMessage priParse(final String syslogMsg, final byte[] data) {
		if (syslogMsg.length() > 0 && syslogMsg.charAt(0) == '<') {
			return versionParse(syslogMsg, data, priOf(syslogMsg));
		}
		return Rfc3164SyslogMessageParser.parseNoPri(syslogMsg, data);
	}


	private static SyslogMessage versionParse(final String syslogMsg, final byte[] data,
			final ATuple<Integer> priTuple) {
		if (isCorrectVersion(syslogMsg, priTuple.index)) {
			final ATuple<Integer> verTuple = versionOf(syslogMsg, priTuple.index);
			final ATuple<String> timestampTuple = SyslogMessageImpl.nextStr(syslogMsg, verTuple.index, "timestamp");
			final ATuple<String> hostNameTuple = SyslogMessageImpl.nextStr(syslogMsg, timestampTuple.index, "hostname");
			final ATuple<String> appNameTuple = SyslogMessageImpl.nextStr(syslogMsg, hostNameTuple.index, "app-name");
			final ATuple<String> procIdTuple = SyslogMessageImpl.nextStr(syslogMsg, appNameTuple.index, "proc-id");
			final ATuple<String> msgIdTuple = SyslogMessageImpl.nextStr(syslogMsg, procIdTuple.index, "msg-id");
			final ATuple<Map<String, Map<String, String>>> structuredDataTuple = sdOf(syslogMsg, msgIdTuple.index);
			return new SyslogMessageImpl(
					SyslogMessageImpl.facilityOf(priTuple),
					SyslogMessageImpl.severityOf(priTuple), 
					verTuple.value,
					timestampTuple.value,
					checkNull(hostNameTuple.value),
					checkNull(appNameTuple.value),
					checkNull(procIdTuple.value),
					checkNull(msgIdTuple.value),
					structuredDataTuple.value,
					msgOf(syslogMsg, structuredDataTuple.index),
					null,
					data);
		}
		return Rfc3164SyslogMessageParser.parseAfterPri(syslogMsg, data, priTuple);
	}


	private static boolean isCorrectVersion(final String syslogMsg, final int index) {
		return syslogMsg.length() > index && syslogMsg.charAt(index) == VERSION;
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
		} catch (final ParseException e) {
			throw e;
		} catch (final Exception e) {
			throw new ParseException(String.format(SyslogMessageImpl.parseErrorMsg, "structured-data", syslogMsg));
		}
	}

	public static final ATuple<Map<String, Map<String, String>>> subSdOf(final String syslogMsg, final int start) {
		final Map<String, Map<String, String>> m = new HashMap<>();
		int ix = start;
		do {
			final ATuple<String> idTuple = SyslogMessageImpl.nextStr(syslogMsg, ix + 1, "id in structured data");
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
			final ATuple<String> keyTuple = SyslogMessageImpl.nextStr(syslogMsg, subStart, '=', "sd-key");
			final ATuple<String> valueTuple = paramValueOf(syslogMsg, keyTuple.index + 1);
//			final ATuple<String> valueTuple = nextStr(syslogMsg, keyTuple.index + 1, '"', "sd-value");
			m.put(keyTuple.value, valueTuple.value);
			subStart = valueTuple.index + 1;
		} while (syslogMsg.charAt(subStart - 1) == ' ');
		if (syslogMsg.charAt(subStart - 1) != ']') throw new ParseException(String.format(SyslogMessageImpl.parseErrorMsg, String.format("sd-id, due to no ending square bracket (map so far: %s)", m), syslogMsg));
		return new ATuple<>(subStart, m);
	}


	public static final ATuple<String> paramValueOf(final String syslogMsg, final int index) {
		int ix = index;
		boolean escape = false;
		final StringBuilder buf = new StringBuilder();
		while (true) {
			try {
				final char ch = syslogMsg.charAt(ix);
				if (escape) {
					buf.append(ch);
					escape = false;
				} else {
					switch (ch) {
					case BACKSLASH: 
						escape = true;
						break;
					case '"': 
						return new ATuple<String>(ix + 1, buf.toString());
					default:
						buf.append(ch);
					}
				}
				ix++;
			} catch (final IndexOutOfBoundsException e) {
				throw new ParseException(String.format(SyslogMessageImpl.parseErrorMsg, "sd-param-value", syslogMsg));
			}
		}
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
			return new ATuple<>(closingPriIx + 1, checkPriRange(Integer.valueOf(msg.substring(1, closingPriIx))));
		} catch (final ParseException e) {
			throw e;
		} catch (final Exception e) {
			throw new ParseException(String.format(SyslogMessageImpl.parseErrorMsg, "priority", msg));
		}
	}
	public static final int checkPriRange(final int priVal) {
		if (priVal >= 0 && priVal <= 191) return priVal; //according to rfc
		throw new ParseException(String.format(SyslogMessageImpl.parseErrorMsg, "priority", "0 >= pri <= 191"));
	}

	public static final ATuple<Integer> versionOf(final String msg, final int offset) {
		try { 
			final ATuple<String> versionTuple = SyslogMessageImpl.nextStr(msg, offset, "version");
			final int version = Integer.parseInt(versionTuple.value);
			if (version > 0 && version < 1000) {
				return new ATuple<>(versionTuple.index, version);
			}
			throw new VersionParseException(String.format(SyslogMessageImpl.parseErrorMsg, "version", msg));
		} catch (final Exception e) {
			throw new VersionParseException(String.format(SyslogMessageImpl.parseErrorMsg, "version", msg));
		}
	}
}

