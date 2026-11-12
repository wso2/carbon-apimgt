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
import org.powermock.reflect.Whitebox;
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
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
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

    /**
     * Mocks the API Manager configuration and the subscription data loader used by the datastore.
     */
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

    /**
     * Creates a datastore with the given subscription not found expiry configuration.
     */
    private SubscriptionDataStoreImpl createStore(String subscriptionNotFoundExpiry) {

        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.SUBSCRIPTION_NOT_FOUND_CACHE_EXPIRY))
                .thenReturn(subscriptionNotFoundExpiry);
        return new SubscriptionDataStoreImpl(TENANT_DOMAIN);
    }

    /**
     * Creates an unblocked subscription for the application and API used in the tests.
     */
    private Subscription createSubscription() {

        Subscription subscription = new Subscription();
        subscription.setAppId(APP_ID);
        subscription.setApiId(API_ID);
        subscription.setSubscriptionId("1");
        subscription.setSubscriptionState(APIConstants.SubscriptionStatus.UNBLOCKED);
        return subscription;
    }

    /**
     * Verifies the number of times the subscription is retrieved from the control plane.
     */
    private void verifyControlPlaneCalls(int times) throws DataLoadingException {

        Mockito.verify(subscriptionDataLoader, Mockito.times(times))
                .getSubscriptionById(Integer.toString(API_ID), Integer.toString(APP_ID));
    }

    /**
     * Waits until the given number of threads are blocked on a lock of the datastore.
     *
     * @param threads threads to check
     * @param count   expected number of blocked threads
     * @return true if the given number of threads are blocked within 10 seconds
     */
    private boolean waitForThreadsBlockedInDataStore(List<Thread> threads, int count) throws InterruptedException {

        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
        while (System.currentTimeMillis() < deadline) {
            int blockedThreads = 0;
            for (Thread thread : threads) {
                if (thread.getState() != Thread.State.BLOCKED) {
                    continue;
                }
                StackTraceElement[] stackTrace = thread.getStackTrace();
                if (stackTrace.length > 0
                        && SubscriptionDataStoreImpl.class.getName().equals(stackTrace[0].getClassName())) {
                    blockedThreads++;
                }
            }
            if (blockedThreads >= count) {
                return true;
            }
            Thread.sleep(10);
        }
        return false;
    }

    /**
     * Tests that the subscription not found entries are not kept when the expiry is not configured.
     */
    @Test
    public void testSubscriptionNotFoundIsNotKeptWhenNotConfigured() throws Exception {

        SubscriptionDataStoreImpl store = createStore(null);
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(2);
    }

    /**
     * Tests that the subscription not found entries are not kept when the expiry is configured as 0.
     */
    @Test
    public void testSubscriptionNotFoundIsNotKeptWhenDisabled() throws Exception {

        SubscriptionDataStoreImpl store = createStore("0");
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(2);
    }

    /**
     * Tests that the subscription not found entries are not kept when the expiry is invalid or negative.
     */
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

    /**
     * Tests that the control plane is not called again for a subscription recently reported as not found.
     */
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

    /**
     * Tests that the subscription not found entries are not kept beyond the maximum size, while the existing entries
     * are still used.
     */
    @Test
    public void testSubscriptionNotFoundIsNotKeptBeyondMaximumSize() throws Exception {

        int maximumSize = 10000;
        SubscriptionDataStoreImpl store = createStore("30");
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(1);
        Map<String, Long> subscriptionNotFoundMap = Whitebox.getInternalState(store, "subscriptionNotFoundMap");
        long expiryTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1);
        for (int i = 0; subscriptionNotFoundMap.size() < maximumSize; i++) {
            subscriptionNotFoundMap.put("other-subscription-" + i, expiryTime);
        }
        // The existing entry is still used when the map is full.
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(1);

        // The map is full, hence a new subscription not found entry is not kept.
        int otherApiId = API_ID + 1;
        Assert.assertNull(store.getSubscriptionById(APP_ID, otherApiId));
        Assert.assertNull(store.getSubscriptionById(APP_ID, otherApiId));
        Mockito.verify(subscriptionDataLoader, Mockito.times(2))
                .getSubscriptionById(Integer.toString(otherApiId), Integer.toString(APP_ID));
        Assert.assertEquals(maximumSize, subscriptionNotFoundMap.size());

        // Once the other entries are expired, they are removed and the new entry is kept.
        subscriptionNotFoundMap.replaceAll((key, value) -> key.startsWith("other-subscription-") ? 0L : value);
        Assert.assertNull(store.getSubscriptionById(APP_ID, otherApiId));
        Assert.assertNull(store.getSubscriptionById(APP_ID, otherApiId));
        Mockito.verify(subscriptionDataLoader, Mockito.times(3))
                .getSubscriptionById(Integer.toString(otherApiId), Integer.toString(APP_ID));
        Assert.assertEquals(2, subscriptionNotFoundMap.size());
    }

    /**
     * Tests that a very large expiry does not overflow the expiry time.
     */
    @Test
    public void testSubscriptionNotFoundIsKeptWhenExpiryIsVeryLarge() throws Exception {

        SubscriptionDataStoreImpl store = createStore(Long.toString(Long.MAX_VALUE));
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(1);
    }

    /**
     * Tests that a subscription found in the control plane is returned and kept in the datastore.
     */
    @Test
    public void testFoundSubscriptionIsReturnedWhenEnabled() throws Exception {

        Subscription subscription = createSubscription();
        Mockito.when(subscriptionDataLoader.getSubscriptionById(anyString(), anyString())).thenReturn(subscription);
        SubscriptionDataStoreImpl store = createStore("30");
        Assert.assertEquals(subscription, store.getSubscriptionById(APP_ID, API_ID));
        Assert.assertEquals(subscription, store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(1);
    }

    /**
     * Tests that the subscription not found entry is cleared when the subscription event is received.
     */
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

    /**
     * Tests that the control plane is called again once the subscription not found entry expires.
     */
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

    /**
     * Tests that the subscription not found entries are not kept when the control plane call fails.
     */
    @Test
    public void testSubscriptionNotFoundIsNotKeptOnControlPlaneError() throws Exception {

        Mockito.when(subscriptionDataLoader.getSubscriptionById(anyString(), anyString()))
                .thenThrow(new DataLoadingException("Error while retrieving subscriptions"));
        SubscriptionDataStoreImpl store = createStore("30");
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(2);
    }

    /**
     * Tests that the internal subscription checks the control plane without using the subscription not found entries.
     */
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
        // The internal subscription should not change the subscription not found entry used by other lookups.
        Assert.assertNull(store.getSubscriptionById(APP_ID, API_ID));
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

        // The subscription not found entry should be cleared once the subscription is found. Hence the subscription
        // should be retrieved from the control plane once it is removed.
        store.removeSubscription(createSubscription());
        Assert.assertNotNull(store.getSubscriptionById(APP_ID, API_ID));
        verifyControlPlaneCalls(4);
    }

    /**
     * Tests that threads waiting on the lock do not call the control plane once the subscription is reported as not
     * found.
     */
    @Test
    public void testWaitingThreadsDoNotCallControlPlaneWhenSubscriptionNotFound() throws Exception {

        int threadCount = 20;
        CountDownLatch controlPlaneCallStarted = new CountDownLatch(1);
        CountDownLatch controlPlaneCallReleased = new CountDownLatch(1);
        Mockito.when(subscriptionDataLoader.getSubscriptionById(anyString(), anyString())).thenAnswer(invocation -> {
            controlPlaneCallStarted.countDown();
            // Keeps the lock until the other threads are waiting on it.
            controlPlaneCallReleased.await(10, TimeUnit.SECONDS);
            return null;
        });
        SubscriptionDataStoreImpl store = createStore("30");
        List<Thread> threads = new CopyOnWriteArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount, runnable -> {
            Thread thread = new Thread(runnable);
            threads.add(thread);
            return thread;
        });
        try {
            List<Future<Subscription>> results = new ArrayList<>();
            results.add(executor.submit(() -> store.getSubscriptionById(APP_ID, API_ID)));
            Assert.assertTrue(controlPlaneCallStarted.await(10, TimeUnit.SECONDS));
            for (int i = 1; i < threadCount; i++) {
                results.add(executor.submit(() -> store.getSubscriptionById(APP_ID, API_ID)));
            }
            // The control plane call returns only after all the other threads are waiting on the lock, hence they
            // check the subscription not found entry inside the lock.
            Assert.assertTrue(waitForThreadsBlockedInDataStore(threads, threadCount - 1));
            controlPlaneCallReleased.countDown();
            for (Future<Subscription> result : results) {
                Assert.assertNull(result.get(10, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdownNow();
        }
        verifyControlPlaneCalls(1);
    }
}
