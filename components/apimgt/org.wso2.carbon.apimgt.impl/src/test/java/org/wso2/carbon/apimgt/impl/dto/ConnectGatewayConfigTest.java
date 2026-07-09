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

package org.wso2.carbon.apimgt.impl.dto;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class ConnectGatewayConfigTest {

    @Test
    public void testResolveOrganizationDefaultsToSuperTenant() {
        ConnectGatewayConfig config = new ConnectGatewayConfig();
        Assert.assertEquals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, config.resolveOrganization());
    }

    @Test
    public void testSetUrlAcceptsHttpsBaseUrl() {
        ConnectGatewayConfig config = new ConnectGatewayConfig();
        config.setUrl("https://gw.example.com:8243");
        Assert.assertEquals("https://gw.example.com:8243", config.getUrl());
    }

    @Test
    public void testSetUrlBlankClearsValue() {
        ConnectGatewayConfig config = new ConnectGatewayConfig();
        config.setUrl("https://gw.example.com:8243");
        config.setUrl("  ");
        Assert.assertNull(config.getUrl());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetUrlRejectsUserInfo() {
        ConnectGatewayConfig config = new ConnectGatewayConfig();
        config.setUrl("https://user:pass@gw.example.com:8243");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetUrlRejectsQuery() {
        ConnectGatewayConfig config = new ConnectGatewayConfig();
        config.setUrl("https://gw.example.com:8243?x=1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetUrlRejectsFragment() {
        ConnectGatewayConfig config = new ConnectGatewayConfig();
        config.setUrl("https://gw.example.com:8243#frag");
    }
}
