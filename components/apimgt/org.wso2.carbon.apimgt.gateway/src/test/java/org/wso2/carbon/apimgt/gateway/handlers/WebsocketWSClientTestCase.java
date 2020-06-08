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
 */

package org.wso2.carbon.apimgt.gateway.handlers;

import org.apache.axis2.client.ServiceClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.keymgt.stub.validator.APIKeyValidationServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

import static org.junit.Assert.fail;

/**
 * Test class for WebsocketWSClient
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({CarbonUtils.class, ServiceReferenceHolder.class})
public class WebsocketWSClientTestCase {
    private APIManagerConfiguration apiManagerConfiguration;

    @Before
    public void setup() {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL))
                .thenReturn("http://localhost:18083");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME))
                .thenReturn("username");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD))
                .thenReturn("abc123");
    }

    /*
    * Test for APISecurityException when Error while accessing backend services
    * */
    @Test
    public void testGetAPIKeyDataAPISecurityException() {
        try {
            WebsocketWSClient websocketWSClient = new WebsocketWSClient();
            APIKeyValidationInfoDTO apiKeyValidationInfoDTOActual = websocketWSClient.getAPIKeyData("/ishara",
                    "1.0", "PhoneVerify","carbon.super");
        } catch (APISecurityException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error while accessing backend services for API key validation"));
        }
    }

    /*
    * Test APISecurityException when Required connection details for the key management server not provided
    * */
    @Test
    public void testGetAPIKeyDataAPISecurityException1() {
        try {
            // serviceURL = null
            Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL))
                    .thenReturn(null);
            WebsocketWSClient websocketWSClient = new WebsocketWSClient();
            fail("Expected APISecurityConstants.API_AUTH_GENERAL_ERROR is not thrown when serviceURL = null.");

            // username = null
            Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL))
                    .thenReturn("http://localhost:18083");
            Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).thenReturn(null);
            WebsocketWSClient websocketWSClient1 = new WebsocketWSClient();
            fail("Expected APISecurityConstants.API_AUTH_GENERAL_ERROR is not thrown when username = null");

            // password = null
            Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME))
                    .thenReturn("username");
            Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD))
                    .thenReturn("abc123");
            fail("Expected APISecurityConstants.API_AUTH_GENERAL_ERROR is not thrown when password = null");

        } catch (APISecurityException e) {
            Assert.assertTrue(e.getMessage().startsWith("Required connection details for the key management server not" +
                    " provided"));
        }
    }


    @Test
    public void testGetAPIKeyData() throws Exception {
        WebsocketWSClient websocketWSClient = new WebsocketWSClient();
        ServiceClient serviceClient = Mockito.mock(ServiceClient.class);
        PowerMockito.mockStatic(CarbonUtils.class);
        org.wso2.carbon.apimgt.impl.dto.xsd.APIKeyValidationInfoDTO apiKeyValidationInfoDTO1 =
                Mockito.mock(org.wso2.carbon.apimgt.impl.dto.xsd.APIKeyValidationInfoDTO.class);
        Mockito.when(apiKeyValidationInfoDTO1.getThrottlingDataList()).thenReturn(new String[]{""});
        Mockito.when(apiKeyValidationInfoDTO1.getAuthorized()).thenReturn(true);
        APIKeyValidationServiceStub apiKeyValidationServiceStub = Mockito.mock(APIKeyValidationServiceStub.class);
        Mockito.when(apiKeyValidationServiceStub._getServiceClient()).thenReturn(serviceClient);
        PowerMockito.doNothing().when(CarbonUtils.class, "setBasicAccessSecurityHeaders", "", "",
                serviceClient);

        websocketWSClient.setKeyValidationServiceStub(apiKeyValidationServiceStub);
        Mockito.when(apiKeyValidationServiceStub.validateKeyforHandshake("/ishara", "1.0",
                "PhoneVerify","carbon.super",new String[]{"all"})).thenReturn(apiKeyValidationInfoDTO1);
        APIKeyValidationInfoDTO apiKeyValidationInfoDTOActual = websocketWSClient.getAPIKeyData("/ishara", "1.0",
                "PhoneVerify","carbon.super");
        Assert.assertTrue(apiKeyValidationInfoDTOActual.isAuthorized());

        //when scopes are set
        Mockito.when(apiKeyValidationInfoDTO1.getScopes()).thenReturn(new String[]{"scope1", "scope2"});
        APIKeyValidationInfoDTO apiKeyValidationInfoDTOActual1 =
                websocketWSClient.getAPIKeyData("/ishara", "1.0", "PhoneVerify","carbon.super");
        Assert.assertTrue(apiKeyValidationInfoDTOActual1.isAuthorized());
    }
}
