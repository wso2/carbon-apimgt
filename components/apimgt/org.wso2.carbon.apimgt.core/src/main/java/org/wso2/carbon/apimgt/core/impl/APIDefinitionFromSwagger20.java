/***********************************************************************************************************************
 * *
 * *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * *
 * *   WSO2 Inc. licenses this file to you under the Apache License,
 * *   Version 2.0 (the "License"); you may not use this file except
 * *   in compliance with the License.
 * *   You may obtain a copy of the License at
 * *
 * *     http://www.apache.org/licenses/LICENSE-2.0
 * *
 * *  Unless required by applicable law or agreed to in writing,
 * *  software distributed under the License is distributed on an
 * *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * *  KIND, either express or implied.  See the License for the
 * *  specific language governing permissions and limitations
 * *  under the License.
 * *
 */

package org.wso2.carbon.apimgt.core.impl;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIDefinition;
import org.wso2.carbon.apimgt.core.api.Scope;
import org.wso2.carbon.apimgt.core.api.URITemplate;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.util.APIConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class APIDefinitionFromSwagger20 extends APIDefinition {


    private static final Logger log = LoggerFactory.getLogger(APIDefinitionFromSwagger20.class);

    /**
     * This method returns URI templates according to the given swagger file
     *
     * @param resourceConfigsJSON swaggerJSON
     * @return URI Templates
     * @throws APIManagementException
     */
    @Override
    public Set<URITemplate> getURITemplates(String resourceConfigsJSON) throws APIManagementException {
        JSONParser parser = new JSONParser();
        JSONObject swagger;
        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        Set<Scope> scopes = getScopes(resourceConfigsJSON);
        try {
            swagger = (JSONObject) parser.parse(resourceConfigsJSON);
            if (swagger.get("paths") != null) {
                JSONObject paths = (JSONObject) swagger.get("paths");
                for (Object o : paths.keySet()) {
                    String uriTempVal = (String) o;
                    //if url template is a custom attribute "^x-" ignore.
                    if (uriTempVal.startsWith("x-") || uriTempVal.startsWith("X-")) {
                        continue;
                    }
                    JSONObject path = (JSONObject) paths.get(uriTempVal);
                    // Following code check is done to handle $ref objects supported by swagger spec
                    // See field types supported by "Path Item Object" in swagger spec.
                    if (path.containsKey("$ref")) {
                        log.info("Reference " + uriTempVal + " path object was ignored when generating URL template " +
                                "for api");
                        continue;
                    }
                    for (Object o1 : path.keySet()) {
                        String httpVerb = (String) o1;

                        //Only continue for supported operations
                        if (APIConstants.SUPPORTED_METHODS.contains(httpVerb.toLowerCase(Locale.getDefault()))) {
                            JSONObject operation = (JSONObject) path.get(httpVerb);
                            URITemplate template = new URITemplate();
                            Scope scope = APIUtils.findScopeByKey(scopes, (String) operation.get(APIConstants
                                    .SWAGGER_X_SCOPE));
                            String authType = (String) operation.get(APIConstants.SWAGGER_X_AUTH_TYPE);
                            if ("Application & Application User".equals(authType)) {
                                authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                            } else if ("Application User".equals(authType)) {
                                authType = APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN;
                            } else if ("None".equals(authType)) {
                                authType = APIConstants.AUTH_NO_AUTHENTICATION;
                            } else if ("Application".equals(authType)) {
                                authType = APIConstants.AUTH_APPLICATION_LEVEL_TOKEN;
                            } else {
                                authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                            }
                            template.setThrottlingTier((String) operation.get(APIConstants.SWAGGER_X_THROTTLING_TIER));
                            template.setThrottlingTiers((String) operation.get(APIConstants.SWAGGER_X_THROTTLING_TIER));
                            template.setMediationScript((String) operation.get(APIConstants
                                    .SWAGGER_X_MEDIATION_SCRIPT));
                            template.setMediationScripts((String) operation.get(
                                    APIConstants.SWAGGER_X_MEDIATION_SCRIPT), httpVerb.toUpperCase(Locale.getDefault
                                    ()));
                            template.setUriTemplate(uriTempVal);
                            template.setHTTPVerb(httpVerb.toUpperCase(Locale.getDefault()));
                            template.setHttpVerbs(httpVerb.toUpperCase(Locale.getDefault()));
                            template.setAuthType(authType);
                            template.setAuthTypes(authType);
                            template.setScope(scope);
                            template.setScopes(scope);

                            uriTemplates.add(template);
                        }
                    }
                }
            }
        } catch (ParseException e) {
            APIUtils.logAndThrowException("Invalid resource configuration ", e, log);
        }
        return uriTemplates;
    }

    /**
     * This method returns the oauth scopes according to the given swagger
     *
     * @param resourceConfigsJSON resource json
     * @return scope set
     * @throws APIManagementException
     */
    @Override
    public Set<Scope> getScopes(String resourceConfigsJSON) throws APIManagementException {
        Set<Scope> scopeList = new LinkedHashSet<Scope>();
        JSONObject swaggerObject;
        JSONParser parser = new JSONParser();
        try {
            swaggerObject = (JSONObject) parser.parse(resourceConfigsJSON);

            //Check whether security definitions are defined or not
            if (swaggerObject.get(APIConstants.SWAGGER_X_WSO2_SECURITY) != null) {
                JSONObject securityDefinitionsObjects = (JSONObject) swaggerObject.get(APIConstants
                        .SWAGGER_X_WSO2_SECURITY);

                for (JSONObject securityDefinition : (Iterable<JSONObject>) securityDefinitionsObjects.values()) {
                    //Read scopes from custom wso2 scopes

                    if (securityDefinition.get(APIConstants.SWAGGER_X_WSO2_SCOPES) != null) {
                        JSONArray oauthScope = (JSONArray) securityDefinition.get(APIConstants.SWAGGER_X_WSO2_SCOPES);
                        for (Object anOauthScope : oauthScope) {
                            Scope scope = new Scope();
                            JSONObject scopeObj = (JSONObject) anOauthScope;
                            scope.setKey((String) scopeObj.get(APIConstants.SWAGGER_SCOPE_KEY));
                            scope.setName((String) scopeObj.get(APIConstants.SWAGGER_NAME));
                            scope.setDescription((String) scopeObj.get(APIConstants.SWAGGER_DESCRIPTION));
                            scope.setRoles(scopeObj.get(APIConstants.SWAGGER_ROLES).toString());

                            scopeList.add(scope);
                        }
                    }
                }
            }
        } catch (ParseException e) {
        }
        return scopeList;
    }

    /**
     * This method returns api definition json for given api
     *
     * @param apiId  api identifier
     * @param apiDAO user Database
     * @return api definition json as json string
     * @throws APIManagementException
     */
    @Override
    public String getAPIDefinition(String apiId, ApiDAO apiDAO) throws APIManagementException {
        try {
            return apiDAO.getSwaggerDefinition(apiId);
        } catch (SQLException e) {
            APIUtils.logAndThrowException("Couldn't read Swagger definition for api ID " + apiId, e, log);
        }
        return null;
    }

}
