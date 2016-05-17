package syslog;

public interface DateTime {
	String getSecond();
	String getMinute();
	String getHour();
	String getDay();
	String getMonth();
	String getYear();
	int getTimeOffsetInSecs();
}
