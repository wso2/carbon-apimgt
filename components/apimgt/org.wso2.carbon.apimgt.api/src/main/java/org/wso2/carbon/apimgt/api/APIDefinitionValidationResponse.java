/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.api;

import java.util.ArrayList;
import java.util.List;

/**
 * The model containing API Definition (OpenAPI/Swagger) Validation Information
 */
public class APIDefinitionValidationResponse {
    private boolean isValid = false;
    private String content;
    private String jsonContent;
    private Info info;
    private APIDefinition parser;
    private ArrayList<ErrorHandler> errorItems = new ArrayList<>();

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getJsonContent() {
        return jsonContent;
    }

    public void setJsonContent(String jsonContent) {
        this.jsonContent = jsonContent;
    }

    public void setErrorItems(ArrayList<ErrorHandler> errorItems) {
        this.errorItems = errorItems;
    }

    public ArrayList<ErrorHandler> getErrorItems() {
        return errorItems;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public Info getInfo() {
        return info;
    }

    public static class Info {
        private String openAPIVersion;
        private String name;
        private String version;
        private String context;
        private String description;
        private List<String> endpoints;

        public String getOpenAPIVersion() {
            return openAPIVersion;
        }

        public void setOpenAPIVersion(String openAPIVersion) {
            this.openAPIVersion = openAPIVersion;
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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getContext() {
            return context;
        }

        public void setContext(String context) {
            this.context = context;
        }

        public List<String> getEndpoints() { return endpoints; }

        public void setEndpoints(List<String> endpoints) { this.endpoints = endpoints; }
    }

    public APIDefinition getParser() {
        return parser;
    }

    public void setParser(APIDefinition parser) {
        this.parser = parser;
    }
}
