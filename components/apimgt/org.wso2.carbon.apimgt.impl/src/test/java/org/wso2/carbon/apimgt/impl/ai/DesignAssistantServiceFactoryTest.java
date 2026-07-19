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
import org.wso2.carbon.apimgt.api.DesignAssistant;
import org.wso2.carbon.apimgt.api.DesignAssistantRequest;
import org.wso2.carbon.apimgt.api.DesignAssistantResponse;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.ai.DesignAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

/**
 * Tests {@link DesignAssistantServiceFactory}'s resolution of the configured {@link DesignAssistant}
 * implementation: default fallback, valid custom class, wrong-type / non-instantiable rejection, and caching.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class})
public class DesignAssistantServiceFactoryTest {

    private APIManagerConfigurationService configurationService;
    private DesignAssistantConfigurationDTO configDto;

    @Before
    public void setUp() {
        // The factory caches the resolved instance in a static field; clear it so every test resolves afresh.
        Whitebox.setInternalState(DesignAssistantServiceFactory.class, "designAssistant", (DesignAssistant) null);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        configurationService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        configDto = new DesignAssistantConfigurationDTO();
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(configurationService);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);
        Mockito.when(configuration.getDesignAssistantConfigurationDto()).thenReturn(configDto);
    }

    @Test
    public void testReturnsDefaultImplWhenNoImplClassConfigured() throws APIManagementException {
        DesignAssistant service = DesignAssistantServiceFactory.getDesignAssistantService();
        Assert.assertTrue("Expected the default Design Assistant implementation",
                service instanceof DefaultDesignAssistantServiceImpl);
    }

    @Test
    public void testReturnsDefaultImplWhenConfigurationNotInitialized() throws APIManagementException {
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(null);
        DesignAssistant service = DesignAssistantServiceFactory.getDesignAssistantService();
        Assert.assertTrue("Expected the default Design Assistant implementation when configuration is null",
                service instanceof DefaultDesignAssistantServiceImpl);
    }

    @Test
    public void testReturnsDefaultImplWhenImplClassBlank() throws APIManagementException {
        configDto.setImplementationClass("   ");
        DesignAssistant service = DesignAssistantServiceFactory.getDesignAssistantService();
        Assert.assertTrue("Expected the default Design Assistant implementation when impl class is blank",
                service instanceof DefaultDesignAssistantServiceImpl);
    }

    @Test
    public void testReturnsConfiguredCustomImpl() throws APIManagementException {
        configDto.setImplementationClass(ValidTestDesignAssistant.class.getName());
        DesignAssistant service = DesignAssistantServiceFactory.getDesignAssistantService();
        Assert.assertTrue("Expected the configured custom Design Assistant implementation",
                service instanceof ValidTestDesignAssistant);
        Assert.assertEquals(ValidTestDesignAssistant.class.getName(), service.getClass().getName());
    }

    @Test
    public void testThrowsWhenConfiguredClassDoesNotImplementSpi() {
        configDto.setImplementationClass(Object.class.getName());
        try {
            DesignAssistantServiceFactory.getDesignAssistantService();
            Assert.fail("Expected APIManagementException for a class that does not implement DesignAssistant");
        } catch (APIManagementException e) {
            Assert.assertTrue("Message should explain the type mismatch, but was: " + e.getMessage(),
                    e.getMessage().contains("does not implement"));
        }
    }

    @Test
    public void testThrowsWhenConfiguredClassCannotBeInstantiated() {
        configDto.setImplementationClass("org.wso2.carbon.apimgt.impl.ai.NonExistentDesignAssistantImpl");
        try {
            DesignAssistantServiceFactory.getDesignAssistantService();
            Assert.fail("Expected APIManagementException for a non-instantiable / missing class");
        } catch (APIManagementException e) {
            Assert.assertTrue("Message should indicate an instantiation error, but was: " + e.getMessage(),
                    e.getMessage().contains("Error while instantiating"));
        }
    }

    @Test
    public void testCachesResolvedInstance() throws APIManagementException {
        DesignAssistant first = DesignAssistantServiceFactory.getDesignAssistantService();
        DesignAssistant second = DesignAssistantServiceFactory.getDesignAssistantService();
        Assert.assertSame("The factory should cache and return the same instance", first, second);
    }

    /**
     * Minimal valid {@link DesignAssistant} with a public no-arg constructor, used to verify custom-class
     * resolution through {@code Class.forName(...)}.
     */
    public static class ValidTestDesignAssistant implements DesignAssistant {

        @Override
        public DesignAssistantResponse generatePayload(DesignAssistantRequest request) {
            return null;
        }

        @Override
        public DesignAssistantResponse chat(DesignAssistantRequest request) {
            return null;
        }
    }
}
