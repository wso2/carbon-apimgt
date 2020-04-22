/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.definitions;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

public class PreprocessSwagger {

    static final String OPENAPI_SECURITY_SCHEMA_KEY = "default";
    private static final Log log = LogFactory.getLog(OAS3Parser.class);
    static List<String> otherSchemes;

    public static List<String> getOtherSchemes() {
        return otherSchemes;
    }

    public static void setOtherSchemes(List<String> otherSchemes) {
        PreprocessSwagger.otherSchemes = otherSchemes;
    }


    /**
     * Preprocessing of scopes schemes to support multiple schemes other than 'default' type
     * This method will change the given definition
     *
     * @param swaggerContent
     * @param apiToAdd
     * @return processedSwaggerContent
     */
    public static String preprocessSwagger(String swaggerContent, API apiToAdd) throws APIManagementException {
        //Load required properties from swagger to the API
        APIDefinition apiDefinition = OASParserUtil.getOASParser(swaggerContent);

        Set<Scope> scopes = manageScopes_SV3(swaggerContent);
        Set<URITemplate> urlTemplates = manageURITemplates_SV3(swaggerContent);
        apiToAdd.setUriTemplates(urlTemplates);
        apiToAdd.setScopes(scopes);
        SwaggerData updatedSwager = new SwaggerData(apiToAdd);
        String swaggerContentUpdated = apiDefinition.populateCustomManagementInfo(swaggerContent, updatedSwager);
        return swaggerContentUpdated;

    }


    /**
     * This method returns the oauth scopes according to the given swagger(version 3)
     *
     * @param swaggerContent resource json
     * @return scope set as all defaults
     * @throws APIManagementException
     */
    private static Set<Scope> manageScopes_SV3(String swaggerContent) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI_SV3(swaggerContent);
        Map<String, SecurityScheme> securitySchemes = null;
        SecurityScheme securityScheme;
        OAuthFlow oAuthFlow;
        Scopes scopes;
        Components component = openAPI.getComponents();
        List<String> otherSetofSchemes = new ArrayList<>();
        Set<Scope> scopeSet = new HashSet<>();

        if (openAPI.getComponents() != null && (securitySchemes = openAPI.getComponents().getSecuritySchemes()) != null) {
            Iterator<Map.Entry<String, SecurityScheme>> iterator = securitySchemes.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, SecurityScheme> entry = iterator.next();
                if (entry.getKey() != OPENAPI_SECURITY_SCHEMA_KEY) {
                    System.out.println(entry.getKey());
                    otherSetofSchemes.add(entry.getKey());
                    //Check for default one
                    SecurityScheme defaultType = securitySchemes.get(OPENAPI_SECURITY_SCHEMA_KEY);
                    //If there is no default type schemes set a one
                    if (defaultType == null) {
                        defaultType = new SecurityScheme();
                        securitySchemes.put(OPENAPI_SECURITY_SCHEMA_KEY, defaultType);
                    }
                    OAuthFlows defaultTypeFlows = defaultType.getFlows();
                    OAuthFlow defaultTypeFlow = defaultTypeFlows.getImplicit();
                    Scopes defaultFlowScopes = defaultTypeFlow.getScopes();
                    SecurityScheme notdeftype = entry.getValue();
                    OAuthFlows notdefTypeFlows = notdeftype.getFlows();
                    OAuthFlow notdefTypeFlow = notdeftype.getFlows().getImplicit();
                    Scopes notdefFlowScopes = notdefTypeFlow.getScopes();

                    for (Map.Entry<String, String> input : notdefFlowScopes.entrySet()) {
                        String name = input.getKey();
                        String description = input.getValue();
                        //Inject scopes set into default scheme
                        defaultFlowScopes.addString(name, description);
                        defaultTypeFlow.setScopes(defaultFlowScopes);
                        defaultTypeFlows.setImplicit(defaultTypeFlow);
                        defaultType.setFlows(defaultTypeFlows);
                    }
                    //Check X-Scope Bindings
                    Map<String, String> notdefScopeBindings, defScopeBindings = null;
                    Map<String, Object> defTypeExtension = defaultTypeFlow.getExtensions();
                    if (notdefTypeFlow.getExtensions() != null && (notdefScopeBindings =
                            (Map<String, String>) notdefTypeFlow.getExtensions().get(APIConstants.SWAGGER_X_SCOPES_BINDINGS))
                            != null) {
                        for (Map.Entry<String, String> roleInUse : notdefScopeBindings.entrySet()) {
                            String notdeftypescope = roleInUse.getKey();
                            String notdeftypeRole = roleInUse.getValue();
                            defScopeBindings = (Map<String, String>) defTypeExtension.get(APIConstants.SWAGGER_X_SCOPES_BINDINGS);
                            defScopeBindings.put(notdeftypescope, notdeftypeRole);
                        }
                    }
                    defTypeExtension.put(APIConstants.SWAGGER_X_SCOPES_BINDINGS, defScopeBindings);
                    defaultTypeFlow.setExtensions(defTypeExtension);
                    defaultTypeFlows.setImplicit(defaultTypeFlow);
                    defaultType.setFlows(defaultTypeFlows);
                }
            }
        }
        component.setSecuritySchemes(securitySchemes);
        openAPI.setComponents(component);
        //Check and set scope bindings
        if (openAPI.getComponents() != null && (securitySchemes = openAPI.getComponents().getSecuritySchemes()) != null
                && (securityScheme = securitySchemes.get(OPENAPI_SECURITY_SCHEMA_KEY)) != null
                && (oAuthFlow = securityScheme.getFlows().getImplicit()) != null
                && (scopes = oAuthFlow.getScopes()) != null) {
            for (Map.Entry<String, String> entry : scopes.entrySet()) {
                Scope scope = new Scope();
                scope.setKey(entry.getKey());
                scope.setName(entry.getKey());
                scope.setDescription(entry.getValue());
                Map<String, String> scopeBindings;
                if (oAuthFlow.getExtensions() != null && (scopeBindings =
                        (Map<String, String>) oAuthFlow.getExtensions().get(APIConstants.SWAGGER_X_SCOPES_BINDINGS))
                        != null) {
                    if (scopeBindings.get(scope.getKey()) != null) {
                        scope.setRoles(scopeBindings.get(scope.getKey()));
                    }
                }
                scopeSet.add(scope);
            }
            setOtherSchemes(otherSetofSchemes);
            return OASParserUtil.sortScopes(scopeSet);
        } else {
            setOtherSchemes(otherSetofSchemes);
            return OASParserUtil.sortScopes(getScopesFromExtensions_SV3(openAPI));
        }
    }


    /**
     * Get parsed OpenAPI object(Swagger version 3)
     *
     * @param oasDefinition OAS definition
     * @return OpenAPI
     */
    static OpenAPI getOpenAPI_SV3(String oasDefinition) {
        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
        SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(oasDefinition, null, null);
        if (CollectionUtils.isNotEmpty(parseAttemptForV3.getMessages())) {
            log.debug("Errors found when parsing OAS definition");
        }
        return parseAttemptForV3.getOpenAPI();
    }


    /**
     * This method returns URI templates according to the given swagger file(Swagger version 3)
     *
     * @param resourceConfigsJSON swaggerJSON
     * @return URI Templates
     * @throws APIManagementException
     */
    static Set<URITemplate> manageURITemplates_SV3(String resourceConfigsJSON) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI_SV3(resourceConfigsJSON);
        Set<URITemplate> urlTemplates = new LinkedHashSet<>();
        Set<Scope> scopes = manageScopes_SV3(resourceConfigsJSON);
        List<String> schemes = getOtherSchemes();

        for (String pathKey : openAPI.getPaths().keySet()) {
            PathItem pathItem = openAPI.getPaths().get(pathKey);
            for (Map.Entry<PathItem.HttpMethod, Operation> entry : pathItem.readOperationsMap().entrySet()) {
                Operation operation = entry.getValue();
                URITemplate template = new URITemplate();
                if (APIConstants.SUPPORTED_METHODS.contains(entry.getKey().name().toLowerCase())) {
                    template.setHTTPVerb(entry.getKey().name().toUpperCase());
                    template.setHttpVerbs(entry.getKey().name().toUpperCase());
                    template.setUriTemplate(pathKey);
                    List<String> opScopesofDefault = getScopeOfOperations_SV3(OPENAPI_SECURITY_SCHEMA_KEY, operation);
                    //Handling scopes in resources which do not belong to 'default' scheme
                    for (int i = 0; i < schemes.size(); i++) {
                        List<String> opScopesOfOthers = getScopeOfOperations_SV3(schemes.get(i), operation);
                        if (!opScopesOfOthers.isEmpty()) {
                            if (opScopesOfOthers.size() == 1) {
                                String firstScope = opScopesOfOthers.get(0);
                                Scope scope = APIUtil.findScopeByKey(scopes, firstScope);
                                if (scope == null) {
                                    throw new APIManagementException("Scope '" + firstScope + "' not found.");
                                }
                                Scope duplication = scope;
                                template.setScope(duplication);
                                template.setScopes(duplication);
                            } else {
                                for (String scope : opScopesOfOthers) {
                                    Scope scopeObj = new Scope();
                                    scopeObj.setKey(OPENAPI_SECURITY_SCHEMA_KEY);
                                    scopeObj.setName(OPENAPI_SECURITY_SCHEMA_KEY);

                                    template.setScopes(scopeObj);
                                }
                            }
                        }
                    }
                    //Handling scopes in resources which belong to 'default' scheme
                    if (!opScopesofDefault.isEmpty()) {
                        if (opScopesofDefault.size() == 1) {
                            String firstScope = opScopesofDefault.get(0);
                            Scope scope = APIUtil.findScopeByKey(scopes, firstScope);
                            if (scope == null) {
                                throw new APIManagementException("Scope '" + firstScope + "' not found.");
                            }
                            template.setScope(scope);
                            template.setScopes(scope);
                        } else {
                            template = OASParserUtil.setScopesToTemplate(template, opScopesofDefault);
                        }
                    }
                    Map<String, Object> extensios = operation.getExtensions();
                    if (extensios != null) {
                        if (extensios.containsKey(APIConstants.SWAGGER_X_AUTH_TYPE)) {
                            String scopeKey = (String) extensios.get(APIConstants.SWAGGER_X_AUTH_TYPE);
                            template.setAuthType(scopeKey);
                            template.setAuthTypes(scopeKey);
                        } else {
                            template.setAuthType("Any");

                            template.setAuthTypes("Any");
                        }
                        if (extensios.containsKey(APIConstants.SWAGGER_X_THROTTLING_TIER)) {
                            String throttlingTier = (String) extensios.get(APIConstants.SWAGGER_X_THROTTLING_TIER);
                            template.setThrottlingTier(throttlingTier);
                            template.setThrottlingTiers(throttlingTier);
                        }
                        if (extensios.containsKey(APIConstants.SWAGGER_X_MEDIATION_SCRIPT)) {
                            String mediationScript = (String) extensios.get(APIConstants.SWAGGER_X_MEDIATION_SCRIPT);
                            template.setMediationScript(mediationScript);
                            template.setMediationScripts(template.getHTTPVerb(), mediationScript);
                        }
                    }
                    urlTemplates.add(template);
                }
            }
        }
        return urlTemplates;
    }

    /**
     * Gets a list of scopes using the security requirements(Swagger version 3)
     *
     * @param oauth2SchemeKey OAuth2 security element key
     * @param operation       Swagger path operation
     * @return list of scopes using the security requirements
     */
    private static List<String> getScopeOfOperations_SV3(String oauth2SchemeKey, Operation operation) {
        List<SecurityRequirement> security = operation.getSecurity();
        if (security != null) {
            for (Map<String, List<String>> requirement : security) {
                if (requirement.get(oauth2SchemeKey) != null) {
                    return requirement.get(oauth2SchemeKey);
                }
            }
        }
        return getScopeOfOperationsFromExtensions_SV3(operation);
    }

    /**
     * Get scope of operation(Swagger version 3)
     *
     * @param operation
     * @return
     */
    private static List<String> getScopeOfOperationsFromExtensions_SV3(Operation operation) {
        Map<String, Object> extensions = operation.getExtensions();
        if (extensions != null && extensions.containsKey(APIConstants.SWAGGER_X_SCOPE)) {
            String scopeKey = (String) extensions.get(APIConstants.SWAGGER_X_SCOPE);
            return Collections.singletonList(scopeKey);
        }
        return Collections.emptyList();
    }

    /**
     * Get scope information from the extensions(Swagger version 3)
     *
     * @param openAPI openAPI object
     * @return Scope set
     * @throws APIManagementException if an error occurred
     */
    private static Set<Scope> getScopesFromExtensions_SV3(OpenAPI openAPI) throws APIManagementException {
        Set<Scope> scopeList = new LinkedHashSet<>();
        Map<String, Object> extensions = openAPI.getExtensions();
        if (extensions != null && extensions.containsKey(APIConstants.SWAGGER_X_WSO2_SECURITY)) {
            Map<String, Object> securityDefinitions =
                    (Map<String, Object>) extensions.get(APIConstants.SWAGGER_X_WSO2_SECURITY);
            for (Map.Entry<String, Object> entry : securityDefinitions.entrySet()) {
                Map<String, Object> securityDefinition = (Map<String, Object>) entry.getValue();
                if (securityDefinition.containsKey(APIConstants.SWAGGER_X_WSO2_SCOPES)) {
                    List<Map<String, String>> oauthScope =
                            (List<Map<String, String>>) securityDefinition.get(APIConstants.SWAGGER_X_WSO2_SCOPES);
                    for (Map<String, String> anOauthScope : oauthScope) {
                        Scope scope = new Scope();
                        scope.setKey(anOauthScope.get(APIConstants.SWAGGER_SCOPE_KEY));
                        scope.setName(anOauthScope.get(APIConstants.SWAGGER_NAME));
                        scope.setDescription(anOauthScope.get(APIConstants.SWAGGER_DESCRIPTION));
                        scope.setRoles(anOauthScope.get(APIConstants.SWAGGER_ROLES));
                        scopeList.add(scope);
                    }
                }
            }
        }
        return scopeList;
    }
}


