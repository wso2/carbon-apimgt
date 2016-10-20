/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.APIManagementException;

import java.util.Map;

public interface ResourceManager {
    /**
     * This Method will talk to APIResource registration end point  of  authorization server and creates a new resource
     *
     * @param  api this is a API object which contains all details about a API.
     * @param  resourceAttributes this param will contains additional details if required.
     * @return true if sucessfully registered. false if there is a error while registering a new resource.
     * @throws APIManagementException
     */

    boolean registerNewResource(API api , Map resourceAttributes) throws APIManagementException;

    /**
     * This method will be used to retrieve registered resource by given API ID.
     *
     * @param apiId APIM api id.
     * @return It will return a Map with registered resource details.
     * @throws APIManagementException
     */
    Map getResourceByApiId(String apiId) throws APIManagementException;

    /**
     * This method is responsible for update given APIResource  by its resourceId.
     *
     * @param  api this is a API object which contains all details about a API.
     * @param  resourceAttributes this param will contains additional details if required.
     * @return TRUE|FALSE. if it is successfully updated it will return TRUE or else FALSE.
     * @throws APIManagementException
     */
    boolean updateRegisteredResource(API api , Map resourceAttributes) throws APIManagementException;

    /**
     * This method will accept API id  as a parameter  and will delete the registered resource.
     *
     * @param apiID API id.
     * @throws APIManagementException
     */
    void deleteRegisteredResourceByAPIId(String apiID) throws APIManagementException;

}
