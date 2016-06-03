package syslog;

import java.net.DatagramPacket;
import java.util.Map;
import java.util.function.Supplier;

public final class LazySyslogMessage implements SyslogMessage {
	private final byte[] syslogMsg;
	private SyslogMessage parsedSyslogMsg;
	private ParseException parsedException;
	private final DatagramPacket datagramPacket;
	public LazySyslogMessage(final byte[] syslogMsg, final DatagramPacket datagramPacket) {
		this.syslogMsg = syslogMsg;
		this.datagramPacket = datagramPacket;
		
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
	
	@Override
	public String getClientHostName() {
		return datagramPacket.getAddress().getHostName();
	}
	
	private <T> T get(final Supplier<T> supplier) {
		if (parsedException != null) throw parsedException;
		if (parsedSyslogMsg == null) { 
			try {
				parsedSyslogMsg = Rfc5424SyslogMessageParser.parse(syslogMsg, datagramPacket.getLength());
			} catch (final ParseException e) {
				parsedException = e;
				throw e;
			}
		}
		return supplier.get();
	}
	@Override
	public byte[] getRawData() {
		return syslogMsg;
	}

}
