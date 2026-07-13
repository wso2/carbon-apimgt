/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.OASParserOptions;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Arrays;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class})
public class APIUtilRefOptionsTest {

    private static final String TENANT = "carbon.super";

    @Test
    public void testAllowModePlatformProducesAllowList() throws Exception {
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", TENANT);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", true);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityMode", "allow");
        Whitebox.setInternalState(APIUtil.class, "networkSecurityHosts", Arrays.asList("*.wso2.com"));
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", true);

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        Assert.assertEquals(Arrays.asList("*.wso2.com"), out.getRemoteRefAllowList());
        Assert.assertTrue(out.getRemoteRefBlockList() == null || out.getRemoteRefBlockList().isEmpty());
        Assert.assertTrue("A configured platform policy must enable network access control",
                out.isNetworkAccessControlEnabled());
    }

    @Test
    public void testDenyModePlatformProducesBlockList() throws Exception {
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", TENANT);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", true);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityMode", "deny");
        Whitebox.setInternalState(APIUtil.class, "networkSecurityHosts", Arrays.asList("*.internal"));
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", true);

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        Assert.assertEquals(Arrays.asList("*.internal"), out.getRemoteRefBlockList());
        Assert.assertTrue(out.getRemoteRefAllowList() == null || out.getRemoteRefAllowList().isEmpty());
        Assert.assertTrue("A configured platform policy must enable network access control",
                out.isNetworkAccessControlEnabled());
    }

    @Test
    public void testInactivePolicyProducesEmptyLists() throws Exception {
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", TENANT);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", false);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityMode", (String) null);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityHosts", (java.util.List) null);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", false);

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        Assert.assertTrue(out.getRemoteRefAllowList() == null || out.getRemoteRefAllowList().isEmpty());
        Assert.assertTrue(out.getRemoteRefBlockList() == null || out.getRemoteRefBlockList().isEmpty());
        // Backwards compatibility: with no policy configured, network access control must stay off so remote refs
        // resolve exactly as they did before the feature existed.
        Assert.assertFalse("No configured policy must leave network access control disabled",
                out.isNetworkAccessControlEnabled());
    }

    @Test
    public void testPlatformPolicyEnabledWithoutHostsStillEnablesNetworkAccessControl() throws Exception {
        // The policy block can be present with no hosts (e.g. only block_private_network_access set). Presence of the
        // block alone means the admin opted in, so network access control is on even though both lists are empty.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", TENANT);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", true);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityMode", (String) null);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityHosts", (java.util.List) null);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", true);

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        Assert.assertTrue(out.getRemoteRefAllowList() == null || out.getRemoteRefAllowList().isEmpty());
        Assert.assertTrue(out.getRemoteRefBlockList() == null || out.getRemoteRefBlockList().isEmpty());
        Assert.assertTrue(out.isNetworkAccessControlEnabled());
    }

    @Test
    public void testYamlCodePointLimitPreserved() throws Exception {
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", TENANT);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", false);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityMode", (String) null);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityHosts", (java.util.List) null);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", false);

        OASParserOptions base = new OASParserOptions();
        base.setYamlCodePointLimit("10");

        OASParserOptions out = APIUtil.buildRefResolutionOptions(base, TENANT);

        Assert.assertEquals(base.getYamlCodePointLimit(), out.getYamlCodePointLimit());
        Assert.assertNotEquals(Integer.valueOf(Integer.MAX_VALUE), out.getYamlCodePointLimit());
    }

    @Test
    public void testInvalidModePlatformThrowsMisconfigured() throws Exception {
        // An enabled platform policy whose mode is neither 'allow' nor 'deny' must fail fast rather than silently
        // producing empty lists, matching the behaviour of the runtime access-control check.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", TENANT);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", true);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityMode", "block");
        Whitebox.setInternalState(APIUtil.class, "networkSecurityHosts", Arrays.asList("*.wso2.com"));
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", true);

        try {
            APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);
            Assert.fail("An invalid platform mode must raise a misconfiguration error");
        } catch (APIManagementException e) {
            Assert.assertEquals(ExceptionCodes.NETWORK_SECURITY_ACCESS_CONTROL_MISCONFIGURED, e.getErrorHandler());
        }
    }

    @Test
    public void testInvalidModeTenantThrowsMisconfigured() throws Exception {
        // An enabled tenant policy with an invalid mode must also propagate a misconfiguration error instead of being
        // swallowed by the tenant-config read guard.
        PowerMockito.spy(APIUtil.class);
        JSONObject policy = new JSONObject();
        policy.put(APIConstants.NetworkSecurityAccessControl.TENANT_MODE, "block");
        JSONObject tenantConfig = new JSONObject();
        tenantConfig.put(APIConstants.NetworkSecurityAccessControl.TENANT_CONFIG_KEY, policy);
        PowerMockito.doReturn(tenantConfig).when(APIUtil.class, "getTenantConfig", TENANT);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", false);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityMode", (String) null);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityHosts", (java.util.List) null);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", false);

        try {
            APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);
            Assert.fail("An invalid tenant mode must raise a misconfiguration error");
        } catch (APIManagementException e) {
            Assert.assertEquals(ExceptionCodes.NETWORK_SECURITY_ACCESS_CONTROL_MISCONFIGURED, e.getErrorHandler());
        }
    }
}
