/*
 *  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.OASParserOptions;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.URITemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * APIDefinition is responsible for providing uri templates, scopes and
 * save the api definition according to the permission and visibility
 */

@SuppressWarnings("unused")
public abstract class APIDefinition {

    private static final Pattern CURLY_BRACES_PATTERN = Pattern.compile("(?<=\\{)(?!\\s*\\{)[^{}]+");

    /**
     * This method generates Mock/Sample payloads for API prototyping
     *
     * @param apiDefinition
     * @return
     */
    public abstract Map<String, Object> generateExample(String apiDefinition) throws APIManagementException;

    /**
     * This method extracts the URI templates from the API definition
     *
     * @return URI templates
     */
    @UsedByMigrationClient
    public abstract Set<URITemplate> getURITemplates(String resourceConfigsJSON) throws APIManagementException;

    /**
     * This method extracts the scopes from the API definition
     *
     * @param resourceConfigsJSON resource json
     * @return scopes
     */
    public abstract Set<Scope> getScopes(String resourceConfigsJSON) throws APIManagementException;

    /**
     * This method generates API definition to the given api
     *
     * @param swaggerData api
     * @return API definition in string format
     * @throws APIManagementException
     */
    public abstract String generateAPIDefinition(SwaggerData swaggerData) throws APIManagementException;

    /**
     * This method generates API definition using the given api's URI templates and the swagger.
     * It will alter the provided swagger definition based on the URI templates. For example: if there is a new
     * URI template not included in the swagger, it will be added as a basic resource; any additional resources in
     * the swagger that are not represented by URI templates will be removed. Changes to scopes and throttling policies
     * on resources will be reflected in the resulting swagger.
     * When an OASParserOptions `options` parameter is provided, the parser will honor those options when producing the
     * API definition.
     ```
     *
     * @param swaggerData api
     * @param swagger     swagger definition
     * @param options OAS Parser options
     * @return API definition in string format
     * @throws APIManagementException if error occurred when generating API Definition
     */
    public String generateAPIDefinition(SwaggerData swaggerData, String swagger, OASParserOptions options)
            throws APIManagementException {
        return generateAPIDefinition(swaggerData, swagger);
    }

    /**
     * This method generates API definition using the given api's URI templates and the swagger.
     * It will alter the provided swagger definition based on the URI templates. For example: if there is a new
     * URI template which is not included in the swagger, it will be added to the swagger as a basic resource. Any
     * additional resources inside the swagger will be removed from the swagger. Changes to scopes, throtting policies,
     * on the resource will be updated on the swagger
     *
     * @param swaggerData api
     * @param swagger     swagger definition
     * @return API definition in string format
     * @throws APIManagementException if error occurred when generating API Definition
     */
    public abstract String generateAPIDefinition(SwaggerData swaggerData, String swagger) throws APIManagementException;

    /**
     * Extract and return path parameters in the given URI template
     *
     * @param uriTemplate URI Template value
     * @return path parameters in the given URI template
     */
    public List<String> getPathParamNames(String uriTemplate) {
        List<String> params = new ArrayList<>();

        Matcher bracesMatcher = CURLY_BRACES_PATTERN.matcher(uriTemplate);
        while (bracesMatcher.find()) {
            params.add(bracesMatcher.group());
        }
        return params;
    }

    /**
     * Creates a helper resource path map using provided swagger data.
     * Creates map in below format:
     * /order      -> [post -> resource1]
     * /order/{id} -> [get -> resource2, put -> resource3, ..]
     *
     * @param swaggerData Swagger Data object
     * @return a structured uri template map using provided Swagger Data Resource Paths
     */
    public Map<String, Map<String, SwaggerData.Resource>> getResourceMap(SwaggerData swaggerData) {
        Map<String, Map<String, SwaggerData.Resource>> uriTemplateMap = new LinkedHashMap<>();
        for (SwaggerData.Resource resource : swaggerData.getResources()) {
            Map<String, SwaggerData.Resource> resources = uriTemplateMap.get(resource.getPath());
            if (resources == null) {
                resources = new LinkedHashMap<>();
                uriTemplateMap.put(resource.getPath(), resources);
            }
            resources.put(resource.getVerb().toUpperCase(), resource);
        }
        return uriTemplateMap;
    }

    /**
     * This method validates the given OpenAPI definition by content
     *
     * @param apiDefinition     OpenAPI Definition content
     * @param returnJsonContent whether to return the converted json form of the OpenAPI definition
     * @return APIDefinitionValidationResponse object with validation information
     */
    public abstract APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition,
                                                                          boolean returnJsonContent) throws APIManagementException;

    /**
     * This method validates the given OpenAPI definition by content
     *
     * @param apiDefinition     OpenAPI Definition content
     * @param returnJsonContent whether to return the converted json form of the OpenAPI definition
     * @param parserOptions     optional OpenAPI parser options; may be {@code null} to use defaults
     * @return APIDefinitionValidationResponse object with validation information
     * @throws APIManagementException if an error occurs while validating the definition
     */
    public APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, boolean returnJsonContent,
            OASParserOptions parserOptions) throws APIManagementException {
        return validateAPIDefinition(apiDefinition, returnJsonContent);
    }

    /**
     * This method validates the given OpenAPI definition by content
     *
     * @param apiDefinition     OpenAPI Definition content
     * @param url     OpenAPI Definition url
     * @param returnJsonContent whether to return the converted json form of the OpenAPI definition
     * @return APIDefinitionValidationResponse object with validation information
     */
    public abstract APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, String url,
            boolean returnJsonContent) throws APIManagementException;

    /**
     * This method validates the given OpenAPI definition by content with optional parser configuration.
     *
     * @param apiDefinition     OpenAPI definition content
     * @param url               OpenAPI definition URL
     * @param returnJsonContent whether to return the converted JSON form of the OpenAPI definition
     * @param parserOptions     optional OpenAPI parser options; may be {@code null} to use defaults
     * @return APIDefinitionValidationResponse object containing validation information
     * @throws APIManagementException if an error occurs while validating the definition
     */
    public APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, String url,
            boolean returnJsonContent, OASParserOptions parserOptions) throws APIManagementException {
        return validateAPIDefinition(apiDefinition, url, returnJsonContent);
    }

    /**
     * Populate definition with wso2 APIM specific information
     *
     * @param oasDefinition OAS definition
     * @param swaggerData   API
     * @return Generated OAS definition
     * @throws APIManagementException If an error occurred
     */
    public abstract String populateCustomManagementInfo(String oasDefinition, SwaggerData swaggerData)
            throws APIManagementException;

    /**
     * Populate definition with wso2 APIM specific information
     * When an OASParserOptions `options` parameter is provided, the parser will honor those options when producing the
     * API definition.
     *
     * @param oasDefinition OAS definition
     * @param swaggerData   API
     * @param options OAS Parser options
     * @return Generated OAS definition
     * @throws APIManagementException If an error occurred
     */
    public String populateCustomManagementInfo(String oasDefinition, SwaggerData swaggerData, OASParserOptions options)
            throws APIManagementException{
        return populateCustomManagementInfo(oasDefinition, swaggerData);
    }

    /**
     * Update the OAS definition for API consumers
     *
     * @param api                        API
     * @param oasDefinition              OAS definition
     * @param hostsWithSchemes           host addresses with protocol mapping
     * @param keyManagerConfigurationDTO configuration details of the Key Manager
     * @return updated OAS definition
     * @throws APIManagementException throws if an error occurred
     */
    public abstract String getOASDefinitionForStore(API api, String oasDefinition, Map<String, String> hostsWithSchemes,
                                                    KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException;

    /**
     * Update the OAS definition for API consumers
     *
     * @param api                        API
     * @param oasDefinition              OAS definition
     * @param hostsWithSchemes           host addresses with protocol mapping
     * @param keyManagerConfigurationDTO configuration details of the Key Manager
     * @return updated OAS definition
     * @throws APIManagementException throws if an error occurred
     */
    public String getOASDefinitionForStore(API api, String oasDefinition, Map<String, String> hostsWithSchemes,
            KeyManagerConfigurationDTO keyManagerConfigurationDTO, OASParserOptions options) throws APIManagementException {
        return getOASDefinitionForStore(api, oasDefinition, hostsWithSchemes, keyManagerConfigurationDTO);
    }

    /**
     * Update the OAS definition for API consumers
     *
     * @param product                    APIProduct
     * @param oasDefinition              OAS definition
     * @param hostsWithSchemes           host addresses with protocol mapping
     * @param keyManagerConfigurationDTO configuration details of the Key Manager
     * @return updated OAS definition
     * @throws APIManagementException throws if an error occurred
     */
    public abstract String getOASDefinitionForStore(APIProduct product, String oasDefinition,
                                                    Map<String, String> hostsWithSchemes,
                                                    KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException;

    /**
     * Update the OAS definition for API consumers
     *
     * @param product                    APIProduct
     * @param oasDefinition              OAS definition
     * @param hostsWithSchemes           host addresses with protocol mapping
     * @param keyManagerConfigurationDTO configuration details of the Key Manager
     * @return updated OAS definition
     * @throws APIManagementException throws if an error occurred
     */
    public String getOASDefinitionForStore(APIProduct product, String oasDefinition,
            Map<String, String> hostsWithSchemes,
            KeyManagerConfigurationDTO keyManagerConfigurationDTO, OASParserOptions options)
            throws APIManagementException{
        return getOASDefinitionForStore(product, oasDefinition, hostsWithSchemes, keyManagerConfigurationDTO);
    }

    /**
     * Update OAS definition for API Publisher
     *
     * @param api           API
     * @param oasDefinition
     * @return OAS definition
     * @throws APIManagementException throws if an error occurred
     */
    public abstract String getOASDefinitionForPublisher(API api, String oasDefinition)
            throws APIManagementException;

    /**
     * Update OAS definition for API Publisher
     * When an OASParserOptions `options` parameter is provided, the parser will honor those options when producing the
     * API definition.
     *
     * @param api           API
     * @param oasDefinition
     * @param options OAS Parser options
     * @return OAS definition
     * @throws APIManagementException throws if an error occurred
     */
    public String getOASDefinitionForPublisher(API api, String oasDefinition, OASParserOptions options)
            throws APIManagementException{
        return getOASDefinitionForPublisher(api, oasDefinition);
    }

    public abstract String getOASVersion(String oasDefinition) throws APIManagementException;

    public abstract String getOASDefinitionWithTierContentAwareProperty(String oasDefinition,
            List<String> contentAwareTiersList, String apiLevelTier) throws APIManagementException;

    /**
     * This method changes the URI templates from the API definition as it support different schemes
     * @param resourceConfigsJSON json String of oasDefinition
     * @throws APIManagementException throws if an error occurred
     * @return String
     */
    public abstract String processOtherSchemeScopes(String resourceConfigsJSON)
            throws APIManagementException;

    /**
     * This method changes the URI templates from the API definition as it support different schemes
     * When an OASParserOptions `options` parameter is provided, the parser will honor those options when producing the
     * API definition.
     *
     * @param resourceConfigsJSON json String of oasDefinition
     * @param options OAS Parser options
     * @throws APIManagementException throws if an error occurred
     * @return String
     */
    public String processOtherSchemeScopes(String resourceConfigsJSON, OASParserOptions options)
            throws APIManagementException{
        return processOtherSchemeScopes(resourceConfigsJSON);
    }

    /**
     * This method returns OAS definition which replaced X-WSO2-throttling-tier extension comes from
     * mgw with X-throttling-tier extensions in OAS file
     *
     * @param swaggerContent String
     * @return OpenAPI
     * @throws APIManagementException
     */
    public abstract String injectMgwThrottlingExtensionsToDefault(String swaggerContent)
            throws APIManagementException;

    /**
     * This method returns OAS definition which replaced X-WSO2-throttling-tier extension comes from
     * mgw with X-throttling-tier extensions in OAS file
     * When an OASParserOptions `options` parameter is provided, the parser will honor those options when producing the
     * API definition.
     *
     * @param swaggerContent String
     * @param options OAS Parser options
     * @return OpenAPI
     * @throws APIManagementException
     */
    public String injectMgwThrottlingExtensionsToDefault(String swaggerContent, OASParserOptions options)
            throws APIManagementException {
        return injectMgwThrottlingExtensionsToDefault(swaggerContent);
    }

    /**
     * This method returns api that is attached with api extensions related to micro-gw
     *
     * @param swaggerContent String
     * @param api            API
     * @return API
     */
    public abstract API setExtensionsToAPI(String swaggerContent, API api)
            throws APIManagementException;

    /**
     * This method copy the vendor extensions from Existing OAS to the updated OAS
     *
     * @param existingOASContent Current OAS Content
     * @param updatedOASContent Updated OAS Content
     * @return OAS content
     * @throws APIManagementException
     */
    public abstract String copyVendorExtensions(String existingOASContent, String updatedOASContent)
            throws APIManagementException;

    /**
     * This method copy the vendor extensions from Existing OAS to the updated OAS
     * When an OASParserOptions `options` parameter is provided, the parser will honor those options when producing the
     * API definition.
     *
     * @param existingOASContent Current OAS Content
     * @param updatedOASContent Updated OAS Content
     * @param options OAS Parser options
     * @return OAS content
     * @throws APIManagementException
     */
    public String copyVendorExtensions(String existingOASContent, String updatedOASContent, OASParserOptions options)
            throws APIManagementException{
        return copyVendorExtensions(existingOASContent, updatedOASContent);
    }

    /**
     * This method will extractX-WSO2-disable-security extension provided in API level
     * by mgw and inject that extension to all resources in OAS file
     *
     * @param swaggerContent String
     * @return String
     * @throws APIManagementException
     */
    public abstract String processDisableSecurityExtension(String swaggerContent)
            throws APIManagementException;

    /**
     * This method will extractX-WSO2-disable-security extension provided in API level
     * by mgw and inject that extension to all resources in OAS file
     * When an OASParserOptions `options` parameter is provided, the parser will honor those options when producing the
     * API definition.
     *
     * @param swaggerContent String
     * @param options OAS Parser options
     * @return String
     * @throws APIManagementException
     */
    public String processDisableSecurityExtension(String swaggerContent, OASParserOptions options)
            throws APIManagementException {
        return processDisableSecurityExtension(swaggerContent);
    }

    /**
     * This method will extract the vendor provider or the API specification from the extensions list
     *
     * @param swaggerContent String
     * @return String
     */
    @Deprecated
    public abstract String getVendorFromExtension(String swaggerContent);

    /**
     * This method will extract the vendor provider or the API specification form the extensions list
     *
     * @param swaggerContent String
     * @return String
     */
    public abstract String getVendorFromExtensionWithError(String swaggerContent) throws APIManagementException;

    /**
     * Get parser Type
     *
     * @return String parserType
     */
    public abstract String getType();

    /**
     * Generates MCP URITemplates based on the API definition.
     *
     * @param backendApiDefinition API definition of the backend.
     * @param refApiId             Reference API identifier.
     * @param backendId            Backend ID. It can be either backend endpoints ID or API UUID.
     * @param mcpSubtype           MCP Subtype
     * @param uriTemplates         URI templates to generate
     * @return generated set of MCP tool URITemplates
     */
    public abstract Set<URITemplate> generateMCPTools(String backendApiDefinition, APIIdentifier refApiId,
                                                      String backendId, String mcpSubtype,
                                                      Set<URITemplate> uriTemplates) throws APIManagementException;

    /**
     * Updates MCP tool-related URI templates by resolving and matching backend operations.
     *
     * @param backendApiDefinition OpenAPI definition of the backend API as a string
     * @param refApiId             Identifier of the reference API
     * @param backendId            Backend identifier
     * @param mcpSubtype           MCP subtype (e.g., direct endpoint or existing API)
     * @param uriTemplates         Set of existing URI templates to process
     * @return Updated set of URI templates with resolved operation details
     */
    public abstract Set<URITemplate> updateMCPTools(String backendApiDefinition, APIIdentifier refApiId,
                                                    String backendId, String mcpSubtype, Set<URITemplate> uriTemplates)
            throws APIManagementException;

    /**
     * Generates an enriched OpenAPI Specification definition for backend APIs
     *
     * @param swaggerData   API
     * @param apiDefinition OAS definition of the backend API
     * @return Generated OAS definition
     * @throws APIManagementException If an error occurred
     */
    public abstract String generateAPIDefinitionForBackendAPI(SwaggerData swaggerData, String apiDefinition)
            throws APIManagementException;
}
