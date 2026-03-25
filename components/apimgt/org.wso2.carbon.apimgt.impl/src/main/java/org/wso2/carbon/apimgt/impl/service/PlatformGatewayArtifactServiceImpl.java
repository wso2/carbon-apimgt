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

package org.wso2.carbon.apimgt.impl.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.PlatformGatewayArtifactService;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.PlatformGatewayArtifactValidationResult;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayArtifactDAO;
import org.wso2.carbon.apimgt.impl.gateway.PlatformGatewayConstants;
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayAPIYamlConverter;
import org.wso2.carbon.context.CarbonContext;

/**
 * Implementation of platform gateway artifact service. Uses a dedicated platform artifact cache table as a
 * read-through cache and falls back to building platform api.yaml via
 * {@link PlatformGatewayAPIYamlConverter} on cache miss.
 */
public class PlatformGatewayArtifactServiceImpl implements PlatformGatewayArtifactService {

    private static final Log log = LogFactory.getLog(PlatformGatewayArtifactServiceImpl.class);

    private static final PlatformGatewayArtifactServiceImpl INSTANCE = new PlatformGatewayArtifactServiceImpl();

    public static PlatformGatewayArtifactServiceImpl getInstance() {
        return INSTANCE;
    }

    private PlatformGatewayArtifactServiceImpl() {
    }

    @Override
    public String getRevisionUuidByApiAndGatewayName(String apiId, String gatewayName) throws APIManagementException {
        if (StringUtils.isBlank(apiId) || StringUtils.isBlank(gatewayName)) {
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("Resolving platform gateway revision mapping for API " + apiId + " and gateway '"
                    + gatewayName + "'");
        }
        return PlatformGatewayArtifactDAO.getInstance().getRevisionUuidByApiAndGatewayName(apiId.trim(),
                gatewayName.trim());
    }

    @Override
    public String getStoredRevisionArtifact(String apiId, String revisionId) throws APIManagementException {
        if (StringUtils.isBlank(apiId) || StringUtils.isBlank(revisionId)) {
            return null;
        }
        String trimmedApiId = apiId.trim();
        String trimmedRevisionId = revisionId.trim();
        if (log.isDebugEnabled()) {
            log.debug("Fetching platform gateway artifact for API " + trimmedApiId + " revision " + trimmedRevisionId);
        }
        PlatformGatewayArtifactDAO artifactDAO = PlatformGatewayArtifactDAO.getInstance();
        String storedArtifact = artifactDAO.getRevisionArtifact(trimmedApiId, trimmedRevisionId);
        if (StringUtils.isNotBlank(storedArtifact)) {
            if (log.isDebugEnabled()) {
                log.debug("Serving cached platform gateway artifact for API " + trimmedApiId + " revision "
                        + trimmedRevisionId);
            }
            return storedArtifact;
        }

        String generatedArtifact = buildPlatformGatewayYamlOnTheFly(trimmedApiId, trimmedRevisionId);
        if (StringUtils.isBlank(generatedArtifact)) {
            return generatedArtifact;
        }
        artifactDAO.saveRevisionArtifact(trimmedApiId, trimmedRevisionId, generatedArtifact);
        if (log.isDebugEnabled()) {
            log.debug("Cached generated platform gateway artifact for API " + trimmedApiId + " revision "
                    + trimmedRevisionId);
        }
        return generatedArtifact;
    }

    /**
     * Load API at the given revision and convert to platform YAML.
     */
    private static String buildPlatformGatewayYamlOnTheFly(String apiId, String revisionId)
            throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Building platform gateway YAML on the fly for apiId=" + apiId + ", revisionId=" + revisionId);
        }
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        APIRevision revision = apiMgtDAO.getRevisionByRevisionUUID(revisionId);
        if (revision == null) {
            if (log.isDebugEnabled()) {
                log.debug("No API revision found for revisionUUID: " + revisionId);
            }
            return null;
        }
        if (!apiId.equals(revision.getApiUUID())) {
            if (log.isDebugEnabled()) {
                log.debug("API UUID does not match revision " + revisionId + " owner API");
            }
            return null;
        }
        String organization = apiMgtDAO.getOrganizationByAPIUUID(revision.getApiUUID());
        if (StringUtils.isBlank(organization)) {
            return null;
        }
        APIProvider provider = APIManagerFactory.getInstance().getAPIProvider(resolveProviderUsername(organization));
        // Load by revision UUID so populateAPIInformation resolves revision-scoped URI templates/policies.
        API api = provider.getAPIbyUUID(revisionId, organization);
        if (api == null) {
            if (log.isDebugEnabled()) {
                log.debug("getAPIbyUUID returned null for revision UUID " + revisionId + ", organization "
                        + organization);
            }
            return null;
        }
        if (api.getId() == null) {
            if (log.isDebugEnabled()) {
                log.debug("Loaded API has no identifier for revision UUID " + revisionId);
            }
            return null;
        }
        api.setRevisionedApiId(revision.getRevisionUUID());
        api.setRevisionId(revision.getId());
        api.setUuid(apiId);
        api.getId().setUuid(apiId);
        if (log.isDebugEnabled()) {
            log.debug("Converting API " + apiId + " revision " + revisionId + " to platform gateway YAML for "
                    + "organization " + organization);
        }
        return PlatformGatewayAPIYamlConverter.toPlatformGatewayYaml(api);
    }

    /**
     * Uses {@link CarbonContext} username set on the request thread by auth interceptors.
     */
    private static String resolveProviderUsername(String organization) throws APIManagementException {
        String ctxUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isNotBlank(ctxUser)) {
            return ctxUser;
        }
        throw new APIManagementException("Unable to resolve provider username from request context for organization: "
                + organization);
    }

    @Override
    public void saveRevisionArtifact(String apiId, String revisionId, String yamlContent) throws APIManagementException {
        if (StringUtils.isBlank(apiId) || StringUtils.isBlank(revisionId)) {
            throw new APIManagementException("API ID and revision ID are required");
        }
        if (yamlContent == null || yamlContent.trim().isEmpty()) {
            throw new APIManagementException("YAML content is required");
        }
        PlatformGatewayArtifactDAO.getInstance().saveRevisionArtifact(apiId.trim(), revisionId.trim(), yamlContent);
    }

    @Override
    public void deleteRevisionArtifact(String apiId, String revisionId) throws APIManagementException {
        if (StringUtils.isBlank(apiId) || StringUtils.isBlank(revisionId)) {
            return;
        }
        PlatformGatewayArtifactDAO.getInstance().deleteRevisionArtifact(apiId.trim(), revisionId.trim());
    }

    @Override
    public void deleteAllRevisionArtifactsForApi(String apiId) throws APIManagementException {
        if (StringUtils.isBlank(apiId)) {
            return;
        }
        PlatformGatewayArtifactDAO.getInstance().deleteAllRevisionArtifactsForApi(apiId.trim());
    }

    @Override
    public PlatformGatewayArtifactValidationResult convertAndValidate(API api, String organization, String environment)
            throws APIManagementException {
        PlatformGatewayArtifactValidationResult result = new PlatformGatewayArtifactValidationResult();
        if (api == null) {
            result.setValid(false);
            result.addInvalidField("api");
            return result;
        }
        try {
            String yaml = PlatformGatewayAPIYamlConverter.toPlatformGatewayYaml(api);
            result.setConvertedYaml(yaml);
            sanitizeAndFillResult(yaml, result);
        } catch (APIManagementException e) {
            result.setValid(false);
            result.setConvertedYaml(null);
            result.addInvalidField(e.getMessage() != null ? e.getMessage() : "conversion_failed");
        }
        return result;
    }

    /**
     * Sanitize converted YAML against platform spec: required fields present and valid.
     */
    private static void sanitizeAndFillResult(String yaml, PlatformGatewayArtifactValidationResult result) {
        if (StringUtils.isBlank(yaml)) {
            result.setValid(false);
            result.addMissingField("yaml_content");
            return;
        }
        boolean valid = true;
        if (!yaml.contains("apiVersion:") || !yaml.contains("gateway.api-platform.wso2.com")) {
            result.addInvalidField("apiVersion");
            valid = false;
        }
        if (!yaml.contains("kind:") || !yaml.contains("RestApi")) {
            result.addInvalidField("kind");
            valid = false;
        }
        if (!yaml.contains("metadata:") || !yaml.contains("name:")) {
            result.addMissingField("metadata.name");
            valid = false;
        }
        if (!yaml.contains("spec:") || !yaml.contains("context:")) {
            result.addMissingField("spec.context");
            valid = false;
        }
        if (!yaml.contains("upstream:") || !yaml.contains("url:")) {
            result.addMissingField("spec.upstream.main.url");
            valid = false;
        }
        result.setValid(valid);
    }
}
