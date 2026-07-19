/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.ai;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.MarketplaceAssistantRequest;
import org.wso2.carbon.apimgt.api.MarketplaceAssistantResponse;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.ai.MarketplaceAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

/**
 * Tests the backward-compatibility guard of {@link DefaultMarketplaceAssistantServiceImpl}: when no AI credentials
 * are configured (neither key nor auth token), every operation short-circuits without invoking the AI service -
 * read operations return {@code null} (so the REST layer produces an empty response) and the async
 * publish/delete operations are no-ops.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, APIUtil.class})
public class DefaultMarketplaceAssistantServiceImplTest {

    private APIManagerConfigurationService configurationService;
    private DefaultMarketplaceAssistantServiceImpl service;

    @Before
    public void setUp() {
        service = new DefaultMarketplaceAssistantServiceImpl();

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        configurationService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        // A fresh DTO has neither key nor auth token provided (both flags default to false).
        MarketplaceAssistantConfigurationDTO configDto = new MarketplaceAssistantConfigurationDTO();
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(configurationService);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);
        Mockito.when(configuration.getMarketplaceAssistantConfigurationDto()).thenReturn(configDto);
    }

    @Test
    public void testExecuteReturnsNullWhenCredentialsAbsent() throws APIManagementException {
        MarketplaceAssistantResponse response = service.execute(new MarketplaceAssistantRequest());
        Assert.assertNull("execute() must return null when no AI credentials are configured", response);
    }

    @Test
    public void testGetApiCountReturnsNullWhenCredentialsAbsent() throws APIManagementException {
        MarketplaceAssistantResponse response = service.getApiCount(new MarketplaceAssistantRequest());
        Assert.assertNull("getApiCount() must return null when no AI credentials are configured", response);
    }

    @Test
    public void testExecuteReturnsNullWhenConfigurationNotInitialized() throws APIManagementException {
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(null);
        MarketplaceAssistantResponse response = service.execute(new MarketplaceAssistantRequest());
        Assert.assertNull("execute() must return null when configuration is not initialized", response);
    }

    @Test
    public void testPublishApiIsNoOpWhenCredentialsAbsent() throws APIManagementException {
        // A publishable REST API is supplied so that, if the credential guard were removed, publishAPI() would
        // reach the AI invocation - making the "never invoked" assertion meaningful rather than trivially true.
        MarketplaceAssistantRequest request = new MarketplaceAssistantRequest();
        API api = Mockito.mock(API.class);
        Mockito.when(api.getType()).thenReturn(APIConstants.API_TYPE_HTTP);
        Mockito.when(api.getId()).thenReturn(Mockito.mock(APIIdentifier.class));
        request.setApi(api);

        service.publishAPI(request);

        PowerMockito.verifyStatic(APIUtil.class, Mockito.never());
        APIUtil.invokeAIService(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testDeleteApiIsNoOpWhenCredentialsAbsent() throws APIManagementException {
        MarketplaceAssistantRequest request = new MarketplaceAssistantRequest();
        request.setUuid("some-uuid");

        service.deleteAPI(request);

        PowerMockito.verifyStatic(APIUtil.class, Mockito.never());
        APIUtil.marketplaceAssistantDeleteService(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString());
    }
}
