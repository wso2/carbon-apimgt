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
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

@RunWith(PowerMockRunner.class)
public class SdkGenApiServiceImplTestCase {
    private static final String USER = "admin";
    @Test
    public void sdkGenLanguagesGet() throws APIManagementException, NotFoundException {
        SdkGenApiServiceImpl sdkGenLanguagesApiService = new SdkGenApiServiceImpl();
        Request request = getRequest();
        Response response = sdkGenLanguagesApiService.sdkGenLanguagesGet(request);

        Assert.assertEquals(200, response.getStatus());
    }

    // Sample request to be used by tests
    private Request getRequest() throws APIMgtSecurityException {
        HTTPCarbonMessage carbonMessage = Mockito.mock(HTTPCarbonMessage.class);
        Mockito.when(carbonMessage.getProperty("LOGGED_IN_USER")).thenReturn(USER);
        Request request = new Request(carbonMessage);
        return request;
    }
}

