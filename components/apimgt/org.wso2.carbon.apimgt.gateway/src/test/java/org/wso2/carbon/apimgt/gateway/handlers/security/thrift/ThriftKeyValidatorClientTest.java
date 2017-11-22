/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.impl.generated.thrift.APIKeyMgtException;
import org.wso2.carbon.apimgt.impl.generated.thrift.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.generated.thrift.APIKeyValidationService;
import org.wso2.carbon.apimgt.impl.generated.thrift.APIManagementException;
import org.wso2.carbon.apimgt.impl.generated.thrift.ConditionDTO;
import org.wso2.carbon.apimgt.impl.generated.thrift.ConditionGroupDTO;
import org.wso2.carbon.apimgt.impl.generated.thrift.URITemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * ThriftKeyValidatorClient test cases
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ThriftUtils.class, ThriftKeyValidatorClient.class, TSSLTransportFactory.class})
public class ThriftKeyValidatorClientTest {

    private ThriftUtils thriftUtils;
    private TSSLTransportFactory.TSSLTransportParameters tsslTransportParameters;
    private APIKeyValidationService.Client keyValClient;
    private TSocket tTransport;
    private String context = "/weatherAPI";
    private String apiVersion = "v1";
    private String apiKey = "12345";
    private String requiredAuthenticationLevel = "ApplicationAndUser";
    private String clientDomain = "carbon.super";
    private String matchingResource = "/temperature";
    private String httpVerb = "GET";
    private APIKeyValidationInfoDTO thriftDTO;

    @Before
    public void init() throws Exception {
        PowerMockito.mockStatic(ThriftUtils.class);
        PowerMockito.mockStatic(TSSLTransportFactory.class);

        tTransport = Mockito.mock(TSocket.class);
        thriftUtils = Mockito.mock(ThriftUtils.class);
        tsslTransportParameters = Mockito.mock(TSSLTransportFactory.TSSLTransportParameters.class);
        keyValClient = Mockito.mock(APIKeyValidationService.Client.class);
        PowerMockito.when(ThriftUtils.getInstance()).thenReturn(thriftUtils);
        PowerMockito.whenNew(TSSLTransportFactory.TSSLTransportParameters.class).withAnyArguments().thenReturn
                (tsslTransportParameters);
        PowerMockito.whenNew(APIKeyValidationService.Client.class).withAnyArguments().thenReturn(keyValClient);
        PowerMockito.doNothing().when(tsslTransportParameters).setTrustStore(Mockito.anyString(), Mockito.anyString());
        thriftDTO = new APIKeyValidationInfoDTO();
        thriftDTO.setSubscriberTenantDomain(clientDomain);
        thriftDTO.setEndUserToken(apiKey);
        thriftDTO.setApplicationName("testApp");
        thriftDTO.setAuthorized(true);
        thriftDTO.setEndUserName("admin");
        thriftDTO.setSubscriber("testSubscriber");
        thriftDTO.setTier("Unlimited");
        thriftDTO.setType("OAuth");
        thriftDTO.setValidationStatus(0);
        thriftDTO.setApplicationId("1");
        thriftDTO.setApplicationTier("Unlimited");
        thriftDTO.setApiName("WeatherAPI");
        thriftDTO.setApiPublisher("testPublisher");
        thriftDTO.setConsumerKey("BwvfDT1KSPxEeLR8SjWL7jNnp8ca");
        thriftDTO.setScopes(new HashSet<String>());
        thriftDTO.setIssuedTime(1507789156);
        thriftDTO.setApiTier("Unlimited");
        thriftDTO.setValidityPeriod(3600);
        thriftDTO.setThrottlingDataList(new ArrayList<String>());
        thriftDTO.setSpikeArrestLimit(20);
        thriftDTO.setSpikeArrestUnit("s");
        thriftDTO.setStopOnQuotaReach(false);
        thriftDTO.setIsContentAware(false);
    }

    @Test
    public void testThriftKeyValidatorClientInitFailure() throws TTransportException {

        String errorMessage = "Error occurred while establishing ssl connection";
        PowerMockito.when(TSSLTransportFactory.getClientSocket(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                (TSSLTransportFactory.TSSLTransportParameters) Mockito.anyObject())).thenThrow(new
                TTransportException(errorMessage));
        try {
            ThriftKeyValidatorClient thriftKeyValidatorClient = new ThriftKeyValidatorClient();
            Assert.fail("Expected APISecurityException is not thrown during ThriftKeyValidatorClient initialisation");
        } catch (APISecurityException e) {
            Assert.assertTrue(e.getMessage().contains(errorMessage));
        }
    }

    @Test
    public void testValidatingAPIKeyData() throws TException, APIKeyMgtException, APIManagementException {

        PowerMockito.when(TSSLTransportFactory.getClientSocket(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt
                (), (TSSLTransportFactory.TSSLTransportParameters) Mockito.anyObject())).thenReturn(tTransport);
        PowerMockito.when(keyValClient.validateKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()
        )).thenReturn(thriftDTO);
        try {
            ThriftKeyValidatorClient thriftKeyValidatorClient = new ThriftKeyValidatorClient();
            org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO apiKeyValidationInfoDTO =
                    thriftKeyValidatorClient.getAPIKeyData(context, apiVersion, apiKey, requiredAuthenticationLevel,
                            clientDomain, matchingResource, httpVerb);
            Assert.assertNotNull(thriftKeyValidatorClient);
        } catch (APISecurityException e) {
            Assert.fail("Unexpected APISecurityException is thrown while validating API key data");
        }
    }

    @Test
    public void testAPIKeyDataValidationFailure() throws TException,
            APIKeyMgtException, APIManagementException {

        String errorMessage = "Error occurred while validation API key data";
        PowerMockito.when(TSSLTransportFactory.getClientSocket(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt
                (), (TSSLTransportFactory.TSSLTransportParameters) Mockito.anyObject())).thenReturn(tTransport);
        PowerMockito.when(keyValClient.validateKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()
        )).thenThrow(new APIKeyMgtException(errorMessage));
        try {
            ThriftKeyValidatorClient thriftKeyValidatorClient = new ThriftKeyValidatorClient();
            org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO apiKeyValidationInfoDTO =
                    thriftKeyValidatorClient.getAPIKeyData(context, apiVersion, apiKey, requiredAuthenticationLevel,
                            clientDomain, matchingResource, httpVerb);
            Assert.fail("Expected APISecurityException is not thrown while validating API key data");
        } catch (APISecurityException e) {
            Assert.assertTrue(e.getMessage().contains(errorMessage));
        }
    }

    @Test
    public void testRetrievingAllURITemplates() throws TException, APIKeyMgtException, APIManagementException {

        ConditionDTO conditionDTO = new ConditionDTO();
        conditionDTO.setConditionType("IPSpecific");
        conditionDTO.setConditionName("IP");
        conditionDTO.setConditionValue("127.0.0.1");
        List<ConditionDTO> conditionDTOS = new ArrayList<>();
        conditionDTOS.add(conditionDTO);
        ConditionGroupDTO conditionGroup = new ConditionGroupDTO();
        conditionGroup.setConditionGroupId("thrift");
        conditionGroup.setConditions(conditionDTOS);
        List<ConditionGroupDTO> conditionGroupDTOS = new ArrayList<>();
        conditionGroupDTOS.add(conditionGroup);

        org.wso2.carbon.apimgt.impl.generated.thrift.URITemplate uriTemplate = new URITemplate();
        uriTemplate.setAuthType("Application");
        uriTemplate.setHttpVerb("GET");
        uriTemplate.setResourceSandboxURI("http://foo");
        uriTemplate.setUriTemplate("/temperature");
        uriTemplate.setThrottlingTier("Unlimited");
        uriTemplate.setConditionGroups(conditionGroupDTOS);


        List<org.wso2.carbon.apimgt.impl.generated.thrift.URITemplate> uriTemplates = new ArrayList<>();
        uriTemplates.add(uriTemplate);


        PowerMockito.when(TSSLTransportFactory.getClientSocket(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt
                (), (TSSLTransportFactory.TSSLTransportParameters) Mockito.anyObject())).thenReturn(tTransport);
        PowerMockito.when(keyValClient.getAllURITemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyString
                ())).thenReturn(uriTemplates);
        try {
            ThriftKeyValidatorClient thriftKeyValidatorClient = new ThriftKeyValidatorClient();
            ArrayList<org.wso2.carbon.apimgt.api.model.URITemplate> retrievedURITemplates = thriftKeyValidatorClient
                    .getAllURITemplates(context, apiVersion);
        } catch (APISecurityException e) {
            Assert.fail("Unxpected APISecurityException is thrown while retrieving URI templates");

        }
    }

    @Test
    public void testRetrievingAllURITemplatesFailure() throws TException, APIKeyMgtException, APIManagementException {
        String errorMessage = "Error occurred while retrieving URITemplates";

        PowerMockito.when(TSSLTransportFactory.getClientSocket(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt
                (), (TSSLTransportFactory.TSSLTransportParameters) Mockito.anyObject())).thenReturn(tTransport);
        PowerMockito.when(keyValClient.getAllURITemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyString
                ())).thenThrow(new APIKeyMgtException(errorMessage));
        try {
            ThriftKeyValidatorClient thriftKeyValidatorClient = new ThriftKeyValidatorClient();
            thriftKeyValidatorClient.getAllURITemplates(context, apiVersion);
            Assert.fail("Expected APISecurityException is not thrown while retrieving URI templates");
        } catch (APISecurityException e) {
            Assert.assertTrue(e.getMessage().contains(errorMessage));
        }
    }


}
