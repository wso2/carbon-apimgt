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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.models.Contact;
import io.swagger.models.HttpMethod;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.RefPath;
import io.swagger.models.RefResponse;
import io.swagger.models.Response;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.registry.api.Registry;

import java.util.ArrayList;
import java.util.HashMap;
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
            addOrUpdatePathToSwagger(swagger, uriTemplate);
        }

        return getSwaggerJsonString(swagger);
    }

    @Override
    public String generateAPIDefinition(API api, String apiDefinition) throws APIManagementException {
        SwaggerParser parser = new SwaggerParser();
        Swagger swaggerObj = parser.parse(apiDefinition);

        //Generates below model using the API's URI template
        // path -> [verb1 -> template1, verb2 -> template2, ..]
        Map<String, Map<String, URITemplate>> uriTemplateMap = getURITemplateMap(api);

        for (Map.Entry<String, Path> pathEntry : swaggerObj.getPaths().entrySet()) {
            String pathName = pathEntry.getKey();
            Path path = pathEntry.getValue();
            Map<String, URITemplate> uriTemplatesForPath = uriTemplateMap.get(pathName);
            if (uriTemplatesForPath == null) {
                //remove paths that are not in URI Templates
                swaggerObj.getPaths().remove(pathName);
            } else {
                //If path is available in the URI template, then check for operations(verbs) 
                for (Map.Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
                    HttpMethod httpMethod = operationEntry.getKey();
                    Operation operation = operationEntry.getValue();
                    URITemplate template = uriTemplatesForPath.get(httpMethod.toString().toUpperCase());
                    if (template == null) {
                        // if particular operation is not available in URI templates, then remove it from swagger
                        path.set(httpMethod.toString().toLowerCase(), null);
                    } else {
                        // if operation is available in URI templates, update swagger operation 
                        // with auth type, scope etc
                        updateOperationManagedInfo(template, operation);
                    }
                }

                // if there are any verbs (operations) not defined in swagger then add them
                for (Map.Entry<String, URITemplate> uriTemplatesForPathEntry : uriTemplatesForPath.entrySet()) {
                    String verb = uriTemplatesForPathEntry.getKey();
                    URITemplate uriTemplate = uriTemplatesForPathEntry.getValue();
                    HttpMethod method = HttpMethod.valueOf(verb.toUpperCase());
                    Operation operation = path.getOperationMap().get(method);
                    if (operation == null) {
                        operation = createOperation(uriTemplate);
                        path.set(uriTemplate.getHTTPVerb().toLowerCase(), operation);
                    }
                }
            }
        }

        // add to swagger if there are any new templates 
        for (Map.Entry<String, Map<String, URITemplate>> uriTemplateMapEntry : uriTemplateMap.entrySet()) {
            String path = uriTemplateMapEntry.getKey();
            Map<String, URITemplate> verbMap = uriTemplateMapEntry.getValue();
            if (swaggerObj.getPath(path) == null) {
                for (Map.Entry<String, URITemplate> verbMapEntry : verbMap.entrySet()) {
                    URITemplate uriTemplate = verbMapEntry.getValue();
                    addOrUpdatePathToSwagger(swaggerObj, uriTemplate);
                }
            }
        }

        return getSwaggerJsonString(swaggerObj);
    }

    @Override
    public Map<String, String> getAPIOpenAPIDefinitionTimeStamps(APIIdentifier apiIdentifier,
            Registry registry) throws APIManagementException {
        return null;
    }

    @Override
    public String validateAPIDefinition(String apiDefinition) throws APIManagementException {

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
        Scope[] templateScopes = new Scope[scopes.size()];
        int idx = 0;
        for (String scope : scopes) {
            Scope scopeObj = new Scope();
            scopeObj.setKey(scope);
            scopeObj.setName(scope);
            templateScopes[idx] = scopeObj;
            idx++;
        }
        template.setScopes(templateScopes);
        return template;
    }

    /**
     * Add a new path based on the provided URI template to swagger if it does not exists. If it exists,
     * adds the respective operation to the existing path
     *
     * @param swagger swagger object
     * @param uriTemplate URI template
     */
    private void addOrUpdatePathToSwagger(Swagger swagger, URITemplate uriTemplate) {
        Path path;
        if (swagger.getPath(uriTemplate.getUriTemplate()) != null) {
            path = swagger.getPath(uriTemplate.getUriTemplate());
        } else {
            path = new Path();
        }

        Operation operation = createOperation(uriTemplate);
        path.set(uriTemplate.getHTTPVerb().toLowerCase(), operation);

        swagger.path(uriTemplate.getUriTemplate(), path);
    }

    /**
     * Creates a new operation object using the URI template object
     *
     * @param uriTemplate URI template
     * @return a new operation object using the URI template object
     */
    private Operation createOperation(URITemplate uriTemplate) {
        Operation operation = new Operation();
        List<String> pathParams = getPathParamNames(uriTemplate.getUriTemplate());
        for (String pathParam : pathParams) {
            PathParameter pathParameter = new PathParameter();
            pathParameter.setName(pathParam);
            pathParameter.setType("string");
            operation.addParameter(pathParameter);
        }

        updateOperationManagedInfo(uriTemplate, operation);

        Response response = new Response();
        response.setDescription("OK");
        operation.addResponse(APIConstants.SWAGGER_RESPONSE_200, response);
        return operation;
    }

    /**
     *  Updates managed info of a provided operation such as auth type and throttling
     *
     * @param uriTemplate URI template
     * @param operation swagger operation
     */
    private void updateOperationManagedInfo(URITemplate uriTemplate, Operation operation) {
        String authType = uriTemplate.getAuthType();
        if (APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN.equals(authType)) {
            authType = APIConstants.OASResourceAuthTypes.APPLICATION_OR_APPLICATION_USER;
        }
        if (APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN.equals(authType)) {
            authType = APIConstants.OASResourceAuthTypes.APPLICATION_USER;
        }
        if (APIConstants.AUTH_APPLICATION_LEVEL_TOKEN.equals(authType)) {
            authType = APIConstants.OASResourceAuthTypes.APPLICATION;
        }
        operation.setVendorExtension(APIConstants.SWAGGER_X_AUTH_TYPE, authType);
        operation.setVendorExtension(APIConstants.SWAGGER_X_THROTTLING_TIER, uriTemplate.getThrottlingTier());
    }

    /**
     * Creates a json string using the swagger object.
     *
     * @param swaggerObj swagger object
     * @return json string using the swagger object
     * @throws APIManagementException error while creating swagger json
     */
    private String getSwaggerJsonString(Swagger swaggerObj) throws APIManagementException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        //this is to ignore "originalRef" in schema objects
        mapper.addMixIn(RefModel.class, IgnoreOriginalRefMixin.class);
        mapper.addMixIn(RefProperty.class, IgnoreOriginalRefMixin.class);
        mapper.addMixIn(RefPath.class, IgnoreOriginalRefMixin.class);
        mapper.addMixIn(RefParameter.class, IgnoreOriginalRefMixin.class);
        mapper.addMixIn(RefResponse.class, IgnoreOriginalRefMixin.class);

        //this is to ignore "responseSchema" in response schema objects
        mapper.addMixIn(Response.class, ResponseSchemaMixin.class);
        try {
            return new String(mapper.writeValueAsBytes(swaggerObj));
        } catch (JsonProcessingException e) {
            throw new APIManagementException("Error while generating Swagger json from model", e);
        }
    }

    /**
     * Used to ignore "originalRef" objects when generating the swagger
     */
    private abstract class IgnoreOriginalRefMixin {
        public IgnoreOriginalRefMixin() {
        }

        @JsonIgnore
        public abstract String getOriginalRef();
    }

    /**
     * Used to ignore "responseSchema" objects when generating the swagger
     */
    private abstract class ResponseSchemaMixin {
        public ResponseSchemaMixin() {
        }

        @JsonIgnore
        public abstract Property getSchema();

        @JsonIgnore
        public abstract void setSchema(Property var1);

        @JsonGetter("schema")
        public abstract Model getResponseSchema();

        @JsonSetter("schema")
        public abstract void setResponseSchema(Model var1);
    }
}
