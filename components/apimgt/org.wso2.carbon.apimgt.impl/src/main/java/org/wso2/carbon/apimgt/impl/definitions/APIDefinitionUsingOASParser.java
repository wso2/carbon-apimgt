/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.definitions;

import io.swagger.models.Contact;
import io.swagger.models.HttpMethod;
import io.swagger.models.Info;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.registry.api.Registry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Models API definition using OAS (swagger 2.0) parser
 */
public class APIDefinitionUsingOASParser extends APIDefinition {

    private static final Log log = LogFactory.getLog(APIDefinitionUsingOASParser.class);
    private final Pattern CURLY_BRACES_PATTERN = Pattern.compile("(?<=\\{)(?!\\s*\\{)[^{}]+");

    @Override
    public Set<URITemplate> getURITemplates(API api, String apiDefinition)
            throws APIManagementException {
        SwaggerParser parser = new SwaggerParser();
        Swagger swagger = parser.parse(apiDefinition);
        Set<URITemplate> urlTemplates = new LinkedHashSet<>();

        String oauth2SchemeKey = getOAuth2SecuritySchemeKey(swagger);
        List<String> globalScopes = getOAuth2ScopeListFromSecurityElement(oauth2SchemeKey, swagger.getSecurity());

        for (String pathString : swagger.getPaths().keySet()) {
            Path path = swagger.getPath(pathString);
            Map<HttpMethod, Operation> operationMap = path.getOperationMap();
            for (Map.Entry<HttpMethod, Operation> entry : operationMap.entrySet()) {
                Operation op = entry.getValue();
                URITemplate template = new URITemplate();
                template.setHTTPVerb(entry.getKey().name().toUpperCase());
                template.setUriTemplate(pathString);

                if (op.getSecurity() != null) {
                    // If scopes defined in the operation level set those to the URL template 
                    List<String> scopes = getOAuth2ScopeListFromSecurityElementMap(oauth2SchemeKey, op.getSecurity());
                    if (scopes != null) {
                        template = setScopesToTemplate(template, scopes);
                    }
                } else if (globalScopes != null && globalScopes.size() > 0) {
                    // If there are no scopes defined in the operation level but there are global scopes, then set those 
                    template = setScopesToTemplate(template, globalScopes);
                }

                urlTemplates.add(template);
            }
        }

        return urlTemplates;
    }

    @Override
    public Set<Scope> getScopes(String apiDefinition) throws APIManagementException {
        SwaggerParser parser = new SwaggerParser();
        Swagger swagger = parser.parse(apiDefinition);
        Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();
        Set<Scope> scopeSet = new HashSet<>();
        for (Map.Entry<String, String> entry : ((OAuth2Definition) (securityDefinitions.get("OAuth2Security"))).
                getScopes().entrySet()) {
            Scope scope = new Scope();
            scope.setKey(entry.getKey());
            scopeSet.add(scope);
        }
        return scopeSet;
    }

    @Override
    public void saveAPIDefinition(API api, String apiDefinitionJSON, Registry registry)
            throws APIManagementException {

    }

    @Override
    public String getAPIDefinition(APIIdentifier apiIdentifier, Registry registry)
            throws APIManagementException {
        return null;
    }

    @Override
    public String generateAPIDefinition(API api) throws APIManagementException {
        Swagger swagger = new Swagger();

        //Create info object
        Info info = new Info();
        info.setTitle(api.getId().getApiName());
        if (api.getDescription() != null) {
            info.setDescription(api.getDescription());
        }

        Contact contact = new Contact();
        //Create contact object and map business owner info
        if (api.getBusinessOwner() != null) {
            contact.setName(api.getBusinessOwner());
        }
        if (api.getBusinessOwnerEmail() != null) {
            contact.setEmail(api.getBusinessOwnerEmail());
        }
        if (api.getBusinessOwner() != null || api.getBusinessOwnerEmail() != null) {
            //put contact object to info object
            info.setContact(contact);
        }

        info.setVersion(api.getId().getVersion());
        OAuth2Definition oAuth2Definition = new OAuth2Definition().password("https://test.com");

        Set<Scope> scopes = api.getScopes();
        
        if (scopes != null && !scopes.isEmpty()) {
            List<Map<String,String>> xSecurityScopesArray = new ArrayList<>();
            for (Scope scope : scopes) {
                oAuth2Definition.addScope(scope.getName(), scope.getDescription());

                Map<String, String> xWso2ScopesObject = new LinkedHashMap<>();
                xWso2ScopesObject.put(APIConstants.SWAGGER_SCOPE_KEY, scope.getKey());
                xWso2ScopesObject.put(APIConstants.SWAGGER_NAME, scope.getName());
                xWso2ScopesObject.put(APIConstants.SWAGGER_ROLES, scope.getRoles());
                xWso2ScopesObject.put(APIConstants.SWAGGER_DESCRIPTION, scope.getDescription());
                xSecurityScopesArray.add(xWso2ScopesObject);
            }
            Map<String, Object> xWSO2Scopes = new LinkedHashMap<>();
            xWSO2Scopes.put(APIConstants.SWAGGER_X_WSO2_SCOPES, xSecurityScopesArray);
            Map<String, Object> xWSO2SecurityDefinitionObject = new LinkedHashMap<>();
            xWSO2SecurityDefinitionObject.put(APIConstants.SWAGGER_OBJECT_NAME_APIM, xWSO2Scopes);

            swagger.setVendorExtension(APIConstants.SWAGGER_X_WSO2_SECURITY, xWSO2SecurityDefinitionObject);
        }

        swagger.addSecurityDefinition("OAuth2Security", oAuth2Definition);
        swagger.setInfo(info);

        for (URITemplate uriTemplate : api.getUriTemplates()) {
            Path path;
            if (swagger.getPath(uriTemplate.getUriTemplate()) != null) {
                path = swagger.getPath(uriTemplate.getUriTemplate());
            } else {
                path = new Path();
            }

            Operation operation = new Operation();
            List<String> pathParams = getPathParamNames(uriTemplate.getUriTemplate());
            for (String pathParam : pathParams) {
                PathParameter pathParameter = new PathParameter();
                pathParameter.setName(pathParam);
                pathParameter.setType("string");
                operation.addParameter(pathParameter);
            }

            String authType = uriTemplate.getAuthType();
            if (!APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN.equals(authType)
                    && !APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN.equals(authType)
                    && !APIConstants.AUTH_APPLICATION_LEVEL_TOKEN.equals(authType)
                    && !APIConstants.AUTH_NO_AUTHENTICATION.equals(authType)) {
                throw new APIManagementException("Invalid auth type provided.");
            }
            operation.setVendorExtension(APIConstants.SWAGGER_X_AUTH_TYPE, authType);
            operation.setVendorExtension(APIConstants.SWAGGER_X_THROTTLING_TIER, uriTemplate.getThrottlingTier());
            
            Response response = new Response();
            response.setDescription("OK");
            operation.addResponse(APIConstants.SWAGGER_RESPONSE_200, response);
            path.set(uriTemplate.getHTTPVerb().toLowerCase(), operation);

            swagger.path(uriTemplate.getUriTemplate(), path);
        }

        return Json.pretty(swagger);
    }

    /**
     * Extract and return path parameters in the given URI template
     * 
     * @param uriTemplate URI Template value
     * @return path parameters in the given URI template
     */
    private List<String> getPathParamNames(String uriTemplate) {
        List<String> params = new ArrayList<>();

        Matcher bracesMatcher = CURLY_BRACES_PATTERN.matcher(uriTemplate);
        while (bracesMatcher.find()) {
            params.add(bracesMatcher.group());
        }
        return params;
    }

    @Override
    public Map<String, String> getAPIOpenAPIDefinitionTimeStamps(APIIdentifier apiIdentifier,
            Registry registry) throws APIManagementException {
        return null;
    }

    /**
     * Retrieves the "Auth2" security scheme key
     *
     * @param swagger Swgger object
     * @return "Auth2" security scheme key
     */
    private String getOAuth2SecuritySchemeKey(Swagger swagger) {
        final String oauth2Type = new OAuth2Definition().getType();
        Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();
        for (Map.Entry<String, SecuritySchemeDefinition> definitionEntry : securityDefinitions.entrySet()) {
            if (oauth2Type.equals(definitionEntry.getValue().getType())) {
                return definitionEntry.getKey();
            }
        }

        return null;
    }

    /**
     * Gets a list of scopes using the security requirements
     *
     * @param oauth2SchemeKey      OAuth2 security element key
     * @param securityRequirements list of security requirements
     * @return list of scopes using the security requirements
     */
    private List<String> getOAuth2ScopeListFromSecurityElement(String oauth2SchemeKey,
            List<SecurityRequirement> securityRequirements) {

        if (securityRequirements != null) {
            for (SecurityRequirement requirement : securityRequirements) {
                if (requirement.getRequirements() != null
                        && requirement.getRequirements().get(oauth2SchemeKey) != null) {
                    return requirement.getRequirements().get(oauth2SchemeKey);
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * Gets a list of scopes using the security requirements
     *
     * @param oauth2SchemeKey      OAuth2 security element key
     * @param securityRequirements map of security requirements
     * @return list of scopes using the security requirements
     */
    private List<String> getOAuth2ScopeListFromSecurityElementMap(String oauth2SchemeKey,
            List<Map<String, List<String>>> securityRequirements) {

        if (securityRequirements != null) {
            for (Map<String, List<String>> requirement : securityRequirements) {
                if (requirement.get(oauth2SchemeKey) != null) {
                    return requirement.get(oauth2SchemeKey);
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * Sets the scopes to the URL template object using the given list of scopes
     *
     * @param template URL template
     * @param scopes   list of scopes
     * @return URL template after setting the scopes
     */
    private URITemplate setScopesToTemplate(URITemplate template, List<String> scopes) {
        for (String scope : scopes) {
            Scope scopeObj = new Scope();
            scopeObj.setKey(scope);
            scopeObj.setName(scope);

            template.setScopes(scopeObj);
        }
        return template;
    }
}
