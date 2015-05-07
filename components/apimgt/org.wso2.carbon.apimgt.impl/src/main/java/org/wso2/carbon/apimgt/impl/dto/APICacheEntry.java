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
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache entry for representing API Object. Used for creating an index for KeyValidationInfoCache.
 */
public class APICacheEntry implements Serializable{

    private String context;
    private String version;
    Map<String,ApplicationCacheEntry> applicationEntryMap = new ConcurrentHashMap<String, ApplicationCacheEntry>();
    private Set<String> applicationEntries;

    public APICacheEntry(String context,String version){
        this.context = context;
        this.version = version;
    }

    public void addApplicationCacheEntry(String applicationId,ApplicationCacheEntry cacheEntry){
        applicationEntryMap.put(applicationId,cacheEntry);
    }

    public ApplicationCacheEntry getApplicationCacheEntry(String applicationId){
        return applicationEntryMap.get(applicationId);
    }

    public String getCacheKey(){
        return createCacheKey(context,version);
    }

    public static String createCacheKey(String context,String version){
        return context+"/"+version;
    }

    public void removeApplicationEntry(String applicationId) {
        applicationEntryMap.remove(applicationId);
    }

    public boolean isEmpty(){
        return applicationEntryMap.isEmpty();
    }

    public Collection<ApplicationCacheEntry> getApplicationEntries() {
        return applicationEntryMap.values();
    }
}
