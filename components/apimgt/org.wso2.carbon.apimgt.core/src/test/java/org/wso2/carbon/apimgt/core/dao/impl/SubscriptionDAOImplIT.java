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
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.TestUtil;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

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
        Assert.assertEquals(subscription.getId(), uuid);
        Assert.assertEquals(subscription.getStatus(), APIMgtConstants.SubscriptionStatus.ACTIVE);
        Assert.assertEquals(subscription.getSubscriptionTier(), subscriptionTier);
        validateApp(subscription.getApplication(), app);
        validateAPI(subscription.getApi(), api);
    }

    @Test
    public void testGetSubscriptionByAPI() throws Exception {
        //add 4 apps
        String username = "admin";
        Application app1 = TestUtil.addCustomApplication("App1", username);
        Application app2 = TestUtil.addCustomApplication("App2", username);
        Application app3 = TestUtil.addCustomApplication("App3", username);
        Application app4 = TestUtil.addCustomApplication("App4", username);

        //add api
        API api  = TestUtil.addTestAPI();
    }

    @Test
    public void testGetSubscriptionByApplication() throws Exception {
        //add new app
        Application currentApp = TestUtil.addTestApplication();
        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        Application newApp = SampleTestObjectCreator.createAlternativeApplication();
        newApp.setUuid(currentApp.getId());
    }

    @Test
    public void testDeleteApplication() throws Exception {
        // add app
        Application app = TestUtil.addTestApplication();
        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        //delete app
        applicationDAO.deleteApplication(app.getId());
        Application appFromDB = applicationDAO.getApplication(app.getId());
        Assert.assertNull(appFromDB);
    }

    @Test
    public void testIsApplicationNameExists() throws Exception {

    }

    private void validateAPI(API actualAPI, API expectedAPI) {
        Assert.assertEquals(actualAPI.getId(), expectedAPI.getId());
        Assert.assertEquals(actualAPI.getName(), expectedAPI.getName());
        Assert.assertEquals(actualAPI.getVersion(), expectedAPI.getVersion());
        Assert.assertEquals(actualAPI.getContext(), expectedAPI.getContext());
        Assert.assertEquals(actualAPI.getProvider(), expectedAPI.getProvider());
    }

    private void validateApp(Application appFromDB, Application expectedApp) {
        Assert.assertEquals(appFromDB.getId(), expectedApp.getId());
        Assert.assertEquals(appFromDB.getName(), expectedApp.getName());
        Assert.assertEquals(appFromDB.getCallbackUrl(), expectedApp.getCallbackUrl());
        Assert.assertEquals(appFromDB.getStatus(), expectedApp.getStatus());
    }

}