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

package org.wso2.carbon.apimgt.core.dao;


import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.Application;

import javax.annotation.CheckForNull;

/**
 * Provides access to Application data layer
 */
public interface ApplicationDAO {

    /**
     * Retrieve a given instance of an Application
     *
     * @param appId   The UUID that uniquely identifies an Application
     * @return valid {@link Application} object or null
     * @throws APIMgtDAOException
     */
    @CheckForNull
    Application getApplication(String appId) throws APIMgtDAOException;

    /**
     * Retrieve a given instance of an Application
     *
     * @param appName Name of the Application
     * @param ownerId ID of the application owner.
     * @return valid {@link Application} object or null
     * @throws APIMgtDAOException
     */
    @CheckForNull
    Application getApplicationByName(String appName, String ownerId) throws APIMgtDAOException;

    /**
     * Retrieves all available Applications that belong to a user.
     *
     * @param ownerId Username of user
     * @return An array of {@link Application}
     * @throws APIMgtDAOException
     */
    Application[] getApplications(String ownerId) throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available Applications. This method supports result pagination and
     * ensures results returned are those that belong to the specified username
     *
     * @param offset   The number of results from the beginning that is to be ignored
     * @param limit    The maximum number of results to be returned after the offset
     * @param userName The username to filter results by
     * @return {@link ApplicationSummaryResults} matching results
     * @throws APIMgtDAOException
     */
    Application[] getApplicationsForUser(int offset, int limit, String userName) throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available Applications. This method supports result pagination and
     * ensures results returned are those that belong to the specified Group ID
     *
     * @param offset  The number of results from the beginning that is to be ignored
     * @param limit   The maximum number of results to be returned after the offset
     * @param groupID The Group ID to filter results by
     * @return {@link ApplicationSummaryResults} matching results
     * @throws APIMgtDAOException
     */
    Application[] getApplicationsForGroup(int offset, int limit, String groupID) throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available Applications that match the given search criteria. This method supports
     * result pagination and ensuring results returned are for Apps belonging to the specified username
     *
     * @param searchString The search string provided
     * @return An array of matching {@link Application} objects
     * @throws APIMgtDAOException
     */
    Application[] searchApplicationsForUser(String searchString, String userId) throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available Applications that match the given search criteria. This method supports
     * result pagination and ensuring results returned are for Apps belonging to the specified Group ID
     *
     * @param searchString The search string provided
     * @param groupID      The Group ID to filter results by
     * @return An array of matching {@link Application} objects
     * @throws APIMgtDAOException
     */
    Application[] searchApplicationsForGroup(String searchString, String groupID) throws APIMgtDAOException;

    /**
     * Add a new instance of an Application
     *
     * @param application The {@link Application} object to be added
     * @throws APIMgtDAOException
     */
    void addApplication(Application application) throws APIMgtDAOException;

    /**
     * Update an existing Application
     *
     * @param appID      The UUID of the Application that needs to be updated
     * @param updatedApp Substitute {@link Application} object that will replace the existing Application
     * @throws APIMgtDAOException
     */
    void updateApplication(String appID, Application updatedApp) throws APIMgtDAOException;

    /**
     * Remove an existing Application
     *
     * @param appID The UUID of the Application that needs to be deleted
     * @throws APIMgtDAOException
     */
    void deleteApplication(String appID) throws APIMgtDAOException;

    /**
     * Check whether given application name is already available in the system
     *
     * @param appName application name
     * @return true if application name is already available
     * @throws APIMgtDAOException if failed to get applications for given subscriber
     */
    boolean isApplicationNameExists(String appName) throws APIMgtDAOException;

}
