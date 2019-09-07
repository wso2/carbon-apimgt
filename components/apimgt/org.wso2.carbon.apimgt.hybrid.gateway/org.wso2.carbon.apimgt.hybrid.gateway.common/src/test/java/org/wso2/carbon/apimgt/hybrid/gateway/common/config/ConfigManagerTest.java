/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.hybrid.gateway.common.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.hybrid.gateway.common.TestBase;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;

import java.lang.reflect.Field;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogFactory.class})
public class ConfigManagerTest extends TestBase {
    @Before
    public void init() {
        Log log = Mockito.mock(Log.class);
        PowerMockito.mockStatic(LogFactory.class);
        PowerMockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(log);
    }

    @Test
    public void testGetConfigManager() throws Exception {
        ConfigManager configManager = ConfigManager.getConfigManager();
        Assert.assertNotNull(configManager);
    }

    @Test
    public void testGetProperty() throws Exception {
        String gatewayUrl = ConfigManager.getConfigManager().getProperty("api.gateway.url");
        Assert.assertNotNull(gatewayUrl);
    }

    @Test(expected = OnPremiseGatewayException.class)
    public void testGetConfigManagerOnPremiseGatewayException() throws Exception {
        Field configManagerField = ConfigManager.class.getDeclaredField("configManager");
        configManagerField.setAccessible(true);
        configManagerField.set(null, null);
        //Set carbon home to a wrong value
        System.setProperty("carbon.home", "");
        ConfigManager.getConfigManager();
    }

}
