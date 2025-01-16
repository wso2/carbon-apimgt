/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.impl.model;

/**
 * This class represents different types of artifacts available for Governing
 */
public enum ArtifactType {
    REST_API,
    SOAP_API,
    GRAPHQL_API,
    ASYNC_API,
    ALL_API;

    public static ArtifactType fromString(String text) {
        if ("rest_api".equalsIgnoreCase(text)) {
            return REST_API;
        } else if ("soap_api".equalsIgnoreCase(text)) {
            return SOAP_API;
        } else if ("graphql_api".equalsIgnoreCase(text)) {
            return GRAPHQL_API;
        } else if ("async_api".equalsIgnoreCase(text)) {
            return ASYNC_API;
        } else if ("all_api".equalsIgnoreCase(text)) {
            return ALL_API;
        }
        return REST_API;
    }

    // TODO: Fix logic to get artifact type from APIM artifact type
    public static ArtifactType fromAPIMArtifactType(String artifactType) {
        if ("rest_api".equalsIgnoreCase(artifactType)) {
            return REST_API;
        } else if ("soap_api".equalsIgnoreCase(artifactType)) {
            return SOAP_API;
        } else if ("graphql_api".equalsIgnoreCase(artifactType)) {
            return GRAPHQL_API;
        } else if ("async_api".equalsIgnoreCase(artifactType)) {
            return ASYNC_API;
        } else if ("all_api".equalsIgnoreCase(artifactType)) {
            return ALL_API;
        }
        return REST_API;
    }
}
