/*
 *
 *   Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.carbon.apimgt.impl.dto;


import org.wso2.carbon.apimgt.api.model.APIIdentifier;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Used for keeping removed entries. This class will be used as a globally accessible structure. Details of updated
 * APIs will be kept in this structure and a Timed Task will periodically poll this Holder when needed to invalidate
 * cache entries associated with those APIs.
 */
public class CacheInvalidationHolder {

    private static CacheInvalidationHolder invalidationHolder = new CacheInvalidationHolder();
    private Set<APIIdentifier> apiKeyMappings = new ConcurrentSkipListSet<APIIdentifier>();
    private static final int API_KEY_MAPPING_LIMIT = 10000;

    private CacheInvalidationHolder() {

    }

    public static CacheInvalidationHolder getInstance() {
        return invalidationHolder;
    }

    public void addApiKeyMapping(APIIdentifier apiIdentifier) {
        if (apiKeyMappings.size() <= API_KEY_MAPPING_LIMIT) {
            apiKeyMappings.add(apiIdentifier);
        }
    }

    public Set<APIIdentifier> getApiKeyMappings() {
        return apiKeyMappings;
    }

}
