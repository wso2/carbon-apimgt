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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.PlatformGatewayArtifactService;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.PlatformGatewayArtifactValidationResult;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayArtifactDAO;
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayAPIYamlConverter;

/**
 * Implementation of platform gateway artifact service. Uses {@link PlatformGatewayArtifactDAO}
 * for storage and {@link PlatformGatewayAPIYamlConverter} for conversion; runs sanitization
 * after conversion to report missing/invalid fields.
 */
public class PlatformGatewayArtifactServiceImpl implements PlatformGatewayArtifactService {

    private static final PlatformGatewayArtifactServiceImpl INSTANCE = new PlatformGatewayArtifactServiceImpl();

    public static PlatformGatewayArtifactServiceImpl getInstance() {
        return INSTANCE;
    }

    private PlatformGatewayArtifactServiceImpl() {
    }

    @Override
    public String getStoredPlatformArtifact(String apiId, String organization) throws APIManagementException {
        if (StringUtils.isBlank(apiId) || StringUtils.isBlank(organization)) {
            return null;
        }
        return PlatformGatewayArtifactDAO.getInstance().getArtifact(apiId.trim(), organization.trim());
    }

    @Override
    public void savePlatformArtifact(String apiId, String organization, String yamlContent) throws APIManagementException {
        if (StringUtils.isBlank(apiId) || StringUtils.isBlank(organization)) {
            throw new APIManagementException("API ID and organization are required");
        }
        if (yamlContent == null) {
            throw new APIManagementException("YAML content is required");
        }
        PlatformGatewayArtifactDAO.getInstance().saveArtifact(apiId.trim(), organization.trim(), yamlContent);
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
