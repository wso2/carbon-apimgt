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
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayAPIYamlConverter;
import org.wso2.carbon.context.CarbonContext;

/**
 * Implementation of platform gateway artifact service. Builds platform api.yaml on demand via
 * {@link PlatformGatewayAPIYamlConverter} (no AM_GW_API_ARTIFACTS read path).
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
        return PlatformGatewayArtifactDAO.getInstance().getRevisionUuidByApiAndGatewayName(apiId.trim(),
                gatewayName.trim());
    }

    @Override
    public String getStoredRevisionArtifact(String apiId, String revisionId) throws APIManagementException {
        if (StringUtils.isBlank(apiId) || StringUtils.isBlank(revisionId)) {
            return null;
        }
        return buildPlatformGatewayYamlOnTheFly(apiId.trim(), revisionId.trim());
    }

    /**
     * Load API at the given revision and convert to platform YAML.
     */
    private static String buildPlatformGatewayYamlOnTheFly(String apiId, String revisionId)
            throws APIManagementException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        APIRevision revision = apiMgtDAO.getRevisionByRevisionUUID(revisionId);
        if (revision == null) {
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
        API api = provider.getAPIbyUUID(revision.getApiUUID(), organization);
        api.setRevisionedApiId(revision.getRevisionUUID());
        api.setRevisionId(revision.getId());
        api.setUuid(apiId);
        api.getId().setUuid(apiId);
        return PlatformGatewayAPIYamlConverter.toPlatformGatewayYaml(api, organization, "default");
    }

    /**
     * Prefer {@link CarbonContext} username (set by platform-gateway api-key flow on the request thread);
     * otherwise {@code admin@} + API organization for {@link APIProvider#getAPIbyUUID}.
     */
    private static String resolveProviderUsername(String organization) {
        String ctxUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isNotBlank(ctxUser)) {
            return ctxUser;
        }
        return "admin@" + organization;
    }

    @Override
    public void saveRevisionArtifact(String apiId, String revisionId, String yamlContent) throws APIManagementException {
        if (StringUtils.isBlank(apiId) || StringUtils.isBlank(revisionId)) {
            throw new APIManagementException("API ID and revision ID are required");
        }
        if (yamlContent == null || yamlContent.trim().isEmpty()) {
            throw new APIManagementException("YAML content is required");
        }
        // Platform gateway artifacts are generated on fetch; AM_GW_API_ARTIFACTS is not populated on deploy.
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
        String env = StringUtils.isNotBlank(environment) ? environment : "default";
        String org = StringUtils.isNotBlank(organization) ? organization : "";

        try {
            String yaml = PlatformGatewayAPIYamlConverter.toPlatformGatewayYaml(api, org, env);
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
