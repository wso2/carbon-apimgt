/*
 *Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SubscriptionDataHolder.class})

public class UtilsTest {
    @Before
    public void init() {
        PowerMockito.mockStatic(SubscriptionDataHolder.class);
    }

    @Test
    public void getSelectedAPI() {
        SubscriptionDataHolder subscriptionDataHolder = Mockito.mock(SubscriptionDataHolder.class);
        Mockito.when(SubscriptionDataHolder.getInstance()).thenReturn(subscriptionDataHolder);
        SubscriptionDataStore subscriptionDataStore = Mockito.mock(SubscriptionDataStore.class);
        Mockito.when(subscriptionDataHolder.getTenantSubscriptionStore("carbon.super")).thenReturn(subscriptionDataStore);
        Mockito.when(subscriptionDataStore.getAllAPIsByContextList()).thenReturn(apiMap());
        Map<String, API> selectedAPIList = Utils.getSelectedAPIList("/api1/cde?c=y", "carbon.super");
        Assert.assertEquals(selectedAPIList.size(), 1);
        Assert.assertEquals(selectedAPIList.keySet().iterator().next(), "/api1");
    }

    @Test
    public void getSelectedAPI2() {
        SubscriptionDataHolder subscriptionDataHolder = Mockito.mock(SubscriptionDataHolder.class);
        Mockito.when(SubscriptionDataHolder.getInstance()).thenReturn(subscriptionDataHolder);
        SubscriptionDataStore subscriptionDataStore = Mockito.mock(SubscriptionDataStore.class);
        Mockito.when(subscriptionDataHolder.getTenantSubscriptionStore("carbon.super")).thenReturn(subscriptionDataStore);
        Mockito.when(subscriptionDataStore.getAllAPIsByContextList()).thenReturn(apiMap());
        Map<String, API> selectedAPIList = Utils.getSelectedAPIList("/api1/abc/cde?c=y", "carbon.super");
        Assert.assertEquals(selectedAPIList.size(), 2);
        Assert.assertEquals(selectedAPIList.keySet().iterator().next(), "/api1/abc");
    }

    @Test
    public void getSelectedAPI3() {
        SubscriptionDataHolder subscriptionDataHolder = Mockito.mock(SubscriptionDataHolder.class);
        Mockito.when(SubscriptionDataHolder.getInstance()).thenReturn(subscriptionDataHolder);
        SubscriptionDataStore subscriptionDataStore = Mockito.mock(SubscriptionDataStore.class);
        Mockito.when(subscriptionDataHolder.getTenantSubscriptionStore("carbon.super")).thenReturn(subscriptionDataStore);
        Mockito.when(subscriptionDataStore.getAllAPIsByContextList()).thenReturn(apiMap());
        Map<String, API> selectedAPIList = Utils.getSelectedAPIList("/api1/1.0.0/cde?c=y", "carbon.super");
        Assert.assertEquals(selectedAPIList.size(), 2);
        Assert.assertEquals(selectedAPIList.keySet().iterator().next(), "/api1/1.0.0");
    }

    @Test
    public void getSelectedAPI4() {
        SubscriptionDataHolder subscriptionDataHolder = Mockito.mock(SubscriptionDataHolder.class);
        Mockito.when(SubscriptionDataHolder.getInstance()).thenReturn(subscriptionDataHolder);
        SubscriptionDataStore subscriptionDataStore = Mockito.mock(SubscriptionDataStore.class);
        Mockito.when(subscriptionDataHolder.getTenantSubscriptionStore("carbon.super")).thenReturn(subscriptionDataStore);
        Mockito.when(subscriptionDataStore.getAllAPIsByContextList()).thenReturn(apiMap());
        Map<String, API> selectedAPIList = Utils.getSelectedAPIList("/api1/abc/1.0.0/cde?c=y", "carbon.super");
        Assert.assertEquals(selectedAPIList.size(), 3);
        Assert.assertEquals(selectedAPIList.keySet().iterator().next(), "/api1/abc/1.0.0");
    }


    private Map<String, API> apiMap() {
        Map<String, API> apiMap = new HashMap<>();
        apiMap.put("/api1", new API("1234566", 1, "admin", "API1", "1.0.0", "/api1/1.0.0", null,
                "HTTP", "PUBLISHED", true));
        apiMap.put("/api1/abc", new API("56321313131313", 2, "admin", "API2", "1.0.0", "/api1/abc/1.0.0", null,
                "HTTP", "PUBLISHED", true));
        apiMap.put("/api1/1.0.0", new API("1234566", 1, "admin", "API1", "1.0.0", "/api1/1.0.0", null,
                "HTTP", "PUBLISHED", true));
        apiMap.put("/api1/abc/1.0.0", new API("56321313131313", 2, "admin", "API2", "1.0.0", "/api1/abc/1.0.0", null,
                "HTTP", "PUBLISHED", true));
        return apiMap;
    }
}