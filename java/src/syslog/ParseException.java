package syslog;

public class ParseException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	public ParseException(String msg) {
		super(msg);
	}
}
