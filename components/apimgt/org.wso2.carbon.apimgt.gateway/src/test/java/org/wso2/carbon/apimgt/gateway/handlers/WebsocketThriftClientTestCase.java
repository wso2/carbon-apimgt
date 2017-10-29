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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
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

import static junit.framework.Assert.fail;

/**
 * Test class for WebsocketThriftClient
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({WebsocketThriftClient.class, ServiceReferenceHolder.class, CarbonUtils.class})
public class WebsocketThriftClientTestCase {

    @Before
    public void setup() {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL))
                .thenReturn("http://localhost:8083");
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
            try {
                ConfigurationContext ctx = ConfigurationContextFactory
                        .createConfigurationContextFromFileSystem(null, null);
                APIKeyValidationInfoDTO apiKeyValidationInfoDTOActual = websocketWSClient.getAPIKeyData("/ishara", "1.0", "PhoneVerify");
            } catch (AxisFault axisFault) {
                fail("AxisFault is thrown when creating ConfigurationContext " + axisFault.getMessage());
            }
        } catch (APISecurityException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error while accessing backend services for API key validation"));
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
        PowerMockito.doNothing().when(CarbonUtils.class, "setBasicAccessSecurityHeaders", "", "", serviceClient);

        websocketWSClient.setKeyValidationServiceStub(apiKeyValidationServiceStub);
        Mockito.when(apiKeyValidationServiceStub.validateKeyforHandshake("/ishara", "1.0",
                "PhoneVerify")).thenReturn(apiKeyValidationInfoDTO1);
        ConfigurationContext ctx = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        APIKeyValidationInfoDTO apiKeyValidationInfoDTOActual = websocketWSClient.getAPIKeyData("/ishara", "1.0", "PhoneVerify");
        Assert.assertTrue(apiKeyValidationInfoDTOActual.isAuthorized());
    }
}
