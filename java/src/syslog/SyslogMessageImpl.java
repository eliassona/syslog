package syslog;

import java.util.Date;
import java.util.Map;

import clojure.lang.Keyword;

public class SyslogMessageImpl implements SyslogMessage {
	static final Keyword facility = Keyword.intern("facility");
	static final Keyword severity = Keyword.intern("severity");
	static final Keyword header = Keyword.intern("header");
	static final Keyword msg = Keyword.intern("msg");
	static final Keyword version = Keyword.intern("version");
	static final Keyword pri = Keyword.intern("pri");
	static final Keyword sd = Keyword.intern("structured-data");
	private final Map<String, Object> map;

	public SyslogMessageImpl(final Object m) {
		if (!(m instanceof Map)) {
			throw new IllegalArgumentException(m.toString());
		}
		this.map = (Map<String, Object>) m;
	}

	@Override
	public String getMsg() {
		return (String) map.get(msg);
	}

	@Override
	public Map<String, Map<String, String>> getStructuredData() {
		return (Map<String, Map<String, String>>) map.get(sd);
	}

	@Override
	public Date getTimestamp() {
		return null;
	}

	@Override
	public int getVersion() {
		return ((Long) getHeader(version)).intValue();
	}
	@Override
	public String toString() {
		return map.toString();
	}

	@Override
	public int getSeverity() {
		return ((Long) getHeader(severity)).intValue();
	}

	@Override
	public int getFacility() {
		return ((Long) getHeader(facility)).intValue();
	}
	private Object getHeader(final Keyword k) {
		final Object headerMap = map.get(header); 
		if (!(headerMap instanceof Map)) {
			throw new IllegalArgumentException(headerMap.toString());
		}
		return ((Map<Keyword, Object>)headerMap).get(k);
	}
}



