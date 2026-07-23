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
import org.wso2.carbon.apimgt.api.APIChatAssistant;
import org.wso2.carbon.apimgt.api.APIChatRequest;
import org.wso2.carbon.apimgt.api.APIChatResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.ai.ApiChatConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

/**
 * Tests {@link APIChatAssistantFactory}'s resolution of the configured {@link APIChatAssistant} implementation:
 * default fallback, valid custom class, wrong-type / non-instantiable rejection, and instance caching.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class})
public class APIChatAssistantFactoryTest {

    private APIManagerConfigurationService configurationService;
    private APIManagerConfiguration configuration;
    private ApiChatConfigurationDTO configDto;

    @Before
    public void setUp() {
        // The factory caches the resolved instance in a static field; clear it so every test resolves afresh.
        Whitebox.setInternalState(APIChatAssistantFactory.class, "apiChatAssistant", (APIChatAssistant) null);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        configurationService = Mockito.mock(APIManagerConfigurationService.class);
        configuration = Mockito.mock(APIManagerConfiguration.class);
        configDto = new ApiChatConfigurationDTO();
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(configurationService);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);
        Mockito.when(configuration.getApiChatConfigurationDto()).thenReturn(configDto);
    }

    @Test
    public void testReturnsDefaultImplWhenNoImplClassConfigured() throws APIManagementException {
        // A fresh DTO carries the default impl class, so the factory resolves the default implementation.
        APIChatAssistant service = APIChatAssistantFactory.getAPIChatService();
        Assert.assertTrue("Expected the default API Chat implementation",
                service instanceof DefaultAPIChatAssistantServiceImpl);
    }

    @Test
    public void testReturnsDefaultImplWhenConfigurationNotInitialized() throws APIManagementException {
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(null);
        APIChatAssistant service = APIChatAssistantFactory.getAPIChatService();
        Assert.assertTrue("Expected the default API Chat implementation when configuration is null",
                service instanceof DefaultAPIChatAssistantServiceImpl);
    }

    @Test
    public void testReturnsDefaultImplWhenImplClassBlank() throws APIManagementException {
        configDto.setImplementationClass("   ");
        APIChatAssistant service = APIChatAssistantFactory.getAPIChatService();
        Assert.assertTrue("Expected the default API Chat implementation when impl class is blank",
                service instanceof DefaultAPIChatAssistantServiceImpl);
    }

    @Test
    public void testReturnsConfiguredCustomImpl() throws APIManagementException {
        configDto.setImplementationClass(ValidTestAPIChatAssistant.class.getName());
        APIChatAssistant service = APIChatAssistantFactory.getAPIChatService();
        Assert.assertTrue("Expected the configured custom API Chat implementation",
                service instanceof ValidTestAPIChatAssistant);
        Assert.assertEquals(ValidTestAPIChatAssistant.class.getName(), service.getClass().getName());
    }

    @Test
    public void testThrowsWhenConfiguredClassDoesNotImplementSpi() {
        configDto.setImplementationClass(Object.class.getName());
        try {
            APIChatAssistantFactory.getAPIChatService();
            Assert.fail("Expected APIManagementException for a class that does not implement APIChatAssistant");
        } catch (APIManagementException e) {
            Assert.assertTrue("Message should explain the type mismatch, but was: " + e.getMessage(),
                    e.getMessage().contains("does not implement"));
        }
    }

    @Test
    public void testThrowsWhenConfiguredClassCannotBeInstantiated() {
        configDto.setImplementationClass("org.wso2.carbon.apimgt.impl.ai.NonExistentAPIChatAssistantImpl");
        try {
            APIChatAssistantFactory.getAPIChatService();
            Assert.fail("Expected APIManagementException for a non-instantiable / missing class");
        } catch (APIManagementException e) {
            Assert.assertTrue("Message should indicate an instantiation error, but was: " + e.getMessage(),
                    e.getMessage().contains("Error while instantiating"));
        }
    }

    @Test
    public void testCachesResolvedInstance() throws APIManagementException {
        APIChatAssistant first = APIChatAssistantFactory.getAPIChatService();
        APIChatAssistant second = APIChatAssistantFactory.getAPIChatService();
        Assert.assertSame("The factory should cache and return the same instance", first, second);
    }

    /**
     * Minimal valid {@link APIChatAssistant} with a public no-arg constructor, used to verify custom-class
     * resolution through {@code Class.forName(...)}.
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
