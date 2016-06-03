package syslog;

public final class ATuple<T> {
	final int index;
	final T value;

	ATuple(final int index, final T value) {
		this.index = index;
		this.value = value;
		
	}
}
