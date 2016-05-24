package syslog;

import java.util.Map;
import java.util.function.Supplier;

public final class LazySyslogMessage implements SyslogMessage {
	private final String syslogMsg;
	private SyslogMessage parsedSyslogMsg;
	private ParseException parsedException;
	public LazySyslogMessage(final String syslogMsg) {
		this.syslogMsg = syslogMsg;
		parsedSyslogMsg = SimpleSyslogMessage.parse(syslogMsg);
		
	}
	@Override
	public String getMsg() {
		return get(() ->  parsedSyslogMsg.getMsg());
	}

	@Override
	public Map<String, Map<String, String>> getStructuredData() {
		return  get(() ->  parsedSyslogMsg.getStructuredData());
	}

	@Override
	public String getTimestamp() {
		return get(() ->  parsedSyslogMsg.getTimestamp());
	}

	@Override
	public int getVersion() {
		return get(() ->  parsedSyslogMsg.getVersion());
	}

	@Override
	public int getSeverity() {
		return get(() ->  parsedSyslogMsg.getSeverity());
	}

	@Override
	public int getFacility() {
		return get(() ->  parsedSyslogMsg.getFacility());
	}

	@Override
	public String getHostName() {
		return get(() ->  parsedSyslogMsg.getHostName());
	}

	@Override
	public String getAppName() {
		return get(() ->  parsedSyslogMsg.getAppName());
	}

	@Override
	public String getProcId() {
		return get(() ->  parsedSyslogMsg.getProcId());
	}

	@Override
	public String getMsgId() {
		return get(() -> parsedSyslogMsg.getMsgId());
	}

	@Override
	public DateTime getTimestampObj() {
		return get(() -> parsedSyslogMsg.getTimestampObj());
	}

	
	
//	private <T> T get(final Supplier<T> supplier) {
//		try {
//			if (parsedException != null) throw parsedException;
//			return supplier.get();
//		} catch (final NullPointerException e) {
//			try {
//				parsedSyslogMsg = SimpleSyslogMessage.parse(syslogMsg);
//				return supplier.get();
//			} catch (final ParseException e1) {
//				parsedException = e1;
//				throw parsedException;
//			}
//		}
//	}
	private <T> T get(final Supplier<T> supplier) {
			if (parsedException != null) throw parsedException;
			if (parsedSyslogMsg == null) { 
				try {
					parsedSyslogMsg = SimpleSyslogMessage.parse(syslogMsg);
				} catch (final ParseException e) {
					parsedException = e;
					throw e;
				}
			}
			return supplier.get();
	}

}
