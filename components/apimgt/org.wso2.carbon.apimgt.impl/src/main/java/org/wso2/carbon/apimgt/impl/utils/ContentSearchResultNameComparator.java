/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.Documentation;

import java.util.Comparator;
import java.util.Map;

/**
 * Reorders
 */
public class ContentSearchResultNameComparator implements Comparator {

    APINameComparator nameComparator = new APINameComparator();
    APIProductNameComparator productNameComparator = new APIProductNameComparator();

    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof API && o2 instanceof API) {
            API api1 = (API) o1;
            API api2 = (API) o2;
            return nameComparator.compare(api1, api2);
        } else if (o1 instanceof APIProduct && o2 instanceof APIProduct) {
            APIProduct apiProduct1 = (APIProduct) o1;
            APIProduct apiProduct2 = (APIProduct) o2;
            return productNameComparator.compare(apiProduct1, apiProduct2);
        } else if (o1 instanceof API &&  o2 instanceof APIProduct) {
            API api = (API) o1;
            APIProduct apiProduct = (APIProduct) o2;
            return api.getId().getApiName().compareToIgnoreCase(apiProduct.getId().getName());
        } else if (o1 instanceof APIProduct && o2 instanceof API) {
            APIProduct apiProduct = (APIProduct) o1;
            API api = (API) o2;
            return apiProduct.getId().getName().compareToIgnoreCase(api.getId().getName());
        } else if (o1 instanceof API && o2 instanceof Map.Entry) {
            API api = (API) o1;
            if (((Map.Entry) o2).getValue() instanceof API) {
                Map.Entry<Documentation, API> documentEntry = (Map.Entry<Documentation, API>) o2;
                Documentation doc = documentEntry.getKey();
                return api.getId().getApiName().compareToIgnoreCase(doc.getName());
            } else {
                Map.Entry<Documentation, APIProduct> documentEntry = (Map.Entry<Documentation, APIProduct>) o2;
                Documentation doc = documentEntry.getKey();
                return api.getId().getApiName().compareToIgnoreCase(doc.getName());
            }
        } else if (o1 instanceof Map.Entry && o2 instanceof API) {
            API api = (API) o2;
            if (((Map.Entry) o1).getValue() instanceof API) {
                Map.Entry<Documentation, API> documentEntry = (Map.Entry<Documentation, API>) o1;
                Documentation doc = documentEntry.getKey();
                return api.getId().getApiName().compareToIgnoreCase(doc.getName());
            } else {
                Map.Entry<Documentation, APIProduct> documentEntry = (Map.Entry<Documentation, APIProduct>) o1;
                Documentation doc = documentEntry.getKey();
                return api.getId().getApiName().compareToIgnoreCase(doc.getName());
            }
        } else if (o1 instanceof APIProduct && o2 instanceof Map.Entry) {
            APIProduct api = (APIProduct) o1;
            if (((Map.Entry) o2).getValue() instanceof API) {
                Map.Entry<Documentation, API> documentEntry = (Map.Entry<Documentation, API>) o2;
                Documentation doc = documentEntry.getKey();
                return api.getId().getName().compareToIgnoreCase(doc.getName());
            } else {
                Map.Entry<Documentation, APIProduct> documentEntry = (Map.Entry<Documentation, APIProduct>) o2;
                Documentation doc = documentEntry.getKey();
                return api.getId().getName().compareToIgnoreCase(doc.getName());
            }
        } else if (o1 instanceof Map.Entry && o2 instanceof APIProduct) {
            APIProduct api = (APIProduct) o2;
            if (((Map.Entry) o1).getValue() instanceof API) {
                Map.Entry<Documentation, API> documentEntry = (Map.Entry<Documentation, API>) o1;
                Documentation doc = documentEntry.getKey();
                return api.getId().getName().compareToIgnoreCase(doc.getName());
            } else {
                Map.Entry<Documentation, APIProduct> documentEntry = (Map.Entry<Documentation, APIProduct>) o1;
                Documentation doc = documentEntry.getKey();
                return api.getId().getName().compareToIgnoreCase(doc.getName());
            }
        } else {
            Documentation doc1;
            Documentation doc2;
            if (((Map.Entry) o1).getValue() instanceof APIProduct && ((Map.Entry) o2).getValue() instanceof
                    APIProduct) {
                Map.Entry<Documentation, APIProduct> documentEntry1 = (Map.Entry<Documentation, APIProduct>) o1;
                doc1 = documentEntry1.getKey();
                Map.Entry<Documentation, APIProduct> documentEntry2 = (Map.Entry<Documentation, APIProduct>) o2;
                doc2 = documentEntry2.getKey();
            } else if (((Map.Entry) o1).getValue() instanceof API && ((Map.Entry) o2).getValue()
                    instanceof APIProduct) {
                Map.Entry<Documentation, API> documentEntry1 = (Map.Entry<Documentation, API>) o1;
                doc1 = documentEntry1.getKey();
                Map.Entry<Documentation, APIProduct> documentEntry2 = (Map.Entry<Documentation, APIProduct>) o2;
                doc2 = documentEntry2.getKey();
            } else if (((Map.Entry) o1).getValue() instanceof APIProduct && ((Map.Entry) o2).getValue()
                    instanceof API) {
                Map.Entry<Documentation, APIProduct> documentEntry1 = (Map.Entry<Documentation, APIProduct>) o1;
                doc1 = documentEntry1.getKey();
                Map.Entry<Documentation, API> documentEntry2 = (Map.Entry<Documentation, API>) o2;
                doc2 = documentEntry2.getKey();
            } else {
                Map.Entry<Documentation, API> documentEntry1 = (Map.Entry<Documentation, API>) o1;
                doc1 = documentEntry1.getKey();
                Map.Entry<Documentation, API> documentEntry2 = (Map.Entry<Documentation, API>) o2;
                doc2 = documentEntry2.getKey();
            }
            return doc1.getName().compareToIgnoreCase(doc2.getName());
        }
    }
}
