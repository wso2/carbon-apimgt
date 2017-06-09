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
import io.swagger.models.Contact;
import io.swagger.models.HttpMethod;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
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
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIResource;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.Scope;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.ServiceMethodInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * Implementation for Swagger 2.0
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "DM_CONVERT_CASE", justification = "Didn't need to do " +
        "as String already did internally")
public class APIDefinitionFromSwagger20 implements APIDefinition {

    private static final Logger log = LoggerFactory.getLogger(APIDefinitionFromSwagger20.class);
    private static Map<String, Map<String, String>> localConfigMap = new HashMap<>();

    @Override
    public String getScopeOfResourcePath(String resourceConfigsJSON, Request request,
                                         ServiceMethodInfo serviceMethodInfo) throws APIManagementException {
        SwaggerParser swaggerParser = new SwaggerParser();
        Swagger swagger = swaggerParser.parse(resourceConfigsJSON);
        String basepath = swagger.getBasePath();
        String verb = (String) request.getProperty("HTTP_METHOD");
//        Method resourceMethod = (Method) request.getProperty("method");todo change to this if msf4j2.3.0-m2 or higher
        Method resourceMethod = serviceMethodInfo.getMethod();

        if (resourceMethod == null || verb == null) {
            String message = "Could not read required properties from HTTP Request. HTTP_METHOD=" + verb +
                    " resourceTemplate=" + resourceMethod;
            log.error(message);
            throw new APIManagementException(message, ExceptionCodes.SWAGGER_URL_MALFORMED);
        }
        String apiPrefix = resourceMethod.getDeclaringClass().getAnnotation(javax.ws.rs.ApplicationPath.class).value();
        String pathTemplate = "";
        if (resourceMethod.getAnnotation(javax.ws.rs.Path.class) != null) {
            pathTemplate = resourceMethod.getAnnotation(javax.ws.rs.Path.class).value();
        }
        String nameSpace = null;
        if (basepath.contains("publisher")) {
            nameSpace = APIMgtConstants.NAMESPACE_PUBLISHER_API;
        } else if (basepath.contains("store")) {
            nameSpace = APIMgtConstants.NAMESPACE_STORE_API;
        } else if (basepath.contains("admin")) {
            nameSpace = APIMgtConstants.NAMESPACE_ADMIN_API;
        }

        //if namespace is not available in local cache add it.
        if (!localConfigMap.containsKey(nameSpace)) {
            localConfigMap.put(nameSpace, new HashMap<>());
        }

        if (nameSpace != null && localConfigMap.get(nameSpace).isEmpty()) {
            Map<String, String> configMap = ServiceReferenceHolder.getInstance().getRestAPIConfigurationMap(nameSpace);
            //update local cache with configs defined in configuration file(dep.yaml)
            if (configMap != null) {
                localConfigMap.get(nameSpace).putAll(configMap);
            }
            //update local cache with the resource to scope mapping read from swagger
            populateConfigMapForScopes(swagger, nameSpace);
        }

        String resourceConfig = verb + "_" + apiPrefix + pathTemplate;
        if (localConfigMap.get(nameSpace).containsKey(resourceConfig)) {
            return localConfigMap.get(nameSpace).get(resourceConfig);
        }
        return null;
    }

    /*
    * This method populates resource to scope mappings into localConfigMap
    *
    * @param swagger swagger oc of the apis
    * String namespacee unigue identifier of the api
    *
    * */
    private void populateConfigMapForScopes(Swagger swagger, String namespace) {
        if (null != swagger) {
            for (Map.Entry<String, Path> entry : swagger.getPaths().entrySet()) {
                Path resource = entry.getValue();
                Map<HttpMethod, Operation> operationsMap = resource.getOperationMap();
                for (Map.Entry<HttpMethod, Operation> httpverbEntry : operationsMap.entrySet()) {
                    if (httpverbEntry.getValue().getVendorExtensions().size() > 0 &&
                            null != httpverbEntry.getValue().getVendorExtensions().get("x-scope")) {
                        StringBuffer resourceConfigPath = new StringBuffer().append(httpverbEntry.getKey()).append("_")
                                .append(entry.getKey());
                        String path = resourceConfigPath.toString();
                        if (!localConfigMap.get(namespace).containsKey(path)) {
                            localConfigMap.get(namespace).put(path,
                                    httpverbEntry.getValue().getVendorExtensions().get("x-scope").toString());
                        }
                    }

                }
            }
        }
    }

    @Override
    public List<APIResource> parseSwaggerAPIResources(StringBuilder resourceConfigsJSON)
            throws APIManagementException {
        List<APIResource> apiResources = new ArrayList<>();
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
                APIResource.Builder apiResourceBuilder = new APIResource.Builder();
                List<String> producesList = operation.getProduces();
                if (producesList != null) {
                    String produceSeparatedString = "\"";
                    produceSeparatedString += String.join("\",\"", producesList) + "\"";
                    apiResourceBuilder.produces(produceSeparatedString);
                }
                List<String> consumesList = operation.getConsumes();
                if (consumesList != null) {
                    String consumesSeparatedString = "\"";
                    consumesSeparatedString += String.join("\",\"", consumesList) + "\"";
                    apiResourceBuilder.consumes(consumesSeparatedString);
                }
                if (operation.getOperationId() != null) {
                    uriTemplateBuilder.templateId(operation.getOperationId());
                } else {
                    uriTemplateBuilder.templateId(APIUtils.generateOperationIdFromPath(resourceEntry.getKey(),
                            operationEntry.getKey().name()));
                }
                uriTemplateBuilder.httpVerb(operationEntry.getKey().name());
                String scope = (String) vendorExtensions.get(APIMgtConstants.SWAGGER_X_SCOPE);
                if (StringUtils.isNotEmpty(scope)) {
                    apiResourceBuilder.scope(scopeMap.get(scope));
                }
                apiResourceBuilder.uriTemplate(uriTemplateBuilder.build());
                apiResources.add(apiResourceBuilder.build());
            }
        }
        resourceConfigsJSON.setLength(0);
        resourceConfigsJSON.append(Json.pretty(swagger));
        return apiResources;
    }

    @Override
    public Map<String, Scope> getScopes(String resourceConfigsJSON) throws APIManagementException {
        SwaggerParser swaggerParser = new SwaggerParser();
        Swagger swagger = swaggerParser.parse(resourceConfigsJSON);
        Map<String, Scope> scopeMap = new HashMap<>();
        try {
            if (swagger.getVendorExtensions() != null) {
                String scopes = swagger.getVendorExtensions().get(APIMgtConstants.SWAGGER_X_WSO2_SECURITY).toString();
                if (StringUtils.isNotEmpty(scopes)) {
                    JSONObject scopesJson = (JSONObject) new JSONParser().parse(scopes);
                    Iterator<?> scopesIterator = ((JSONArray) ((JSONObject) scopesJson
                            .get(APIMgtConstants.SWAGGER_OBJECT_NAME_APIM)).get(APIMgtConstants.SWAGGER_X_WSO2_SCOPES))
                            .iterator();
                    while (scopesIterator.hasNext()) {
                        Scope scope = new Gson().fromJson(((JSONObject) scopesIterator.next()).toJSONString(),
                                Scope.class);
                        scopeMap.put(scope.getKey(), scope);
                    }
                }
            }
        } catch (ParseException e) {
            log.error("Couldn't extract scopes from swagger ");
            throw new APIManagementException("Couldn't extract scopes from swagger ",
                    ExceptionCodes.SWAGGER_PARSE_EXCEPTION);
        }
        return scopeMap;
    }

    /**
     * generate the swagger from uri templates.
     *
     * @param api API Object
     * @return Generated swagger resources as string.
     */
    @Override
    public String generateSwaggerFromResources(API.APIBuilder api) {
        Swagger swagger = new Swagger();
        Info info = new Info();
        info.setTitle(api.getName());
        info.setDescription(api.getDescription());
        Contact contact = new Contact();
        if (api.getBusinessInformation() != null) {
            BusinessInformation businessInformation = api.getBusinessInformation();
            contact.setName(businessInformation.getBusinessOwner());
            contact.setEmail(businessInformation.getBusinessOwnerEmail());
        }
        info.setContact(contact);
        info.setVersion(api.getVersion());
        swagger.setInfo(info);
        Map<String, Path> stringPathMap = new HashMap();
        for (UriTemplate uriTemplate : api.getUriTemplates().values()) {
            String uriTemplateString = uriTemplate.getUriTemplate();
            List<Parameter> parameterList = getParameters(uriTemplateString);
            if (!HttpMethod.GET.toString().equalsIgnoreCase(uriTemplate.getHttpVerb()) && !HttpMethod.DELETE.toString
                    ().equalsIgnoreCase(uriTemplate.getHttpVerb()) && !HttpMethod.OPTIONS.toString().equalsIgnoreCase
                    (uriTemplate.getHttpVerb()) && !HttpMethod.HEAD.toString().equalsIgnoreCase(uriTemplate
                    .getHttpVerb())) {
                parameterList.add(getDefaultBodyParameter());
            }
            Operation operation = new Operation();
            operation.setParameters(parameterList);
            operation.setOperationId(uriTemplate.getTemplateId());
            operation.addResponse("200", getDefaultResponse());
            if (stringPathMap.containsKey(uriTemplateString)) {
                Path path = stringPathMap.get(uriTemplateString);
                path.set(uriTemplate.getHttpVerb().toLowerCase(), operation);
            } else {
                Path path = new Path();
                path.set(uriTemplate.getHttpVerb().toLowerCase(), operation);
                stringPathMap.put(uriTemplateString, path);
            }
        }
        swagger.setPaths(stringPathMap);
        swagger.setPaths(stringPathMap);
        return Json.pretty(swagger);
    }

    /**
     * return API Object
     *
     * @param provider      Provider of the API.
     * @param apiDefinition API definition as string
     * @return API object.
     * @throws APIManagementException If failed to generate API from swagger.
     */
    @Override
    public API.APIBuilder generateApiFromSwaggerResource(String provider, String apiDefinition) throws
            APIManagementException {
        SwaggerParser swaggerParser = new SwaggerParser();
        Swagger swagger = swaggerParser.parse(apiDefinition);

        if (swagger == null) {
            throw new APIManagementException("Swagger could not be generated from provided API definition");
        }

        Info apiInfo = swagger.getInfo();
        if (apiInfo == null) {
            throw new APIManagementException("Swagger doesn't contains the info");
        } else {
            String apiName = apiInfo.getTitle();
            String apiVersion = apiInfo.getVersion();
            String apiDescription = apiInfo.getDescription();
            Contact contact = apiInfo.getContact();
            BusinessInformation businessInformation = new BusinessInformation();
            if (contact != null) {
                businessInformation.setBusinessOwner(contact.getName());
                businessInformation.setBusinessOwnerEmail(contact.getEmail());
            }
            API.APIBuilder apiBuilder = new API.APIBuilder(provider, apiName, apiVersion);
            apiBuilder.businessInformation(businessInformation);
            apiBuilder.description(apiDescription);
            apiBuilder.context(swagger.getBasePath());
            List<APIResource> apiResourceList = parseSwaggerAPIResources(new StringBuilder(apiDefinition));
            Map<String, UriTemplate> uriTemplateMap = new HashMap();
            for (APIResource apiResource : apiResourceList) {
                uriTemplateMap.put(apiResource.getUriTemplate().getTemplateId(), apiResource.getUriTemplate());
            }
            apiBuilder.uriTemplates(uriTemplateMap);
            apiBuilder.id(UUID.randomUUID().toString());
            return apiBuilder;
        }
    }

    @Override
    public void setDefaultSwaggerDefinition(API.APIBuilder apiBuilder) {
        Map<String, UriTemplate> uriTemplateMap = new HashMap<>();
        UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder();
        uriTemplateBuilder.uriTemplate("/*");
        for (String httpVerb : APIMgtConstants.SUPPORTED_HTTP_VERBS.split(",")) {
            if (!"OPTIONS".equals(httpVerb)) {
                uriTemplateBuilder.httpVerb(httpVerb);
                uriTemplateBuilder.templateId(APIUtils.generateOperationIdFromPath(uriTemplateBuilder.getUriTemplate
                        (), httpVerb));
                uriTemplateMap.put(uriTemplateBuilder.getTemplateId(), uriTemplateBuilder.build());
            }
        }
        apiBuilder.uriTemplates(uriTemplateMap);
        String swagger = generateSwaggerFromResources(apiBuilder);
        apiBuilder.apiDefinition(swagger);
    }

    public static List<Parameter> getParameters(String uriTemplate) {
        List<Parameter> parameters = new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(uriTemplate, "/");
        while (stringTokenizer.hasMoreElements()) {
            String part1 = stringTokenizer.nextToken();
            if (part1.contains("{")) {
                String pathParam = part1.replace("{", "").replace("}", "");
                PathParameter parameter = new PathParameter();
                parameter.setName(pathParam);
                parameter.setType("string");
                parameters.add(parameter);
            }
        }
        return parameters;
    }

    private Response getDefaultResponse() {
        Response response = new Response();
        response.setDescription("OK");
        return response;
    }

    private BodyParameter getDefaultBodyParameter() {
        BodyParameter bodyParameter = new BodyParameter();
        bodyParameter.setName("Payload");
        bodyParameter.setDescription("Request Body");
        bodyParameter.setRequired(false);
        Model model = new ModelImpl();
        Map<String, Property> properties = new HashMap<>();
        Property property = new StringProperty();
        properties.put("payload", property);
        model.setProperties(properties);
        bodyParameter.setSchema(model);
        return bodyParameter;
    }
}
