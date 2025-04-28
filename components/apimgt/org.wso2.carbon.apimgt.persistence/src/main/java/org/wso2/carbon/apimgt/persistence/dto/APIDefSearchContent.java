/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.persistence.dto;

/**
 * This hold swagger content search result content.
 */
public class APIDefSearchContent implements SearchContent {

    String id;
    String type = "DEFINITION";
    String name; // Definition file name
    String apiName;
    String apiContext;
    String apiVersion;
    String apiProvider;
    String apiUUID;
    String apiRating;
    ApiType apiType;
    String associatedType = "API";

    /**
     * Holds different API Types for content search.
     */
    public enum ApiType {
        REST,
        ASYNC,
        GRAPHQL,
        SOAP
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiProvider() {
        return apiProvider;
    }

    public void setApiProvider(String apiProvider) {
        this.apiProvider = apiProvider;
    }

    public String getApiUUID() {
        return apiUUID;
    }

    public void setApiUUID(String apiUUID) {
        this.apiUUID = apiUUID;
    }

    public String getAssociatedType() {
        return associatedType;
    }

    public void setAssociatedType(String associatedType) {
        this.associatedType = associatedType;
    }

    public String getApiType() {
        if (apiType == null) {
            return null;
        }
        return apiType.toString();
    }

    public void setApiType(ApiType apiType) {
        this.apiType = apiType;
    }

    public String getApiContext() {
        return apiContext;
    }

    public void setApiContext(String apiContext) {
        this.apiContext = apiContext;
    }

    public String getApiRating() {
        return apiRating;
    }

    public void setApiRating(String apiRating) {
        this.apiRating = apiRating;
    }
}
