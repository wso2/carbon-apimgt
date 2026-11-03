/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.keymgt.model.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscription;
import org.wso2.carbon.apimgt.keymgt.model.exception.DataLoadingException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Tests for keeping the subscriptions which are not found in the control plane in SubscriptionDataStoreImpl.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SubscriptionDataStoreImpl.class, ServiceReferenceHolder.class})
public class SubscriptionDataStoreImplTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final int APP_ID = 1;
    private static final int API_ID = 2;

    private APIManagerConfiguration apiManagerConfiguration;
    private SubscriptionDataLoaderImpl subscriptionDataLoader;

    @Before
    public void setup() throws Exception {

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService apiManagerConfigurationService =
                Mockito.mock(APIManagerConfigurationService.class);
        apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        subscriptionDataLoader = Mockito.mock(SubscriptionDataLoaderImpl.class);
        PowerMockito.whenNew(SubscriptionDataLoaderImpl.class).withNoArguments().thenReturn(subscriptionDataLoader);
    }

    private SubscriptionDataStoreImpl createStore(String subscriptionNotFoundExpiry) {

        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.SUBSCRIPTION_NOT_FOUND_CACHE_EXPIRY))
                .thenReturn(subscriptionNotFoundExpiry);
        return new SubscriptionDataStoreImpl(TENANT_DOMAIN);
    }

    private Subscription createSubscription() {

        Subscription subscription = new Subscription();
        subscription.setAppId(APP_ID);
        subscription.setApiId(API_ID);
        subscription.setSubscriptionId("1");
        subscription.setSubscriptionState(APIConstants.SubscriptionStatus.UNBLOCKED);
        return subscription;
    }

    private void verifyControlPlaneCalls(int times) throws DataLoadingException {

        Mockito.verify(subscriptionDataLoader, Mockito.times(times))
                .getSubscriptionById(Integer.toString(API_ID), Integer.toString(APP_ID));
    }

    @Test
    public void testSubscriptionNotFoundIsNotKeptWhenNotConfigured() throws Exception {

        SubscriptionDataStoreImpl store = createStore(null);
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(2);
    }

    @Test
    public void testSubscriptionNotFoundIsNotKeptWhenDisabled() throws Exception {

        SubscriptionDataStoreImpl store = createStore("0");
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(2);
    }

    @Test
    public void testSubscriptionNotFoundIsNotKeptWhenInvalid() throws Exception {

        SubscriptionDataStoreImpl store = createStore("invalid");
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(2);

        SubscriptionDataStoreImpl negativeExpiryStore = createStore("-10");
        Assert.assertNull(negativeExpiryStore.getSubscriptionById(APP_ID, API_ID));
        Assert.assertNull(negativeExpiryStore.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(4);
    }

    @Test
    public void testSubscriptionNotFoundIsKeptWhenEnabled() throws Exception {

        SubscriptionDataStoreImpl store = createStore("30");
        for (int i = 0; i < 5; i++) {
            Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        }
        // Subscription validation disabled flow uses the same retrieval.
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID, true));
        verifyControlPlaneCalls(1);
        // Another subscription of the same tenant should still be retrieved from the control plane.
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID + 1));
        Mockito.verify(subscriptionDataLoader, Mockito.times(1))
                .getSubscriptionById(Integer.toString(API_ID + 1), Integer.toString(APP_ID));
    }

    @Test
    public void testSubscriptionNotFoundIsKeptWhenExpiryIsVeryLarge() throws Exception {

        SubscriptionDataStoreImpl store = createStore(Long.toString(Long.MAX_VALUE));
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(1);
    }

    @Test
    public void testFoundSubscriptionIsReturnedWhenEnabled() throws Exception {

        Subscription subscription = createSubscription();
        Mockito.when(subscriptionDataLoader.getSubscriptionById(anyString(), anyString())).thenReturn(subscription);
        SubscriptionDataStoreImpl store = createStore("30");
        Assert.assertEquals(subscription, store.getSubscriptionById(APP_ID, API_ID));
        Assert.assertEquals(subscription, store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(1);
    }

    @Test
    public void testSubscriptionNotFoundIsClearedOnSubscriptionEvent() throws Exception {

        SubscriptionDataStoreImpl store = createStore("30");
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(1);

        Subscription subscription = createSubscription();
        store.addOrUpdateSubscription(subscription);
        Assert.assertEquals(subscription, store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(1);

        // Once the subscription is removed, it should be retrieved from the control plane again.
        store.removeSubscription(subscription);
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(2);
    }

    @Test
    public void testSubscriptionNotFoundExpires() throws Exception {

        SubscriptionDataStoreImpl store = createStore("1");
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(1);
        Thread.sleep(1100);
        Subscription subscription = createSubscription();
        Mockito.when(subscriptionDataLoader.getSubscriptionById(anyString(), anyString())).thenReturn(subscription);
        Assert.assertEquals(subscription, store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(2);
    }

    @Test
    public void testSubscriptionNotFoundIsNotKeptOnControlPlaneError() throws Exception {

        Mockito.when(subscriptionDataLoader.getSubscriptionById(anyString(), anyString()))
                .thenThrow(new DataLoadingException("Error while retrieving subscriptions"));
        SubscriptionDataStoreImpl store = createStore("30");
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(2);
    }

    @Test
    public void testInternalSubscriptionChecksControlPlaneWhenSubscriptionNotFound() throws Exception {

        SubscriptionDataStoreImpl store = createStore("30");
        API api = new API();
        api.setApiId(API_ID);
        Application application = new Application();
        application.setId(APP_ID);
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(1);

        // The subscription is not found in the control plane, hence it should be subscribed internally.
        store.subscribeToAPIInternally(api, application, TENANT_DOMAIN);
        Mockito.verify(subscriptionDataLoader, Mockito.timeout(10000).times(1))
                .subscribeToAPIInternally(any(API.class), any(Application.class), anyString());
        verifyControlPlaneCalls(2);

        // The subscription is now available in the control plane, but its event is not received. Hence the internal
        // subscription should find it in the control plane, without subscribing again.
        Mockito.when(subscriptionDataLoader.getSubscriptionById(anyString(), anyString()))
                .thenReturn(createSubscription());
        store.subscribeToAPIInternally(api, application, TENANT_DOMAIN);
        Mockito.verify(subscriptionDataLoader, Mockito.timeout(10000).times(3))
                .getSubscriptionById(Integer.toString(API_ID), Integer.toString(APP_ID));
        Thread.sleep(500);
        Mockito.verify(subscriptionDataLoader, Mockito.times(1))
                .subscribeToAPIInternally(any(API.class), any(Application.class), anyString());
        Assert.assertNotNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(3);
    }

    @Test
    public void testWaitingThreadsDoNotCallControlPlaneWhenSubscriptionNotFound() throws Exception {

        int threadCount = 20;
        CountDownLatch controlPlaneCallStarted = new CountDownLatch(1);
        Mockito.when(subscriptionDataLoader.getSubscriptionById(anyString(), anyString())).thenAnswer(invocation -> {
            controlPlaneCallStarted.countDown();
            // Keeps the lock while the other threads are waiting on it.
            Thread.sleep(500);
            return null;
        });
        SubscriptionDataStoreImpl store = createStore("30");
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            List<Future<Subscription>> results = new ArrayList<>();
            results.add(executor.submit(() -> store.getSubscriptionById(APP_ID, API_ID)));
            Assert.assertTrue(controlPlaneCallStarted.await(10, TimeUnit.SECONDS));
            for (int i = 1; i < threadCount; i++) {
                results.add(executor.submit(() -> store.getSubscriptionById(APP_ID, API_ID)));
            }
            for (Future<Subscription> result : results) {
                Assert.assertNull(result.get(10, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdownNow();
        }
        verifyControlPlaneCalls(1);
    }
}
