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

import org.json.simple.JSONArray;
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
        // Allow-mode is a restrictive whitelist: a wildcard deny blocks every host not on the allow-list.
        Assert.assertEquals(Arrays.asList(APIConstants.NetworkSecurityAccessControl.MATCH_ALL_HOSTS),
                out.getRemoteRefBlockList());
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

    @Test
    public void testTenantConfigReadFailureThrowsMisconfigured() throws Exception {
        // A tenant config that cannot be read must fail the request rather than resolve refs without the tenant policy.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doThrow(new APIManagementException("read failure")).when(APIUtil.class, "getTenantConfig",
                TENANT);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", false);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityMode", (String) null);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityHosts", (java.util.List) null);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", false);

        try {
            APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);
            Assert.fail("A tenant config read failure must raise a misconfiguration error");
        } catch (APIManagementException e) {
            Assert.assertEquals(ExceptionCodes.NETWORK_SECURITY_ACCESS_CONTROL_MISCONFIGURED, e.getErrorHandler());
        }
    }

    @Test
    public void testNonObjectTenantPolicyThrowsMisconfigured() throws Exception {
        PowerMockito.spy(APIUtil.class);
        JSONObject tenantConfig = new JSONObject();
        tenantConfig.put(APIConstants.NetworkSecurityAccessControl.TENANT_CONFIG_KEY, "allow");
        PowerMockito.doReturn(tenantConfig).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(false, null, null);

        try {
            APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);
            Assert.fail("A tenant policy that is not a JSON object must raise a misconfiguration error");
        } catch (APIManagementException e) {
            Assert.assertEquals(ExceptionCodes.NETWORK_SECURITY_ACCESS_CONTROL_MISCONFIGURED, e.getErrorHandler());
        }
    }

    @Test
    public void testNonArrayTenantHostsThrowsMisconfigured() throws Exception {
        PowerMockito.spy(APIUtil.class);
        JSONObject policy = new JSONObject();
        policy.put(APIConstants.NetworkSecurityAccessControl.TENANT_MODE, "deny");
        policy.put(APIConstants.NetworkSecurityAccessControl.TENANT_HOSTS, "internal.example.com");
        JSONObject tenantConfig = new JSONObject();
        tenantConfig.put(APIConstants.NetworkSecurityAccessControl.TENANT_CONFIG_KEY, policy);
        PowerMockito.doReturn(tenantConfig).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(false, null, null);

        try {
            APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);
            Assert.fail("A tenant hosts value that is not a JSON array must raise a misconfiguration error");
        } catch (APIManagementException e) {
            Assert.assertEquals(ExceptionCodes.NETWORK_SECURITY_ACCESS_CONTROL_MISCONFIGURED, e.getErrorHandler());
        }
    }

    @Test
    public void testBothAllowModeIntersects() throws Exception {
        // AND: a host must be allowed by both policies, so the allow-list is the intersection of the two allow lists.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(tenantPolicy("allow", "b.com", "c.com")).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "allow", Arrays.asList("a.com", "b.com"));

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        assertSameElements(Arrays.asList("b.com"), out.getRemoteRefAllowList());
        assertSameElements(Arrays.asList("*"), out.getRemoteRefBlockList());
    }

    @Test
    public void testDisjointBothAllowModeBlocksEverything() throws Exception {
        // Disjoint allow lists → empty intersection → nothing is allowed; the wildcard deny blocks all hosts.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(tenantPolicy("allow", "b.com")).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "allow", Arrays.asList("a.com"));

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        Assert.assertTrue(out.getRemoteRefAllowList() == null || out.getRemoteRefAllowList().isEmpty());
        assertSameElements(Arrays.asList("*"), out.getRemoteRefBlockList());
    }

    @Test
    public void testPlatformDenyTenantAllowAddsWildcardAndKeepsAllowedHost() throws Exception {
        // Platform deny + tenant allow: only the tenant-allowed hosts resolve, and the platform-denied host is blocked.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(tenantPolicy("allow", "203.0.113.10")).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "deny", Arrays.asList("evil.com"));

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        assertSameElements(Arrays.asList("203.0.113.10"), out.getRemoteRefAllowList());
        assertSameElements(Arrays.asList("evil.com", "*"), out.getRemoteRefBlockList());
    }

    @Test
    public void testDeniedHostIsRemovedFromAllowList() throws Exception {
        // A host that appears on both a deny list and an allow list must stay blocked: the resolver's allow-list
        // short-circuits to ALLOW, so a denied host has to be removed from the allow-list, not merely block-listed.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(tenantPolicy("allow", "shared.com", "203.0.113.20"))
                .when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "deny", Arrays.asList("shared.com"));

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        assertSameElements(Arrays.asList("203.0.113.20"), out.getRemoteRefAllowList());
        assertSameElements(Arrays.asList("shared.com", "*"), out.getRemoteRefBlockList());
    }

    @Test
    public void testBothDenyModeUnionsWithoutWildcard() throws Exception {
        // Deny-mode is a blacklist: the block-list is the union of both deny lists and no wildcard is added, so hosts
        // that are not denied still resolve.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(tenantPolicy("deny", "b.com")).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "deny", Arrays.asList("a.com"));

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        Assert.assertTrue(out.getRemoteRefAllowList() == null || out.getRemoteRefAllowList().isEmpty());
        assertSameElements(Arrays.asList("a.com", "b.com"), out.getRemoteRefBlockList());
    }

    @Test
    public void testPlatformPrivateNetworkBlockingFiltersTenantAllowList() throws Exception {
        // The allow-list short-circuits the resolver's private-network check, so a tenant allow entry that the
        // platform's private-network blocking rejects must not reach the allow-list.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(tenantPolicy("allow", "127.0.0.1", "203.0.113.10"))
                .when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "deny", java.util.Collections.<String>emptyList());
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", true);

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        assertSameElements(Arrays.asList("203.0.113.10"), out.getRemoteRefAllowList());
        assertSameElements(Arrays.asList("*"), out.getRemoteRefBlockList());
    }

    @Test
    public void testPlatformBlankModePrivateNetworkBlockingFiltersTenantAllowList() throws Exception {
        // A platform policy with no mode and private-network blocking on restricts the tenant allow-list the same way.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(tenantPolicy("allow", "127.0.0.1")).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, null, null);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", true);

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        Assert.assertTrue(out.getRemoteRefAllowList() == null || out.getRemoteRefAllowList().isEmpty());
        assertSameElements(Arrays.asList("*"), out.getRemoteRefBlockList());
    }

    @Test
    public void testTenantWildcardAllowEntryKeptWhenDisjointFromPlatformDenyList() throws Exception {
        // A wildcard entry that shares no host with any platform deny pattern is kept; '*' matches evil.com and is not.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(tenantPolicy("allow", "*", "*.example.com"))
                .when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "deny", Arrays.asList("evil.com"));

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        assertSameElements(Arrays.asList("*.example.com"), out.getRemoteRefAllowList());
        assertSameElements(Arrays.asList("evil.com", "*"), out.getRemoteRefBlockList());
    }

    @Test
    public void testTenantWildcardAllowEntryDroppedWhenOverlappingPlatformDenyPattern() throws Exception {
        // internal.partner.com matches both patterns, so the wildcard entry would let the resolver bypass the deny.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(tenantPolicy("allow", "*.partner.com")).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "deny", Arrays.asList("internal.*"));

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        Assert.assertTrue(out.getRemoteRefAllowList() == null || out.getRemoteRefAllowList().isEmpty());
    }

    @Test
    public void testTenantWildcardAllowEntryDroppedWhenPlatformDeniesIpPattern() throws Exception {
        // A name match skips the resolver's address checks, so an IP deny pattern cannot be enforced for the entry.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(tenantPolicy("allow", "*.partner.com")).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "deny", Arrays.asList("evil.com", "203.0.113.*"));

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        Assert.assertTrue(out.getRemoteRefAllowList() == null || out.getRemoteRefAllowList().isEmpty());
    }

    @Test
    public void testTenantWildcardAllowEntryDroppedWhenPlatformBlocksPrivateNetworks() throws Exception {
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(tenantPolicy("allow", "*.partner.com")).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "deny", Arrays.asList("evil.com"));
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", true);

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        Assert.assertTrue(out.getRemoteRefAllowList() == null || out.getRemoteRefAllowList().isEmpty());
    }

    @Test
    public void testTenantDenyOverlappingPlatformWildcardDropsIt() throws Exception {
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(tenantPolicy("deny", "internal.partner.com"))
                .when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "allow", Arrays.asList("*.partner.com", "*.example.com"));

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        assertSameElements(Arrays.asList("*.example.com"), out.getRemoteRefAllowList());
        assertSameElements(Arrays.asList("internal.partner.com", "*"), out.getRemoteRefBlockList());
    }

    @Test
    public void testBothAllowModeWildcardAndHost() throws Exception {
        // The platform allows *.partner.com and the tenant allows api.partner.com: only api.partner.com is in both.
        assertBothAllowIntersection(Arrays.asList("*.partner.com"), Arrays.asList("api.partner.com"),
                Arrays.asList("api.partner.com"));
    }

    @Test
    public void testBothAllowModeKeepsNarrowerWildcard() throws Exception {
        assertBothAllowIntersection(Arrays.asList("*.a.com"), Arrays.asList("*.b.a.com"), Arrays.asList("*.b.a.com"));
        assertBothAllowIntersection(Arrays.asList("*.com"), Arrays.asList("*.partner.com", "*.example.org"),
                Arrays.asList("*.partner.com"));
    }

    @Test
    public void testBothAllowModeCombinesPartiallyOverlappingWildcards() throws Exception {
        // Neither pattern contains the other, but hosts starting with 'a' and ending with 'b' match both.
        assertBothAllowIntersection(Arrays.asList("a*"), Arrays.asList("*b"), Arrays.asList("a*b"));
        assertBothAllowIntersection(Arrays.asList("api.*"), Arrays.asList("*.example.com"),
                Arrays.asList("api.example.com", "api.*.example.com"));
    }

    @Test
    public void testBothAllowModeDisjointWildcardsBlockEverything() throws Exception {
        assertBothAllowIntersection(Arrays.asList("*.partner.com"), Arrays.asList("*.other.com"),
                java.util.Collections.<String>emptyList());
    }

    @Test
    public void testBothAllowModeMatchAllKeepsOtherList() throws Exception {
        assertBothAllowIntersection(Arrays.asList("*"), Arrays.asList("api.example.com", "*.partner.com"),
                Arrays.asList("api.example.com", "*.partner.com"));
    }

    @Test
    public void testBothAllowModeIgnoresCase() throws Exception {
        assertBothAllowIntersection(Arrays.asList("*.Partner.com"), Arrays.asList("API.partner.COM"),
                Arrays.asList("api.partner.com"));
    }

    @Test
    public void testAllowEntriesWithResolverOnlySyntaxDropped() throws Exception {
        // The resolver reads '?' as a wildcard and ':' as a port separator; the policy matches them literally.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "allow", Arrays.asList("api.example.com", "ap?.example.com", "api.example.com:8443"));

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        assertSameElements(Arrays.asList("api.example.com"), out.getRemoteRefAllowList());
    }

    @Test
    public void testHostPatternIntersectionMatchesExactlyTheCommonHosts() throws Exception {
        // Exhaustive check over a small alphabet: a host matches some combined pattern exactly when it matches both
        // input patterns under the policy's own matching rules.
        java.util.List<String> patterns = new java.util.ArrayList<>();
        enumerate(patterns, "", new char[]{'a', 'b', '.', '*'}, 3);
        java.util.List<String> hosts = new java.util.ArrayList<>();
        enumerate(hosts, "", new char[]{'a', 'b', '.'}, 4);
        java.util.Map<String, java.util.regex.Pattern> regexes = new java.util.HashMap<>();
        for (String pattern : patterns) {
            regexes.put(pattern, java.util.regex.Pattern.compile(
                    (String) Whitebox.invokeMethod(APIUtil.class, "toWildcardRegex", pattern)));
        }
        for (String first : patterns) {
            for (String second : patterns) {
                java.util.List<String> combined = Whitebox.invokeMethod(APIUtil.class, "intersectHostPatterns",
                        first, second);
                java.util.List<java.util.regex.Pattern> combinedRegexes = new java.util.ArrayList<>();
                for (String pattern : combined) {
                    combinedRegexes.add(java.util.regex.Pattern.compile(
                            (String) Whitebox.invokeMethod(APIUtil.class, "toWildcardRegex", pattern)));
                }
                for (String host : hosts) {
                    boolean expected = regexes.get(first).matcher(host).matches()
                            && regexes.get(second).matcher(host).matches();
                    boolean actual = false;
                    for (java.util.regex.Pattern regex : combinedRegexes) {
                        actual |= regex.matcher(host).matches();
                    }
                    Assert.assertEquals("'" + first + "' and '" + second + "' gave " + combined + " for '" + host
                            + "'", expected, actual);
                }
            }
        }
    }

    private static void enumerate(java.util.List<String> out, String prefix, char[] alphabet, int maxLength) {
        out.add(prefix);
        if (prefix.length() == maxLength) {
            return;
        }
        for (char c : alphabet) {
            enumerate(out, prefix + c, alphabet, maxLength);
        }
    }

    private static void assertBothAllowIntersection(java.util.List<String> platformHosts,
                                                    java.util.List<String> tenantHosts,
                                                    java.util.List<String> expected) throws Exception {
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(tenantPolicy("allow", tenantHosts.toArray(new String[0])))
                .when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "allow", platformHosts);

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        if (expected.isEmpty()) {
            Assert.assertTrue(out.getRemoteRefAllowList() == null || out.getRemoteRefAllowList().isEmpty());
        } else {
            assertSameElements(expected, out.getRemoteRefAllowList());
        }
        assertSameElements(Arrays.asList("*"), out.getRemoteRefBlockList());
    }

    @Test
    public void testPlatformWildcardDenyFiltersTenantAllowList() throws Exception {
        // A platform deny pattern must remove a matching tenant allow entry, not only an identical one.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(tenantPolicy("allow", "db.internal")).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "deny", Arrays.asList("*.internal"));

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        Assert.assertTrue(out.getRemoteRefAllowList() == null || out.getRemoteRefAllowList().isEmpty());
        assertSameElements(Arrays.asList("*.internal", "*"), out.getRemoteRefBlockList());
    }

    @Test
    public void testNonRestrictivePlatformKeepsTenantAllowList() throws Exception {
        // A platform policy that restricts nothing leaves the tenant allow-list, wildcards included, unchanged.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(tenantPolicy("allow", "*.example.com")).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "deny", java.util.Collections.<String>emptyList());

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        assertSameElements(Arrays.asList("*.example.com"), out.getRemoteRefAllowList());
        assertSameElements(Arrays.asList("*"), out.getRemoteRefBlockList());
    }

    @Test
    public void testTenantDenyNarrowsPlatformAllowList() throws Exception {
        // A tenant deny-mode policy may narrow the platform allow-list, including through a wildcard pattern.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(tenantPolicy("deny", "203.0.113.2*")).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "allow", Arrays.asList("203.0.113.10", "203.0.113.20"));

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        assertSameElements(Arrays.asList("203.0.113.10"), out.getRemoteRefAllowList());
        assertSameElements(Arrays.asList("203.0.113.2*", "*"), out.getRemoteRefBlockList());
    }

    @Test
    public void testTenantPrivateNetworkBlockingNarrowsPlatformAllowList() throws Exception {
        // A tenant policy with no mode and private-network blocking on removes private hosts from the platform list.
        PowerMockito.spy(APIUtil.class);
        JSONObject policy = new JSONObject();
        policy.put(APIConstants.NetworkSecurityAccessControl.TENANT_BLOCK_PRIVATE_NETWORK_ACCESS, true);
        JSONObject tenantConfig = new JSONObject();
        tenantConfig.put(APIConstants.NetworkSecurityAccessControl.TENANT_CONFIG_KEY, policy);
        PowerMockito.doReturn(tenantConfig).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "allow", Arrays.asList("127.0.0.1", "203.0.113.10"));

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        assertSameElements(Arrays.asList("203.0.113.10"), out.getRemoteRefAllowList());
        assertSameElements(Arrays.asList("*"), out.getRemoteRefBlockList());
    }

    @Test
    public void testAllowModeWithoutHostsBlocksEverything() throws Exception {
        // Allow-mode with no hosts means "allow nothing": the allow-list is empty and the wildcard deny blocks all.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "allow", null);

        OASParserOptions out = APIUtil.buildRefResolutionOptions(new OASParserOptions(), TENANT);

        Assert.assertTrue(out.getRemoteRefAllowList() == null || out.getRemoteRefAllowList().isEmpty());
        assertSameElements(Arrays.asList("*"), out.getRemoteRefBlockList());
    }

    @Test
    public void testValidateRemoteURLSkipsParameterizedEndpointWhenPolicyEnabled() throws Exception {
        // A parameterized backend endpoint template (resolved to a concrete host later at the gateway) must not be
        // rejected as malformed when the network access-control policy is enabled — mirroring validateEndpointURL.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", TENANT);
        setPlatform(true, "deny", java.util.Collections.<String>emptyList());

        // No APIManagementException expected for any of these.
        APIUtil.validateRemoteURL("http://{uri.var.host}:{uri.var.port}/context", TENANT);
        APIUtil.validateRemoteURL("https://{tenant}.example.com/api", TENANT);
        APIUtil.validateRemoteURL("jms:/queue?transport.jms.ConnectionFactoryJNDIName=QueueConnectionFactory", TENANT);
        APIUtil.validateRemoteURL("consul(http://127.0.0.1:8500 dc1.myService)", TENANT);
    }

    @Test
    public void testValidateRemoteURLBlocksConcreteHostWithParameterizedPath() throws Exception {
        // A concrete, resolvable host must still be validated when only the path/query is parameterized;
        // a parameterized host itself remains exempt.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", TENANT);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", true);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityMode", "deny");
        Whitebox.setInternalState(APIUtil.class, "networkSecurityHosts", java.util.Collections.<String>emptyList());
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", true);

        try {
            APIUtil.validateRemoteURL("http://127.0.0.1/{resource}", TENANT);
            Assert.fail("A private host must be blocked even when the path is parameterized");
        } catch (APIManagementException e) {
            // expected: the concrete loopback host is blocked by the private-network check
        }
        // A parameterized host is not resolvable and stays exempt.
        APIUtil.validateRemoteURL("http://{uri.var.host}/{resource}", TENANT);
    }

    @Test
    public void testExtractURLsFromEndpointConfigSkipsNonObjectArrayElements() throws Exception {
        // A malformed endpoint config with a non-object array element must be skipped, not throw an unchecked
        // JSONException that surfaces as HTTP 500.
        org.json.JSONArray prod = new org.json.JSONArray();
        prod.put("http://bare-string.example.com/api");   // non-object element (must be skipped)
        org.json.JSONObject valid = new org.json.JSONObject();
        valid.put(APIConstants.API_DATA_URL, "http://valid.example.com/api");
        prod.put(valid);                                  // valid object element
        org.json.JSONObject endpointConfig = new org.json.JSONObject();
        endpointConfig.put(APIConstants.API_DATA_PRODUCTION_ENDPOINTS, prod);

        java.util.ArrayList<String> endpoints = new java.util.ArrayList<>();
        APIUtil.extractURLsFromEndpointConfig(endpointConfig, APIConstants.API_DATA_PRODUCTION_ENDPOINTS, endpoints);

        Assert.assertEquals(java.util.Collections.singletonList("http://valid.example.com/api"), endpoints);
    }

    private static void setPlatform(boolean enabled, String mode, java.util.List<String> hosts) {
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", enabled);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityMode", mode);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityHosts", hosts);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", false);
    }

    private static JSONObject tenantPolicy(String mode, String... hosts) {
        JSONObject policy = new JSONObject();
        policy.put(APIConstants.NetworkSecurityAccessControl.TENANT_MODE, mode);
        JSONArray hostArray = new JSONArray();
        hostArray.addAll(Arrays.asList(hosts));
        policy.put(APIConstants.NetworkSecurityAccessControl.TENANT_HOSTS, hostArray);
        JSONObject config = new JSONObject();
        config.put(APIConstants.NetworkSecurityAccessControl.TENANT_CONFIG_KEY, policy);
        return config;
    }

    private static void assertSameElements(java.util.List<String> expected, java.util.List<String> actual) {
        java.util.List<String> actualCopy = actual == null
                ? new java.util.ArrayList<>() : new java.util.ArrayList<>(actual);
        java.util.List<String> expectedCopy = new java.util.ArrayList<>(expected);
        java.util.Collections.sort(actualCopy);
        java.util.Collections.sort(expectedCopy);
        Assert.assertEquals(expectedCopy, actualCopy);
    }
}
