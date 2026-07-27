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
import org.wso2.carbon.apimgt.api.AIServiceConfiguration;
import org.wso2.carbon.apimgt.api.APIChatAssistant;
import org.wso2.carbon.apimgt.api.APIChatRequest;
import org.wso2.carbon.apimgt.api.APIChatResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.DesignAssistant;
import org.wso2.carbon.apimgt.api.DesignAssistantRequest;
import org.wso2.carbon.apimgt.api.DesignAssistantResponse;
import org.wso2.carbon.apimgt.api.MarketplaceAssistant;
import org.wso2.carbon.apimgt.api.MarketplaceAssistantRequest;
import org.wso2.carbon.apimgt.api.MarketplaceAssistantResponse;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.ai.ApiChatConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dto.ai.DesignAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dto.ai.MarketplaceAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

/**
 * Tests {@link AIServiceFactory}'s resolution of the configured {@link DesignAssistant}, {@link MarketplaceAssistant}
 * and {@link APIChatAssistant} implementations: default fallback, valid custom class, wrong-type / non-instantiable
 * rejection, injection of the resolved configuration, and instance caching.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class})
public class AIServiceFactoryTest {

    private APIManagerConfigurationService configurationService;
    private APIManagerConfiguration configuration;
    private DesignAssistantConfigurationDTO designConfigDto;
    private MarketplaceAssistantConfigurationDTO marketplaceConfigDto;
    private ApiChatConfigurationDTO apiChatConfigDto;

    @Before
    public void setUp() {
        // The factory caches each resolved instance in a static field; clear them so every test resolves afresh.
        Whitebox.setInternalState(AIServiceFactory.class, "designAssistant", (DesignAssistant) null);
        Whitebox.setInternalState(AIServiceFactory.class, "marketplaceAssistant", (MarketplaceAssistant) null);
        Whitebox.setInternalState(AIServiceFactory.class, "apiChatAssistant", (APIChatAssistant) null);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        configurationService = Mockito.mock(APIManagerConfigurationService.class);
        configuration = Mockito.mock(APIManagerConfiguration.class);
        designConfigDto = new DesignAssistantConfigurationDTO();
        marketplaceConfigDto = new MarketplaceAssistantConfigurationDTO();
        apiChatConfigDto = new ApiChatConfigurationDTO();
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(configurationService);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);
        Mockito.when(configuration.getDesignAssistantConfigurationDto()).thenReturn(designConfigDto);
        Mockito.when(configuration.getMarketplaceAssistantConfigurationDto()).thenReturn(marketplaceConfigDto);
        Mockito.when(configuration.getApiChatConfigurationDto()).thenReturn(apiChatConfigDto);
    }

    // ---- Design Assistant -------------------------------------------------------------------------------------

    @Test
    public void testReturnsDefaultDesignAssistantWhenNoImplClassConfigured() throws APIManagementException {
        DesignAssistant service = AIServiceFactory.getDesignAssistantService();
        Assert.assertTrue("Expected the default Design Assistant implementation",
                service instanceof DefaultDesignAssistantServiceImpl);
    }

    @Test
    public void testReturnsDefaultDesignAssistantWhenConfigurationNotInitialized() throws APIManagementException {
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(null);
        DesignAssistant service = AIServiceFactory.getDesignAssistantService();
        Assert.assertTrue("Expected the default Design Assistant implementation when configuration is null",
                service instanceof DefaultDesignAssistantServiceImpl);
    }

    @Test
    public void testReturnsDefaultDesignAssistantWhenImplClassBlank() throws APIManagementException {
        designConfigDto.setImplementationClass("   ");
        DesignAssistant service = AIServiceFactory.getDesignAssistantService();
        Assert.assertTrue("Expected the default Design Assistant implementation when impl class is blank",
                service instanceof DefaultDesignAssistantServiceImpl);
    }

    @Test
    public void testReturnsConfiguredCustomDesignAssistant() throws APIManagementException {
        designConfigDto.setImplementationClass(ValidTestDesignAssistant.class.getName());
        DesignAssistant service = AIServiceFactory.getDesignAssistantService();
        Assert.assertTrue("Expected the configured custom Design Assistant implementation",
                service instanceof ValidTestDesignAssistant);
    }

    @Test
    public void testInjectsConfigurationIntoDesignAssistant() throws APIManagementException {
        designConfigDto.setImplementationClass(ValidTestDesignAssistant.class.getName());
        designConfigDto.setEndpoint("https://ai.example.com");
        DesignAssistant service = AIServiceFactory.getDesignAssistantService();
        AIServiceConfiguration injected = ((ValidTestDesignAssistant) service).injectedConfiguration;
        Assert.assertNotNull("The factory must push the resolved configuration into the implementation", injected);
        Assert.assertEquals("https://ai.example.com", injected.getEndpoint());
    }

    @Test
    public void testThrowsWhenDesignAssistantClassDoesNotImplementSpi() {
        designConfigDto.setImplementationClass(Object.class.getName());
        try {
            AIServiceFactory.getDesignAssistantService();
            Assert.fail("Expected APIManagementException for a class that does not implement DesignAssistant");
        } catch (APIManagementException e) {
            Assert.assertTrue("Message should explain the type mismatch, but was: " + e.getMessage(),
                    e.getMessage().contains("does not implement"));
        }
    }

    @Test
    public void testThrowsWhenDesignAssistantClassCannotBeInstantiated() {
        designConfigDto.setImplementationClass("org.wso2.carbon.apimgt.impl.ai.NonExistentDesignAssistantImpl");
        try {
            AIServiceFactory.getDesignAssistantService();
            Assert.fail("Expected APIManagementException for a non-instantiable / missing class");
        } catch (APIManagementException e) {
            Assert.assertTrue("Message should indicate an instantiation error, but was: " + e.getMessage(),
                    e.getMessage().contains("Error while instantiating"));
        }
    }

    @Test
    public void testCachesResolvedDesignAssistant() throws APIManagementException {
        DesignAssistant first = AIServiceFactory.getDesignAssistantService();
        DesignAssistant second = AIServiceFactory.getDesignAssistantService();
        Assert.assertSame("The factory should cache and return the same instance", first, second);
    }

    // ---- Marketplace Assistant --------------------------------------------------------------------------------

    @Test
    public void testReturnsDefaultMarketplaceAssistantWhenNoImplClassConfigured() throws APIManagementException {
        MarketplaceAssistant service = AIServiceFactory.getMarketplaceAssistantService();
        Assert.assertTrue("Expected the default Marketplace Assistant implementation",
                service instanceof DefaultMarketplaceAssistantServiceImpl);
    }

    @Test
    public void testReturnsConfiguredCustomMarketplaceAssistant() throws APIManagementException {
        marketplaceConfigDto.setImplementationClass(ValidTestMarketplaceAssistant.class.getName());
        MarketplaceAssistant service = AIServiceFactory.getMarketplaceAssistantService();
        Assert.assertTrue("Expected the configured custom Marketplace Assistant implementation",
                service instanceof ValidTestMarketplaceAssistant);
    }

    @Test
    public void testThrowsWhenMarketplaceAssistantClassDoesNotImplementSpi() {
        marketplaceConfigDto.setImplementationClass(Object.class.getName());
        try {
            AIServiceFactory.getMarketplaceAssistantService();
            Assert.fail("Expected APIManagementException for a class that does not implement MarketplaceAssistant");
        } catch (APIManagementException e) {
            Assert.assertTrue("Message should explain the type mismatch, but was: " + e.getMessage(),
                    e.getMessage().contains("does not implement"));
        }
    }

    @Test
    public void testCachesResolvedMarketplaceAssistant() throws APIManagementException {
        MarketplaceAssistant first = AIServiceFactory.getMarketplaceAssistantService();
        MarketplaceAssistant second = AIServiceFactory.getMarketplaceAssistantService();
        Assert.assertSame("The factory should cache and return the same instance", first, second);
    }

    // ---- API Chat -------------------------------------------------------------------------------------------

    @Test
    public void testReturnsDefaultAPIChatAssistantWhenNoImplClassConfigured() throws APIManagementException {
        APIChatAssistant service = AIServiceFactory.getAPIChatService();
        Assert.assertTrue("Expected the default API Chat implementation",
                service instanceof DefaultAPIChatAssistantServiceImpl);
    }

    @Test
    public void testReturnsConfiguredCustomAPIChatAssistant() throws APIManagementException {
        apiChatConfigDto.setImplementationClass(ValidTestAPIChatAssistant.class.getName());
        APIChatAssistant service = AIServiceFactory.getAPIChatService();
        Assert.assertTrue("Expected the configured custom API Chat implementation",
                service instanceof ValidTestAPIChatAssistant);
    }

    @Test
    public void testThrowsWhenAPIChatAssistantClassDoesNotImplementSpi() {
        apiChatConfigDto.setImplementationClass(Object.class.getName());
        try {
            AIServiceFactory.getAPIChatService();
            Assert.fail("Expected APIManagementException for a class that does not implement APIChatAssistant");
        } catch (APIManagementException e) {
            Assert.assertTrue("Message should explain the type mismatch, but was: " + e.getMessage(),
                    e.getMessage().contains("does not implement"));
        }
    }

    @Test
    public void testCachesResolvedAPIChatAssistant() throws APIManagementException {
        APIChatAssistant first = AIServiceFactory.getAPIChatService();
        APIChatAssistant second = AIServiceFactory.getAPIChatService();
        Assert.assertSame("The factory should cache and return the same instance", first, second);
    }

    // ---- Test doubles ---------------------------------------------------------------------------------------

    /**
     * Minimal valid {@link DesignAssistant} with a public no-arg constructor that captures the configuration it was
     * initialized with, used to verify custom-class resolution and configuration injection.
     */
    public static class ValidTestDesignAssistant implements DesignAssistant {

        private AIServiceConfiguration injectedConfiguration;

        @Override
        public void init(AIServiceConfiguration configuration) {
            this.injectedConfiguration = configuration;
        }

        @Override
        public DesignAssistantResponse generatePayload(DesignAssistantRequest request) {
            return null;
        }

        @Override
        public DesignAssistantResponse chat(DesignAssistantRequest request) {
            return null;
        }
    }

    /**
     * Minimal valid {@link MarketplaceAssistant} with a public no-arg constructor, used to verify custom-class
     * resolution.
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

    /**
     * Minimal valid {@link APIChatAssistant} with a public no-arg constructor, used to verify custom-class
     * resolution.
     */
    public static class ValidTestAPIChatAssistant implements APIChatAssistant {

        @Override
        public APIChatResponse prepare(APIChatRequest request) {
            return null;
        }

        @Override
        public APIChatResponse execute(APIChatRequest request) {
            return null;
        }
    }
}
