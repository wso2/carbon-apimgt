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
import org.wso2.carbon.apimgt.core.models.APIResource;
import org.wso2.carbon.apimgt.core.models.Scope;

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
     * @return SwaggerAPIResourceData
     */
    List<APIResource> parseSwaggerAPIResources(StringBuilder resourceConfigsJSON) throws APIManagementException;

    /**
     * This method extracts the scopes from the API definition
     *
     * @param resourceConfigsJSON resource json
     * @return scopes
     */
    Map<String, Scope> getScopes(String resourceConfigsJSON) throws APIManagementException;

}
