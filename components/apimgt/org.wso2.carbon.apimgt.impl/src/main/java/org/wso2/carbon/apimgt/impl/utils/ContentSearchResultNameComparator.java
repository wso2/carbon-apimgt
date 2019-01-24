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
import org.wso2.carbon.apimgt.api.model.Documentation;

import java.util.Comparator;
import java.util.Map;

/**
 * Reorders
 */
public class ContentSearchResultNameComparator implements Comparator{

    APINameComparator nameComparator = new APINameComparator();
    @Override public int compare(Object o1, Object o2) {
        if (o1 instanceof API && o2 instanceof API) {
            API api1 = (API) o1;
            API api2 = (API) o2;
            return nameComparator.compare(api1, api2);
        } else if (o1 instanceof API && o2 instanceof Map.Entry) {
            API api = (API) o1;
            Map.Entry<Documentation, API> documentEntry = (Map.Entry<Documentation, API>) o2;
            Documentation doc = documentEntry.getKey();
            return api.getId().getApiName().compareToIgnoreCase(doc.getName());
        } else if (o1 instanceof Map.Entry && o2 instanceof API) {
            Map.Entry<Documentation, API> documentEntry = (Map.Entry<Documentation, API>) o1;
            Documentation doc = documentEntry.getKey();
            API api = (API) o2;
            return doc.getName().compareToIgnoreCase(api.getId().getApiName());
        } else{
            Map.Entry<Documentation, API> documentEntry1 = (Map.Entry<Documentation, API>) o1;
            Documentation doc1 = documentEntry1.getKey();
            Map.Entry<Documentation, API> documentEntry2 = (Map.Entry<Documentation, API>) o2;
            Documentation doc2 = documentEntry2.getKey();
            return doc1.getName().compareToIgnoreCase(doc2.getName());
        }
    }
}
