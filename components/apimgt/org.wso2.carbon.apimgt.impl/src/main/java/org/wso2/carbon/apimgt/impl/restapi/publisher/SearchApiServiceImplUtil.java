/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.apimgt.impl.restapi.publisher;

import java.util.*;

public class SearchApiServiceImplUtil {

    private SearchApiServiceImplUtil() {
        //to hide default constructor
    }

    /**
     * @param resultsMap API search result map
     * @return API List
     */
    public static List<Object> getAPIListFromAPISearchResult(Map<String, Object> resultsMap) {

        List<Object> apis;
        Object apiSearchResults = resultsMap.get("apis");
        if (apiSearchResults instanceof List<?>) {
            apis = (ArrayList<Object>) apiSearchResults;
        } else if (apiSearchResults instanceof HashMap) {
            Collection<String> values = ((HashMap) apiSearchResults).values();
            apis = new ArrayList<>(values);
        } else {
            apis = new ArrayList<>();
            apis.addAll((Collection<?>) apiSearchResults);
        }
        return apis;
    }

}
