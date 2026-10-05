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
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.URITemplate;

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
    public String generateAPIDefinition(SwaggerData swaggerData) throws APIManagementException {
        return null;
    }

    @Override
    public String generateAPIDefinition(SwaggerData swaggerData, String swagger) throws APIManagementException {
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
                ExceptionCodes.ERROR_MCP_TOOL_GENERATION_NOT_SUPPORTED);
    }

    @Override
    public Set<URITemplate> updateMCPTools(String backendApiDefinition, APIIdentifier refApiId,
                                           String backendId, String mcpSubtype, Set<URITemplate> uriTemplates)
            throws APIManagementException {
        throw new APIManagementException("MCP tool generation is not supported for Async API definitions.",
                ExceptionCodes.ERROR_MCP_TOOL_GENERATION_NOT_SUPPORTED);
    }

    @Override
    public String generateAPIDefinitionForBackendAPI(SwaggerData swaggerData, String apiDefinition)
            throws APIManagementException {
        return null;
    }

    /**
     * Extracts and builds URI templates from the given AsyncAPI definition.
     *
     * @param apiDefinition   AsyncAPI definition in JSON/YAML format
     * @param includePublish  Whether to include publish/subscribe operations in the result
     * @return Set of constructed URI templates
     * @throws APIManagementException if parsing or template building fails
     */
    public abstract Set<URITemplate> getURITemplates(String apiDefinition, boolean includePublish)
            throws APIManagementException;

    /**
     * Generates an AsyncAPI definition based on the provided API artifact.
     *
     * @param api API object containing metadata and configuration
     * @return Generated AsyncAPI definition as a String
     * @throws APIManagementException if generation fails
     */
    public abstract String generateAsyncAPIDefinition(API api) throws APIManagementException;

    /**
     * Prepares and customizes the AsyncAPI definition for the Developer Portal (Store).
     *
     * @param api                 API object
     * @param asyncAPIDefinition  Original AsyncAPI definition
     * @param hostsWithSchemes    Map of hosts and corresponding schemes
     * @return Modified AsyncAPI definition for Store consumption
     * @throws APIManagementException if processing fails
     */
    public abstract String getAsyncApiDefinitionForStore(
            API api, String asyncAPIDefinition, Map<String, String> hostsWithSchemes) throws APIManagementException;

    /**
     * Updates an existing AsyncAPI definition using the provided API details.
     *
     * @param oldDefinition Existing AsyncAPI definition
     * @param apiToUpdate   API object containing updated values
     * @return Updated AsyncAPI definition
     * @throws APIManagementException if update fails
     */
    public abstract String updateAsyncAPIDefinition(
            String oldDefinition, API apiToUpdate) throws APIManagementException;

    /**
     * Builds a WebSocket URI to channel mapping from the AsyncAPI definition.
     *
     * @param apiDefinition AsyncAPI definition
     * @return map of channel names to WebSocket URIs
     */
    public abstract Map<String, String> buildWSUriMapping(String apiDefinition);
}
