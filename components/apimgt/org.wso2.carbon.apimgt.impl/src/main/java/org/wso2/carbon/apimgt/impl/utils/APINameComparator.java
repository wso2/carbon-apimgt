/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.io.Serializable;
import java.util.Comparator;

/**
 * This comparator used to order APIs by name.
 */

/**
 * TODO add logic to put weight on each parameter. compare by provider and then by name and finally by version
 */
public class APINameComparator implements Comparator<API>, Serializable {

    public int compare(API api1, API api2) {
        if (api1.getId().getProviderName().equals(api2.getId().getProviderName())) {
            if (api1.getId().getApiName().equals(api2.getId().getApiName())) {
                //only compare version
                return api1.getId().getVersion().compareToIgnoreCase(api2.getId().getVersion());
            } else {
                //only compare API name
                return api1.getId().getApiName().compareToIgnoreCase(api2.getId().getApiName());
            }
        } else {
            //only compare provider name
            return api1.getId().getProviderName().compareToIgnoreCase(api2.getId().getProviderName());
        }
    }
}
