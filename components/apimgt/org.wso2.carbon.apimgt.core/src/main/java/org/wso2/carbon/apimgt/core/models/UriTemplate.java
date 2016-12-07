
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

import java.io.Serializable;

/**
 * This Class contains the model of Uri Templates
 */
public final class UriTemplate implements Serializable {

    private static final long serialVersionUID = 2829155480731229681L;
    private final String uriTemplate;
    private final String httpVerb;
    private final String authType;
    private final String policy;


    private UriTemplate(UriTemplateBuilder uriTemplateBuilder) {
        uriTemplate = uriTemplateBuilder.uriTemplate;
        httpVerb = uriTemplateBuilder.httpVerb;
        authType = uriTemplateBuilder.authType;
        policy = uriTemplateBuilder.policy;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UriTemplate that = (UriTemplate) o;

        if ((uriTemplate != null) ?
                !uriTemplate.equals(that.uriTemplate) : (that.uriTemplate != null)) {
            return false;
        }
        if ((httpVerb != null) ?
                !httpVerb.equals(that.httpVerb) : (that.httpVerb != null)) {
            return false;
        }
        if ((authType != null) ?
                !authType.equals(that.authType) : (that.authType != null)) {
            return false;
        }
        if ((policy != null) ?
                !policy.equals(that.policy) : (that.policy != null)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = uriTemplate != null ? uriTemplate.hashCode() : 0;
        result = 31 * result + (httpVerb != null ? httpVerb.hashCode() : 0);
        result = 31 * result + (authType != null ? authType.hashCode() : 0);
        result = 31 * result + (policy != null ? policy.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UriTemplate{" +
                "uriTemplate='" + uriTemplate + '\'' +
                ", httpVerb='" + httpVerb + '\'' +
                ", authType='" + authType + '\'' +
                ", policy='" + policy + '\'' +
                '}';
    }

    /**
     *  Builder class for getInstance
     */
    public static final class UriTemplateBuilder {
        private String uriTemplate;
        private String httpVerb;
        private String authType;
        private String policy;

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

        public UriTemplate build() {
            return new UriTemplate(this);
        }
    }
}
