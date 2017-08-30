package com.ladjzero.hipda.parsers;

import com.ladjzero.hipda.api.Response;

/**
 * Created by chenzhuo on 2017/4/23.
 */
public interface Parsable {
	Response parse(String html);
	boolean test(String urlPattern);
}
