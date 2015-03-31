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

import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.apimgt.impl.dto.Environment;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class APIDefinitionFromSwagger12 extends APIDefinition {
    @Override
    public Set<URITemplate> getURITemplates(API api, String resourceConfigsJSON) throws APIManagementException {
        JSONParser parser = new JSONParser();
        JSONObject resourceConfigs;
        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        try {
            resourceConfigs = (JSONObject) parser.parse(resourceConfigsJSON);
            JSONArray resources = (JSONArray) resourceConfigs.get("resources");
            //Iterating each resourcePath config
            for (Object resource : resources) {
                JSONObject resourceConfig = (JSONObject) resource;
                APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();

                Map<String, Environment> environments = config.getApiGatewayEnvironments();
                Environment environment = null;
                String endpoint = null;
                if (environments != null) {
                    Set<String> publishedEnvironments = api.getEnvironments();
                    if (publishedEnvironments.isEmpty() || publishedEnvironments.contains("none")) {
                        environment = environments.values()
                                .toArray(new Environment[environments.size()])[0];
                        String gatewayEndpoint = environment.getApiGatewayEndpoint();
                        if (gatewayEndpoint.contains(",")) {
                            endpoint = gatewayEndpoint.split(",")[0];
                        } else {
                            endpoint = gatewayEndpoint;
                        }
                    } else {
                        for (String environmentName : publishedEnvironments) {
                            // find environment that has hybrid type
                            if (APIConstants.GATEWAY_ENV_TYPE_HYBRID
                                    .equals(environments.get(environmentName).getType())) {
                                environment = environments.get(environmentName);
                                break;
                            }
                        }
                        //if not having any hybrid environment give 1st environment in api published list
                        if (environment == null) {
                            environment = environments.get(publishedEnvironments.toArray()[0]);
                        }
                        String gatewayEndpoint = environment.getApiGatewayEndpoint();
                        if (gatewayEndpoint != null && gatewayEndpoint.contains(",")) {
                            endpoint = gatewayEndpoint.split(",")[0];
                        } else {
                            endpoint = gatewayEndpoint;
                        }
                    }
                }
                //String apiPath = APIUtil.getAPIPath(apiIdentifier);
                if (endpoint.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    endpoint.substring(0, endpoint.length() - 1);
                }
                // We do not need the version in the base path since with the context version strategy, the version is
                // embedded in the context
                String basePath = endpoint + api.getContext();
                resourceConfig.put("basePath", basePath);
                //String resourceJSON = resourceConfig.toJSONString();

                JSONArray resource_configs = (JSONArray) resourceConfig.get("apis");

                //Iterating each Sub resourcePath config
                int subResCount = 0;
                while (subResCount < resource_configs.size()) {
                    JSONObject subResource = (JSONObject) resource_configs.get(subResCount);
                    String uriTempVal = (String) subResource.get("path");
                    uriTempVal = uriTempVal.startsWith("/") ? uriTempVal : ("/" + uriTempVal);

                    JSONArray operations = (JSONArray) subResource.get("operations");
                    //Iterating each operation config
                    for (Object operation1 : operations) {
                        JSONObject operation = (JSONObject) operation1;
                        String httpVerb = (String) operation.get("method");
                        /* Right Now PATCH is not supported. Need to remove this check when PATCH is supported*/
                        if (!"PATCH".equals(httpVerb)) {
                            URITemplate template = new URITemplate();
                            Scope scope = APIUtil.findScopeByKey(getScopes(resourceConfigsJSON), (String) operation.get("scope"));

                            String authType = (String) operation.get("auth_type");
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
                            template.setThrottlingTier((String) operation.get("throttling_tier"));
                            template.setMediationScript((String) operation.get("mediation_script"));
                            template.setUriTemplate(uriTempVal);
                            template.setHTTPVerb(httpVerb);
                            template.setAuthType(authType);
                            template.setScope(scope);

                            uriTemplates.add(template);
                        }
                    }

                    subResCount++;
                }
            }
        } catch (ParseException e) {
            handleException("Invalid resource configuration ", e);
        }
        return uriTemplates;
    }

    @Override
    public Set<Scope> getScopes(String resourceConfigsJSON) throws APIManagementException {
        Set<Scope> scopeList = new LinkedHashSet<Scope>();
        JSONObject resourceConfigs;
        JSONParser parser = new JSONParser();
        try {
            resourceConfigs = (JSONObject) parser.parse(resourceConfigsJSON);
            JSONObject api_doc = (JSONObject) resourceConfigs.get("api_doc");
            if (api_doc.get("authorizations") != null) {
                JSONObject authorizations = (JSONObject) api_doc.get("authorizations");
                if (authorizations.get("oauth2") != null) {
                    JSONObject oauth2 = (JSONObject) authorizations.get("oauth2");
                    if (oauth2.get("scopes") != null) {
                        JSONArray scopes = (JSONArray) oauth2.get("scopes");

                        if (scopes != null) {
                            for (Object scopeObj : scopes) {
                                Map scopeMap = (Map) scopeObj;
                                if (scopeMap.get("key") != null) {
                                    Scope scope = new Scope();
                                    scope.setKey((String) scopeMap.get("key"));
                                    scope.setName((String) scopeMap.get("name"));
                                    scope.setRoles((String) scopeMap.get("roles"));
                                    scope.setDescription((String) scopeMap.get("description"));
                                    scopeList.add(scope);
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
            String resourcePath = APIUtil.getSwagger12DefinitionFilePath(apiName, apiVersion, apiProviderName);
            //todo remove APIConstants.API_DOC_1_2_RESOURCE_NAME and add filename
            resourcePath = resourcePath + APIConstants.API_DOC_1_2_RESOURCE_NAME;
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
            if (registry.resourceExists(resourcePath + APIConstants.API_DOC_1_2_RESOURCE_NAME)) {
                Resource apiDocResource = registry.get(resourcePath + APIConstants.API_DOC_1_2_RESOURCE_NAME);
                String apiDocContent = new String((byte[]) apiDocResource.getContent());
                apiJSON = (JSONObject) parser.parse(apiDocContent);
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
            handleException("Error while retrieving Swagger Definition for " + apiIdentifier.getApiName() + "-" +
                    apiIdentifier.getVersion(), e);
        } catch (ParseException e) {
            handleException("Error while parsing Swagger Definition for " + apiIdentifier.getApiName() + "-" +
                    apiIdentifier.getVersion() + " in " + resourcePath, e);
        }
        return apiJSON.toJSONString();

    }
}
