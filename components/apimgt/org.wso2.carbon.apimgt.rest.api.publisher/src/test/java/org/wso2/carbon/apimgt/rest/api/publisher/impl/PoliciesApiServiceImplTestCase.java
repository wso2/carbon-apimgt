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
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.LabelException;
import org.wso2.carbon.apimgt.core.impl.APIPublisherImpl;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.common.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestAPIPublisherUtil.class)
public class PoliciesApiServiceImplTestCase {

    private static final Logger log = LoggerFactory.getLogger(PoliciesApiServiceImplTestCase.class);
    private static final String USER = "admin";

    @Test
    public void testPoliciesTierLevelGet() throws Exception {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        List<Policy> policies = new ArrayList<>();
        policies.add(SampleTestObjectCreator.goldSubscriptionPolicy);
        policies.add(SampleTestObjectCreator.silverSubscriptionPolicy);
        Mockito.doReturn(policies).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getAllPoliciesByLevel(
                    RestApiUtil.mapRestApiPolicyLevelToPolicyLevelEnum("subscription"));
        Response response = policiesApiService.
                policiesTierLevelGet("subscription", null, null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity().toString().contains("Gold"));
        assertTrue(response.getEntity().toString().contains("Silver"));
    }

    @Test
    public void testPoliciesTierLevelGetException() throws Exception {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Mockito.doThrow(new LabelException("Error occurred", ExceptionCodes.POLICY_LEVEL_NOT_SUPPORTED))
                .when(apiPublisher).getAllPoliciesByLevel(
                RestApiUtil.mapRestApiPolicyLevelToPolicyLevelEnum("subscription"));
        Response response = policiesApiService.
                policiesTierLevelGet("subscription", null, null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Throttle Policy level invalid"));
    }

    @Test
    public void testPoliciesTierLevelTierNameGet() throws Exception {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Policy gold = SampleTestObjectCreator.goldSubscriptionPolicy;
        Mockito.doReturn(gold).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getPolicyByName(
                RestApiUtil.mapRestApiPolicyLevelToPolicyLevelEnum("subscription"), "Gold");
        Response response = policiesApiService.
                policiesTierLevelTierNameGet("Gold", "subscription", null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity().toString().contains("Gold"));
    }

    @Test
    public void testPoliciesTierLevelTierNameGetException() throws Exception {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Mockito.doThrow(new LabelException("Error occurred", ExceptionCodes.POLICY_LEVEL_NOT_SUPPORTED))
                .when(apiPublisher).getPolicyByName(
                RestApiUtil.mapRestApiPolicyLevelToPolicyLevelEnum("subscription"), "Gold");
        Response response = policiesApiService.
                policiesTierLevelTierNameGet("Gold", "subscription", null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Throttle Policy level invalid"));
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
