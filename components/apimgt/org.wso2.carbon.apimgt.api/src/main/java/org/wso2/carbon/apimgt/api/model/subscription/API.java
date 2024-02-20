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

package org.wso2.carbon.apimgt.api.model.subscription;

import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Entity for keeping API related information.
 */
public class API implements CacheableEntity<String> {

    private String apiUUID;
    private int apiId = -1;
    private String provider = null;
    private String name = null;
    private String version = null;
    private String context = null;
    private String policy = null;
    private String apiType = null;
    private Boolean isDefaultVersion = false;
    private String environment;
    private String status;
    private String revision;
    private String organization;
    private Set<OperationPolicy> apiPolicies = new HashSet<>();

    public String getRevision() {

        return revision;
    }

    public void setRevision(String revision) {

        this.revision = revision;
    }

    public String getEnvironment() {

        return environment;
    }

    public void setEnvironment(String environment) {

        this.environment = environment;
    }


    private Map<String, URLMapping> resources = new HashMap<>();

    public void addResource(URLMapping urlMapping) {

        String key = urlMapping.getUrlPattern().concat(DELEM_PERIOD).concat(urlMapping.getHttpMethod());
        resources.put(key, urlMapping);
    }

    public boolean removeResource(URLMapping urlMapping) {

        String key = urlMapping.getUrlPattern().concat(DELEM_PERIOD).concat(urlMapping.getHttpMethod());
        resources.remove(key);
        return true;
    }

    public int getApiId() {

        return apiId;
    }

    public void setApiId(int apiId) {

        this.apiId = apiId;
    }

    public String getContext() {

        return context;
    }

    public void setContext(String context) {

        this.context = context;
    }

    public String getProvider() {

        return provider;
    }

    public void setProvider(String provider) {

        this.provider = provider;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getVersion() {

        return version;
    }

    public void setVersion(String version) {

        this.version = version;
    }

    public String getPolicy() {

        return policy;
    }

    public void setPolicy(String policy) {

        this.policy = policy;
    }

    public String getCacheKey() {

        return context + DELEM_PERIOD + version;
    }

    public String getApiType() {

        return apiType;
    }

    public void setApiType(String apiType) {

        this.apiType = apiType;
    }

    public URLMapping getResource(String urlPattern, String httpMethod) {

        String key = urlPattern.concat(DELEM_PERIOD).concat(httpMethod);
        return resources.get(key);
    }

    public Map<String, URLMapping> getAllResources() {

        return resources;
    }

    public Boolean isDefaultVersion() {

        return isDefaultVersion;
    }

    public void setIsDefaultVersion(Boolean defaultVersion) {

        isDefaultVersion = defaultVersion;
    }

    public String getApiUUID() {

        return apiUUID;
    }

    public void setApiUUID(String apiUUID) {

        this.apiUUID = apiUUID;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public void setApiPolicy(OperationPolicy apiPolicy) {
        this.apiPolicies.add(apiPolicy);
    }

    public Set<OperationPolicy> getApiPolicies() {
        return apiPolicies;
    }
}
