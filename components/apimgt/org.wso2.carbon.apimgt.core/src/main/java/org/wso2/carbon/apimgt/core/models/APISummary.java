/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.core.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean Class for API summery to be loaded at server startup
 */
public class APISummary implements Serializable{
    private String id;

    private String name;

    private String context;

    private List<UriTemplate> uriTemplates = new ArrayList<UriTemplate>();

    public APISummary(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public List<UriTemplate> getUriTemplates() {
        return uriTemplates;
    }

    public void setUriTemplates(List<UriTemplate> uriTemplates) {
        this.uriTemplates = uriTemplates;
    }

    /*
        * API resources associated with an api
        * */
 /*   public class APIResource {
        private String uriTemplate;

        private String httpVerb;

        private String authType;

        public String getUriTemplate() {
            return uriTemplate;
        }

        public void setUriTemplate(String uriTemplate) {
            this.uriTemplate = uriTemplate;
        }

        public String getHttpVerb() {
            return httpVerb;
        }

        public void setHttpVerb(String httpVerb) {
            this.httpVerb = httpVerb;
        }

        public String getAuthType() {
            return authType;
        }

        public void setAuthType(String authType) {
            this.authType = authType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof APIResource)) return false;

            APIResource that = (APIResource) o;

            if (authType != null ? !authType.equals(that.authType) : that.authType != null) return false;
            if (httpVerb != null ? !httpVerb.equals(that.httpVerb) : that.httpVerb != null) return false;
            if (uriTemplate != null ? !uriTemplate.equals(that.uriTemplate) : that.uriTemplate != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = uriTemplate != null ? uriTemplate.hashCode() : 0;
            result = 31 * result + (httpVerb != null ? httpVerb.hashCode() : 0);
            result = 31 * result + (authType != null ? authType.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "APIResource{" +
                    "uriTemplate='" + uriTemplate + '\'' +
                    ", httpVerb='" + httpVerb + '\'' +
                    ", authType='" + authType + '\'' +
                    '}';
        }
    }
*/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof APISummary)) return false;

        APISummary apiInfo = (APISummary) o;

        if (context != null ? !context.equals(apiInfo.context) : apiInfo.context != null) return false;
        if (!id.equals(apiInfo.id)) return false;
        if (name != null ? !name.equals(apiInfo.name) : apiInfo.name != null) return false;
        if (uriTemplates != null ? !uriTemplates.equals(apiInfo.uriTemplates) : apiInfo.uriTemplates != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        result = 31 * result + (uriTemplates != null ? uriTemplates.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "APIInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", context='" + context + '\'' +
                uriTemplates.toString() +
                '}';
    }
}
