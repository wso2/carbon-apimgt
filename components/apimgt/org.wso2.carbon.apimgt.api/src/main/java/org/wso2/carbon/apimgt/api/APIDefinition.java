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
package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.registry.api.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * APIDefinition is responsible for providing uri templates, scopes and
 * save the api definition according to the permission and visibility
 */

@SuppressWarnings("unused")
public abstract class APIDefinition {


    private static final Pattern CURLY_BRACES_PATTERN = Pattern.compile("(?<=\\{)(?!\\s*\\{)[^{}]+");

    /**
     * This method extracts the URI templates from the API definition
     *
     * @return URI templates
     */
    public abstract Set<URITemplate> getURITemplates(API api, String resourceConfigsJSON) throws APIManagementException;

    /**
     * This method extracts the scopes from the API definition
     *
     * @param resourceConfigsJSON resource json
     * @return scopes
     */
    public abstract Set<Scope> getScopes(String resourceConfigsJSON) throws APIManagementException;

    /**
     * This method saves the API definition
     *
     * @param api               API to be saved
     * @param apiDefinitionJSON API definition as JSON string
     * @param registry          user registry
     */
    public abstract void saveAPIDefinition(API api, String apiDefinitionJSON, Registry registry) throws APIManagementException;

    /**
     * This method saves the APIProduct definition
     *
     * @param apiProduct               API to be saved
     * @param apiDefinitionJSON API definition as JSON string
     * @param registry          user registry
     */
    public abstract void saveAPIDefinition(APIProduct apiProduct, String apiDefinitionJSON, Registry registry)
            throws APIManagementException;

    /**
     * This method reads the API definition from registry
     *
     * @param apiIdentifier api identifier
     * @param registry      user registry
     * @return API definition
     */
    public abstract String getAPIDefinition(APIIdentifier apiIdentifier, Registry registry) throws APIManagementException;

    /**
     * This method generates API definition to the given api
     *
     * @param api api
     * @return API definition in string format
     * @throws APIManagementException
     */
    public abstract String generateAPIDefinition(API api) throws APIManagementException;

    /**
     * This method generates API definition to the given apiProduct
     *
     * @param apiProduct apiProduct
     * @return API definition in string format
     * @throws APIManagementException
     */
    public abstract String generateAPIDefinition(APIProduct apiProduct) throws APIManagementException;

    /**
     * This method generates API definition using the given api's URI templates and the swagger.
     * It will alter the provided swagger definition based on the URI templates. For example: if there is a new
     * URI template which is not included in the swagger, it will be added to the swagger as a basic resource. Any
     * additional resources inside the swagger will be removed from the swagger. Changes to scopes, throtting policies,
     * on the resource will be updated on the swagger
     *
     * @param api api
     * @param swagger swagger definition
     * @param syncOperations whether to sync operations between API and swagger. If true, the operations of the swagger
     *                       will be synced from the API's operations. Additional operations of the swagger will be
     *                       removed and new operations of API will be added. If false, all the operations will be
     *                       taken from swagger.
     * @return API definition in string format
     * @throws APIManagementException if error occurred when generating API Definition
     */
    public abstract String generateAPIDefinition(API api, String swagger, boolean syncOperations)
            throws APIManagementException;

    /**
     * This method returns the timestamps for a given API
     * @param apiIdentifier
     * @param registry
     * @return
     * @throws APIManagementException
     */
    public abstract Map<String ,String> getAPIOpenAPIDefinitionTimeStamps(APIIdentifier apiIdentifier, Registry registry) throws APIManagementException;

    /**
     * Extract and return path parameters in the given URI template
     *
     * @param uriTemplate URI Template value
     * @return path parameters in the given URI template
     */
    public List<String> getPathParamNames(String uriTemplate) {
        List<String> params = new ArrayList<>();

        Matcher bracesMatcher = CURLY_BRACES_PATTERN.matcher(uriTemplate);
        while (bracesMatcher.find()) {
            params.add(bracesMatcher.group());
        }
        return params;
    }

    /**
     * Creates a helper uri template map using provided API's URI templates.
     * Creates map in below format:
     *      /order      -> [post -> template1]
     *      /order/{id} -> [get -> template2, put -> template3, ..]
     *
     * @param api API object
     * @return a structured uri template map using provided API's URI templates
     */
    public Map<String, Map<String, URITemplate>> getURITemplateMap(API api) {
        Map<String, Map<String, URITemplate>> uriTemplateMap = new HashMap<>();
        for (URITemplate uriTemplate : api.getUriTemplates()) {
            Map<String, URITemplate> templates = uriTemplateMap.get(uriTemplate.getUriTemplate());
            if (templates == null) {
                templates = new HashMap<>();
                uriTemplateMap.put(uriTemplate.getUriTemplate(), templates);
            }
            templates.put(uriTemplate.getHTTPVerb().toUpperCase(), uriTemplate);
        }
        return uriTemplateMap;
    }

    /**
     * This method validates the given OpenAPI definition by content
     *
     * @param apiDefinition OpenAPI Definition content
     * @param returnJsonContent whether to return the converted json form of the OpenAPI definition
     * @return APIDefinitionValidationResponse object with validation information
     */
    public abstract APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition,
            boolean returnJsonContent) throws APIManagementException;

    /**
     * This method validates the given OpenAPI definition by URL
     *
     * @param url URL of the API definition
     * @param returnJsonContent whether to return the converted json form of the
     * @return APIDefinitionValidationResponse object with validation information
     */
    public abstract APIDefinitionValidationResponse validateAPIDefinitionByURL(String url, boolean returnJsonContent)
            throws APIManagementException;

}
