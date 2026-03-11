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
 * Service for API Platform Gateway artifact handling: revision-scoped store/retrieve in AM_GW_API_ARTIFACTS,
 * convert internal API to platform format and validate.
 * Follows the pattern: deploy → save revision artifact; callback → resolve revision, return stored artifact.
 */
public interface PlatformGatewayArtifactService {

    /**
     * Resolve REVISION_UUID for (apiId, gateway name) using AM_DEPLOYMENT_REVISION_MAPPING and AM_REVISION.
     *
     * @param apiId       API UUID
     * @param gatewayName gateway/environment name
     * @return REVISION_UUID or null if no deployment found
     */
    String getRevisionUuidByApiAndGatewayName(String apiId, String gatewayName) throws APIManagementException;

    /**
     * Get stored revision artifact (platform api.yaml) from AM_GW_API_ARTIFACTS.
     *
     * @param apiId      API UUID
     * @param revisionId REVISION_UUID
     * @return stored YAML content, or null if not found
     */
    String getStoredRevisionArtifact(String apiId, String revisionId) throws APIManagementException;

    /**
     * Save platform revision artifact (api.yaml) to AM_GW_API_ARTIFACTS. Call when deploy occurs.
     *
     * @param apiId       API UUID
     * @param revisionId  REVISION_UUID
     * @param yamlContent platform-format api.yaml content
     */
    void saveRevisionArtifact(String apiId, String revisionId, String yamlContent) throws APIManagementException;

    /**
     * Delete revision artifact for (apiId, revisionId). Optional (e.g. when revision undeployed from all).
     */
    void deleteRevisionArtifact(String apiId, String revisionId) throws APIManagementException;

    /**
     * Delete all artifact rows for an API from AM_GW_API_ARTIFACTS. Call on API delete.
     *
     * @param apiId API UUID
     */
    void deleteAllRevisionArtifactsForApi(String apiId) throws APIManagementException;

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
