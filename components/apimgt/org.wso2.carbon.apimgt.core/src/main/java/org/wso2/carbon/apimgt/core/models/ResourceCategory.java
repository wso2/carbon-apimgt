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

package org.wso2.carbon.apimgt.core.models;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents Resource Categories supported for {@code ArtifactResourceMetaData}.
 */

public enum ResourceCategory {
    OTHER,
    SWAGGER,
    WSDL_URI,
    IMAGE,
    DOC;

    private static final Map<String, ResourceCategory> lookup = new HashMap<>();

    static {
        for (ResourceCategory category : EnumSet.allOf(ResourceCategory.class)) {
            lookup.put(category.toString(), category);
        }

        lookup.remove(OTHER.toString());
    }

    public static ResourceCategory toValue(String dayOfWeek) {
        ResourceCategory value = lookup.get(dayOfWeek);

        if (value == null) {
            return ResourceCategory.OTHER;
        }

        return value;
    }

    public static Map<String, ResourceCategory> getStandardCategories() {
        return new HashMap<>(lookup);
    }

}
