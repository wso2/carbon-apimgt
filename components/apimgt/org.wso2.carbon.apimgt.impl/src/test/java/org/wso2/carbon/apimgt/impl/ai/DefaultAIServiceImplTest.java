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
import org.wso2.carbon.apimgt.api.APIChatRequest;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.DesignAssistantRequest;
import org.wso2.carbon.apimgt.api.MarketplaceAssistantRequest;

/**
 * Tests the default behaviour inherited from {@link AbstractAIService} through {@link DefaultAIServiceImpl}: it reads
 * its per-capability settings from the injected {@link AIServiceConfiguration}, and when a capability has no
 * credentials (neither key nor auth token) that capability fails fast with an {@link APIManagementException} before
 * invoking the AI service. An instance that was never initialized (no {@code init()} call) fails the same way.
 */
public class DefaultAIServiceImplTest {

    private DefaultAIServiceImpl service;

    @Before
    public void setUp() {
        service = new DefaultAIServiceImpl();
        // A fresh configuration has empty sections, so no capability has key or auth token provided.
        service.init(new AIServiceConfiguration());
    }

    @Test(expected = APIManagementException.class)
    public void testDesignChatFailsWhenCredentialsAbsent() throws APIManagementException {
        service.chat(new DesignAssistantRequest());
    }

    @Test(expected = APIManagementException.class)
    public void testMarketplaceExecuteFailsWhenCredentialsAbsent() throws APIManagementException {
        service.execute(new MarketplaceAssistantRequest());
    }

    @Test(expected = APIManagementException.class)
    public void testMarketplacePublishFailsWhenCredentialsAbsent() throws APIManagementException {
        service.publishAPI(new MarketplaceAssistantRequest());
    }

    @Test(expected = APIManagementException.class)
    public void testApiChatExecuteFailsWhenCredentialsAbsent() throws APIManagementException {
        service.execute(new APIChatRequest());
    }

    @Test(expected = APIManagementException.class)
    public void testFailsWhenConfigurationNotInjected() throws APIManagementException {
        // An instance whose init() was never called has no configuration to work with.
        DefaultAIServiceImpl uninitialized = new DefaultAIServiceImpl();
        uninitialized.execute(new MarketplaceAssistantRequest());
    }
}
