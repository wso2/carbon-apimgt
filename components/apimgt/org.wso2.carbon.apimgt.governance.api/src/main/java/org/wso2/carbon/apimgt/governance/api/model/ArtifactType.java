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

package org.wso2.carbon.apimgt.governance.api.model;

/**
 * This class represents different types of artifacts available for Governing
 */
public enum ArtifactType {
    REST_API,
    SOAP_API,
    GRAPHQL_API,
    ASYNC_API,
    API;

    public static ArtifactType fromString(String text) {
        if ("rest_api".equalsIgnoreCase(text)) {
            return REST_API;
        } else if ("soap_api".equalsIgnoreCase(text)) {
            return SOAP_API;
        } else if ("graphql_api".equalsIgnoreCase(text)) {
            return GRAPHQL_API;
        } else if ("async_api".equalsIgnoreCase(text)) {
            return ASYNC_API;
        } else if ("api".equalsIgnoreCase(text)) {
            return API;
        }
        return API;
    }

    /**
     * Check whether the artifact type is an API
     *
     * @param artifactType Artifact type
     * @return True if the artifact type is an API
     */
    public static boolean isArtifactAPI(ArtifactType artifactType) {
        return artifactType == REST_API || artifactType == SOAP_API ||
                artifactType == GRAPHQL_API || artifactType == ASYNC_API
                || artifactType == API;
    }

    /**
     * Check whether the artifact type is an API
     *
     * @param artifactType Artifact type
     * @return True if the artifact type is an API
     */
    public static boolean isArtifactAPI(String artifactType) {
        ArtifactType type = fromString(artifactType);
        return type == REST_API || type == SOAP_API ||
                type == GRAPHQL_API || type == ASYNC_API
                || type == API;
    }

    /**
     * Convert from API Manager artifact type to API Manager Governance artifact type
     *
     * @param artifactType Artifact type
     * @return API Manager Governance artifact type
     */
    public static ArtifactType fromAPIMArtifactType(String artifactType) {
        if ("rest".equalsIgnoreCase(artifactType) || "http".equalsIgnoreCase(artifactType)) {
            return REST_API;
        } else if ("soap".equalsIgnoreCase(artifactType)) {
            return SOAP_API;
        } else if ("graphql".equalsIgnoreCase(artifactType)) {
            return GRAPHQL_API;
        } else if ("async".equalsIgnoreCase(artifactType)) {
            return ASYNC_API;
        }
        return null;
    }


}
