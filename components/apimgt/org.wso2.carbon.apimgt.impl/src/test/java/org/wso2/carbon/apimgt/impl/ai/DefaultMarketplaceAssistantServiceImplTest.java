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

import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.AIServiceConfiguration;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.MarketplaceAssistantRequest;

/**
 * Tests the credential guard of {@link DefaultMarketplaceAssistantServiceImpl}: when the injected
 * {@link AIServiceConfiguration} carries no credentials (neither key nor auth token), every operation fails fast
 * with an {@link APIManagementException} before invoking the AI service. The resolved configuration is supplied
 * through {@link DefaultMarketplaceAssistantServiceImpl#init(AIServiceConfiguration)} - the implementation no longer
 * reaches into the API Manager configuration itself.
 */
public class DefaultMarketplaceAssistantServiceImplTest {

    private DefaultMarketplaceAssistantServiceImpl service;

    @Before
    public void setUp() {
        service = new DefaultMarketplaceAssistantServiceImpl();
        // A fresh configuration has neither key nor auth token provided (both flags default to false).
        service.init(new AIServiceConfiguration());
    }

    @Test(expected = APIManagementException.class)
    public void testExecuteFailsWhenCredentialsAbsent() throws APIManagementException {
        service.execute(new MarketplaceAssistantRequest());
    }

    @Test(expected = APIManagementException.class)
    public void testGetApiCountFailsWhenCredentialsAbsent() throws APIManagementException {
        service.getApiCount(new MarketplaceAssistantRequest());
    }

    @Test(expected = APIManagementException.class)
    public void testPublishApiFailsWhenCredentialsAbsent() throws APIManagementException {
        // publishAPI() validates the configuration up-front (before touching the request), so it fails fast when
        // no credentials are configured and never reaches the AI invocation.
        service.publishAPI(new MarketplaceAssistantRequest());
    }

    @Test(expected = APIManagementException.class)
    public void testDeleteApiFailsWhenCredentialsAbsent() throws APIManagementException {
        MarketplaceAssistantRequest request = new MarketplaceAssistantRequest();
        request.setUuid("some-uuid");
        service.deleteAPI(request);
    }

    @Test(expected = APIManagementException.class)
    public void testExecuteFailsWhenConfigurationNotInjected() throws APIManagementException {
        // An implementation that was never initialized (no init() call) has no configuration to work with.
        DefaultMarketplaceAssistantServiceImpl uninitialized = new DefaultMarketplaceAssistantServiceImpl();
        uninitialized.execute(new MarketplaceAssistantRequest());
    }
}
