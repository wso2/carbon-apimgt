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
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;

import java.util.List;
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
     * @throws APIMgtDAOException   If failed to get application.
     */
    @CheckForNull
    Application getApplication(String appId) throws APIMgtDAOException;

    /**
     * Retrieve a given instance of an Application
     *
     * @param appName Name of the Application
     * @param ownerId ID of the application owner.
     * @return valid {@link Application} object or null
     * @throws APIMgtDAOException   If failed to get application.
     */
    @CheckForNull
    Application getApplicationByName(String appName, String ownerId) throws APIMgtDAOException;

    /**
     * Retrieves all available Applications that belong to a user.
     *
     * @param ownerId Username of user
     * @return A list of {@link Application}
     * @throws APIMgtDAOException   If failed to get applications.
     */
    List<Application> getApplications(String ownerId) throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available Applications. This method supports result pagination and
     * ensures results returned are those that belong to the specified username
     *
     * @param offset   The number of results from the beginning that is to be ignored
     * @param limit    The maximum number of results to be returned after the offset
     * @param userName The username to filter results by
     * @return {@code Application[]} matching results
     * @throws APIMgtDAOException   If failed to get applications.
     */
    Application[] getApplicationsForUser(int offset, int limit, String userName) throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available Applications. This method supports result pagination and
     * ensures results returned are those that belong to the specified Group ID
     *
     * @param offset  The number of results from the beginning that is to be ignored
     * @param limit   The maximum number of results to be returned after the offset
     * @param groupID The Group ID to filter results by
     * @return {@code Application[]} matching results
     * @throws APIMgtDAOException   If failed to get applications.
     */
    Application[] getApplicationsForGroup(int offset, int limit, String groupID) throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available Applications that match the given search criteria. This method supports
     * result pagination and ensuring results returned are for Apps belonging to the specified username
     *
     * @param searchString The search string provided
     * @param userId    Id of the user.
     * @return An array of matching {@link Application} objects
     * @throws APIMgtDAOException   If failed to get applications.
     */
    Application[] searchApplicationsForUser(String searchString, String userId) throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available Applications that match the given search criteria. This method supports
     * result pagination and ensuring results returned are for Apps belonging to the specified Group ID
     *
     * @param searchString The search string provided
     * @param groupID      The Group ID to filter results by
     * @return An array of matching {@link Application} objects
     * @throws APIMgtDAOException   If failed to get applications.
     */
    Application[] searchApplicationsForGroup(String searchString, String groupID) throws APIMgtDAOException;

    /**
     * Add a new instance of an Application
     *
     * @param application The {@link Application} object to be added
     * @throws APIMgtDAOException   If failed to add application.
     */
    void addApplication(Application application) throws APIMgtDAOException;

    /**
     * Update an existing Application
     *
     * @param appID      The UUID of the Application that needs to be updated
     * @param updatedApp Substitute {@link Application} object that will replace the existing Application
     * @throws APIMgtDAOException   If failed to update application.
     */
    void updateApplication(String appID, Application updatedApp) throws APIMgtDAOException;

    /**
     * Remove an existing Application
     *
     * @param appID The UUID of the Application that needs to be deleted
     * @throws APIMgtDAOException If failed to delete application.
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

    /**
     * Add application key related information
     *
     * @param appId     UUID of the application
     * @param oAuthAppDetails   Oauth application detail object.
     * @throws APIMgtDAOException   If failed to add application keys.
     */
    void addApplicationKeys(String appId, OAuthApplicationInfo oAuthAppDetails) throws APIMgtDAOException;
    
    /**
     * Update the state of an existing Application
     *
     * @param appID      The UUID of the Application that needs to be updated
     * @param state      State of the application
     * @throws APIMgtDAOException
     */
    void updateApplicationState(String appID, String state) throws APIMgtDAOException;


    /**
     * Retrieves the last updated time of the application
     *
     * @param applicationId UUID of the application
     * @return  Last updated time of the resource
     * @throws APIMgtDAOException if DB level exception occurred
     */
    String getLastUpdatedTimeOfApplication(String applicationId) throws APIMgtDAOException;
}
