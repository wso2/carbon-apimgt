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

import com.google.gson.Gson;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIDefinition;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.Scope;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation for Swagger 2.0
 */
public class APIDefinitionFromSwagger20 implements APIDefinition {

    private static final Logger log = LoggerFactory.getLogger(APIDefinitionFromSwagger20.class);

    /**
     * This method returns URI templates according to the given swagger file
     *
     * @param resourceConfigsJSON swaggerJSON
     * @return URI Templates
     * @throws APIManagementException
     */
    public Set<UriTemplate> getURITemplates(StringBuilder resourceConfigsJSON) throws APIManagementException {
        Set<UriTemplate> uriTemplateSet = new HashSet<>();
        SwaggerParser swaggerParser = new SwaggerParser();
        Swagger swagger = swaggerParser.parse(resourceConfigsJSON.toString());
        Map<String, Path> resourceList = swagger.getPaths();
        Map<String, Scope> scopeMap = getScopes(resourceConfigsJSON.toString());
        for (Map.Entry<String, Path> resourceEntry : resourceList.entrySet()) {
            Path resource = resourceEntry.getValue();
            UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder();
            uriTemplateBuilder.uriTemplate(resourceEntry.getKey());
            for (Map.Entry<HttpMethod, Operation> operationEntry : resource.getOperationMap().entrySet()) {
                Operation operation = operationEntry.getValue();
                Map<String, Object> vendorExtensions = operation.getVendorExtensions();
                String authType = (String) vendorExtensions.get(APIMgtConstants.SWAGGER_X_AUTH_TYPE);
                if (authType == null) {
                    uriTemplateBuilder.authType(APIMgtConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
                    vendorExtensions.put(APIMgtConstants.SWAGGER_X_AUTH_TYPE, APIMgtConstants
                            .AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
                } else {
                    uriTemplateBuilder.authType(authType);
                }
                String policy = (String) vendorExtensions.get(APIMgtConstants.SWAGGER_X_THROTTLING_TIER);
                if (policy == null) {
                    uriTemplateBuilder.policy(APIUtils.getDefaultAPIPolicy());
                    vendorExtensions.put(APIMgtConstants.SWAGGER_X_THROTTLING_TIER, APIUtils.getDefaultAPIPolicy());
                } else {
                    uriTemplateBuilder.policy(policy);
                }
                List<String> producesList = operation.getProduces();
                if (producesList != null) {
                    String produceSeparatedString = "\"";
                    produceSeparatedString += String.join("\",\"", producesList) + "\"";
                    uriTemplateBuilder.produces(produceSeparatedString);
                }
                List<String> consumesList = operation.getConsumes();
                if (consumesList != null) {
                    String consumesSeparatedString = "\"";
                    consumesSeparatedString += String.join("\",\"", consumesList) + "\"";
                    uriTemplateBuilder.produces(consumesSeparatedString);
                }
                if (operation.getOperationId() != null) {
                    uriTemplateBuilder.templateId(operation.getOperationId());
                }
                uriTemplateBuilder.httpVerb(operationEntry.getKey().name());
                String scope = (String) vendorExtensions.get(APIMgtConstants.SWAGGER_X_SCOPE);
                if (StringUtils.isNotEmpty(scope)) {
                    uriTemplateBuilder.scope(scopeMap.get(scope));
                }
                uriTemplateSet.add(uriTemplateBuilder.build());
            }
        }
        resourceConfigsJSON.setLength(0);
        resourceConfigsJSON.append(Json.pretty(swagger));
        return uriTemplateSet;
    }

    public Map<String, Scope> getScopes(String resourceConfigsJSON) throws APIManagementException {
        SwaggerParser swaggerParser = new SwaggerParser();
        Swagger swagger = swaggerParser.parse(resourceConfigsJSON);
        Map<String, Scope> scopeMap = new HashMap<>();
        try {
            if (swagger.getVendorExtensions() != null) {
                String scopes = (String) swagger.getVendorExtensions().get(APIMgtConstants.SWAGGER_X_WSO2_SECURITY);
                if (StringUtils.isNotEmpty(scopes)) {
                    JSONObject scopesJson = (JSONObject) new JSONParser().parse(scopes);
                    Iterator<JSONObject> scopesIterator = ((JSONArray) ((JSONObject) scopesJson
                            .get(APIMgtConstants.SWAGGER_OBJECT_NAME_APIM)).get(APIMgtConstants.SWAGGER_X_WSO2_SCOPES))
                            .iterator();
                    while (scopesIterator.hasNext()) {
                        Scope scope = new Gson().fromJson(scopesIterator.next().toJSONString(), Scope.class);
                        scopeMap.put(scope.getKey(), scope);
                    }
                }
            }
        } catch (ParseException e) {
            log.error("Couldn't extract scopes from swagger ", e);
            throw new APIManagementException("Couldn't extract scopes from swagger ",
                    ExceptionCodes.SWAGGER_PARSE_EXCEPTION);
        }
        return scopeMap;
    }

}
