package syslog;

import java.util.Map;

import clojure.lang.Keyword;
import clojure.lang.Var;

final public class SyslogMessageImpl implements SyslogMessage {
	static final Keyword facility = Keyword.intern("facility");
	static final Keyword severity = Keyword.intern("severity");
	static final Keyword header = Keyword.intern("header");
	static final Keyword msg = Keyword.intern("msg");
	static final Keyword version = Keyword.intern("version");
	static final Keyword pri = Keyword.intern("pri");
	static final Keyword sd = Keyword.intern("structured-data");
	private final Map<String, Object> map;
	private final Var cljTimeOffsetFn;

	@SuppressWarnings("unchecked")
	public SyslogMessageImpl(final Object m, final Var cljTimeOffsetFn) {
		this.cljTimeOffsetFn = cljTimeOffsetFn;
		if (!(m instanceof Map)) {
			throw new IllegalArgumentException(m.toString());
		}
		this.map = (Map<String, Object>) m;
	}

	@Override
	public String getMsg() {
		return (String) map.get(msg);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Map<String, String>> getStructuredDataMap() {
		return (Map<String, Map<String, String>>) map.get(sd);
	}

	@Override
	public DateTime getTimestampObj() {
		return new DateTimeImpl(getHeader(), cljTimeOffsetFn);
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
		return getHeader().get(k);
	}
	
	@SuppressWarnings("unchecked")
	private Map<Keyword, Object> getHeader() {
		final Object headerMap = map.get(header); 
		if (!(headerMap instanceof Map)) {
			throw new IllegalArgumentException(headerMap.toString());
		}
		return (Map<Keyword, Object>) headerMap;
	}
	
	@Override
	public int hashCode() {
		return map.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		return map.equals(obj);
	}

	@Override
	public String getStructuredData() {
		return null;
	}

	@Override
	public String getTimestamp() {
		return null;
	}
}


final class DateTimeImpl implements DateTime {
	static final Keyword sec = Keyword.intern("sec");
	static final Keyword min = Keyword.intern("min");
	static final Keyword hour = Keyword.intern("hour");
	static final Keyword day = Keyword.intern("day");
	static final Keyword month = Keyword.intern("month");
	static final Keyword year = Keyword.intern("year");
	static final Keyword timeOffset = Keyword.intern("time-offset");

	private final Map<Keyword, Object> header;
	private final Var cljTimeOffsetFn;

	public DateTimeImpl(final Map<Keyword, Object> header, final Var cljTimeOffsetFn) {
		this.header = header;
		this.cljTimeOffsetFn = cljTimeOffsetFn;
	}

	@Override
	public String getSecond() {
		return (String) header.get(sec);
	}

	@Override
	public String getMinute() {
		return (String) header.get(min);
	}

	@Override
	public String getHour() {
		return (String) header.get(hour);
	}

	@Override
	public String getDay() {
		return (String) header.get(day);
	}

	@Override
	public String getMonth() {
		return (String) header.get(month);
	}

	@Override
	public String getYear() {
		return (String) header.get(year);
	}

	@Override
	public int getTimeOffsetInSecs() {
		return ((Long)cljTimeOffsetFn.invoke(header)).intValue();
	}
	
	@Override
	public String toString() {
		return header.toString(); //TODO
	}
	
	@Override
	public int hashCode() {
		return header.hashCode(); //TODO
	}
	@Override
	public boolean equals(Object obj) {
		return header.equals(obj); //TODO
	}
}



