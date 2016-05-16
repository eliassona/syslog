package syslog;

import java.util.Date;
import java.util.Map;

import clojure.lang.Keyword;

public class SyslogMessageImpl implements SyslogMessage {
	static final Keyword header = Keyword.intern(null, "header");
	static final Keyword msg = Keyword.intern(null, "msg");
	static final Keyword version = Keyword.intern(null, "version");
	static final Keyword pri = Keyword.intern(null, "pri");
	static final Keyword sd = Keyword.intern(null, "structured-data");
	private Map<String, Object> map;

	public SyslogMessageImpl(Object m) {
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
	public Priority getPri() {
		return new PriorityImpl(getHeader(pri));
	}

	private Object getHeader(Keyword k) {
		Object headerMap = map.get(header); 
		if (!(headerMap instanceof Map)) {
			throw new IllegalArgumentException(headerMap.toString());
		}
		return ((Map<Keyword, Object>)headerMap).get(k);
	}

	@Override
	public long getVersion() {
		return (long) getHeader(version);
	}
	@Override
	public String toString() {
		return map.toString();
	}
}



final class PriorityImpl implements Priority {

	public PriorityImpl(Object object) {
	}
	
}