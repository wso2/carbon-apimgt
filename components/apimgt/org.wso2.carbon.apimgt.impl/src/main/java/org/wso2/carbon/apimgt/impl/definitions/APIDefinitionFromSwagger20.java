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

package org.wso2.carbon.apimgt.impl.definitions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class APIDefinitionFromSwagger20 extends APIDefinition {

    private static final Log log = LogFactory.getLog(APIDefinitionFromSwagger20.class);
    private final String SWAGGER_2_0_FILE_NAME = "/swagger.json";

    /**
     * This method returns URI templates according to the given swagger file
     *
     * @param api                 API
     * @param resourceConfigsJSON swaggerJSON
     * @return URI Templates
     * @throws APIManagementException
     */
    @Override
    public Set<URITemplate> getURITemplates(API api, String resourceConfigsJSON) throws APIManagementException {
        JSONParser parser = new JSONParser();
        JSONObject swagger;
        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        try {
            swagger = (JSONObject) parser.parse(resourceConfigsJSON);
            if (swagger.get("paths") != null) {
                JSONObject paths = (JSONObject) swagger.get("paths");
                for (Iterator pathsIterator = paths.keySet().iterator(); pathsIterator.hasNext(); ) {
                    String uriTempVal = (String) pathsIterator.next();
                    JSONObject path = (JSONObject) paths.get(uriTempVal);
                    for (Iterator pathIterator = path.keySet().iterator(); pathIterator.hasNext(); ) {
                        String httpVerb = (String) pathIterator.next();
                        JSONObject operation = (JSONObject) path.get(httpVerb);

                        //PATCH is not supported. Need to remove this check when PATCH is supported
                        if (!"PATCH".equals(httpVerb)) {
                            URITemplate template = new URITemplate();
                            //Scope scope= APIUtil.findScopeByKey(scopeList,(String) operation.get("scope"));
                            String authType = (String) operation.get("auth_type");
                            if ("Application & Application User".equals(authType)) {
                                authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                            } else if ("Application User".equals(authType)) {
                                authType = APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN;
                            } else {
                                authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                            }
                            template.setThrottlingTier((String) operation.get("x-throttling-tier"));
                            template.setMediationScript((String) operation.get("x-mediation-script"));
                            template.setUriTemplate(uriTempVal);
                            template.setHTTPVerb(httpVerb.toUpperCase());
                            template.setAuthType(authType);
                            //template.setScope(scope);

                            uriTemplates.add(template);
                        }
                    }
                }
            } else {
                //@todo generate default paths
            }
        } catch (ParseException e) {
            handleException("Invalid resource configuration ", e);
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
            if (swaggerObject.get("securityDefinitions") != null) {
                JSONObject securityDefinitionsObjects = (JSONObject) swaggerObject.get("securityDefinitions");
                Iterator<JSONObject> definitionIterator = securityDefinitionsObjects.values().iterator();
                while (definitionIterator.hasNext()) {
                    JSONObject securityDefinition = definitionIterator.next();
                    if (securityDefinition.get("scopes") != null) {
                        JSONObject scopes = (JSONObject) securityDefinition.get("scopes");
                        JSONObject roles = null;
                        if (securityDefinition.get("x-scope-roles") != null) {
                            roles = (JSONObject) securityDefinition.get("x-scope-roles");
                        }
                        Set keySet = scopes.keySet();
                        for (Object key : keySet) {
                            Scope scope = new Scope();
                            scope.setKey(key.toString());
                            scope.setDescription((String) scopes.get(key));
                            if (roles != null) {
                                if (roles.get(key) != null) {
                                    scope.setRoles(roles.get(key).toString());
                                } else {
                                    scope.setRoles("[]");
                                }
                            }
                            scopeList.add(scope);
                        }
                    }
                }

            }
        } catch (ParseException e) {
            handleException("Invalid resource configuration ", e);
        }
        return scopeList;
    }

    /**
     * This method saves api definition json in the registry
     *
     * @param api               API to be saved
     * @param apiDefinitionJSON API definition as JSON string
     * @param registry          user registry
     * @throws ParseException
     * @throws APIManagementException
     */
    @Override
    public void saveAPIDefinition(API api, String apiDefinitionJSON, Registry registry) throws ParseException, APIManagementException {
        String apiName = api.getId().getApiName();
        String apiVersion = api.getId().getVersion();
        String apiProviderName = api.getId().getProviderName();

        try {
            String resourcePath = APIUtil.getSwagger20DefinitionFilePath(apiName, apiVersion, apiProviderName);
            resourcePath = resourcePath + SWAGGER_2_0_FILE_NAME;
            Resource resource;
            if (!registry.resourceExists(resourcePath)) {
                resource = registry.newResource();
            } else {
                resource = registry.get(resourcePath);
            }
            resource.setContent(apiDefinitionJSON);
            resource.setMediaType("application/json");
            registry.put(resourcePath, resource);

            //Set visibility as same as the API visibility
            APIUtil.setResourcePermissions(apiProviderName, api.getVisibility(), null, resourcePath);

        } catch (RegistryException e) {
            handleException("Error while adding Swagger Definition for " + apiName + "-" + apiVersion, e);
        } catch (APIManagementException e) {
            handleException("Error while adding Swagger Definition for " + apiName + "-" + apiVersion, e);
        }

    }


    /**
     * This method returns api definition json for given api
     *
     * @param apiIdentifier api identifier
     * @param registry      user registry
     * @return api definition json as json string
     * @throws APIManagementException
     */
    @Override
    public String getAPIDefinition(APIIdentifier apiIdentifier, Registry registry) throws APIManagementException {
        String resourcePath = APIUtil.getSwagger20DefinitionFilePath(apiIdentifier.getApiName(),
                apiIdentifier.getVersion(), apiIdentifier.getProviderName());

        JSONParser parser = new JSONParser();
        JSONObject apiJSON;
        String apiDefinition = null;
        try {
            if (registry.resourceExists(resourcePath + SWAGGER_2_0_FILE_NAME)) {
                Resource apiDocResource = registry.get(resourcePath + SWAGGER_2_0_FILE_NAME);
                String apiDocContent = new String((byte[]) apiDocResource.getContent());
                apiJSON = (JSONObject) parser.parse(apiDocContent);
                apiDefinition = apiJSON.toJSONString();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Resource " + SWAGGER_2_0_FILE_NAME + " not found at " + resourcePath);
                }
            }
        } catch (RegistryException e) {
            handleException("Error while retrieving Swagger v2.0 Definition for " + apiIdentifier.getApiName() + "-" +
                    apiIdentifier.getVersion(), e);
        } catch (ParseException e) {
            handleException("Error while parsing Swagger v2.0 Definition for " + apiIdentifier.getApiName() + "-" +
                    apiIdentifier.getVersion() + " in " + resourcePath, e);
        }
        return apiDefinition;
    }
}
