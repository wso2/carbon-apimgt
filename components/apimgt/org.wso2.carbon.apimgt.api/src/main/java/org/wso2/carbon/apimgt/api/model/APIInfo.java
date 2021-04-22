/*
 *
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.APIConstants;

public class APIInfo {
    private String id;
    private String name;
    private String version;
    private String provider;
    private String context;
    private String contextTemplate;
    private String apiTier;
    private String apiType;
    private String createdTime;
    private String createdBy;
    private String updatedTime;
    private String updatedBy;
    private APIStatus status;
    private int revisionsCreated;

    private APIInfo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getContextTemplate() {
        return contextTemplate;
    }

    public void setContextTemplate(String contextTemplate) {
        this.contextTemplate = contextTemplate;
    }

    public String getApiTier() {
        return apiTier;
    }

    public void setApiTier(String apiTier) {
        this.apiTier = apiTier;
    }

    public String getApiType() {
        return apiType;
    }

    public void setApiType(String apiType) {
        this.apiType = apiType;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public APIStatus getStatus() {
        return status;
    }

    public APIIdentifier toAPIIdentifier() {
        String providerEmailDomainReplaced = this.provider;
        if (provider != null && provider.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR)) {
            providerEmailDomainReplaced = provider.replace(APIConstants.EMAIL_DOMAIN_SEPARATOR, APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT);
        }
        return new APIIdentifier(providerEmailDomainReplaced, name, version);
    }

    public void setStatus(APIStatus status) {
        this.status = status;
    }

    public int getRevisionsCreated() {
        return revisionsCreated;
    }

    public void setRevisionsCreated(int revisionsCreated) {
        this.revisionsCreated = revisionsCreated;
    }

    public static class Builder {
        private String id;
        private String name;
        private String version;
        private String provider;
        private String context;
        private String contextTemplate;
        private String apiTier;
        private String apiType;
        private String createdTime;
        private String createdBy;
        private String updatedTime;
        private String updatedBy;
        private APIStatus status;
        private int revisionsCreated;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder context(String context) {
            this.context = context;
            return this;
        }

        public Builder contextTemplate(String contextTemplate) {
            this.contextTemplate = contextTemplate;
            return this;
        }

        public Builder apiTier(String apiTier) {
            this.apiTier = apiTier;
            return this;
        }

        public Builder apiType(String apiType) {
            this.apiType = apiType;
            return this;
        }

        public Builder createdTime(String createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder updatedTime(String updatedTime) {
            this.updatedTime = updatedTime;
            return this;
        }

        public Builder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public Builder status(APIStatus status) {
            this.status = status;
            return this;
        }

        public Builder revisionsCreated(int revisionsCreated) {
            this.revisionsCreated = revisionsCreated;
            return this;
        }

        public APIInfo build() {
            APIInfo apiInfo = new APIInfo();
            apiInfo.id = id;
            apiInfo.name = name;
            apiInfo.version = version;
            apiInfo.provider = provider;
            apiInfo.context = context;
            apiInfo.contextTemplate = contextTemplate;
            apiInfo.apiTier = apiTier;
            apiInfo.apiType = apiType;
            apiInfo.createdTime = createdTime;
            apiInfo.createdBy = createdBy;
            apiInfo.updatedTime = updatedTime;
            apiInfo.updatedBy = updatedBy;
            apiInfo.status = status;
            apiInfo.revisionsCreated = revisionsCreated;
            return apiInfo;
        }
    }
}

