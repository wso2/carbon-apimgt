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

package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIKey;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.Tag;
import org.wso2.carbon.apimgt.core.models.policy.Policy;

import java.util.List;
import java.util.Map;

/**
 * This interface used to write Store specific methods.
 *
 */
public interface APIStore extends APIManager {

    /**
     * Returns a paginated list of all APIs in given Status list. If a given API has multiple APIs,
     * only the latest version will be included in this list.
     *
     * @param offset offset
     * @param limit  limit
     * @param status One or more Statuses
     * @return List<API>
     * @throws APIManagementException if failed to API set
     */
    List<API> getAllAPIsByStatus(int offset, int limit, String[] status) throws APIManagementException;

    /**
     * Returns a paginated list of all APIs which match the given search criteria.
     *
     * @param query searchType
     * @param limit limit
     * @return List<API>
     * @throws APIManagementException
     */
    List<API> searchAPIs(String query, int offset, int limit) throws APIManagementException;

    /**
     * Function to remove an Application from the API Store
     *
     * @param appId - The Application id of the Application
     * @throws APIManagementException
     */
    void deleteApplication(String appId) throws APIManagementException;

    /**
     * Adds an application
     *
     * @param application Application
     * @return uuid of the newly created application
     * @throws APIManagementException if failed to add Application
     */
    String addApplication(Application application) throws APIManagementException;

    /**
     * This will return APIM application by giving name and subscriber
     *
     * @param applicationName APIM application name
     * @param ownerId          Application owner ID.
     * @param groupId         Group id.
     * @return it will return Application.
     * @throws APIManagementException
     */
    Application getApplicationByName(String applicationName, String ownerId, String groupId)
            throws APIManagementException;

    /**
     * Returns a list of applications for a given subscriber
     *
     * @param subscriber Subscriber
     * @param groupId    the groupId to which the applications must belong.
     * @return Applications
     * @throws APIManagementException if failed to applications for given subscriber
     */

    List<Application> getApplications(String subscriber, String groupId) throws APIManagementException;

    /**
     * Updates the details of the specified user application.
     * @param uuid Uuid of the existing application
     * @param application Application object containing updated data
     * @throws APIManagementException If an error occurs while updating the application
     */
    void updateApplication(String uuid, Application application) throws APIManagementException;

    /**
     * Generates oAuth keys for an application.
     *
     * @param userId          Subsriber name.
     * @param applicationName name of the Application.
     * @param applicationId   id of the Application.
     * @param tokenType       Token type (PRODUCTION | SANDBOX)
     * @param callbackUrl     callback URL
     * @param allowedDomains  allowedDomains for token.
     * @param validityTime    validity time period.
     * @param groupingId      APIM application id.
     * @param tokenScope      Scopes for the requested tokens.
     * @throws APIManagementException if failed to applications for given subscriber
     */
    Map<String, Object> generateApplicationKeys(String userId, String applicationName, String applicationId,
            String tokenType, String callbackUrl, String[] allowedDomains, String validityTime,
            String tokenScope, String groupingId) throws APIManagementException;

    /**
     * Retrieve an application given the uuid.
     *
     * @param uuid
     * @return Application object of the given uuid
     * @throws APIManagementException
     */
    Application getApplicationByUuid(String uuid) throws APIManagementException;

    /**
     * Retrieve list of subscriptions given the application.
     *
     * @param application
     * @return List of subscriptions objects of the given application.
     * @throws APIManagementException
     */
    List<Subscription> getAPISubscriptionsByApplication(Application application) throws APIManagementException;

    /**
     * Add an api subscription.
     *
     * @param apiId
     * @param applicationId
     * @param tier
     * @return
     * @throws APIManagementException
     */
    String addApiSubscription(String apiId, String applicationId, String tier) throws APIManagementException;

    /**
     * Delete an API subscription.
     *
     * @param subscriptionId
     * @throws APIManagementException
     */
    void deleteAPISubscription(String subscriptionId) throws APIManagementException;

    /**
     * Retrieve all tags
     *
     * @return
     * @throws APIManagementException
     */
    List<Tag> getAllTags() throws APIManagementException;

    /**
     * Retrieve all policies of given tier level.
     *
     * @return
     * @throws APIManagementException
     */
    List<Policy> getPolicies(String tierLevel) throws APIManagementException;

    /**
     * Retrieve all policies of given tier level.
     *
     * @return
     * @throws APIManagementException
     */
    Policy getPolicy(String tierLevel, String tierName) throws APIManagementException;

    /**
     * Creates an OAuth2 app for a given APIM Application and generate keys.
     *
     * @param application    Application for which keys should be generated
     * @return Generated keys
     */
    APIKey generateKeysForApplication(Application application);

}
