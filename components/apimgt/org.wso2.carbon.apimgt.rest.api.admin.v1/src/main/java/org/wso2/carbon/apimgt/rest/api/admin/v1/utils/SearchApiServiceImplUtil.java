/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SearchApiServiceImplUtil {

    private SearchApiServiceImplUtil() {
        // to hide default constructor
    }

    /**
     * Get API List from API search results
     *
     * @param resultsMap API search result map
     * @return API List
     */
    public static List<Object> getAPIListFromAPISearchResult(Map<String, Object> resultsMap) {

        if (resultsMap == null || !resultsMap.containsKey("apis"))
            return new ArrayList<>();

        Object apiSearchResults = resultsMap.get("apis");

        if (apiSearchResults == null) {
            return new ArrayList<>();
        }

        List<Object> apis = new ArrayList<>();

        if (apiSearchResults instanceof List<?>) {
            apis.addAll((List<?>) apiSearchResults);
        } else if (apiSearchResults instanceof Map<?, ?>) {
            Map<?, ?> resultMap = (Map<?, ?>) apiSearchResults;
            apis.addAll(resultMap.values());
        } else if (apiSearchResults instanceof Collection<?>) {
            apis.addAll((Collection<?>) apiSearchResults);
        } else {
            apis.add(apiSearchResults);
        }
        return apis;
    }

}
