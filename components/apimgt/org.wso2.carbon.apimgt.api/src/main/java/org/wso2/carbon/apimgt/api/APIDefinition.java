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
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.registry.api.Registry;

import java.util.Map;
import java.util.Set;

/**
 * APIDefinition is responsible for providing uri templates, scopes and
 * save the api definition according to the permission and visibility
 */

@SuppressWarnings("unused")
public abstract class APIDefinition {

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
     * This method returns the timestamps for a given API
     * @param apiIdentifier
     * @param registry
     * @return
     * @throws APIManagementException
     */
    public abstract Map<String ,String> getAPISwaggerDefinitionTimeStamps(APIIdentifier apiIdentifier, Registry registry) throws APIManagementException;
}
