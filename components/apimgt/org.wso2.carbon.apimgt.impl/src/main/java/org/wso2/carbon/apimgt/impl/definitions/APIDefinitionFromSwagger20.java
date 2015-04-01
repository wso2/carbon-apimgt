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

import org.json.simple.JSONArray;
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
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class APIDefinitionFromSwagger20 extends APIDefinition {

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

                            String authType = (String) operation.get("x-auth-type");
                            if (authType != null) {
                                if (authType.equals("Application & Application User")) {
                                    authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                                }
                                if (authType.equals("Application User")) {
                                    authType = "Application_User";
                                }
                            } else {
                                authType = APIConstants.AUTH_NO_AUTHENTICATION;
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

    @Override
    public Set<Scope> getScopes(String resourceConfigsJSON) throws APIManagementException {
        Set<Scope> scopeList = new LinkedHashSet<Scope>();
        JSONObject swaggerObject;
        JSONParser parser = new JSONParser();
        try {
            swaggerObject = (JSONObject) parser.parse(resourceConfigsJSON);

            //Check whether security definitions are defined or not
            if (swaggerObject.get("securityDefinitions") != null) {
                JSONObject securityDefinitionsObj = (JSONObject) swaggerObject.get("securityDefinitions");
                //check for oauth2
                if (securityDefinitionsObj.get("oauth2") != null) {
                    JSONObject oauth2 = (JSONObject) securityDefinitionsObj.get("oauth2");
                    if (oauth2.get("scopes") != null) {
                        JSONArray scopes = (JSONArray) oauth2.get("scopes");
                        if (scopes != null) {
                            for (int i=0; i < scopes.size(); i++)
                            {
                                Map scope = (Map) scopes.get(i);
                                if (scope.get("key") != null) {
                                    Scope scopeObj = new Scope();
                                    scopeObj.setKey((String) scope.get("key"));
                                    scopeObj.setName((String) scope.get("name"));
                                    scopeObj.setRoles((String) scope.get("roles"));
                                    scopeObj.setDescription((String) scope.get("description"));
                                    scopeList.add(scopeObj);
                                }
                            }
                        }
                    }
                }
            }
        } catch (ParseException e) {
            handleException("Invalid resource configuration ", e);
        }
        return scopeList;
    }

    @Override
    public void saveAPIDefinition(API api, String apiDefinitionJSON, Registry registry) throws ParseException, APIManagementException {
        String apiName = api.getId().getApiName();
        String apiVersion = api.getId().getVersion();
        String apiProviderName = api.getId().getProviderName();

        try {
            String resourcePath = APIUtil.getSwagger20DefinitionFilePath(apiName, apiVersion, apiProviderName);
            resourcePath = resourcePath + "/swagger.json";
            Resource resource = registry.newResource();

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

    @Override
    public String getAPIDefinition(APIIdentifier apiIdentifier, Registry registry) throws APIManagementException {
        String resourcePath = APIUtil.getSwagger12DefinitionFilePath(apiIdentifier.getApiName(),
                apiIdentifier.getVersion(), apiIdentifier.getProviderName());

        JSONParser parser = new JSONParser();
        JSONObject apiJSON = null;
        try {
            if (registry.resourceExists(resourcePath + APIConstants.API_DOC_2_0_RESOURCE_NAME)) {
                Resource apiDocResource = registry.get(resourcePath + APIConstants.API_DOC_2_0_RESOURCE_NAME);
                String apiDocContent = new String((byte[]) apiDocResource.getContent());
                apiJSON = (JSONObject) parser.parse(apiDocContent);
                //todo not "apis"
                JSONArray pathConfigs = (JSONArray) apiJSON.get("apis");

                for (int k = 0; k < pathConfigs.size(); k++) {
                    JSONObject pathConfig = (JSONObject) pathConfigs.get(k);
                    String pathName = (String) pathConfig.get("path");
                    pathName = pathName.startsWith("/") ? pathName : ("/" + pathName);

                    Resource pathResource = registry.get(resourcePath + pathName);
                    String pathContent = new String((byte[]) pathResource.getContent());
                    JSONObject pathJSON = (JSONObject) parser.parse(pathContent);
                    pathConfig.put("file", pathJSON);
                }
            }
        } catch (RegistryException e) {
            handleException("Error while retrieving Swagger v2.0 Definition for " + apiIdentifier.getApiName() + "-" +
                    apiIdentifier.getVersion(), e);
        } catch (ParseException e) {
            handleException("Error while parsing Swagger v2.0 Definition for " + apiIdentifier.getApiName() + "-" +
                    apiIdentifier.getVersion() + " in " + resourcePath, e);
        }
        return apiJSON.toJSONString();
    }
}
