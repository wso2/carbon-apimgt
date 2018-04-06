/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
