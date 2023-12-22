/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;

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
public class APIProductVersionComparator implements Comparator<APIProduct>,Serializable {

    private APIVersionStringComparator stringComparator = new APIVersionStringComparator();

    @Override
    public int compare(APIProduct apiProduct1, APIProduct apiProduct2) {
        if (apiProduct1.getId().getProviderName().equals(apiProduct2.getId().getProviderName()) &&
                apiProduct1.getId().getName().equals(apiProduct2.getId().getName())) {
            return stringComparator.compare(apiProduct1.getId().getVersion(), apiProduct2.getId().getVersion());
        } else {
            APIProductNameComparator apiproductNameComparator = new APIProductNameComparator();
            return apiproductNameComparator.compare(apiProduct1, apiProduct2);
        }

    }
}
