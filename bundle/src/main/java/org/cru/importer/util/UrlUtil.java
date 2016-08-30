package org.cru.importer.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class UrlUtil {

	public static Map<String, String> splitQuery(String parameters) throws UnsupportedEncodingException {
	    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
	    String[] pairs = parameters.split("&");
	    for (String pair : pairs) {
	        int idx = pair.indexOf("=");
	        query_pairs.put(pair.substring(0, idx), (idx<pair.length())? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : "");
	    }
	    return query_pairs;
	}
	
}
