/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.model.API;

import java.io.Serializable;
import java.util.Comparator;

/**
 * <p>Compares APIs by their versions. This comparator supports following version string
 * format.</p>
 * <ul>
 *     <li>VersionString := VersionToken+</li>
 *     <li>VersionToken  := VersionNumber | VersionSuffix | VersionNumber VersionSuffix</li>
 *     <li>VersionNumber := [0-9]+</li>
 *     <li>VersionSuffix := ~(0-9) AnyChar*</li>
 * </ul>
 * <p>Some example version strings supported by the comparator are given below.</p>
 * <ul>
 *     <li>1.5</li>
 *     <li>2.1.1</li>
 *     <li>2.1.2b</li>
 *     <li>1.3-SNAPSHOT</li>
 *     <li>2.0.0.wso2v4</li>
 * </ul>
 * <p>Version matching is carried out by comparing the version strings token by token. Version
 * numbers are compared in the conventional manner and the suffixes are compared
 * lexicographically.</p>
 */
public class APIVersionComparator implements Comparator<API>,Serializable {

    private APIVersionStringComparator stringComparator = new APIVersionStringComparator();

    public int compare(API api1, API api2) {
        //In tenant mode, we could have same api published by two tenants to public store. So we need to check the provider as well.
        if (api1.getId().getProviderName().equals(api2.getId().getProviderName()) &&
                api1.getId().getApiName().equals(api2.getId().getApiName())) {
            return stringComparator.compare(api1.getId().getVersion(), api2.getId().getVersion());
        } else {
            APINameComparator apiNameComparator = new APINameComparator();
            return apiNameComparator.compare(api1, api2);
        }

    }
}
