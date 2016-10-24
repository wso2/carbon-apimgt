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

import org.wso2.carbon.apimgt.models.Application;
import org.wso2.carbon.apimgt.models.ApplicationSummaryResults;

import javax.annotation.CheckForNull;

/**
 * Provides access to Application data layer
 */
public interface AplicationDAO {
    /**
     * Retrieve a given instance of an Application
     * @param appID The UUID that uniquely identifies an Application
     * @return valid {@link Application} object or null
     * @throws APIManagementDAOException
     *
     */
    @CheckForNull Application getApplication(String appID) throws APIManagementDAOException;

    /**
     * Retrieves summary data of all available Applications. This method supports result pagination
     * @param offset The number of results from the beginning that is to be ignored
     * @param limit The maximum number of results to be returned after the offset
     * @return {@link ApplicationSummaryResults} matching results
     * @throws APIManagementDAOException
     *
     */
    ApplicationSummaryResults getApplications(int offset, int limit) throws APIManagementDAOException;
}
