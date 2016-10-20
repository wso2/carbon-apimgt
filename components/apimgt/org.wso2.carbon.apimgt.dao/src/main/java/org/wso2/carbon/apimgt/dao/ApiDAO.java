/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.dao;

import org.wso2.carbon.apimgt.models.API;
import org.wso2.carbon.apimgt.models.APIIdentifier;

public interface ApiDAO {
    /**
     * Retrieve a given instance of an API
     * @param apiID The {@link APIIdentifier} that uniquely identifies an API
     * @return valid {@link API} object or null
     * @throws APIManagementDAOException
     *
     */
    API getAPI(APIIdentifier apiID) throws APIManagementDAOException;

    /**
     * Add a new instance of an API
     * @param api The {@link API} object to be added
     * @return true if addition is successful else false
     * @throws APIManagementDAOException
     *
     */
    boolean addAPI(API api) throws APIManagementDAOException;

    /**
     * Update an existing API
     * @param apiID The {@link APIIdentifier} of the API that needs to be updated
     * @param updatedAPI The updated {@link API} object that will replace the existing API
     * @return true if update is successful else false
     * @throws APIManagementDAOException
     *
     */
    boolean updateAPI(APIIdentifier apiID, API updatedAPI) throws APIManagementDAOException;

    /**
     * Remove an existing API
     * @param apiID The {@link APIIdentifier} of the API that needs to be deleted
     * @return true if update is successful else false
     * @throws APIManagementDAOException
     *
     */
    boolean deleteAPI(APIIdentifier apiID) throws APIManagementDAOException;
}
