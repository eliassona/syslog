package syslog;

import static syslog.Rfc5424SyslogMessageParser.BACKSLASH;

import java.util.Map;

public class SyslogEncoder {

	public static String encode(final SyslogMessage slm) {
		if (slm.getVersion() == 1) {
			return encodeRfc5424(slm);
		}
		return encodeRfc3164(slm);
	}

	private static String encodeRfc3164(final SyslogMessage slm) {
		final StringBuilder buf = new StringBuilder(1024);
		if (priValueOf(slm) == 0) {
			buf.append(slm.getMsg());
		} else {
			addPriToStr(slm, buf);
			buf.append(slm.getTimestamp());
			buf.append(' ');
			buf.append(slm.getHostName());
			buf.append(' ');
			buf.append(slm.getMsg() == null ? "": slm.getMsg());
		}
		return buf.toString();
	}

	private static int priValueOf(final SyslogMessage slm) {
		return (slm.getFacility() << 3) | slm.getSeverity();
	}

	private static String encodeRfc5424(final SyslogMessage slm) {		// TODO Auto-generated method stub
		final StringBuilder buf = new StringBuilder(1024);
		addPriToStr(slm, buf);
		buf.append("1 ");
		buf.append(slm.getTimestamp());
		buf.append(spaceAndNull(slm.getHostName()));
		buf.append(spaceAndNull(slm.getAppName()));
		buf.append(spaceAndNull(slm.getProcId()));
		buf.append(spaceAndNull(slm.getMsgId()));
		buf.append(spaceAndNull(sdOf(slm)));
		buf.append(msgOf(slm));
		return buf.toString();
	}

	private static void addPriToStr(final SyslogMessage slm, final StringBuilder buf) {
		buf.append('<');
		buf.append(priValueOf(slm));
		buf.append(">");
	}

	private static String msgOf(final SyslogMessage slm) {
		return (slm.getMsg() == null) ? "": " " + slm.getMsg();
	}

	public static String sdOf(final SyslogMessage slm) {
		if (slm.getStructuredData() == null) return null;
		final StringBuilder buf = new StringBuilder(1024);
		for (final String key: slm.getStructuredData().keySet()) {
			addToSdStr(key, slm.getStructuredData().get(key), buf);
		}
		return buf.toString();
	}

	public static void addToSdStr(final String key, final Map<String, String> map, final StringBuilder buf) {
		buf.append('[');
		buf.append(key);
		addValuesToSdStr(map, buf);
		buf.append(']');
	}

	private static void addValuesToSdStr(final Map<String, String> map, final StringBuilder buf) {
		for (final String key: map.keySet()) {
			buf.append(' ');
			buf.append(key);
			buf.append('=');
			buf.append('"');
			buf.append(addEscapes(map.get(key)));
			buf.append('"');
		}
	}

	private static String addEscapes(final String str) {
		final StringBuilder buf = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (ch == '"' || ch == ']' || ch == BACKSLASH) {
				buf.append(BACKSLASH);
			}
			buf.append(ch);
		}
		return buf.toString();
	}

	private static String spaceAndNull(final String str) {
		return " " + ((str == null) ? "-": str);  
	}

}
