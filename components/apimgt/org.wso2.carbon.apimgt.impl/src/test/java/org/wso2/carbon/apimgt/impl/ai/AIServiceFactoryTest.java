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
import org.wso2.carbon.apimgt.api.AIService;
import org.wso2.carbon.apimgt.api.AIServiceConfiguration;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.ai.ApiChatConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dto.ai.DesignAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dto.ai.MarketplaceAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

/**
 * Tests {@link AIServiceFactory}'s resolution of the single configured {@link AIService} implementation that serves
 * every AI capability: default fallback, valid custom class, wrong-type / non-instantiable rejection, injection of
 * the resolved configuration, and caching.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class})
public class AIServiceFactoryTest {

    private APIManagerConfigurationService configurationService;
    private APIManagerConfiguration configuration;
    private DesignAssistantConfigurationDTO designConfigDto;

    @Before
    public void setUp() {
        // The factory caches the resolved instance in a static field; clear it so every test resolves afresh.
        Whitebox.setInternalState(AIServiceFactory.class, "aiService", (AIService) null);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        configurationService = Mockito.mock(APIManagerConfigurationService.class);
        configuration = Mockito.mock(APIManagerConfiguration.class);
        designConfigDto = new DesignAssistantConfigurationDTO();
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(configurationService);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);
        // By default no custom class is configured (the field defaults to the shipped implementation class name).
        Mockito.when(configuration.getAIServiceImplementationClass())
                .thenReturn(APIConstants.AI.AI_SERVICE_DEFAULT_IMPL);
        // The factory reads the per-capability DTOs to build the injected configuration.
        Mockito.when(configuration.getDesignAssistantConfigurationDto()).thenReturn(designConfigDto);
        Mockito.when(configuration.getMarketplaceAssistantConfigurationDto())
                .thenReturn(new MarketplaceAssistantConfigurationDTO());
        Mockito.when(configuration.getApiChatConfigurationDto()).thenReturn(new ApiChatConfigurationDTO());
    }

    @Test
    public void testReturnsDefaultImplWhenNoImplClassConfigured() throws APIManagementException {
        AIService service = AIServiceFactory.getAIService();
        Assert.assertTrue("Expected the default AI service implementation",
                service instanceof DefaultAIServiceImpl);
    }

    @Test
    public void testReturnsDefaultImplWhenConfigurationNotInitialized() throws APIManagementException {
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(null);
        AIService service = AIServiceFactory.getAIService();
        Assert.assertTrue("Expected the default AI service implementation when configuration is null",
                service instanceof DefaultAIServiceImpl);
    }

    @Test
    public void testReturnsDefaultImplWhenImplClassBlank() throws APIManagementException {
        Mockito.when(configuration.getAIServiceImplementationClass()).thenReturn("   ");
        AIService service = AIServiceFactory.getAIService();
        Assert.assertTrue("Expected the default AI service implementation when impl class is blank",
                service instanceof DefaultAIServiceImpl);
    }

    @Test
    public void testReturnsConfiguredCustomImpl() throws APIManagementException {
        Mockito.when(configuration.getAIServiceImplementationClass())
                .thenReturn(ValidTestAIService.class.getName());
        AIService service = AIServiceFactory.getAIService();
        Assert.assertTrue("Expected the configured custom AI service implementation",
                service instanceof ValidTestAIService);
    }

    @Test
    public void testInjectsConfigurationIntoImplementation() throws APIManagementException {
        designConfigDto.setEndpoint("https://ai.example.com");
        Mockito.when(configuration.getAIServiceImplementationClass())
                .thenReturn(ValidTestAIService.class.getName());

        AIService service = AIServiceFactory.getAIService();
        AIServiceConfiguration injected = ((AbstractAIService) service).getConfiguration();
        Assert.assertNotNull("The factory must push the resolved configuration into the implementation", injected);
        Assert.assertEquals("The injected Design Assistant endpoint should match api-manager.xml",
                "https://ai.example.com", injected.getDesignAssistant().getEndpoint());
    }

    @Test
    public void testThrowsWhenConfiguredClassDoesNotImplementSpi() {
        Mockito.when(configuration.getAIServiceImplementationClass()).thenReturn(Object.class.getName());
        try {
            AIServiceFactory.getAIService();
            Assert.fail("Expected APIManagementException for a class that does not implement AIService");
        } catch (APIManagementException e) {
            Assert.assertTrue("Message should explain the type mismatch, but was: " + e.getMessage(),
                    e.getMessage().contains("does not implement"));
        }
    }

    @Test
    public void testThrowsWhenConfiguredClassCannotBeInstantiated() {
        Mockito.when(configuration.getAIServiceImplementationClass())
                .thenReturn("org.wso2.carbon.apimgt.impl.ai.NonExistentAIServiceImpl");
        try {
            AIServiceFactory.getAIService();
            Assert.fail("Expected APIManagementException for a non-instantiable / missing class");
        } catch (APIManagementException e) {
            Assert.assertTrue("Message should indicate an instantiation error, but was: " + e.getMessage(),
                    e.getMessage().contains("Error while instantiating"));
        }
    }

    @Test
    public void testCachesResolvedInstance() throws APIManagementException {
        AIService first = AIServiceFactory.getAIService();
        AIService second = AIServiceFactory.getAIService();
        Assert.assertSame("The factory should cache and return the same instance", first, second);
    }

    /**
     * Minimal valid {@link AIService} with a public no-arg constructor. It extends {@link AbstractAIService} so it
     * inherits every capability and the configuration injection, mirroring how a real custom implementation overrides
     * only what it needs.
     */
    public static class ValidTestAIService extends AbstractAIService {
    }
}
