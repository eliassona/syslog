package syslog;

public final class ATuple<T> {
	final int index;
	final T value;

	ATuple(final int index, final T value) {
		this.index = index;
		this.value = value;
		
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ATuple) {
			@SuppressWarnings("unchecked")
			final ATuple<T> other = (ATuple<T>)obj;
			return index == other.index && isValueEqual(other);
		}
		return false;
	}
	private boolean isValueEqual(final ATuple<T> other) {
		if (value == null) {
			return other.value == null;
		}
		return value.equals(other.value);
	}

	@Override
	public int hashCode() {
		if (value == null) {
			return index;
		}
		return value.hashCode();
	}
	@Override
	public String toString() {
		return String.format("index=%s, value=%s", index, value);
	}
}
