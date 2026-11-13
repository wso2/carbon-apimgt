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
package org.wso2.carbon.apimgt.impl.utils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.ai.AIAPIConfigurationsDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

/**
 * Tests for {@link APIUtil#isFaultFailoverEnabled()}.
 */
public class APIUtilFaultFailoverTest {

    private static final String AI_CONFIG_FIELD = "aiapiConfigurationsDTO";

    private APIManagerConfigurationService originalConfigService;
    private AIAPIConfigurationsDTO originalAIConfig;

    @Before
    public void setUp() {

        originalConfigService = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();
        originalAIConfig = APIManagerConfiguration.getAiApiConfigurationsDTO();
        APIManagerConfigurationService configService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(configService.getAPIManagerConfiguration())
                .thenReturn(Mockito.mock(APIManagerConfiguration.class));
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(configService);
    }

    @After
    public void tearDown() {

        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(originalConfigService);
        setAIConfig(originalAIConfig);
    }

    @Test
    public void testEnabledWhenConfigServiceIsMissing() {

        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
        Assert.assertTrue(APIUtil.isFaultFailoverEnabled());
    }

    @Test
    public void testEnabledWhenAIConfigsAreMissing() {

        setAIConfig(null);
        Assert.assertTrue(APIUtil.isFaultFailoverEnabled());
    }

    @Test
    public void testEnabledWhenFailoverConfigsAreMissing() {

        AIAPIConfigurationsDTO aiConfig = new AIAPIConfigurationsDTO();
        aiConfig.setFailoverConfigurations(null);
        setAIConfig(aiConfig);
        Assert.assertTrue(APIUtil.isFaultFailoverEnabled());
    }

    @Test
    public void testReturnsTheConfiguredValue() {

        AIAPIConfigurationsDTO aiConfig = new AIAPIConfigurationsDTO();
        aiConfig.getFailoverConfigurations().setFaultFailoverEnabled(false);
        setAIConfig(aiConfig);
        Assert.assertFalse(APIUtil.isFaultFailoverEnabled());
    }

    private static void setAIConfig(AIAPIConfigurationsDTO aiConfig) {

        Whitebox.setInternalState(APIManagerConfiguration.class, AI_CONFIG_FIELD, aiConfig);
    }
}
