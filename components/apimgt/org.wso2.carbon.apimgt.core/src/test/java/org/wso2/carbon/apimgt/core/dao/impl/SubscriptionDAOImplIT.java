/*
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
 */

package org.wso2.carbon.apimgt.core.dao.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.TestUtil;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.ETagUtils;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SubscriptionDAOImplIT extends DAOIntegrationTestBase {

    @Test
    public void testAddAndGetSubscription() throws Exception {

        //add new app
        Application app = TestUtil.addTestApplication();
        //add new api
        API api = TestUtil.addTestAPI();
        //add subscription
        String subscriptionTier = "Gold";
        APISubscriptionDAO apiSubscriptionDAO = DAOFactory.getAPISubscriptionDAO();
        String uuid = UUID.randomUUID().toString();
        apiSubscriptionDAO.addAPISubscription(uuid, api.getId(), app.getId(), subscriptionTier,
                APIMgtConstants.SubscriptionStatus.ACTIVE);
        //get subscription
        Subscription subscription = apiSubscriptionDAO.getAPISubscription(uuid);
        //validate
        Assert.assertNotNull(subscription);
        Assert.assertEquals(subscription.getId(), uuid);
        Assert.assertEquals(subscription.getStatus(), APIMgtConstants.SubscriptionStatus.ACTIVE);
        Assert.assertEquals(subscription.getSubscriptionTier(), subscriptionTier);
        Assert.assertEquals(subscription.getApi(), TestUtil.createSummaryAPI(api),
                TestUtil.printDiff(subscription.getApi(), TestUtil.createSummaryAPI(api)));
        Assert.assertEquals(subscription.getApplication(), TestUtil.createSummaryApplication(app),
                TestUtil.printDiff(subscription.getApplication(), TestUtil.createSummaryApplication(app)));
    }

    @Test
    public void testUpdateSubscription() throws Exception {

        //add new app
        Application app = TestUtil.addTestApplication();
        //add new api
        API api = TestUtil.addTestAPI();
        //add subscription
        String subscriptionPolicy = "Gold";
        APISubscriptionDAO apiSubscriptionDAO = DAOFactory.getAPISubscriptionDAO();
        String uuid = UUID.randomUUID().toString();
        apiSubscriptionDAO.addAPISubscription(uuid, api.getId(), app.getId(), subscriptionPolicy,
                APIMgtConstants.SubscriptionStatus.ACTIVE);
        //get subscription
        Subscription subscription = apiSubscriptionDAO.getAPISubscription(uuid);

        //validate fingerprint
        String fingerprintBeforeUpdate = ETagUtils
                .generateETag(apiSubscriptionDAO.getLastUpdatedTimeOfSubscription(uuid));
        Assert.assertNotNull(fingerprintBeforeUpdate);

        //validate tier and status
        Assert.assertNotNull(subscription);
        Assert.assertEquals(subscription.getId(), uuid);
        Assert.assertEquals(subscription.getStatus(), APIMgtConstants.SubscriptionStatus.ACTIVE);
        Assert.assertEquals(subscription.getSubscriptionTier(), subscriptionPolicy);

        //update subscription policy
        String newSubscriptionPolicy = "Silver";
        apiSubscriptionDAO.updateSubscriptionPolicy(uuid, newSubscriptionPolicy);
        //get subscription
        subscription = apiSubscriptionDAO.getAPISubscription(uuid);
        //validate
        Assert.assertNotNull(subscription);
        Assert.assertEquals(subscription.getId(), uuid);
        Assert.assertEquals(subscription.getStatus(), APIMgtConstants.SubscriptionStatus.ACTIVE);
        Assert.assertEquals(subscription.getSubscriptionTier(), newSubscriptionPolicy);
        Assert.assertEquals(subscription.getApi(), TestUtil.createSummaryAPI(api),
                TestUtil.printDiff(subscription.getApi(), TestUtil.createSummaryAPI(api)));
        Assert.assertEquals(subscription.getApplication(), TestUtil.createSummaryApplication(app),
                TestUtil.printDiff(subscription.getApplication(), TestUtil.createSummaryApplication(app)));

        //update subscription status
        APIMgtConstants.SubscriptionStatus newSubscriptionStatus = APIMgtConstants.SubscriptionStatus.PROD_ONLY_BLOCKED;
        apiSubscriptionDAO.updateSubscriptionStatus(uuid, newSubscriptionStatus);
        //get subscription
        subscription = apiSubscriptionDAO.getAPISubscription(uuid);
        String fingerprintAfterUpdate = ETagUtils
                .generateETag(apiSubscriptionDAO.getLastUpdatedTimeOfSubscription(uuid));
        //validate
        Assert.assertNotNull(subscription);
        Assert.assertNotNull(fingerprintAfterUpdate);
        Assert.assertEquals(subscription.getId(), uuid);
        Assert.assertEquals(subscription.getStatus(), newSubscriptionStatus);
        Assert.assertEquals(subscription.getSubscriptionTier(), newSubscriptionPolicy);
        Assert.assertEquals(subscription.getApi(), TestUtil.createSummaryAPI(api),
                TestUtil.printDiff(subscription.getApi(), TestUtil.createSummaryAPI(api)));
        Assert.assertEquals(subscription.getApplication(), TestUtil.createSummaryApplication(app),
                TestUtil.printDiff(subscription.getApplication(), TestUtil.createSummaryApplication(app)));
        Assert.assertNotEquals(fingerprintBeforeUpdate, fingerprintAfterUpdate);
    }

    @Test
    public void testGetSubscriptionsForValidation() throws Exception {

        //add test apis, apps and subscriptions
        ApisAndApps apisAndApps = createApisAppsAndSubscriptions();

        APISubscriptionDAO subscriptionDAO = DAOFactory.getAPISubscriptionDAO();
        ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();

        API api1 = apisAndApps.getApis().get(0);
        API api2 = apisAndApps.getApis().get(1);
        API api3 = apisAndApps.getApis().get(2);
        API api4 = apisAndApps.getApis().get(3);

        Application app1 = apisAndApps.getApps().get(0);
        Application app2 = apisAndApps.getApps().get(1);
        Application app3 = apisAndApps.getApps().get(2);
        Application app4 = apisAndApps.getApps().get(3);

        registerOAuthAppForApplication(applicationDAO, "client-key-for-app-1", "client-secret-for-app-1", app1.getId());
        registerOAuthAppForApplication(applicationDAO, "client-key-for-app-2", "client-secret-for-app-2", app2.getId());
        registerOAuthAppForApplication(applicationDAO, "client-key-for-app-3", "client-secret-for-app-3", app3.getId());
        registerOAuthAppForApplication(applicationDAO, "client-key-for-app-4", "client-secret-for-app-4", app4.getId());

        //get all subscriptions
        List<SubscriptionValidationData> subscriptions = subscriptionDAO.getAPISubscriptionsOfAPIForValidation(-1);
        //validate subscription count
        Assert.assertEquals(subscriptions.size(), 9, "There should be 9 subscriptions (only).");

        //validate subscriptions
        for (SubscriptionValidationData subscription : subscriptions) {
            Assert.assertNotNull(subscription);
            if (subscription.getApplicationName().equals(app1.getName()) &&
                    subscription.getApiName().equals(api2.getName())) {
                //app1  & api 2
                validateSubscriptionsOfApi(subscription, api2, app1);
            } else if (subscription.getApplicationName().equals(app2.getName()) &&
                    subscription.getApiName().equals(api1.getName())) {
                //app2  & api 1
                validateSubscriptionsOfApi(subscription, api1, app2);
            } else if (subscription.getApplicationName().equals(app2.getName()) &&
                    subscription.getApiName().equals(api2.getName())) {
                //app2  & api 2
                validateSubscriptionsOfApi(subscription, api2, app2);
            } else if (subscription.getApplicationName().equals(app2.getName()) &&
                    subscription.getApiName().equals(api3.getName())) {
                //app2  & api 3
                validateSubscriptionsOfApi(subscription, api3, app2);
            } else if (subscription.getApplicationName().equals(app2.getName()) &&
                    subscription.getApiName().equals(api4.getName())) {
                //app2  & api 4
                validateSubscriptionsOfApi(subscription, api4, app2);
            } else if (subscription.getApplicationName().equals(app3.getName()) &&
                    subscription.getApiName().equals(api3.getName())) {
                //app3  & api 3
                validateSubscriptionsOfApi(subscription, api3, app3);
            } else if (subscription.getApplicationName().equals(app4.getName()) &&
                    subscription.getApiName().equals(api1.getName())) {
                //app4  & api 1
                validateSubscriptionsOfApi(subscription, api1, app4);
            } else if (subscription.getApplicationName().equals(app4.getName()) &&
                    subscription.getApiName().equals(api2.getName())) {
                //app4  & api 2
                validateSubscriptionsOfApi(subscription, api2, app4);
            } else if (subscription.getApplicationName().equals(app4.getName()) &&
                    subscription.getApiName().equals(api3.getName())) {
                //app4  & api 3
                validateSubscriptionsOfApi(subscription, api3, app4);
            } else if (subscription.getApplicationName().equals(app4.getName()) &&
                    subscription.getApiName().equals(api4.getName())) {
                //app4  & api 4
                validateSubscriptionsOfApi(subscription, api4, app4);
            } else {
                Assert.fail("Invalid subscription found!!! API: " + subscription.getApiName()
                        + " Application: " + subscription.getApplicationName());
            }
        }
    }

    private void registerOAuthAppForApplication(ApplicationDAO applicationDAO, String clientKey, String clintSecret,
                                                String appId) throws APIMgtDAOException {
        OAuthApplicationInfo oAuthAppInfo = new OAuthApplicationInfo();
        oAuthAppInfo.setClientId(clientKey);
        oAuthAppInfo.setClientSecret(clintSecret);
        oAuthAppInfo.addParameter(KeyManagerConstants.APP_KEY_TYPE, "Application");
        applicationDAO.addApplicationKeys(appId, oAuthAppInfo);
    }

    @Test
    public void testGetSubscriptionForValidationByApiContextAndVersion() throws Exception {
        //add test apis, apps and subscriptions
        ApisAndApps apisAndApps = createApisAppsAndSubscriptions();

        APISubscriptionDAO subscriptionDAO = DAOFactory.getAPISubscriptionDAO();
        ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();

        API api1 = apisAndApps.getApis().get(0);
        API api2 = apisAndApps.getApis().get(1);
        API api3 = apisAndApps.getApis().get(2);
        API api4 = apisAndApps.getApis().get(3);

        Application app1 = apisAndApps.getApps().get(0);
        Application app2 = apisAndApps.getApps().get(1);
        Application app3 = apisAndApps.getApps().get(2);
        Application app4 = apisAndApps.getApps().get(3);

        registerOAuthAppForApplication(applicationDAO, "client-key-for-app-1", "client-secret-for-app-1", app1.getId());
        registerOAuthAppForApplication(applicationDAO, "client-key-for-app-2", "client-secret-for-app-2", app2.getId());
        registerOAuthAppForApplication(applicationDAO, "client-key-for-app-3", "client-secret-for-app-3", app3.getId());
        registerOAuthAppForApplication(applicationDAO, "client-key-for-app-4", "client-secret-for-app-4", app4.getId());

        //get subscriptions of api1 (app2, app4)
        List<SubscriptionValidationData> subscriptions = subscriptionDAO.getAPISubscriptionsOfAPIForValidation(
                api1.getContext(), api1.getVersion());
        //validate subscription count
        Assert.assertEquals(subscriptions.size(), 2, "There should be 2 subscriptions (only).");
        Assert.assertEquals(subscriptionDAO.getSubscriptionCountByAPI(api1.getId()), 2L,
                "There should be 2 subscriptions (only).");
        //validate subscriptions
        for (SubscriptionValidationData subscription : subscriptions) {
            Assert.assertNotNull(subscription);
            if (subscription.getApplicationName().equals(app2.getName())) {
                validateSubscriptionsOfApi(subscription, api1, app2);
            } else if (subscription.getApplicationName().equals(app4.getName())) {
                validateSubscriptionsOfApi(subscription, api1, app4);
            } else {
                Assert.fail("Invalid subscription found!!!");
            }
        }

        //get subscriptions of api2 (app1, app2, app4)
        subscriptions = subscriptionDAO.getAPISubscriptionsOfAPIForValidation(api2.getContext(), api2.getVersion());
        //validate subscription count
        Assert.assertEquals(subscriptions.size(), 3, "There should be 3 subscriptions (only).");
        Assert.assertEquals(subscriptionDAO.getSubscriptionCountByAPI(api2.getId()), 3L,
                "There should be 3 subscriptions (only).");
        //validate subscriptions
        for (SubscriptionValidationData subscription : subscriptions) {
            Assert.assertNotNull(subscription);
            if (subscription.getApplicationName().equals(app1.getName())) {
                validateSubscriptionsOfApi(subscription, api2, app1);
            } else if (subscription.getApplicationName().equals(app2.getName())) {
                validateSubscriptionsOfApi(subscription, api2, app2);
            } else if (subscription.getApplicationName().equals(app4.getName())) {
                validateSubscriptionsOfApi(subscription, api2, app4);
            } else {
                Assert.fail("Invalid subscription found!!!");
            }
        }

        //get subscriptions of api3 (app2, app3, app4)
        subscriptions = subscriptionDAO.getAPISubscriptionsOfAPIForValidation(api3.getContext(), api3.getVersion());
        //validate subscription count
        Assert.assertEquals(subscriptions.size(), 3, "There should be 3 subscription (only).");
        Assert.assertEquals(subscriptionDAO.getSubscriptionCountByAPI(api3.getId()), 3L,
                "There should be 3 subscription (only).");
        //validate subscriptions
        for (SubscriptionValidationData subscription : subscriptions) {
            Assert.assertNotNull(subscription);
            if (subscription.getApplicationName().equals(app2.getName())) {
                validateSubscriptionsOfApi(subscription, api3, app2);
            } else if (subscription.getApplicationName().equals(app3.getName())) {
                validateSubscriptionsOfApi(subscription, api3, app3);
            } else if (subscription.getApplicationName().equals(app4.getName())) {
                validateSubscriptionsOfApi(subscription, api3, app4);
            } else {
                Assert.fail("Invalid subscription found!!!");
            }
        }

        //get subscriptions of api4 (app4)
        subscriptions = subscriptionDAO.getAPISubscriptionsOfAPIForValidation(api4.getContext(), api4.getVersion());
        //validate subscription count
        Assert.assertEquals(subscriptions.size(), 1, "There should be 1 subscriptions (only).");
        Assert.assertEquals(subscriptionDAO.getSubscriptionCountByAPI(api4.getId()), 1L,
                "There should be 1 subscriptions (only).");
        //validate subscriptions
        for (SubscriptionValidationData subscription : subscriptions) {
            Assert.assertNotNull(subscription);
            if (subscription.getApplicationName().equals(app4.getName())) {
                validateSubscriptionsOfApi(subscription, api4, app4);
            } else {
                Assert.fail("Invalid subscription found!!!");
            }
        }
    }

    private void validateSubscriptionsOfApi(SubscriptionValidationData validationData, API api, Application app) {
        Assert.assertEquals(validationData.getApiContext(), api.getContext());
        Assert.assertEquals(validationData.getApiProvider(), api.getProvider());
        Assert.assertEquals(validationData.getApiVersion(), api.getVersion());
        Assert.assertEquals(validationData.getApplicationOwner(), app.getCreatedUser());
        Assert.assertEquals(validationData.getSubscriptionPolicy(), "Gold");
    }

    @Test
    public void testGetSubscriptionByApiId() throws Exception {
        //add test apis, apps and subscriptions
        ApisAndApps apisAndApps = createApisAppsAndSubscriptions();

        APISubscriptionDAO subscriptionDAO = DAOFactory.getAPISubscriptionDAO();

        API api1 = apisAndApps.getApis().get(0);
        API api2 = apisAndApps.getApis().get(1);
        API api3 = apisAndApps.getApis().get(2);
        API api4 = apisAndApps.getApis().get(3);

        Application app1 = apisAndApps.getApps().get(0);
        Application app2 = apisAndApps.getApps().get(1);
        Application app3 = apisAndApps.getApps().get(2);
        Application app4 = apisAndApps.getApps().get(3);

        //get subscriptions of api1 (app2, app4)
        List<Subscription> subscriptions = subscriptionDAO.getAPISubscriptionsByAPI(api1.getId());
        //validate subscription count
        Assert.assertEquals(subscriptions.size(), 2, "There should be 2 subscriptions (only).");
        Assert.assertEquals(subscriptionDAO.getSubscriptionCountByAPI(api1.getId()), 2L,
                "There should be 2 subscriptions (only).");
        //validate subscriptions
        for (Subscription subscription : subscriptions) {
            Assert.assertNotNull(subscription);
            if (subscription.getApplication().getId().equals(app2.getId())) {
                Assert.assertEquals(subscription.getApplication(), TestUtil.createSummaryApplication(app2),
                        TestUtil.printDiff(subscription.getApplication(), TestUtil.createSummaryApplication(app2)));
            } else if (subscription.getApplication().getId().equals(app4.getId())) {
                Assert.assertEquals(subscription.getApplication(), TestUtil.createSummaryApplication(app4),
                        TestUtil.printDiff(subscription.getApplication(), TestUtil.createSummaryApplication(app4)));
            } else {
                Assert.fail("Invalid subscription found!!!");
            }
        }

        //get subscriptions of api2 (app1, app2, app4)
        subscriptions = subscriptionDAO.getAPISubscriptionsByAPI(api2.getId());
        //validate subscription count
        Assert.assertEquals(subscriptions.size(), 3, "There should be 3 subscriptions (only).");
        Assert.assertEquals(subscriptionDAO.getSubscriptionCountByAPI(api2.getId()), 3L,
                "There should be 3 subscriptions (only).");
        //validate subscriptions
        for (Subscription subscription : subscriptions) {
            Assert.assertNotNull(subscription);
            if (subscription.getApplication().getId().equals(app1.getId())) {
                Assert.assertEquals(subscription.getApplication(), TestUtil.createSummaryApplication(app1),
                        TestUtil.printDiff(subscription.getApplication(), TestUtil.createSummaryApplication(app1)));
            } else if (subscription.getApplication().getId().equals(app2.getId())) {
                Assert.assertEquals(subscription.getApplication(), TestUtil.createSummaryApplication(app2),
                        TestUtil.printDiff(subscription.getApplication(), TestUtil.createSummaryApplication(app2)));
            } else if (subscription.getApplication().getId().equals(app4.getId())) {
                Assert.assertEquals(subscription.getApplication(), TestUtil.createSummaryApplication(app4),
                        TestUtil.printDiff(subscription.getApplication(), TestUtil.createSummaryApplication(app4)));
            } else {
                Assert.fail("Invalid subscription found!!!");
            }
        }

        //get subscriptions of api3 (app2, app3, app4)
        subscriptions = subscriptionDAO.getAPISubscriptionsByAPI(api3.getId());
        //validate subscription count
        Assert.assertEquals(subscriptions.size(), 3, "There should be 3 subscription (only).");
        Assert.assertEquals(subscriptionDAO.getSubscriptionCountByAPI(api3.getId()), 3L,
                "There should be 3 subscription (only).");
        //validate subscriptions
        for (Subscription subscription : subscriptions) {
            Assert.assertNotNull(subscription);
            if (subscription.getApplication().getId().equals(app2.getId())) {
                Assert.assertEquals(subscription.getApplication(), TestUtil.createSummaryApplication(app2),
                        TestUtil.printDiff(subscription.getApplication(), TestUtil.createSummaryApplication(app2)));
            } else if (subscription.getApplication().getId().equals(app3.getId())) {
                Assert.assertEquals(subscription.getApplication(), TestUtil.createSummaryApplication(app3),
                        TestUtil.printDiff(subscription.getApplication(), TestUtil.createSummaryApplication(app3)));
            } else if (subscription.getApplication().getId().equals(app4.getId())) {
                Assert.assertEquals(subscription.getApplication(), TestUtil.createSummaryApplication(app4),
                        TestUtil.printDiff(subscription.getApplication(), TestUtil.createSummaryApplication(app4)));
            } else {
                Assert.fail("Invalid subscription found!!!");
            }
        }

        //get subscriptions of api4 (app4)
        subscriptions = subscriptionDAO.getAPISubscriptionsByAPI(api4.getId());
        //validate subscription count
        Assert.assertEquals(subscriptions.size(), 1, "There should be 1 subscriptions (only).");
        Assert.assertEquals(subscriptionDAO.getSubscriptionCountByAPI(api4.getId()), 1L,
                "There should be 1 subscriptions (only).");
        //validate subscriptions
        for (Subscription subscription : subscriptions) {
            Assert.assertNotNull(subscription);
            if (subscription.getApplication().getId().equals(app4.getId())) {
                Assert.assertEquals(subscription.getApplication(), TestUtil.createSummaryApplication(app4),
                        TestUtil.printDiff(subscription.getApplication(), TestUtil.createSummaryApplication(app4)));
            } else {
                Assert.fail("Invalid subscription found!!!");
            }
        }
    }

    @Test
    public void testGetSubscriptionByApplication() throws Exception {

        //add test apis, apps and subscriptions
        ApisAndApps apisAndApps = createApisAppsAndSubscriptions();

        APISubscriptionDAO subscriptionDAO = DAOFactory.getAPISubscriptionDAO();

        API api1 = apisAndApps.getApis().get(0);
        API api2 = apisAndApps.getApis().get(1);
        API api3 = apisAndApps.getApis().get(2);
        API api4 = apisAndApps.getApis().get(3);

        Application app1 = apisAndApps.getApps().get(0);
        Application app2 = apisAndApps.getApps().get(1);
        Application app3 = apisAndApps.getApps().get(2);
        Application app4 = apisAndApps.getApps().get(3);

        //get subscriptions of app1 (api2)
        List<Subscription> subscriptions = subscriptionDAO.getAPISubscriptionsByApplication(app1.getId());
        //validate subscriptions
        Assert.assertEquals(subscriptions.size(), 1, "There should be 1 subscriptions (only).");
        for (Subscription subscription : subscriptions) {
            Assert.assertNotNull(subscription);
            if (subscription.getApi().getId().equals(api2.getId())) {
                Assert.assertEquals(subscription.getApi(), TestUtil.createSummaryAPI(api2),
                        TestUtil.printDiff(subscription.getApi(), TestUtil.createSummaryAPI(api2)));
            } else {
                Assert.fail("Invalid subscription found!!!");
            }
        }

        //get subscriptions of app2 (api1, api2, api3)
        subscriptions = subscriptionDAO.getAPISubscriptionsByApplication(app2.getId());
        //validate subscriptions
        Assert.assertEquals(subscriptions.size(), 3, "There should be 3 subscriptions (only).");
        for (Subscription subscription : subscriptions) {
            Assert.assertNotNull(subscription);
            if (subscription.getApi().getId().equals(api1.getId())) {
                Assert.assertEquals(subscription.getApi(), TestUtil.createSummaryAPI(api1),
                        TestUtil.printDiff(subscription.getApi(), TestUtil.createSummaryAPI(api1)));
            } else if (subscription.getApi().getId().equals(api2.getId())) {
                Assert.assertEquals(subscription.getApi(), TestUtil.createSummaryAPI(api2),
                        TestUtil.printDiff(subscription.getApi(), TestUtil.createSummaryAPI(api2)));
            } else if (subscription.getApi().getId().equals(api3.getId())) {
                Assert.assertEquals(subscription.getApi(), TestUtil.createSummaryAPI(api3),
                        TestUtil.printDiff(subscription.getApi(), TestUtil.createSummaryAPI(api3)));
            } else {
                Assert.fail("Invalid subscription found!!!");
            }
        }

        //get subscriptions of app3 (api3)
        subscriptions = subscriptionDAO.getAPISubscriptionsByApplication(app3.getId());
        //validate subscriptions
        Assert.assertEquals(subscriptions.size(), 1, "There should be 1 subscriptions (only).");
        for (Subscription subscription : subscriptions) {
            Assert.assertNotNull(subscription);
            if (subscription.getApi().getId().equals(api3.getId())) {
                Assert.assertEquals(subscription.getApi(), TestUtil.createSummaryAPI(api3),
                        TestUtil.printDiff(subscription.getApi(), TestUtil.createSummaryAPI(api3)));
            } else {
                Assert.fail("Invalid subscription found!!!");
            }
        }

        //get subscriptions of app4 (api1, api2, api3, api4)
        subscriptions = subscriptionDAO.getAPISubscriptionsByApplication(app4.getId());
        //validate subscriptions
        Assert.assertEquals(subscriptions.size(), 4, "There should be 4 subscriptions (only).");
        for (Subscription subscription : subscriptions) {
            Assert.assertNotNull(subscription);
            if (subscription.getApi().getId().equals(api1.getId())) {
                Assert.assertEquals(subscription.getApi(), TestUtil.createSummaryAPI(api1),
                        TestUtil.printDiff(subscription.getApi(), TestUtil.createSummaryAPI(api1)));
            } else if (subscription.getApi().getId().equals(api2.getId())) {
                Assert.assertEquals(subscription.getApi(), TestUtil.createSummaryAPI(api2),
                        TestUtil.printDiff(subscription.getApi(), TestUtil.createSummaryAPI(api2)));
            } else if (subscription.getApi().getId().equals(api3.getId())) {
                Assert.assertEquals(subscription.getApi(), TestUtil.createSummaryAPI(api3),
                        TestUtil.printDiff(subscription.getApi(), TestUtil.createSummaryAPI(api4)));
            } else if (subscription.getApi().getId().equals(api4.getId())) {
                Assert.assertEquals(subscription.getApi(), TestUtil.createSummaryAPI(api4),
                        TestUtil.printDiff(subscription.getApi(), TestUtil.createSummaryAPI(api4)));
            } else {
                Assert.fail("Invalid subscription found!!!");
            }
        }
    }

    @Test
    public void testDeleteSubscription() throws Exception {
        //add new app
        Application app = TestUtil.addTestApplication();
        //add new api
        API api = TestUtil.addTestAPI();
        //add subscription
        String subscriptionTier = "Gold";
        APISubscriptionDAO apiSubscriptionDAO = DAOFactory.getAPISubscriptionDAO();
        String uuid = UUID.randomUUID().toString();
        apiSubscriptionDAO.addAPISubscription(uuid, api.getId(), app.getId(), subscriptionTier,
                APIMgtConstants.SubscriptionStatus.ACTIVE);
        //get subscription
        Subscription subscription = apiSubscriptionDAO.getAPISubscription(uuid);
        //validate
        Assert.assertEquals(subscription.getId(), uuid);
        //delete subscription
        apiSubscriptionDAO.deleteAPISubscription(uuid);
        //validate
        Assert.assertNull(apiSubscriptionDAO.getAPISubscription(uuid));
    }

    private ApisAndApps createApisAppsAndSubscriptions() throws Exception {
        List<Application> apps = new ArrayList<>();
        //add 4 apps
        String username = "admin";
        Application app1 = TestUtil.addCustomApplication("App1", username);
        apps.add(app1);
        Application app2 = TestUtil.addCustomApplication("App2", username);
        apps.add(app2);
        Application app3 = TestUtil.addCustomApplication("App3", username);
        apps.add(app3);
        Application app4 = TestUtil.addCustomApplication("App4", username);
        apps.add(app4);

        //add 4 apis
        List<API> apis = new ArrayList<>();
        API api1 = TestUtil.addCustomAPI("API1", "1.0.0", "api1");
        apis.add(api1);
        API api2 = TestUtil.addCustomAPI("API2", "1.0.0", "api2");
        apis.add(api2);
        API api3 = TestUtil.addCustomAPI("API3", "1.0.0", "api3");
        apis.add(api3);
        API api4 = TestUtil.addCustomAPI("API4", "1.0.0", "api4");
        apis.add(api4);

        //Add subscriptions
        APISubscriptionDAO subscriptionDAO = DAOFactory.getAPISubscriptionDAO();

        //app1: api2
        subscriptionDAO.addAPISubscription(UUID.randomUUID().toString(), api2.getId(), app1.getId(), "Gold",
                APIMgtConstants.SubscriptionStatus.ACTIVE);

        //app2: api1, api2, api3
        subscriptionDAO.addAPISubscription(UUID.randomUUID().toString(), api1.getId(), app2.getId(), "Gold",
                APIMgtConstants.SubscriptionStatus.ACTIVE);
        subscriptionDAO.addAPISubscription(UUID.randomUUID().toString(), api2.getId(), app2.getId(), "Gold",
                APIMgtConstants.SubscriptionStatus.ACTIVE);
        subscriptionDAO.addAPISubscription(UUID.randomUUID().toString(), api3.getId(), app2.getId(), "Gold",
                APIMgtConstants.SubscriptionStatus.ACTIVE);

        //app3: api3
        subscriptionDAO.addAPISubscription(UUID.randomUUID().toString(), api3.getId(), app3.getId(), "Gold",
                APIMgtConstants.SubscriptionStatus.ACTIVE);

        //app4: api1, api2, api3, api4
        subscriptionDAO.addAPISubscription(UUID.randomUUID().toString(), api1.getId(), app4.getId(), "Gold",
                APIMgtConstants.SubscriptionStatus.ACTIVE);
        subscriptionDAO.addAPISubscription(UUID.randomUUID().toString(), api2.getId(), app4.getId(), "Gold",
                APIMgtConstants.SubscriptionStatus.ACTIVE);
        subscriptionDAO.addAPISubscription(UUID.randomUUID().toString(), api3.getId(), app4.getId(), "Gold",
                APIMgtConstants.SubscriptionStatus.ACTIVE);
        subscriptionDAO.addAPISubscription(UUID.randomUUID().toString(), api4.getId(), app4.getId(), "Gold",
                APIMgtConstants.SubscriptionStatus.ACTIVE);

        return new ApisAndApps(apis, apps);
    }

    private static class ApisAndApps {
        private List<API> apis;
        private List<Application> apps;

        ApisAndApps(List<API> apis, List<Application> apps) {
            this.apis = apis;
            this.apps = apps;
        }

        List<API> getApis() {
            return apis;
        }

        List<Application> getApps() {
            return apps;
        }
    }

}