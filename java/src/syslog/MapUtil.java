package syslog;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author anderse
 *
 */
public class MapUtil {
	public static final Map<String, Object> create(final Object... keyValuePairs) {
		return createFromArray(keyValuePairs);
	}
	
	public static final Map<String, Object> createFromArray(final Object[] keyValuePairs) {
		return createFromArray(new HashMap<String, Object>(keyValuePairs.length / 2), keyValuePairs);
	}
	public static final Map<String, Object> createFromArray(Map<String, Object> hm, final Object[] keyValuePairs) {
		for (int i = 0; i < keyValuePairs.length; i = i + 2) {
			hm.put((String) keyValuePairs[i], keyValuePairs[i + 1]);
		}		
		return hm;
	}
	
	public static final Map<String, Object> assoc(Map<String, Object> map, final Object... keyValuePairs) {
		return createFromArray(new HashMap<>(map), keyValuePairs);
	}
	
}
