package cn.sznxkj.cache;

import java.util.HashMap;
import java.util.Map;

public class MyCache {
	
	private static Map cache = new HashMap();
	
	private MyCache(){};
	
	public static Object get(String key) {
		if (cache.containsKey(key)) {
			return cache.get(key);
		}
		return null;
	}
	
	public static void put(String key, Object o) {
		cache.put(key, o);
	}
	
	public static void flush() {
		cache.clear();
	}
}
