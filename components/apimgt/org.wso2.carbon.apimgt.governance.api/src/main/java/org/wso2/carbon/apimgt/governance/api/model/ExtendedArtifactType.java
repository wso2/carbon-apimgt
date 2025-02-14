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

import java.util.EnumSet;
import java.util.Locale;

/**
 * Enum class to represent the artifact types of a ruleset
 */
public enum ExtendedArtifactType {
    REST_API,
    SOAP_API,
    GRAPHQL_API,
    ASYNC_API;

    public static ExtendedArtifactType fromString(String text) {
        try {
            return ExtendedArtifactType.valueOf(text.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Check whether the artifact type is an API.
     *
     * @param artifactType Artifact type
     * @return True if the artifact type is an API
     */
    public static boolean isArtifactAPI(ExtendedArtifactType artifactType) {
        return EnumSet.of(REST_API, SOAP_API, GRAPHQL_API, ASYNC_API).contains(artifactType);
    }
}


