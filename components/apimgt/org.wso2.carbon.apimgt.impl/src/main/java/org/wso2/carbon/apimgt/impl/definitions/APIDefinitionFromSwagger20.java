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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
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
    private final String SWAGGER_2_0_FILE_NAME = "swagger.json";

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
        Set<Scope> scopes = getScopes(resourceConfigsJSON);
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
                            Scope scope= APIUtil.findScopeByKey(scopes,(String) operation.get("x-scope"));
                            String authType = (String) operation.get("auth_type");
                            if ("Application & Application User".equals(authType)) {
                                authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                            } else if ("Application User".equals(authType)) {
                                authType = APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN;
                            } else if ("None".equals(authType)) {
                                authType = APIConstants.AUTH_TYPE_NONE;
                            } else {
                                authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                            }
                            template.setThrottlingTier((String) operation.get("x-throttling-tier"));
                            template.setMediationScript((String) operation.get("x-mediation-script"));
                            template.setUriTemplate(uriTempVal);
                            template.setHTTPVerb(httpVerb.toUpperCase());
                            template.setAuthType(authType);
                            template.setScope(scope);

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
                    //Read scopes from custom wso2 scopes

                    if (securityDefinition.get("x-wso2-scopes") != null) {
                        JSONArray oauthScope = (JSONArray) securityDefinition.get("x-wso2-scopes");
                        for (Object anOauthScope : oauthScope) {
                            Scope scope = new Scope();
                            JSONObject scopeObj = (JSONObject) anOauthScope;
                            scope.setKey((String) scopeObj.get("key"));
                            scope.setName((String) scopeObj.get("name"));
                            scope.setDescription((String) scopeObj.get("description"));
                            scope.setRoles(scopeObj.get("roles").toString());

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
     * @throws APIManagementException
     */
    @Override
    public void saveAPIDefinition(API api, String apiDefinitionJSON, Registry registry) throws APIManagementException {
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

            //@todo Set visibility as same as the API visibility
            //Need to set anonymous if the visibility is public
            APIUtil.setResourcePermissions(apiProviderName, null, null, resourcePath);

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

    /**
     * This method generates swagger 2.0 definition to the given api
     *
     * @param api api
     * @return swagger v2.0 doc as string
     * @throws APIManagementException
     */
    @Override
    @SuppressWarnings("unchecked")
    public String createAPIDefinition(API api) throws APIManagementException {
        JSONParser parser = new JSONParser();
        String contactObjectTemplate = "{\"name\":\"\", \"url\":\"\", \"email\":\"\"}";
        String licenceObjectTemplate = "{\"name\":\"\", \"url\":\"\"}";
        String infoObjectTemplate = "{\"title\":\"\",\"description\":\"\",\"termsOfService\":\"\",\"contact\":[]," +
                "\"licence\":[],\"version\":\"\"}";

        String pathsObjectTemplate = "{}";

        String pathItemObjectTemplate = "{\"$ref\":\"\",\"get\":\"\",\"put\":\"\",\"post\":\"\",\"delete\":\"\"," +
                "\"options\":\"\",\"head\":\"\",\"patch\":\"\",\"parameters\":\"\"}";
        String externalDocObjectTemplate = "{\"description\":\"\",\"url\":\"\"}";
        String parameterObjectTemplate = "{\"name\":\"\", \"in\":\"\", \"description\":\"\", \"required\":\"\"}";
        String schemaObjectTemplate = "{\"discriminator\":\"\", \"readOnly\":\"\", \"xml\":[], \"externalDocs\":[], \"example\":\"\"}";
        String xmlObjectTemplate = "{\"name\":\"\",\"namespace\":\"\",\"prefix\":\"\",\"attribute\":\"\",\"wrapped\":\"\"}";
        String responsesObjectTemplate = "{\"default\":\"\"}";
        String responseObjectTemplate = "{\"description\":\"\",\"schema\":[],\"headers\":[],\"example\":[]}";

        String headersObject = "{}";
        String exampleObject = "{}";
        String definitionObject = "{}";
        String parameterDefinitionObject = "{}";
        String responsesDefinitionObject = "{}";
        String securityDefinitionObject = "{}";

        String securitySchemeObjectTemplate = "{\"type\":\"\",\"description\":\"\",\"name\":\"\",\"in\":\"\"," +
                "\"flow\":\"\",\"authorizationUrl\":\"\",\"tokenUrl\":\"\",\"scopes\":[]}";

        String scopesObject = "{}";
        String securityRequirementObject = "{}";

        String tagObjectTemplate = "{\"name\":\"\", \"description\":\"\",\"externalDocs\":[]}";


        String swaggerObjectTemplate = "{\"swagger\":\"2.0\",\"info\":[],\"host\":\"\",\"basePath\":\"\",\"schemes\":[]," +
                "\"consumes\":[],\"produces\":[],\"paths\":[],\"definitions\":[],\"parameters\":[],\"responses\":[]," +
                "\"securityDefinitions\":[],\"security\":[],\"tags\":[],\"externalDocs\":[]}";

        APIIdentifier identifier = api.getId();
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();

        Environment environment = (Environment) config.getApiGatewayEnvironments().values().toArray()[0];
        String endpoints = environment.getApiGatewayEndpoint();
        String[] endpointsSet = endpoints.split(",");
        Set<URITemplate> uriTemplates = api.getUriTemplates();

        if (endpointsSet.length < 1) {
            throw new APIManagementException("Error in creating JSON representation of the API" + identifier.getApiName());
        }

        JSONObject swaggerObject;

        try {
            swaggerObject = (JSONObject) parser.parse(swaggerObjectTemplate);

            //Create info object
            JSONObject infoObject = (JSONObject) parser.parse(infoObjectTemplate);
            infoObject.put("title", api.getId().getApiName());
            infoObject.put("description", api.getDescription());

            //Create contact object and map business owner info
            JSONObject contactObject = (JSONObject) parser.parse(contactObjectTemplate);
            contactObject.put("name", api.getBusinessOwner());
            contactObject.put("email", api.getBusinessOwnerEmail());
            //put contact object to info object
            infoObject.put("contact", contactObject);

            //Create licence object
            JSONObject licenceObject = (JSONObject) parser.parse(licenceObjectTemplate);

            infoObject.put("licence", licenceObject);
            infoObject.put("version", api.getId().getVersion());

            //add info object to swaggerObject
            swaggerObject.put("info", infoObject);

            for (URITemplate uriTemplate : uriTemplates) {

            }

            //Create security scheme object
            JSONObject securitySchemeObject = (JSONObject) parser.parse(securitySchemeObjectTemplate);

        } catch (ParseException e) {
            throw new APIManagementException("Error while generating swagger v2.0 resource for api " + api.getId().getProviderName()
                    + "-" + api.getId().getApiName()
                    + "-" + api.getId().getVersion(), e);
        }

        return  swaggerObject.toJSONString();
    }

}
