/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIDefinitionContentSearchResult;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.Documentation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * This new class can be used instead of old ContentSearchResultNameComparator. This is written in an expandable
 * manner unlike ContentSearchResultNameComparator class.
 */
public class SimpleContentSearchResultNameComparator implements Comparator {

    APINameComparator nameComparator = new APINameComparator();
    APIProductNameComparator productNameComparator = new APIProductNameComparator();

    @Override
    public int compare(Object o1, Object o2) {

        // Handle simple API, APIProduct comparisons
        if (o1 instanceof API && o2 instanceof API) {
            API api1 = (API) o1;
            API api2 = (API) o2;
            return nameComparator.compare(api1, api2);
        } else if (o1 instanceof APIProduct && o2 instanceof APIProduct) {
            APIProduct apiProduct1 = (APIProduct) o1;
            APIProduct apiProduct2 = (APIProduct) o2;
            return productNameComparator.compare(apiProduct1, apiProduct2);
        }

        // Handle other comparisons
        Object[] objects = {o1, o2};
        List<String> names = new ArrayList<>();

        for (Object obj : objects) {
            if (obj instanceof API) {
                API api = (API) obj;
                names.add(api.getId().getName());
            } else if (obj instanceof APIProduct) {
                APIProduct product = (APIProduct) obj;
                names.add(product.getId().getName());
            } else if (obj instanceof APIDefinitionContentSearchResult) {
                APIDefinitionContentSearchResult defSearch = (APIDefinitionContentSearchResult) obj;
                names.add(defSearch.getApiName());
            } else if (obj instanceof Map.Entry) {
                Map.Entry entry = (Map.Entry) obj;
                if (entry.getKey() instanceof Documentation) {
                    Map.Entry<Documentation, ?> docEntry = (Map.Entry<Documentation, ?>) entry;
                    names.add(docEntry.getKey().getName());
                }
            }
        }
        return names.get(0).compareToIgnoreCase(names.get(1));

    }
}
