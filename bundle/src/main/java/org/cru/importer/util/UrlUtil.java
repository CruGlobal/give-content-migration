package org.cru.importer.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class UrlUtil {

	public static Map<String, String> splitQuery(String parameters) {
	    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
	    String[] pairs = parameters.split("&");
	    for (String pair : pairs) {
	        int idx = pair.indexOf("=");
	        query_pairs.put(pair.substring(0, idx), pair.substring(idx + 1));
	    }
	    return query_pairs;
	}
	
}
