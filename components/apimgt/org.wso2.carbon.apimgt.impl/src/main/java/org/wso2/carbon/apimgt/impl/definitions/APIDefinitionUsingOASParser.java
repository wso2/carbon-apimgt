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

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.registry.api.Registry;

import java.util.*;

/**
 * Models API definition using OAS (swagger 2.0) parser
 */
public class APIDefinitionUsingOASParser extends APIDefinition {

    private static final Log log = LogFactory.getLog(APIDefinitionUsingOASParser.class);

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
        return null;
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
