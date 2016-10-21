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

package org.wso2.carbon.apimgt.dao.impl;

import org.wso2.carbon.apimgt.dao.APIManagementDAOException;
import org.wso2.carbon.apimgt.dao.ApiDAO;
import org.wso2.carbon.apimgt.models.API;
import org.wso2.carbon.apimgt.models.APIIdentifier;

/**
 * Default implementation of the ApiDAO interface. Uses SQL syntax that is common to H2 and MySQL DBs.
 * Hence is considered as the default due to its re-usability.
 */
public class DefaultApiDAOImpl implements ApiDAO {

    DefaultApiDAOImpl() {}

    /**
     * Retrieve a given instance of an API
     *
     * @param apiID The {@link APIIdentifier} that uniquely identifies an API
     * @return valid {@link API} object or null
     * @throws APIManagementDAOException
     */
    @Override
    public API getAPI(APIIdentifier apiID) throws APIManagementDAOException {
        return null;
    }

    /**
     * Add a new instance of an API
     *
     * @param api The {@link API} object to be added
     * @return true if addition is successful else false
     * @throws APIManagementDAOException
     */
    @Override
    public boolean addAPI(API api) throws APIManagementDAOException {
        return false;
    }

    /**
     * Update an existing API
     *
     * @param apiID      The {@link APIIdentifier} of the API that needs to be updated
     * @param updatedAPI The updated {@link API} object that will replace the existing API
     * @return true if update is successful else false
     * @throws APIManagementDAOException
     */
    @Override
    public boolean updateAPI(APIIdentifier apiID, API updatedAPI) throws APIManagementDAOException {
        return false;
    }

    /**
     * Remove an existing API
     *
     * @param apiID The {@link APIIdentifier} of the API that needs to be deleted
     * @return true if update is successful else false
     * @throws APIManagementDAOException
     */
    @Override
    public boolean deleteAPI(APIIdentifier apiID) throws APIManagementDAOException {
        return false;
    }
}
