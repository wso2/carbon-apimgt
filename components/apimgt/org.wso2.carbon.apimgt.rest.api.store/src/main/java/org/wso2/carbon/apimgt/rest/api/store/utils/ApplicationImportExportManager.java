/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.store.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.Application;

import static org.wso2.carbon.apimgt.core.dao.impl.DAOFactory.getApplicationDAO;

/**
 * Manager class for Applications Import and Export handling
 */
public class ApplicationImportExportManager {

    private static final Logger log = LoggerFactory.getLogger(ApplicationImportExportManager.class);
    private APIStore apiStore;

    public ApplicationImportExportManager(APIStore apiStore) {
        this.apiStore = apiStore;
    }

    /**
     * Retrieve all the details of an Application for a given search query.
     *
     * @param query    searchQuery
     * @param username logged in user
     * @return {@link Application} instance
     * @throws APIManagementException if an error occurs while retrieving Application details
     */
    public Application getApplicationDetails(String query, String username) throws
            APIManagementException {
        Application application = null;
        if (query == null || query.isEmpty()) {
            return application;
        } else {
            application = apiStore.getApplication(query, username);
        }
        return application;
    }

    /**
     * Update details of an existing Application when imported
     *
     * @param importedApplication
     * @param username
     * @throws APIManagementException
     */
    public Application updateApplication(Application importedApplication, String username)
            throws APIManagementException {
        Application updatedApp = null;
        try {
            if (getApplicationDAO().isApplicationNameExists(importedApplication.getName())) {
                Application existingApplication = apiStore.getApplicationByName(importedApplication.getName(),
                        username);
                apiStore.updateApplication(existingApplication.getUuid(), importedApplication);
                updatedApp = apiStore.getApplication(existingApplication.getUuid(), username);
            }
            return updatedApp;
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while finding application matching the provided name";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        }
    }
}
