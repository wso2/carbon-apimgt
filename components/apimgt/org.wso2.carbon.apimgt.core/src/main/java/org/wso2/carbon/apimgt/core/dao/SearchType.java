package org.wso2.carbon.apimgt.core.dao;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.CheckForNull;


/**
 * Supported API search types
 */
public enum SearchType {
    NAME,
    TAG,
    SUBCONTEXT,
    PROVIDER,
    VERSION,
    CONTEXT,
    DESCRIPTION;

    private static final Map<String, SearchType> stringToEnum = new HashMap<>();

    static { // Initialize map from constant name to enum constant
        for (SearchType searchType : values()) {
            stringToEnum.put(searchType.toString(), searchType);
        }
    }

    // Returns SearchType for string, or null if string is invalid
    @CheckForNull
    public static SearchType fromString(String symbol) {
        return stringToEnum.get(symbol.toUpperCase(Locale.ENGLISH));
    }
}
