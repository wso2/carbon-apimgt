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
package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIResource;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.Scope;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.ServiceMethodInfo;

import java.util.List;
import java.util.Map;

/**
 * APIDefinition is responsible for providing uri templates, scopes and
 * save the api definition according to the permission and visibility
 */

@SuppressWarnings("unused")
public interface APIDefinition {

    /**
     * This method extracts the API resource related data which includes URI templates from the Swagger API definition
     *
     * @param resourceConfigsJSON resource json
     * @return SwaggerAPIResourceData
     * @throws APIManagementException If error occurs while parsing swagger resources.
     */
    List<APIResource> parseSwaggerAPIResources(StringBuilder resourceConfigsJSON) throws APIManagementException;

    /**
     * This method extracts the scopes from the API definition
     *
     * @param resourceConfigsJSON resource json
     * @return scopes   Map of the scopes.
     * @throws APIManagementException If error occurs while parsing swagger resources.
     */
    Map<String, Scope> getScopes(String resourceConfigsJSON) throws APIManagementException;

    /**
     * Get Scopes Extracted from the API definition
     *
     * @param resourceConfigJSON resource json
     * @return Map of the scopes
     * @throws APIManagementException If error occurs while parsing swagger resources.
     */
    Map<String, String> getScopesFromSecurityDefinition(String resourceConfigJSON) throws APIManagementException;

    /**
     * This method extracts the scope from the API definition matching to a resource path
     *
     * @param resourceConfigsJSON resource json
     * @return request   HttpRequest being processed.
     * @throws APIManagementException If error occurs while parsing swagger resources.
     */
    String getScopeOfResourcePath(String resourceConfigsJSON, Request request, ServiceMethodInfo serviceMethodInfo)
            throws APIManagementException;

    /**
     * generate the swagger from uri templates.
     *
     * @param api API object
     * @return generated swagger as a string.
     */
    String generateSwaggerFromResources(API.APIBuilder api);

    /**
     * generate the swagger from uri templates.
     *
     * @param api CompositeAPI.Builder object
     * @return generated swagger as a string.
     */
    String generateSwaggerFromResources(CompositeAPI.Builder api);

    /**
     * return API Object
     *
     * @param apiDefinition API definition as a string
     * @param provider      Provider of the API
     * @return API object.
     * @throws APIManagementException If error occurs while generate swagger from resources.
     */
    API.APIBuilder generateApiFromSwaggerResource(String provider, String apiDefinition) throws APIManagementException;

    /**
     * return CompositeAPI Object
     *
     * @param apiDefinition API definition as a string
     * @param provider      Provider of the API
     * @return CompositeAPI.Builder object.
     * @throws APIManagementException If error occurs while generate swagger from resources.
     */
    CompositeAPI.Builder generateCompositeApiFromSwaggerResource(String provider, String apiDefinition)
            throws APIManagementException;

    /**
     * This method used to remove scope from swagger
     *
     * @param resourceConfigJSON resource json
     * @param name               scope to Remove
     */
    String removeScopeFromSwaggerDefinition(String resourceConfigJSON, String name);

    /**
     * This method used to update the scope of swagger definition
     *
     * @param resourceConfigJSON resource json
     * @param scope              Scope to be update
     * @return updated swaggerDefinition
     */
    String updateScopesOnSwaggerDefinition(String resourceConfigJSON, Scope scope);

    /**
     * This method used to add scope into swagger definition
     *
     * @param resourceConfigJSON resource json
     * @param scope              Scope to be add
     * @return updated swagger definition with scope
     */
    String addScopeToSwaggerDefinition(String resourceConfigJSON, Scope scope);


    /**
     * Get Scopes Extracted from the API definition for Product apis
     *
     * @param resourceConfigJSON resource json
     * @return Map of the scopes
     * @throws APIManagementException If error occurs while parsing swagger resources.
     */
    Map<String, Scope> getScopesFromSecurityDefinitionForWebApps(String resourceConfigJSON) throws
            APIManagementException;

    /**
     * get scopes assigned Globally
     *
     * @param resourceConfigJson swagger
     * @return set of scopes
     * @throws APIManagementException if Error occurred while passing swagger
     */

    List<String> getGlobalAssignedScopes(String resourceConfigJson) throws APIManagementException;


    /**
     * Used to generate merged swagger definition
     *
     * @param resourceConfigJson swagger definition
     * @param api                api model
     * @return merged swagger
     */
    String generateMergedResourceDefinition(String resourceConfigJson, API api);
}
