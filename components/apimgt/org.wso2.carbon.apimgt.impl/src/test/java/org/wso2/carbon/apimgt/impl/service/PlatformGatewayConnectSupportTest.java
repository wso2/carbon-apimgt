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

package org.wso2.carbon.apimgt.impl.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiMgtDAO.class})
public class PlatformGatewayConnectSupportTest {

    @Test
    public void testResolveConnectGatewayIdIsDeterministic() {
        String first = PlatformGatewayServiceImpl.resolveConnectGatewayId("token-id-123");
        String second = PlatformGatewayServiceImpl.resolveConnectGatewayId("token-id-123");
        Assert.assertNotNull(first);
        Assert.assertEquals(first, second);
    }

    @Test
    public void testResolveStorageOrganizationIdUsesRequestOrg() throws Exception {
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);

        Environment env = new Environment();
        env.setGatewayType(APIConstants.WSO2_API_PLATFORM_GATEWAY);
        Mockito.when(apiMgtDAO.getEnvironment("carbon.super", "gw-1")).thenReturn(env);

        String storageOrg = PlatformGatewayServiceImpl.resolveStorageOrganizationId("carbon.super", "gw-1");
        Assert.assertEquals("carbon.super", storageOrg);
    }

    @Test
    public void testResolveStorageOrganizationIdReturnsNullWhenNotInRequestOrg() throws Exception {
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);

        Mockito.when(apiMgtDAO.getEnvironment("carbon.super", "gw-1")).thenReturn(null);

        Assert.assertNull(PlatformGatewayServiceImpl.resolveStorageOrganizationId("carbon.super", "gw-1"));
    }

    @Test
    public void testResolveStorageOrganizationIdReturnsNullForBlankGatewayId() throws APIManagementException {
        Assert.assertNull(PlatformGatewayServiceImpl.resolveStorageOrganizationId("carbon.super", " "));
    }
}
