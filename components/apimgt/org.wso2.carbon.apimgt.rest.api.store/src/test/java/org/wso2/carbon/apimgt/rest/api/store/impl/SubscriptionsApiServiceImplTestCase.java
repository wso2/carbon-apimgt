/*
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
package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.dao.ApiType;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIStoreImpl;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.common.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.SubscriptionMappingUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestApiUtil.class)
public class SubscriptionsApiServiceImplTestCase {

    private static final String USER = "admin";
    private static final String contentType = "application/json";

    @Test
    public void testSubscriptionsGetfromAPI() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String apiId = UUID.randomUUID().toString();
        String subsID1 = UUID.randomUUID().toString();
        String subsID2 = UUID.randomUUID().toString();

        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = TestUtil.getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(SampleTestObjectCreator.createSubscription(subsID1));
        subscriptionList.add(SampleTestObjectCreator.createSubscription(subsID2));

        Mockito.when(apiStore.getSubscriptionsByAPI(apiId)).thenReturn(subscriptionList);

        Response response = subscriptionsApiService.subscriptionsGet
                (apiId, null, null, 0, 10, null, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testSubscriptionsGetfromApplication() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();
        String subsID1 = UUID.randomUUID().toString();
        String subsID2 = UUID.randomUUID().toString();

        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = TestUtil.getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Application application = SampleTestObjectCreator.createDefaultApplication();
        List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(SampleTestObjectCreator.createSubscription(subsID1));
        subscriptionList.add(SampleTestObjectCreator.createSubscription(subsID2));

        Mockito.when(apiStore.getApplicationByUuid(applicationId)).thenReturn(application);
        Mockito.when(apiStore.getAPISubscriptionsByApplication(application, ApiType.STANDARD))
                .thenReturn(subscriptionList);

        Response response = subscriptionsApiService.subscriptionsGet
                (null, applicationId, ApiType.STANDARD.toString(), 0, 10, null, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testSubscriptionsPost() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();
        String apiId = UUID.randomUUID().toString();
        String subsID1 = UUID.randomUUID().toString();
        String subsID2 = UUID.randomUUID().toString();

        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = TestUtil.getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Application application = SampleTestObjectCreator.createDefaultApplication();
        Endpoint api1SandBoxEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("abcd").build();
        Endpoint api1ProdEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("cdef").build();
        API api = TestUtil.createApi("provider1", apiId, "testapi1", "1.0.0", "Test API 1 - version 1.0.0",
                TestUtil.createEndpointTypeToIdMap(api1SandBoxEndpointId, api1ProdEndpointId)).build();

        Mockito.when(apiStore.getApplicationByUuid(applicationId)).thenReturn(application);
        Mockito.when(apiStore.getAPIbyUUID(apiId)).thenReturn(api);

        SubscriptionDTO subscriptionDTO =
                SubscriptionMappingUtil.fromSubscriptionToDTO(SampleTestObjectCreator.createSubscription(subsID1));

        Response response = subscriptionsApiService.subscriptionsPost
                (subscriptionDTO, request);

        Assert.assertEquals(404, response.getStatus());
    }

    @Test
    public void testSubscriptionsSubscriptionIdDelete() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String subsID1 = UUID.randomUUID().toString();

        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = TestUtil.getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        WorkflowResponse workflowResponse = new GeneralWorkflowResponse();
        workflowResponse.setWorkflowStatus(WorkflowStatus.APPROVED);

        Mockito.when(apiStore.deleteAPISubscription(subsID1)).thenReturn(workflowResponse);

        Response response = subscriptionsApiService.subscriptionsSubscriptionIdDelete
                (subsID1, null, null, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testSubscriptionsSubscriptionIdGet() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String subsID1 = UUID.randomUUID().toString();

        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = TestUtil.getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Subscription subscription = SampleTestObjectCreator.createSubscription(subsID1);

        Mockito.when(apiStore.getSubscriptionByUUID(subsID1)).thenReturn(subscription);

        Response response = subscriptionsApiService.subscriptionsSubscriptionIdGet
                (subsID1, null, null, request);

        Assert.assertEquals(200, response.getStatus());
    }
}
