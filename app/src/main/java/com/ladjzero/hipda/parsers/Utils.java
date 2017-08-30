package com.ladjzero.hipda.parsers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenzhuo on 15-9-19.
 */
public class Utils {

	public static Map<String, String> getUriQueryParameter(String url) {
		Map<String, String> query_pairs = new HashMap<>();
		String query = url.substring(url.indexOf('?') + 1);
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");

			if (idx > -1) {
				try {
					query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			} else {
				query_pairs.put(pair, null);
			}
		}
		return query_pairs;
	}
}
