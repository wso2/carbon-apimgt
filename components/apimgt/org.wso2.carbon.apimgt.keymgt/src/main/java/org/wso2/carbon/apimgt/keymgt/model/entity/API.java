/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.keymgt.model.entity;

import org.wso2.carbon.apimgt.api.model.subscription.CachableEntity;
import org.wso2.carbon.apimgt.api.InMemorySubscriptionValidationConstants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity for keeping API related information.
 */
public class API implements CachableEntity<String> {

    private Integer apiId = null;
    private String provider = null;
    private String name = null;
    private String version = null;
    private String context = null;
    private String policy = null;

    private Map<String, Resource> resourceMap = new HashMap<>();

    public void addResource(Resource resource) {

        resourceMap.put(resource.getUrlPattern(), resource);
    }

    public Resource getResource(String urlMapping) {

        return resourceMap.get(urlMapping);
    }

    public List<Resource> getAllResources() {

        return Arrays.asList(resourceMap.values().toArray(new Resource[]{}));
    }

    public String getContext() {

        return context;
    }

    public void setContext(String context) {

        this.context = context;
    }

    public String getApiTier() {

        return policy;
    }

    public void setApiTier(String apiTier) {

        this.policy = apiTier;
    }

    public int getApiId() {

        return apiId;
    }

    public void setApiId(int apiId) {

        this.apiId = apiId;
    }

    public String getApiProvider() {

        return provider;
    }

    public void setApiProvider(String apiProvider) {

        this.provider = apiProvider;
    }

    public String getApiName() {

        return name;
    }

    public void setApiName(String apiName) {

        this.name = apiName;
    }

    public String getApiVersion() {

        return version;
    }

    public void setApiVersion(String apiVersion) {

        this.version = apiVersion;
    }

    public String getCacheKey() {

        return context + InMemorySubscriptionValidationConstants.DELEM_PERIOD + version;
    }
}
