package syslog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Immutable implementation of map that works well for small number of entries, requires much less memory than a hashmap. The map is based on an array. 
 * @author anderse
 *
 */
public final class ArrayMap implements Map<String, Object> {

	private final Object[] array;
	
	/**
	 * Create the map as an array map if size of array is less than 30 otherwise create a hash map.
	 * @param keyValuePairs, i.e k1, v1, k2, v2...
	 * @return
	 */
	public static final Map<String, Object> create(final Object... keyValuePairs) {
		return createFromArray(keyValuePairs);
	}
	
	public static final Map<String, Object> createFromArray(final Object[] keyValuePairs) {
		if (keyValuePairs.length < 30) {
			return new ArrayMap(keyValuePairs);
		}
		final Map<String, Object> hm = new HashMap<>(keyValuePairs.length / 2);
		for (int i = 0; i < keyValuePairs.length; i = i + 2) {
			hm.put((String) keyValuePairs[i], keyValuePairs[i + 1]);
		}		
		return hm;
	}
	
	
	private ArrayMap(final Object[] keyValuePairs) {
		assert (keyValuePairs.length % 2 == 0);
		array = keyValuePairs;
	}
	
	
	private static Set<java.util.Map.Entry<String, Object>> entrySetOf(final Object[] array) {
		final Set<java.util.Map.Entry<String, Object>> result = new HashSet<>();
		for (int i = 0; i < array.length; i = i + 2) {
			result.add(new EntryImpl((String)array[i], array[i +1]));
		}
		return result;
	}


	@Override
	public int size() {
		return array.length / 2;
	}

	@Override
	public boolean isEmpty() {
		return array.length <= 0;
	}

	@Override
	public boolean containsKey(final Object key) {
		for (int i = 0; i < array.length; i = i + 2) {
			if (array[i].equals(key)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsValue(final Object value) {
		for (int i = 1; i < array.length; i = i + 2) {
			if (array[i].equals(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object get(final Object key) {
		for (int i = 0; i < array.length; i = i + 2) {
			if (array[i].equals(key)) {
				return array[i + 1];
			}
		}
		return null;
	}

	@Override
	public Object put(final String key, final Object value) {
		throw new IllegalStateException("Not implemented for this map");
	}

	@Override
	public Object remove(final Object key) {
		throw new IllegalStateException("Not implemented for this map");
	}

	@Override
	public void putAll(final Map<? extends String, ? extends Object> m) {
		throw new IllegalStateException("Not implemented for this map");
	}

	@Override
	public void clear() {
		throw new IllegalStateException("Not implemented for this map");
	}

	@Override
	public Set<String> keySet() {
		final Set<String> result = new HashSet<>();
		for (int i = 0; i < array.length; i = i + 2) {
			result.add((String) array[i]);
		}		
		return result;
	}

	@Override
	public Collection<Object> values() {
		final List<Object> result = new ArrayList<>();
		for (int i = 0; i < array.length; i = i + 2) {
			result.add(array[i + 1]);
		}		
		return result;
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return entrySetOf(array);
	}

	/**
	 * @Note this is slow!!
	 */
	@Override public String toString() {
		return new HashMap<>(this).toString();
	}
	
}


class EntryImpl implements Entry<String, Object> {

	private final String k;
	private final Object v;

	public EntryImpl(final String k, final Object v) {
		this.k = k;
		this.v = v;
	}

	@Override
	public String getKey() {
		return k;
	}

	@Override
	public Object getValue() {
		return v;
	}

	@Override
	public Object setValue(final Object value) {
		throw new IllegalStateException("Not implemented for this map");
	}
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Map.Entry) {
			final Map.Entry other = (Map.Entry)obj;
			return k.equals(other.getKey()) && nullEquals(v, other.getValue());
		}
		return false;
	}
	
	private boolean nullEquals(final Object o1, final Object o2) {
		if (o1 == null) {
			return o1 == o2;
		}
		return o1.equals(o2);
	}

	@Override
	public int hashCode() {
		return k.hashCode() ^ nullHashCode(v);
	}

	private int nullHashCode(final Object o) {
		if (o == null) return 0;
		return o.hashCode();
	}
}