/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.keymgt.model;

import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscription;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscriber;
import org.wso2.carbon.apimgt.keymgt.model.entity.SubscriptionPolicy;
import org.wso2.carbon.apimgt.keymgt.model.exception.DataLoadingException;

import java.util.List;

/**
 * This interface abstracts Data Loading operations. Interface will be consumed by
 * {@link SubscriptionDataStore} while populating in memory storage. The entries can be
 * fetched directly by the Database or by calling a service.
 */
public interface SubscriptionDataLoader {

    /**
     * Loads all Subscriptions from underlying Storage.
     *
     * @return A list of all {@link Subscription} objects at the time of calling.
     * @throws DataLoadingException
     */
    public List<Subscriber> loadAllSubscribers(int tenantId) throws DataLoadingException;

    /**
     * Loads all subscribers from underlying Storage.
     *
     * @return A list of all {@link Subscription} objects at the time of calling.
     * @throws DataLoadingException
     */
    public List<Subscription> loadAllSubscriptions(int tenantId) throws DataLoadingException;

    /**
     * Load all Applications from the Database belonging to all Tenants
     *
     * @return A list of all {@link Application}s.
     * @throws DataLoadingException
     */
    public List<Application> loadAllApplications(int tenantId) throws DataLoadingException;

    /**
     * Load all Key Mappings (Mapping between the Consumer Key and Application) from the Database
     * owned by all tenants
     *
     * @return A list of {@link ApplicationKeyMapping}s
     * @throws DataLoadingException
     */
    public List<ApplicationKeyMapping> loadAllKeyMappings(int tenantId) throws DataLoadingException;

    /**
     * Load all {@link API} objects owned by all Tenants.
     *
     * @return A list of {@link API}
     * @throws DataLoadingException
     */
    public List<API> loadAllApis(int tenantId) throws DataLoadingException;

    /**
     * Load All Subscription Throttling Policies.
     *
     * @return A list of Subscription Throttling Policies.
     * @throws DataLoadingException
     */
    public List<SubscriptionPolicy> loadAllSubscriptionPolicies(int tenantId) throws DataLoadingException;

    /**
     * Loads All Application Throttling Policies.
     *
     * @return A list of Api Throttling Policies.
     * @throws DataLoadingException
     */
    public List<ApplicationPolicy> loadAllAppPolicies(int tenantId) throws DataLoadingException;

    /**
     * Retrieve a Subscriber from db.
     *
     * @return A {@link Subscriber}.
     * @throws DataLoadingException
     */
    public Subscriber getSubscriberById(int subscriberId) throws DataLoadingException;

    /**
     * Retrieve Subscription from db.
     *
     * @return A {@link Subscription}.
     * @throws DataLoadingException
     */
    public Subscription getSubscriptionById(int subscriptionId) throws DataLoadingException;

    /**
     * Retrieve Application from db.
     *
     * @return An {@link Application}s.
     * @throws DataLoadingException
     */
    public Application getApplicationById(int appId) throws DataLoadingException;

    /**
     * Retrieve Key Mapping (Mapping between the Consumer Key and Application) from the Database
     *
     * @return A list of {@link ApplicationKeyMapping}s
     * @throws DataLoadingException
     */
    public ApplicationKeyMapping getKeyMapping(int applicationId, String keyType) throws DataLoadingException;

    /**
     * Retrieve {@link API} object.
     *
     * @return An {@link API}
     * @throws DataLoadingException
     */
    public API getApiById(int apiId) throws DataLoadingException;

    /**
     * Retrieve Subscription Throttling Policy.
     *
     * @return A {@link SubscriptionPolicy}.
     * @throws DataLoadingException
     */
    public SubscriptionPolicy getSubscriptionPolicyById(int policyId) throws DataLoadingException;

    /**
     * Retrieve Application Throttling Policy.
     *
     * @return A {@link ApplicationPolicy}.
     * @throws DataLoadingException
     */
    public ApplicationPolicy getApplicationPolicy(int policyId) throws DataLoadingException;

}
