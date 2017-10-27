/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;

import java.util.Set;

/**
 * Responsible for managing the application scope cache.
 */
public interface ApplicationScopeCacheManager {

    /**
     * To add a new value to the cache.
     *
     * @param applicationUUID UUID of the application.
     * @param userName        Name of the user.
     * @param scopeSet        Set of scopes.
     * @param isFiltered      to indicate whether the scopes set passed is filtered by user roles, or all the scopes
     *                        that are relevant to particular application.
     */
    void addToCache(String applicationUUID, String userName, Set<Scope> scopeSet, boolean isFiltered);

    /**
     * To get scope list from the cache.
     *
     * @param applicationUUID UUID of the application.
     * @param userName        Name of the subscriber, who is requesting the scope
     * @param isFiltered      to indicate whether the scopes should be filtered based on the roles.
     * @return Scope list relevant to application.
     */
    Set<Scope> getValueFromCache(String applicationUUID, String userName, boolean isFiltered);

    /**
     * To notify after deleting an application, to remove the cache entries regarding that application.
     *
     * @param applicationUUID UUID of the application.
     */
    void notifyOnApplicationDelete(String applicationUUID);

    /**
     * To notify regarding an application update, so there will be track on which cache entries are out-dated.
     *
     * @param applicationUUID UUID of the application.
     */
    void notifyUpdateOnCache(String applicationUUID);

    /**
     * To notify regarding an API update, if that API, has been subscribed to any of the applications, those cache
     * entries need to be updated.
     *
     * @param apiIdentifier API Identifier, that has been updated.
     * @throws APIManagementException API Management Exception.
     */
    void notifyUpdateOnApi(APIIdentifier apiIdentifier) throws APIManagementException;
}
