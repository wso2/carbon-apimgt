/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.rest.api.core.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.SampleTestObjectCreator;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionsApiServiceImplTestCase {

    private static final String API_CONTEXT = "/api";
    private static final String API_VERSION = "1.0.0";
    private static final Integer LIMIT = 2;

    @Test
    public void subscriptionsGetTest() throws Exception {
        APIMgtAdminService apiMgtAdminService = Mockito.mock(APIMgtAdminService.class);

        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl(apiMgtAdminService);

        Mockito.when(apiMgtAdminService.getAPISubscriptionsOfApi(API_CONTEXT, API_VERSION))
                .thenReturn(createSubscriptionValidationDataList());
        Response response = subscriptionsApiService
                .subscriptionsGet(API_CONTEXT, API_VERSION, LIMIT, null, getRequest());

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(((SubscriptionListDTO) response.getEntity()).getList().size(), 2);
    }

    @Test
    public void subscriptionsApiContextEmptyGetTest() throws Exception {
        Response response = getResponse(null, API_VERSION);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(((SubscriptionListDTO) response.getEntity()).getList().size(), 2);
    }

    @Test
    public void subscriptionsApiVersionEmptyGetTest() throws Exception {
        Response response = getResponse(API_CONTEXT, null);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(((SubscriptionListDTO) response.getEntity()).getList().size(), 2);
    }

    @Test
    public void policiesGetExceptionTest() throws Exception {
        APIMgtAdminService apiMgtAdminService = Mockito.mock(APIMgtAdminService.class);

        Mockito.when(apiMgtAdminService.getAPISubscriptionsOfApi(API_CONTEXT, API_VERSION)).thenThrow(
                new APIManagementException("", ExceptionCodes.APIMGT_DAO_EXCEPTION));

        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl(apiMgtAdminService);
        Response response = subscriptionsApiService
                .subscriptionsGet(API_CONTEXT, API_VERSION, LIMIT, null, getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    private Response getResponse(String apiContext, String apiVersion) throws Exception {
        APIMgtAdminService apiMgtAdminService = Mockito.mock(APIMgtAdminService.class);

        SubscriptionsApiServiceImpl subscriptionsApiService = new SubscriptionsApiServiceImpl(apiMgtAdminService);

        Mockito.when(apiMgtAdminService.getAPISubscriptions(LIMIT)).thenReturn(createSubscriptionValidationDataList());
        return subscriptionsApiService.subscriptionsGet(apiContext, apiVersion, LIMIT, null, getRequest());
    }

    private List<SubscriptionValidationData> createSubscriptionValidationDataList() throws Exception {
        SubscriptionValidationData subscriptionValidationDataOne = SampleTestObjectCreator
                .createSubscriptionValidationData();
        SubscriptionValidationData subscriptionValidationDataTwo = SampleTestObjectCreator
                .createSubscriptionValidationData();

        List<SubscriptionValidationData> subscriptionValidationDataList = new ArrayList<>();

        subscriptionValidationDataList.add(subscriptionValidationDataOne);
        subscriptionValidationDataList.add(subscriptionValidationDataTwo);

        return subscriptionValidationDataList;
    }

    private Request getRequest() throws Exception {
        return Mockito.mock(Request.class);
    }

}
