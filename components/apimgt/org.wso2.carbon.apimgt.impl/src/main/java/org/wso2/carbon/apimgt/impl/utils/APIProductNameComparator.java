/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.model.APIProduct;

import java.io.Serializable;
import java.util.Comparator;

/**
 * This comparator used to order APIProducts by name.
 */
public class APIProductNameComparator implements Comparator<APIProduct>, Serializable {

    public int compare(APIProduct product1, APIProduct product2) {
        if (product1.getId().getName().equalsIgnoreCase(product2.getId().getName())) {
            if (product1.getId().getVersion().equals(product2.getId().getVersion())) {
                //only compare APIProduct provider name
                return product1.getId().getProviderName().compareToIgnoreCase(product2.getId().getProviderName());
            } else {
                //only compare APIProduct version
                return product1.getId().getVersion().compareToIgnoreCase(product2.getId().getVersion());
            }
        } else {
            //only compare APIProduct name
            return product1.getId().getName().compareToIgnoreCase(product2.getId().getName());
        }
    }
}
