/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.api.model;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Intermediate model used to store data required for swagger processing
 */
public class SwaggerData {
    /**
     * Maps to Swagger PathItem/Operation
     */
    public static class Resource {
        private String path;
        private String verb;
        private String authType;
        private String policy;
        private Scope scope;
        private String amznResourceName;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getVerb() {
            return verb;
        }

        public void setVerb(String verb) {
            this.verb = verb;
        }

        public String getAuthType() {
            return authType;
        }

        public void setAuthType(String authType) {
            this.authType = authType;
        }

        public String getPolicy() {
            return policy;
        }

        public void setPolicy(String policy) {
            this.policy = policy;
        }

        public Scope getScope() {
            return scope;
        }

        public void setScope(Scope scope) {
            this.scope = scope;
        }

        public String getAmznResourceName() {
            return amznResourceName;
        }

        public void setAmznResourceName(String amznResourceName) {
            this.amznResourceName = amznResourceName;
        }

    }

    private String title;
    private String description;
    private String version;
    private String contactName;
    private String contactEmail;
    private String transportType;
    private String security;
    private String apiLevelPolicy;
    private Set<Resource> resources = new HashSet<>();
    private Set<Scope> scopes = new HashSet<>();

    public SwaggerData(API api) {
        title = api.getId().getName();
        description = api.getDescription();
        version = api.getId().getVersion();

        if (api.getBusinessOwner() != null) {
            contactName = api.getBusinessOwner();
        }
        if (api.getBusinessOwnerEmail() != null) {
            contactEmail = api.getBusinessOwnerEmail();
        }

        Set<URITemplate> uriTemplates = api.getUriTemplates();

        for (URITemplate uriTemplate : uriTemplates) {
            Resource resource = new Resource();
            resource.path = uriTemplate.getUriTemplate();
            resource.verb = uriTemplate.getHTTPVerb();
            resource.authType = uriTemplate.getAuthType();
            resource.policy = uriTemplate.getThrottlingTier();
            resource.scope = uriTemplate.getScope();
            resource.amznResourceName = uriTemplate.getAmznResourceName();
            resources.add(resource);
        }

        transportType = api.getType();
        security = api.getApiSecurity();
        apiLevelPolicy = api.getApiLevelPolicy();
        Set<Scope> scopes = api.getScopes();
        if (scopes != null) {
            this.scopes.addAll(scopes);
        }
    }

    public SwaggerData(APIProduct apiProduct) {
        title = apiProduct.getId().getName();
        description = apiProduct.getDescription();
        version = apiProduct.getId().getVersion();

        if (apiProduct.getBusinessOwner() != null) {
            contactName = apiProduct.getBusinessOwner();
        }
        if (apiProduct.getBusinessOwnerEmail() != null) {
            contactEmail = apiProduct.getBusinessOwnerEmail();
        }

        List<APIProductResource> productResources = apiProduct.getProductResources();

        for (APIProductResource productResource : productResources) {
            URITemplate uriTemplate = productResource.getUriTemplate();
            Resource resource = new Resource();

            resource.path = uriTemplate.getUriTemplate();
            resource.verb = uriTemplate.getHTTPVerb();
            resource.authType = uriTemplate.getAuthType();
            resource.policy = uriTemplate.getThrottlingTier();
            resource.scope = uriTemplate.getScope();
            resource.amznResourceName = uriTemplate.getAmznResourceName();
            resources.add(resource);
        }
        Set<Scope> scopes = apiProduct.getScopes();

        if (scopes != null) {
            this.scopes.addAll(scopes);
        }
    }

    public Set<Resource> getResources() {
        return resources;
    }

    public Set<Scope> getScopes() {
        return scopes;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getTransportType() {
        return transportType;
    }

    public String getSecurity() {
        return security;
    }

    public String getApiLevelPolicy() {
        return apiLevelPolicy;
    }
}
