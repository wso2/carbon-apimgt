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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.api.model.OASParserOptions;

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
}
