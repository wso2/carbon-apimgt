/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.PlatformGatewayArtifactValidationResult;

/**
 * Service for API Platform Gateway artifact handling: store/retrieve platform api.yaml
 * (Scenario 1), convert internal API to platform format and validate (Scenario 2).
 * Follows the pattern: REST/Internal layer calls service, service uses DAO and converter.
 */
public interface PlatformGatewayArtifactService {

    /**
     * Get stored platform gateway api.yaml for an API and organization, if any.
     * Used when serving zip to gateway (prefer stored over conversion).
     *
     * @param apiId         API UUID
     * @param organization organization id (tenant domain)
     * @return stored YAML content, or null if none stored
     */
    String getStoredPlatformArtifact(String apiId, String organization) throws APIManagementException;

    /**
     * Save platform gateway api.yaml for an API and organization.
     *
     * @param apiId         API UUID
     * @param organization organization id
     * @param yamlContent   platform-format api.yaml content
     */
    void savePlatformArtifact(String apiId, String organization, String yamlContent) throws APIManagementException;

    /**
     * Convert internal API to platform api.yaml and run sanitization.
     * Returns converted YAML and list of missing/invalid fields for UI to fill.
     *
     * @param api         internal API model
     * @param organization organization id
     * @param environment  environment segment (e.g. "default")
     * @return validation result with convertedYaml, missingFields, invalidFields, valid
     */
    PlatformGatewayArtifactValidationResult convertAndValidate(API api, String organization, String environment)
            throws APIManagementException;
}
