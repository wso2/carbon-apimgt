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
