/*
 *   Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com)
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.spec.parser.definitions;

import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class used to create AsyncAPI parsers.
 * It extends the APIDefinition class and provides common parsing capabilities for AsyncAPI specifications
 */
public abstract class AbstractAsyncApiParser extends APIDefinition {

    @Override
    public Map<String, Object> generateExample(String apiDefinition) throws APIManagementException {
        return null;
    }

    @Override
    public Set<URITemplate> getURITemplates(String resourceConfigsJSON) throws APIManagementException {
        return null;
    }

    @Override
    public Set<Scope> getScopes(String resourceConfigsJSON) throws APIManagementException {
        return null;
    }

    @Override
    public String generateAPIDefinition(SwaggerData swaggerData) throws APIManagementException {
        return null;
    }

    @Override
    public String generateAPIDefinition(SwaggerData swaggerData, String swagger) throws APIManagementException {
        return null;
    }

    @Override
    public APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, boolean returnJsonContent)
            throws APIManagementException {
        return null;
    }

    @Override
    public APIDefinitionValidationResponse validateAPIDefinition(
            String apiDefinition, String url, boolean returnJsonContent) throws APIManagementException {
        return null;
    }

    @Override
    public String populateCustomManagementInfo(
            String oasDefinition, SwaggerData swaggerData) throws APIManagementException {
        return null;
    }

    @Override
    public String getOASDefinitionForStore(API api, String oasDefinition, Map<String, String> hostsWithSchemes,
                                           KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException {
        return null;
    }

    @Override
    public String getOASDefinitionForStore(APIProduct product, String oasDefinition,
                                           Map<String, String> hostsWithSchemes,
                                           KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException {
        return null;
    }

    @Override
    public String getOASDefinitionForPublisher(API api, String oasDefinition) throws APIManagementException {
        return null;
    }

    @Override
    public String getOASVersion(String oasDefinition) throws APIManagementException {
        return null;
    }

    @Override
    public String getOASDefinitionWithTierContentAwareProperty(String oasDefinition,
                                                               List<String> contentAwareTiersList, String apiLevelTier)
            throws APIManagementException {
        return null;
    }

    @Override
    public String processOtherSchemeScopes(String resourceConfigsJSON) throws APIManagementException {
        return null;
    }

    @Override
    public String injectMgwThrottlingExtensionsToDefault(String swaggerContent) throws APIManagementException {
        return null;
    }

    @Override
    public API setExtensionsToAPI(String swaggerContent, API api) throws APIManagementException {
        return null;
    }

    @Override
    public String copyVendorExtensions(String existingOASContent, String updatedOASContent)
            throws APIManagementException {
        return null;
    }

    @Override
    public String processDisableSecurityExtension(String swaggerContent) throws APIManagementException {
        return null;
    }

    @Override
    public String getVendorFromExtension(String swaggerContent) {
        return APISpecParserConstants.WSO2_GATEWAY_ENVIRONMENT;
    }

    @Override
    public String getVendorFromExtensionWithError(String swaggerContent) throws APIManagementException {
        return APISpecParserConstants.WSO2_GATEWAY_ENVIRONMENT;
    }

    @Override
    public String getType() {
        return APISpecParserConstants.WSO2_GATEWAY_ENVIRONMENT;
    }

    @Override
    public Set<URITemplate> generateMCPTools(String backendApiDefinition, APIIdentifier refApiId,
                                             String backendId, String mcpSubtype, Set<URITemplate> uriTemplates)
            throws APIManagementException {
        throw new APIManagementException("MCP tool generation is not supported for Async API definitions.",
                ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
    }

    @Override
    public Set<URITemplate> updateMCPTools(String backendApiDefinition, APIIdentifier refApiId,
                                           String backendId, String mcpSubtype, Set<URITemplate> uriTemplates)
            throws APIManagementException {
        throw new APIManagementException("MCP tool generation is not supported for Async API definitions.",
                ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
    }

    @Override
    public String generateAPIDefinitionForBackendAPI(SwaggerData swaggerData, String apiDefinition)
            throws APIManagementException {
        return null;
    }

    public abstract Set<URITemplate> getURITemplates(String apiDefinition, boolean includePublish)
            throws APIManagementException;

    public abstract String generateAsyncAPIDefinition(API api) throws APIManagementException;

    public abstract String getAsyncApiDefinitionForStore(API api, String asyncAPIDefinition,
                                                         Map<String, String> hostsWithSchemes) throws APIManagementException;

    public abstract String updateAsyncAPIDefinition(String oldDefinition, API apiToUpdate) throws APIManagementException;

    public abstract Map<String, String> buildWSUriMapping(String apiDefinition);
}
