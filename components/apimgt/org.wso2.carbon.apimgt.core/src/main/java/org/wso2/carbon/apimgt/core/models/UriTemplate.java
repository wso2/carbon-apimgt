
/***********************************************************************************************************************
 *
 *  *
 *  *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  *   WSO2 Inc. licenses this file to you under the Apache License,
 *  *   Version 2.0 (the "License"); you may not use this file except
 *  *   in compliance with the License.
 *  *   You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing,
 *  *  software distributed under the License is distributed on an
 *  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  *  KIND, either express or implied.  See the License for the
 *  *  specific language governing permissions and limitations
 *  *  under the License.
 *  *
 *
 */

package org.wso2.carbon.apimgt.core.models;

/**
 * This Class contains the model of Uri Templates
 */
public final class UriTemplate {

    private final String uriTemplate;
    private final String httpVerb;
    private final String authType;
    private final String policy;
    private final Scope scope;
    private final String templateId;
    private final String produces;
    private final String consumes;


    private UriTemplate(UriTemplateBuilder uriTemplateBuilder) {
        uriTemplate = uriTemplateBuilder.uriTemplate;
        httpVerb = uriTemplateBuilder.httpVerb;
        authType = uriTemplateBuilder.authType;
        policy = uriTemplateBuilder.policy;
        scope = uriTemplateBuilder.scope;
        templateId = uriTemplateBuilder.templateId;
        produces = uriTemplateBuilder.produces;
        consumes = uriTemplateBuilder.consumes;
    }

    public String getUriTemplate() {
        return uriTemplate;
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public String getAuthType() {
        return authType;
    }

    public String getPolicy() {
        return policy;
    }

    public Scope getScope() {
        return scope;
    }

    /**
     *  Builder class for getInstance
     */
    public static final class UriTemplateBuilder {
        private String uriTemplate;
        private String httpVerb;
        private String authType;
        private String policy;
        private String templateId;
        private String produces;
        private String consumes;
        private Scope scope;

        public UriTemplateBuilder() {
        }

        public static UriTemplateBuilder getInstance() {
            return new UriTemplateBuilder();
        }

        public UriTemplateBuilder uriTemplate(String uriTemplate) {
            this.uriTemplate = uriTemplate;
            return this;
        }

        public UriTemplateBuilder httpVerb(String httpVerb) {
            this.httpVerb = httpVerb;
            return this;
        }

        public UriTemplateBuilder authType(String authType) {
            this.authType = authType;
            return this;
        }

        public UriTemplateBuilder policy(String policy) {
            this.policy = policy;
            return this;
        }

        public UriTemplateBuilder scope(Scope scope) {
            this.scope = scope;
            return this;
        }

        public UriTemplateBuilder produces(String produces) {
            this.produces = produces;
            return this;
        }
        public UriTemplateBuilder consumes(String consumes) {
            this.consumes = consumes;
            return this;
        }
        public UriTemplateBuilder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }
        public UriTemplate build() {

            return new UriTemplate(this);
        }
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getProduces() {
        return produces;
    }

    public String getConsumes() {
        return consumes;
    }
}
