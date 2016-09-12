/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.model.APIProduct;

import java.io.Serializable;
import java.util.Comparator;

/**
 * This comparator used to order APIProducts by name.
 */
public class APIProductNameComparator implements Comparator<APIProduct>, Serializable {

    @Override
    public int compare(APIProduct apiProduct1, APIProduct apiProduct2) {
        if (apiProduct1.getId().getProviderName().equals(apiProduct2.getId().getProviderName())) {
            if (apiProduct1.getId().getApiProductName().equals(apiProduct2.getId().getApiProductName())) {
                //only compare version
                return apiProduct1.getId().getVersion().compareToIgnoreCase(apiProduct2.getId().getVersion());
            } else {
                //only compare API name
                return apiProduct1.getId().getApiProductName().compareToIgnoreCase(apiProduct2.getId().getApiProductName());
            }
        } else {
            //only compare provider name
            return apiProduct1.getId().getProviderName().compareToIgnoreCase(apiProduct2.getId().getProviderName());
        }
    }
}
