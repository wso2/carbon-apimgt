/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.LabelException;
import org.wso2.carbon.apimgt.core.impl.APIPublisherImpl;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.common.exception.BadRequestException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.common.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestAPIPublisherUtil.class)
public class SubscriptionsApiServiceImplTestCase {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionsApiServiceImplTestCase.class);
    private static final String USER = "admin";

    @Test
    public void testSubscriptionsBlockSubscriptionPostNotExist() throws Exception {
        printTestMethodName();
        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String subscriptionId = UUID.randomUUID().toString();
        Mockito.doReturn(null).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getSubscriptionByUUID(subscriptionId);
        Response response = subscriptionsApiService.
                            subscriptionsBlockSubscriptionPost(subscriptionId, null, null, null, getRequest());
        assertEquals(response.getStatus(), 404);
        assertTrue(response.getEntity().toString().contains("Subscription not found"));
    }

    @Test
    public void testSubscriptionsBlockSubscriptionPostIllegalState() throws Exception {
        printTestMethodName();
        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String subscriptionId = UUID.randomUUID().toString();
        Subscription subscription = SampleTestObjectCreator.createSubscription(subscriptionId);
        subscription.setStatus(APIMgtConstants.SubscriptionStatus.REJECTED);
        Mockito.doReturn(subscription).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getSubscriptionByUUID(subscriptionId);
        Response response = subscriptionsApiService.
                subscriptionsBlockSubscriptionPost(subscriptionId, APIMgtConstants.SubscriptionStatus.BLOCKED.name()
                                                        , null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Invalid state change for subscription"));
    }

    @Test
    public void testSubscriptionsBlockSubscriptionPost() throws Exception {
        printTestMethodName();
        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String subscriptionId = UUID.randomUUID().toString();
        Subscription subscription = SampleTestObjectCreator.createSubscription(subscriptionId);
        subscription.setStatus(APIMgtConstants.SubscriptionStatus.ACTIVE);
        Subscription newSubscription = SampleTestObjectCreator.createSubscription(subscriptionId);
        newSubscription.setStatus(APIMgtConstants.SubscriptionStatus.BLOCKED);
        Mockito.doReturn(subscription).doReturn(newSubscription).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getSubscriptionByUUID(subscriptionId);
        Mockito.doNothing().doThrow(new IllegalArgumentException())
                .when(apiPublisher).updateSubscriptionStatus(subscriptionId,
                APIMgtConstants.SubscriptionStatus.BLOCKED);
        Response response = subscriptionsApiService.
                subscriptionsBlockSubscriptionPost(subscriptionId, APIMgtConstants.SubscriptionStatus.BLOCKED.name()
                        , null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity().toString().contains("BLOCKED"));
    }

    @Test
    public void testSubscriptionsBlockSubscriptionPostException() throws Exception {
        printTestMethodName();
        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String subscriptionId = UUID.randomUUID().toString();
        Subscription subscription = SampleTestObjectCreator.createSubscription(subscriptionId);
        subscription.setStatus(APIMgtConstants.SubscriptionStatus.REJECTED);
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.SUBSCRIPTION_STATE_INVALID))
                .when(apiPublisher).getSubscriptionByUUID(subscriptionId);
        Response response = subscriptionsApiService.
                subscriptionsBlockSubscriptionPost(subscriptionId, APIMgtConstants.SubscriptionStatus.BLOCKED.name()
                        , null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Invalid state change for subscription"));
    }

    @Test
    public void testSubscriptionsUnblockSubscriptionPostNotExist() throws Exception {
        printTestMethodName();
        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String subscriptionId = UUID.randomUUID().toString();
        Mockito.doReturn(null).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getSubscriptionByUUID(subscriptionId);
        Response response = subscriptionsApiService.
                subscriptionsUnblockSubscriptionPost(subscriptionId, null, null, getRequest());
        assertEquals(response.getStatus(), 404);
        assertTrue(response.getEntity().toString().contains("Subscription not found"));
    }

    @Test
    public void testSubscriptionsUnblockSubscriptionPostIllegalState() throws Exception {
        printTestMethodName();
        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String subscriptionId = UUID.randomUUID().toString();
        Subscription subscription = SampleTestObjectCreator.createSubscription(subscriptionId);
        subscription.setStatus(APIMgtConstants.SubscriptionStatus.REJECTED);
        Mockito.doReturn(subscription).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getSubscriptionByUUID(subscriptionId);
        Response response = subscriptionsApiService.
                subscriptionsUnblockSubscriptionPost(subscriptionId, null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Invalid state change for subscription"));
    }

    @Test
    public void testSubscriptionsUnblockSubscriptionPost() throws Exception {
        printTestMethodName();
        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String subscriptionId = UUID.randomUUID().toString();
        Subscription subscription = SampleTestObjectCreator.createSubscription(subscriptionId);
        subscription.setStatus(APIMgtConstants.SubscriptionStatus.BLOCKED);
        Subscription newSubscription = SampleTestObjectCreator.createSubscription(subscriptionId);
        newSubscription.setStatus(APIMgtConstants.SubscriptionStatus.ACTIVE);
        Mockito.doReturn(subscription).doReturn(newSubscription).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getSubscriptionByUUID(subscriptionId);
        Mockito.doNothing().doThrow(new IllegalArgumentException())
                .when(apiPublisher).updateSubscriptionStatus(subscriptionId,
                APIMgtConstants.SubscriptionStatus.BLOCKED);
        Response response = subscriptionsApiService.
                subscriptionsUnblockSubscriptionPost(subscriptionId, null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity().toString().contains("ACTIVE"));
    }

    @Test
    public void testSubscriptionsGetException() throws Exception {
        printTestMethodName();
        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String subscriptionId = UUID.randomUUID().toString();
        Subscription subscription = SampleTestObjectCreator.createSubscription(subscriptionId);
        subscription.setStatus(APIMgtConstants.SubscriptionStatus.REJECTED);
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.SUBSCRIPTION_STATE_INVALID))
                .when(apiPublisher).getSubscriptionByUUID(subscriptionId);
        Response response = subscriptionsApiService.
                subscriptionsUnblockSubscriptionPost(subscriptionId, null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Invalid state change for subscription"));
    }

    @Test
    public void testSubscriptionsGet() throws Exception {
        printTestMethodName();
        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        List<Subscription> subscriptions = new ArrayList<>();
        String sub1 = UUID.randomUUID().toString();
        String sub2 = UUID.randomUUID().toString();
        subscriptions.add(SampleTestObjectCreator.createSubscription(sub1));
        subscriptions.add(SampleTestObjectCreator.createSubscription(sub2));
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        Mockito.doReturn(subscriptions).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getSubscriptionsByAPI(apiId);
        Response response = subscriptionsApiService.
                subscriptionsGet(apiId, 10, 0, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity().toString().contains(sub1));
        assertTrue(response.getEntity().toString().contains(sub2));
    }

    @Test(expected = BadRequestException.class)
    public void testSubscriptionsGetNotExist() throws Exception {
        printTestMethodName();
        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        List<Subscription> subscriptions = new ArrayList<>();
        String sub1 = UUID.randomUUID().toString();
        String sub2 = UUID.randomUUID().toString();
        subscriptions.add(SampleTestObjectCreator.createSubscription(sub1));
        subscriptions.add(SampleTestObjectCreator.createSubscription(sub2));
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        Mockito.doReturn(subscriptions).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getSubscriptionsByAPI(apiId);
        Response response = subscriptionsApiService.
                subscriptionsGet(null, 10, 0, null, getRequest());
    }

    @Test
    public void testSubscriptionsUnblockSubscriptionPostException() throws Exception {
        printTestMethodName();
        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        List<Subscription> subscriptions = new ArrayList<>();
        String sub1 = UUID.randomUUID().toString();
        String sub2 = UUID.randomUUID().toString();
        subscriptions.add(SampleTestObjectCreator.createSubscription(sub1));
        subscriptions.add(SampleTestObjectCreator.createSubscription(sub2));
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        String apiId = UUID.randomUUID().toString();
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.SUBSCRIPTION_STATE_INVALID))
                .when(apiPublisher).getSubscriptionsByAPI(apiId);
        Response response = subscriptionsApiService.
                subscriptionsGet(apiId, 10, 0, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Invalid state change for subscription"));
    }

    @Test
    public void testSubscriptionsSubscriptionIdGet() throws Exception {
        printTestMethodName();
        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        String sub1 = UUID.randomUUID().toString();
        Subscription subscription = SampleTestObjectCreator.createSubscription(sub1);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Mockito.doReturn(subscription).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getSubscriptionByUUID(sub1);
        Response response = subscriptionsApiService.
                subscriptionsSubscriptionIdGet(sub1, null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity().toString().contains(sub1));
    }

    @Test
    public void testSubscriptionsSubscriptionIdGetExist() throws Exception {
        printTestMethodName();
        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        String sub1 = UUID.randomUUID().toString();
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Mockito.doReturn(null).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getSubscriptionByUUID(sub1);
        Response response = subscriptionsApiService.
                subscriptionsSubscriptionIdGet(sub1, null, null, getRequest());
        assertEquals(response.getStatus(), 404);
        assertTrue(response.getEntity().toString().contains("Subscription not found"));
    }

    @Test
    public void testSubscriptionsSubscriptionIdGetException() throws Exception {
        printTestMethodName();
        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        String sub1 = UUID.randomUUID().toString();
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.SUBSCRIPTION_STATE_INVALID))
                .when(apiPublisher).getSubscriptionByUUID(sub1);
        Response response = subscriptionsApiService.
                subscriptionsSubscriptionIdGet(sub1, null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Invalid state change for subscription"));
    }

    // Sample request to be used by tests
    private Request getRequest() throws Exception {
        CarbonMessage carbonMessage = new HTTPCarbonMessage();
        carbonMessage.setProperty("LOGGED_IN_USER", USER);
        Request request = new Request(carbonMessage);
        return request;
    }

    private static void printTestMethodName () {
        log.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }
}
