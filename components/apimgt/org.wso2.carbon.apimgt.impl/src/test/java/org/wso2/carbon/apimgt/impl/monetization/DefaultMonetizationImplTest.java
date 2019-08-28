/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.monetization;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.MonetizationException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.MonetizationUsagePublishInfo;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;

import java.util.HashMap;
import java.util.Map;

public class DefaultMonetizationImplTest {

    private DefaultMonetizationImpl monetizationImpl;
    private SubscriptionPolicy subPolicy;
    private API api;
    private APIProvider apiProvider;
    private MonetizationUsagePublishInfo monetizationUsagePublishInfo;
    private String tenantDomain = "test.com";
    Map<String, String> dataMap = new HashMap<String, String>();
    String subscriptionUUID = "test";

    @Before
    public void init() throws Exception {

        monetizationImpl = Mockito.mock(DefaultMonetizationImpl.class);
        subPolicy = Mockito.mock(SubscriptionPolicy.class);
        api = Mockito.mock(API.class);
        apiProvider = Mockito.mock(APIProvider.class);
        monetizationUsagePublishInfo = Mockito.mock(MonetizationUsagePublishInfo.class);
        Mockito.when(monetizationImpl.createBillingPlan(subPolicy)).thenReturn(true);
        Mockito.when(monetizationImpl.updateBillingPlan(subPolicy)).thenReturn(true);
        Mockito.when(monetizationImpl.deleteBillingPlan(subPolicy)).thenReturn(true);
        Mockito.when(monetizationImpl.enableMonetization(tenantDomain, api, dataMap)).thenReturn(true);
        Mockito.when(monetizationImpl.disableMonetization(tenantDomain, api, dataMap)).thenReturn(true);
        Mockito.when(monetizationImpl.getMonetizedPoliciesToPlanMapping(api)).thenReturn(dataMap);
        Mockito.when(monetizationImpl.getCurrentUsageForSubscription(subscriptionUUID, apiProvider)).thenReturn(dataMap);
        Mockito.when(monetizationImpl.getTotalRevenue(api, apiProvider)).thenReturn(dataMap);
        Mockito.when(monetizationImpl.publishMonetizationUsageRecords(monetizationUsagePublishInfo)).thenReturn(true);
    }

    @Test
    public void testCreateBillingPlan() throws MonetizationException {
        Assert.assertTrue("Failed to create a plan in the billing engine.",
                monetizationImpl.createBillingPlan(subPolicy));
    }

    @Test
    public void testUpdateBillingPlan() throws MonetizationException {
        Assert.assertTrue("Failed to update the plan in the billing engine.",
                monetizationImpl.updateBillingPlan(subPolicy));
    }

    @Test
    public void testDeleteBillingPlan() throws MonetizationException {
        Assert.assertTrue("Failed to delete the plan in the billing engine.",
                monetizationImpl.deleteBillingPlan(subPolicy));
    }

    @Test
    public void testEnableMonetization() throws MonetizationException {
        Assert.assertTrue("Failed to enable monetization in the billing engine.",
                monetizationImpl.enableMonetization(tenantDomain, api, dataMap));
    }

    @Test
    public void testDisableMonetization() throws MonetizationException {
        Assert.assertTrue("Failed to disable monetization in the billing engine.",
                monetizationImpl.enableMonetization(tenantDomain, api, dataMap));

    }

    @Test
    public void testGetMonetizedPoliciesToPlanMapping() throws MonetizationException {
        Assert.assertNotNull("Failed to get the policy to plan mapping from billing engine.",
                monetizationImpl.getMonetizedPoliciesToPlanMapping(api));

    }

    @Test
    public void testGetCurrentUsageForSubscription() throws MonetizationException {
        Assert.assertNotNull("Failed to get current usage from billing engine.",
                monetizationImpl.getCurrentUsageForSubscription(subscriptionUUID, apiProvider));
    }

    @Test
    public void testGetTotalRevenue() throws MonetizationException {
        Assert.assertNotNull("Failed to get total revenue data from billing engine.",
                monetizationImpl.getTotalRevenue(api, apiProvider));
    }

    @Test
    public void testPublishMonetizationUsageRecords() throws MonetizationException {
        Assert.assertTrue("Failed to publish monetization records to billing engine.",
                monetizationImpl.publishMonetizationUsageRecords(monetizationUsagePublishInfo));
    }
}
