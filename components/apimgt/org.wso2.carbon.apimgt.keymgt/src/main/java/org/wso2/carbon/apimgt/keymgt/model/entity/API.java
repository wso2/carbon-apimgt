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

import org.wso2.carbon.apimgt.api.model.subscription.CacheableEntity;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity for keeping API related information.
 */
public class API implements CacheableEntity<String> {
    private String uuid;
    private Integer apiId = null;
    private String provider = null;
    private String name = null;
    private String version = null;
    private String context = null;
    private String policy = null;
    private String apiType = null;
    private String status;
    private boolean isDefaultVersion = false;

    public API() {
    }

    /**
     *
     * @param uuid
     * @param apiId
     * @param provider
     * @param name
     * @param version
     * @param context
     * @param policy
     * @param apiType
     * @param status
     * @param isDefaultVersion
     */
    public API(String uuid, Integer apiId, String provider, String name, String version, String context,
               String policy, String apiType, String status, boolean isDefaultVersion) {
        this.uuid = uuid;
        this.apiId = apiId;
        this.provider = provider;
        this.name = name;
        this.version = version;
        this.context = context;
        this.policy = policy;
        this.apiType = apiType;
        this.status = status;
        this.isDefaultVersion = isDefaultVersion;
    }

    private List<URLMapping> urlMappings = new ArrayList<>();


    public void addResource(URLMapping resource) {

        urlMappings.add(resource);
    }

    public List<URLMapping> getResources() {

        return urlMappings;
    }

    public void removeResource(URLMapping resource) {
        urlMappings.remove(resource);
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

        return context + DELEM_PERIOD + version;
    }

    public String getApiType() {

        return apiType;
    }

    public void setApiType(String apiType) {

        this.apiType = apiType;
    }

    @Override
    public String toString() {

        return "API{" +
                "uuid='" + uuid + '\'' +
                ", apiId=" + apiId +
                ", provider='" + provider + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", context='" + context + '\'' +
                ", policy='" + policy + '\'' +
                ", apiType='" + apiType + '\'' +
                ", status='" + status + '\'' +
                ", isDefaultVersion=" + isDefaultVersion +
                ", urlMappings=" + urlMappings +
                '}';
    }

    public boolean isDefaultVersion() {
        return isDefaultVersion;
    }

    public void setDefaultVersion(boolean isDefaultVersion) {
        this.isDefaultVersion = isDefaultVersion;
    }

    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }
}
