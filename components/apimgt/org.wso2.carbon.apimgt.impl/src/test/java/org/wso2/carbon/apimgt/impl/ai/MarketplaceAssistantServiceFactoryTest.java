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
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.MarketplaceAssistant;
import org.wso2.carbon.apimgt.api.MarketplaceAssistantRequest;
import org.wso2.carbon.apimgt.api.MarketplaceAssistantResponse;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.ai.MarketplaceAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

/**
 * Tests {@link MarketplaceAssistantServiceFactory}'s resolution of the configured {@link MarketplaceAssistant}
 * implementation: default fallback, valid custom class, wrong-type / non-instantiable rejection, and caching.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class})
public class MarketplaceAssistantServiceFactoryTest {

    private APIManagerConfigurationService configurationService;
    private MarketplaceAssistantConfigurationDTO configDto;

    @Before
    public void setUp() {
        // The factory caches the resolved instance in a static field; clear it so every test resolves afresh.
        Whitebox.setInternalState(MarketplaceAssistantServiceFactory.class, "marketplaceAssistant",
                (MarketplaceAssistant) null);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        configurationService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        configDto = new MarketplaceAssistantConfigurationDTO();
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(configurationService);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);
        Mockito.when(configuration.getMarketplaceAssistantConfigurationDto()).thenReturn(configDto);
    }

    @Test
    public void testReturnsDefaultImplWhenNoImplClassConfigured() throws APIManagementException {
        MarketplaceAssistant service = MarketplaceAssistantServiceFactory.getMarketplaceAssistantService();
        Assert.assertTrue("Expected the default Marketplace Assistant implementation",
                service instanceof DefaultMarketplaceAssistantServiceImpl);
    }

    @Test
    public void testReturnsDefaultImplWhenConfigurationNotInitialized() throws APIManagementException {
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(null);
        MarketplaceAssistant service = MarketplaceAssistantServiceFactory.getMarketplaceAssistantService();
        Assert.assertTrue("Expected the default Marketplace Assistant implementation when configuration is null",
                service instanceof DefaultMarketplaceAssistantServiceImpl);
    }

    @Test
    public void testReturnsDefaultImplWhenImplClassBlank() throws APIManagementException {
        configDto.setImplementationClass("   ");
        MarketplaceAssistant service = MarketplaceAssistantServiceFactory.getMarketplaceAssistantService();
        Assert.assertTrue("Expected the default Marketplace Assistant implementation when impl class is blank",
                service instanceof DefaultMarketplaceAssistantServiceImpl);
    }

    @Test
    public void testReturnsConfiguredCustomImpl() throws APIManagementException {
        configDto.setImplementationClass(ValidTestMarketplaceAssistant.class.getName());
        MarketplaceAssistant service = MarketplaceAssistantServiceFactory.getMarketplaceAssistantService();
        Assert.assertTrue("Expected the configured custom Marketplace Assistant implementation",
                service instanceof ValidTestMarketplaceAssistant);
        Assert.assertEquals(ValidTestMarketplaceAssistant.class.getName(), service.getClass().getName());
    }

    @Test
    public void testThrowsWhenConfiguredClassDoesNotImplementSpi() {
        configDto.setImplementationClass(Object.class.getName());
        try {
            MarketplaceAssistantServiceFactory.getMarketplaceAssistantService();
            Assert.fail("Expected APIManagementException for a class that does not implement MarketplaceAssistant");
        } catch (APIManagementException e) {
            Assert.assertTrue("Message should explain the type mismatch, but was: " + e.getMessage(),
                    e.getMessage().contains("does not implement"));
        }
    }

    @Test
    public void testThrowsWhenConfiguredClassCannotBeInstantiated() {
        configDto.setImplementationClass("org.wso2.carbon.apimgt.impl.ai.NonExistentMarketplaceAssistantImpl");
        try {
            MarketplaceAssistantServiceFactory.getMarketplaceAssistantService();
            Assert.fail("Expected APIManagementException for a non-instantiable / missing class");
        } catch (APIManagementException e) {
            Assert.assertTrue("Message should indicate an instantiation error, but was: " + e.getMessage(),
                    e.getMessage().contains("Error while instantiating"));
        }
    }

    @Test
    public void testCachesResolvedInstance() throws APIManagementException {
        MarketplaceAssistant first = MarketplaceAssistantServiceFactory.getMarketplaceAssistantService();
        MarketplaceAssistant second = MarketplaceAssistantServiceFactory.getMarketplaceAssistantService();
        Assert.assertSame("The factory should cache and return the same instance", first, second);
    }

    /**
     * Minimal valid {@link MarketplaceAssistant} with a public no-arg constructor, used to verify custom-class
     * resolution through {@code Class.forName(...)}.
     */
    public static class ValidTestMarketplaceAssistant implements MarketplaceAssistant {

        @Override
        public MarketplaceAssistantResponse execute(MarketplaceAssistantRequest request) {
            return null;
        }

        @Override
        public MarketplaceAssistantResponse getApiCount(MarketplaceAssistantRequest request) {
            return null;
        }

        @Override
        public void publishAPI(MarketplaceAssistantRequest request) {
            // no-op test double
        }

        @Override
        public void deleteAPI(MarketplaceAssistantRequest request) {
            // no-op test double
        }
    }
}
