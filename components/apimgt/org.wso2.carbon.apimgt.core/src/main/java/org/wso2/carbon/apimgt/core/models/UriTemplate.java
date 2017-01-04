
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

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Objects;

/**
 * This Class contains the model of Uri Templates
 */
public final class UriTemplate implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String uriTemplate;
    private final String httpVerb;
    private final String authType;
    private final String policy;
    private final String endpointId;

    private UriTemplate(UriTemplateBuilder uriTemplateBuilder) {
        uriTemplate = uriTemplateBuilder.uriTemplate;
        httpVerb = uriTemplateBuilder.httpVerb;
        authType = uriTemplateBuilder.authType;
        policy = uriTemplateBuilder.policy;
        endpointId = uriTemplateBuilder.endpointId;
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

    public String getEndpointId() {
        return endpointId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UriTemplate that = (UriTemplate) o;
        return Objects.equals(uriTemplate, that.uriTemplate) &&
                Objects.equals(httpVerb, that.httpVerb) &&
                Objects.equals(authType, that.authType) &&
                Objects.equals(policy, that.policy) &&
                Objects.equals(endpointId, that.endpointId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uriTemplate, httpVerb, authType, policy, endpointId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("uriTemplate", uriTemplate)
                .append("httpVerb", httpVerb)
                .append("authType", authType)
                .append("policy", policy).append("endpointId", endpointId)
                .toString();
    }

    /**
     * Builder class for getInstance
     */
    public static final class UriTemplateBuilder {
        private String uriTemplate;
        private String httpVerb;
        private String authType;
        private String policy;
        private String endpointId;

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

        public UriTemplateBuilder endpointId(String endpointId) {
            this.endpointId = endpointId;
            return this;
        }

        public UriTemplate build() {
            return new UriTemplate(this);
        }
    }
}
