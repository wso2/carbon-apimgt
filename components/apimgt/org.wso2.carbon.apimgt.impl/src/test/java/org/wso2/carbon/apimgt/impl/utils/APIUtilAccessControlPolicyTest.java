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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Direct unit tests for the core network-security-access-control decision logic in
 * {@link APIUtil} — the private statics {@code applyAccessControlPolicy},
 * {@code isHostInList}, {@code isPrivateNetworkAddress}, {@code toWildcardRegex}.
 * These are the heart of the outbound host validation gate and previously had no direct
 * coverage (only slow integration suites / tests that mock {@code APIUtil} away).
 *
 * All cases are deterministic: hosts are LITERAL IPs (so {@code InetAddress.getAllByName}
 * returns them without a DNS lookup), allow/deny hostname matches short-circuit before any
 * resolution, and the unknown-host case uses a guaranteed-non-resolving {@code .invalid} name.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class})
public class APIUtilAccessControlPolicyTest {

    private static final String PUBLIC_IP = "93.184.216.34";   // literal public IP, no DNS
    private static final String PUBLIC_IP2 = "8.8.8.8";

    private static List<String> list(String... v) {
        return new ArrayList<>(Arrays.asList(v));
    }

    /** @return true if applyAccessControlPolicy BLOCKS the host (throws), false if it allows. */
    private boolean blocked(String host, String mode, List<String> hosts, boolean blockPrivate) throws Exception {
        try {
            Whitebox.invokeMethod(APIUtil.class, "applyAccessControlPolicy", host, mode, hosts, blockPrivate);
            return false;
        } catch (APIManagementException e) {
            return true;
        }
    }

    private boolean hostInList(String host, List<String> hosts) throws Exception {
        return (Boolean) Whitebox.invokeMethod(APIUtil.class, "isHostInList", host, hosts);
    }

    private boolean isPrivate(String ip) throws Exception {
        return (Boolean) Whitebox.invokeMethod(APIUtil.class, "isPrivateNetworkAddress", InetAddress.getByName(ip));
    }

    // ---------------------------------------------------------------- allow mode

    @Test
    public void allowMode_listedLiteralIp_isAllowed() throws Exception {
        Assert.assertFalse(blocked(PUBLIC_IP, "allow", list(PUBLIC_IP), false));
    }

    @Test
    public void allowMode_unlistedLiteralIp_isBlocked() throws Exception {
        Assert.assertTrue(blocked(PUBLIC_IP, "allow", list(PUBLIC_IP2), false));
    }

    @Test
    public void allowMode_emptyHosts_failsClosed() throws Exception {
        Assert.assertTrue(blocked(PUBLIC_IP, "allow", list(), true));
    }

    @Test
    public void allowMode_wildcardHostnameMatch_isAllowedWithoutDns() throws Exception {
        Assert.assertFalse(blocked("sub.example.com", "allow", list("*.example.com"), false));
    }

    @Test
    public void allowMode_caseInsensitiveMatch() throws Exception {
        Assert.assertFalse(blocked("API.WSO2.COM", "allow", list("*.wso2.com"), false));
    }

    @Test
    public void allowMode_ignoresBlockPrivate_listedPrivateIpAllowed() throws Exception {
        Assert.assertFalse(blocked("10.0.0.1", "allow", list("10.0.0.1"), true));
    }

    // ---------------------------------------------------------------- deny mode

    @Test
    public void denyMode_listedWildcardHost_isBlocked() throws Exception {
        Assert.assertTrue(blocked("169.254.169.254", "deny", list("169.254.*"), false));
    }

    @Test
    public void denyMode_unlistedPublicIp_isAllowed() throws Exception {
        Assert.assertFalse(blocked(PUBLIC_IP, "deny", list("169.254.*"), true));
    }

    @Test
    public void denyMode_privateIp_blockPrivateOn_isBlocked() throws Exception {
        Assert.assertTrue(blocked("10.0.0.1", "deny", list(), true));
    }

    @Test
    public void denyMode_privateIp_blockPrivateOff_isAllowed() throws Exception {
        Assert.assertFalse(blocked("10.0.0.1", "deny", list("169.254.*"), false));
    }

    // ---------------------------------------------------------------- blank/absent mode

    @Test
    public void blankMode_privateIp_blockPrivateOn_isBlocked() throws Exception {
        Assert.assertTrue(blocked("192.168.1.1", "", list(), true));
    }

    @Test
    public void blankMode_publicIp_blockPrivateOn_isAllowed() throws Exception {
        Assert.assertFalse(blocked(PUBLIC_IP, null, list(), true));
    }

    @Test
    public void blankMode_privateIp_blockPrivateOff_isAllowed() throws Exception {
        Assert.assertFalse(blocked("10.0.0.1", "", list(), false));
    }

    // ---------------------------------------------------------------- misconfiguration + fail-closed

    @Test
    public void garbageMode_isBlockedAsMisconfigured() throws Exception {
        boolean threw = false;
        try {
            Whitebox.invokeMethod(APIUtil.class, "applyAccessControlPolicy", PUBLIC_IP, "garbage", list(), false);
        } catch (APIManagementException e) {
            threw = true;
            Assert.assertEquals("garbage mode must surface the MISCONFIGURED error code",
                    ExceptionCodes.NETWORK_SECURITY_ACCESS_CONTROL_MISCONFIGURED.getErrorCode(),
                    e.getErrorHandler().getErrorCode());
        }
        Assert.assertTrue("a garbage mode value must block", threw);
    }

    @Test
    public void unknownHost_allowMode_failsClosed() throws Exception {
        Assert.assertTrue(blocked("nonexistent-host-xyz.invalid", "allow", list(PUBLIC_IP2), false));
    }

    @Test
    public void unknownHost_denyMode_failsClosed() throws Exception {
        Assert.assertTrue(blocked("nonexistent-host-xyz.invalid", "deny", list("169.254.*"), true));
    }

    // ---------------------------------------------------------------- wildcard matching (pure)

    @Test
    public void wildcard_subdomainMatchesButParentDoesNot() throws Exception {
        Assert.assertTrue(hostInList("sub.example.com", list("*.example.com")));
        Assert.assertFalse("'*.example.com' must NOT match the bare parent", hostInList("example.com", list("*.example.com")));
        Assert.assertFalse("'*.example.com' must NOT match a same-suffix different host",
                hostInList("evilexample.com", list("*.example.com")));
    }

    @Test
    public void wildcard_middleAndStar() throws Exception {
        Assert.assertTrue(hostInList("api.test.com", list("api.*.com")));
        Assert.assertTrue("'*' matches any host", hostInList("anything.anywhere", list("*")));
    }

    @Test
    public void wildcard_dotIsLiteralNotRegexAny() throws Exception {
        Assert.assertFalse(hostInList("axb", list("a.b")));
        Assert.assertTrue(hostInList("a.b", list("a.b")));
    }

    @Test
    public void toWildcardRegex_producesAnchoredMatchingRegex() throws Exception {
        String regex = (String) Whitebox.invokeMethod(APIUtil.class, "toWildcardRegex", "*.x.com");
        Assert.assertTrue("sub.x.com".matches(regex));
        Assert.assertFalse("sub.x.com.evil.com".matches(regex)); // anchored at end
    }

    // ---------------------------------------------------------------- private-range detection

    @Test
    public void privateRanges_areFlagged() throws Exception {
        Assert.assertTrue(isPrivate("127.0.0.1"));   // IPv4 loopback
        Assert.assertTrue(isPrivate("::1"));          // IPv6 loopback
        Assert.assertTrue(isPrivate("10.0.0.1"));     // RFC1918 /8
        Assert.assertTrue(isPrivate("172.16.0.1"));   // RFC1918 /12
        Assert.assertTrue(isPrivate("192.168.1.1"));  // RFC1918 /16
        Assert.assertTrue(isPrivate("169.254.0.1"));  // link-local
        Assert.assertTrue(isPrivate("224.0.0.1"));    // multicast
        Assert.assertTrue(isPrivate("0.0.0.0"));      // any-local
        Assert.assertTrue(isPrivate("fc00::1"));      // IPv6 ULA fc00::/7
        Assert.assertTrue(isPrivate("fd00::1"));      // IPv6 ULA fc00::/7
    }

    @Test
    public void publicAddresses_areNotFlagged() throws Exception {
        Assert.assertFalse(isPrivate(PUBLIC_IP));
        Assert.assertFalse(isPrivate(PUBLIC_IP2));
    }

    // ---------------------------------------------------------------- KNOWN-LIMITATION guards (deferred fixes)

    @Test
    public void knownLimitation_G6_ipv6DenyEntryDoesNotMatchExpandedResolvedForm() throws Exception {
        // G6: IPv6 literal deny entries dead vs expanded resolved form. Deferred.
        Assert.assertFalse(hostInList("0:0:0:0:0:0:0:1", list("::1")));
    }

    @Test
    public void knownLimitation_G7_trailingDotFqdnBypassesNameDenyList() throws Exception {
        // G7: trailing-dot FQDN bypasses name-based deny patterns. Deferred.
        Assert.assertFalse(hostInList("internal.corp.", list("internal.corp")));
    }
}
