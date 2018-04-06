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

package org.wso2.carbon.apimgt.core.dao.impl;

import org.wso2.carbon.apimgt.core.dao.SearchType;

import java.util.EnumMap;
import java.util.Map;

class SearchQueryDictionary {
    private static final Map<SearchType, StoreApiAttributeSearch> searchMap = new EnumMap<>(SearchType.class);

    static {
        searchMap.put(SearchType.TAG, new StoreTagSearchImpl());
        //for subcontext search, need to check AM_API_OPERATION_MAPPING table
        searchMap.put(SearchType.SUBCONTEXT, new StoreSubcontextSearchImpl());
        //for any other attribute search, need to check AM_API table
        searchMap.put(SearchType.PROVIDER, new StoreGenericSearchImpl());
        searchMap.put(SearchType.VERSION, new StoreGenericSearchImpl());
        searchMap.put(SearchType.CONTEXT, new StoreGenericSearchImpl());
        searchMap.put(SearchType.DESCRIPTION, new StoreGenericSearchImpl());
    }

    static StoreApiAttributeSearch getSearchQuery(SearchType searchType) {
        return searchMap.get(searchType);
    }

}
