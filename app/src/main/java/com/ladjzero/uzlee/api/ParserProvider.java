package com.ladjzero.uzlee.api;

import com.ladjzero.hipda.parsers.Parsable;

/**
 * Created by chenzhuo on 8/31/17.
 */
public interface ParserProvider {
    Parsable getParserByUrl(String urlPattern);
}
