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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIDefinition;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.Scope;
import org.wso2.carbon.apimgt.core.models.URITemplate;
import org.wso2.carbon.apimgt.core.util.APIConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;

import java.util.*;

public class APIDefinitionFromSwagger20 implements APIDefinition {


    private static final Logger log = LoggerFactory.getLogger(APIDefinitionFromSwagger20.class);

    /**
     * This method returns URI templates according to the given swagger file
     *
     * @param resourceConfigsJSON swaggerJSON
     * @return URI Templates
     * @throws APIManagementException
     */
    public Set<URITemplate> getURITemplates(String resourceConfigsJSON) throws APIManagementException {
        Set<URITemplate> uriTemplateSet = new HashSet<>();
        SwaggerParser swaggerParser = new SwaggerParser();
        Swagger swagger = swaggerParser.parse(resourceConfigsJSON);
        Map<String, Path> resourceList = swagger.getPaths();
        Map<String, Scope> scopeMap = getScopes(resourceConfigsJSON);
        for (Map.Entry<String, Path> resourceEntry : resourceList.entrySet()) {
            Path resource = resourceEntry.getValue();
            URITemplate.URITemplateBuilder uriTemplateBuilder = new URITemplate.URITemplateBuilder();
            uriTemplateBuilder.uriTemplate(resourceEntry.getKey());
            for (Map.Entry<HttpMethod, Operation> operationEntry : resource.getOperationMap().entrySet()) {
                Operation operation = operationEntry.getValue();
                Map<String, Object> vendorExtensions =operation.getVendorExtensions();
                String authType = (String) vendorExtensions.get(APIConstants.SWAGGER_X_AUTH_TYPE);
                if (authType == null){
                    uriTemplateBuilder.authType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
                }else{
                    uriTemplateBuilder.authType(authType);
                }
                String policy = (String) vendorExtensions.get(APIConstants.SWAGGER_X_THROTTLING_TIER);
                if (policy == null){
                    uriTemplateBuilder.policy(APIConstants.DEFAULT_API_POLICY);
                }else{
                    uriTemplateBuilder.policy(policy);
                }
                List<String> producesList = operation.getProduces();
                if (producesList != null){
                    String produceSeparatedString = "\"";
                    produceSeparatedString += String.join("\",\"", producesList) + "\"";
                    uriTemplateBuilder.produces(produceSeparatedString);
                }
                List<String> consumesList = operation.getConsumes();
                if (consumesList != null){
                    String consumesSeparatedString = "\"";
                    consumesSeparatedString += String.join("\",\"", consumesList) + "\"";
                    uriTemplateBuilder.produces(consumesSeparatedString);
                }
                if (operation.getOperationId() != null){
                    uriTemplateBuilder.templateId(operation.getOperationId());
                }
                uriTemplateBuilder.httpVerb(operationEntry.getKey().name());
                uriTemplateBuilder.scope(scopeMap.get(vendorExtensions.get(APIConstants.SWAGGER_X_SCOPE)));
                uriTemplateSet.add(uriTemplateBuilder.build());
            }
        }
        return uriTemplateSet;
    }

    public Map<String, Scope> getScopes(String resourceConfigsJSON) throws APIManagementException {
        SwaggerParser swaggerParser = new SwaggerParser();
        Swagger swagger = swaggerParser.parse(resourceConfigsJSON);
        Map<String, Scope> scopeMap = new HashMap<>();
        try {
            if (swagger.getVendorExtensions() != null) {
                JSONObject scopesJson;
                scopesJson = (JSONObject) new JSONParser().parse(swagger.getVendorExtensions().get
                        (APIConstants.SWAGGER_X_WSO2_SECURITY).toString());
                Iterator<JSONObject> scopesIterator = ((JSONArray) ((JSONObject) scopesJson.get(APIConstants
                        .SWAGGER_OBJECT_NAME_APIM)).get
                        (APIConstants.SWAGGER_X_WSO2_SCOPES)).iterator();
                while (scopesIterator.hasNext()) {
                    Scope scope = new Gson().fromJson(scopesIterator.next().toJSONString(), Scope.class);
                    scopeMap.put(scope.getKey(), scope);
                }
            }
        } catch (ParseException e) {
            APIUtils.logAndThrowException("Couldn't extract scopes from swagger ", e, log);
        }
        return scopeMap;
    }

}
