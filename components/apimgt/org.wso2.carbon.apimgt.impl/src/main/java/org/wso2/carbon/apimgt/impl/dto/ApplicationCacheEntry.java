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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cache entry for grouping Cache Keys by Application Id.
 */
public class ApplicationCacheEntry implements Serializable{
    private String applicationId;
    private Set<String> cacheKeys = new HashSet<String>();

    public ApplicationCacheEntry(String applicationId){
        this.applicationId = applicationId;
    }

    public void addCacheKey(String cacheKey){
        cacheKeys.add(cacheKey);
    }

    public void removeCacheKey(String cacheKey){
        cacheKeys.remove(cacheKey);
    }

    public boolean isExist(String cacheKey){
        return cacheKeys.contains(cacheKey);
    }

    public Set<String> getCacheKeys(){
        return cacheKeys;
    }

    public String getApplicationId() {
        return applicationId;
    }
}
