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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.fail;

/**
 * Tests for {@link APIUtil#validateRemoteURL(String, String)} and the concrete-host extraction behind it.
 * <p>
 * Every case uses a host that appears literally in the configured hosts list, because both allow and deny mode
 * match the host name before falling back to DNS resolution. That keeps these tests free of any name lookup.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class})
public class APIUtilRemoteUrlTest {

    private static final String TENANT = "carbon.super";
    private static final String BLOCKED_HOST = "blocked.test";
    private static final String ALLOWED_HOST = "backend.test";

    /**
     * Applies a platform-level deny policy naming {@value #BLOCKED_HOST}, with no tenant policy.
     */
    private void denyBlockedHost() throws Exception {
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", TENANT);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", true);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityMode", "deny");
        Whitebox.setInternalState(APIUtil.class, "networkSecurityHosts",
                Collections.singletonList(BLOCKED_HOST));
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", false);
    }

    /**
     * Applies a platform-level allow policy naming {@value #ALLOWED_HOST}, with no tenant policy.
     */
    private void allowBackendHost() throws Exception {
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", TENANT);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", true);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityMode", "allow");
        Whitebox.setInternalState(APIUtil.class, "networkSecurityHosts",
                Collections.singletonList(ALLOWED_HOST));
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", false);
    }

    private void assertRejected(String url, ExceptionCodes expected) {
        try {
            APIUtil.validateRemoteURL(url, TENANT);
            fail("Expected " + expected.name() + " for URL: " + url);
        } catch (APIManagementException e) {
            Assert.assertNotNull("A rejection must carry an error handler, URL: " + url, e.getErrorHandler());
            Assert.assertEquals("Unexpected error code for URL: " + url,
                    expected.getErrorCode(), e.getErrorHandler().getErrorCode());
        }
    }

    private void assertAccepted(String url) throws APIManagementException {
        APIUtil.validateRemoteURL(url, TENANT);
    }

    @Test
    public void testQueryParameterTemplateDoesNotBypassPolicy() throws Exception {
        // A template anywhere outside the host must not exempt the URL from the policy: the host is concrete.
        denyBlockedHost();
        assertRejected("http://" + BLOCKED_HOST + "/latest?param={uri.var.id}", ExceptionCodes.UNTRUSTED_URL);
    }

    @Test
    public void testPathAndFragmentTemplatesDoNotBypassPolicy() throws Exception {
        denyBlockedHost();
        assertRejected("http://" + BLOCKED_HOST + "/{uri.var.path}", ExceptionCodes.UNTRUSTED_URL);
        assertRejected("http://" + BLOCKED_HOST + "/latest#{uri.var.fragment}", ExceptionCodes.UNTRUSTED_URL);
    }

    @Test
    public void testTemplatedUserInfoDoesNotBypassPolicy() throws Exception {
        // The host sits after the user info, so a template in the user info must not hide it.
        denyBlockedHost();
        assertRejected("http://{uri.var.token}@" + BLOCKED_HOST + "/path", ExceptionCodes.UNTRUSTED_URL);
        assertRejected("http://user:{uri.var.password}@" + BLOCKED_HOST + "/path", ExceptionCodes.UNTRUSTED_URL);
    }

    @Test
    public void testTemplatedPortDoesNotBypassPolicy() throws Exception {
        // A templated port stops the authority reading as a host and port, so the host must still be found.
        denyBlockedHost();
        assertRejected("http://" + BLOCKED_HOST + ":{uri.var.port}/path", ExceptionCodes.UNTRUSTED_URL);
    }

    @Test
    public void testTemplatedSchemeDoesNotBypassPolicy() throws Exception {
        denyBlockedHost();
        assertRejected("{uri.var.scheme}://" + BLOCKED_HOST + "/path", ExceptionCodes.UNTRUSTED_URL);
    }

    @Test
    public void testTemplatedHostIsSkipped() throws Exception {
        // A host that is not known until runtime cannot be resolved to a policy decision, so it is skipped.
        allowBackendHost();
        assertAccepted("http://{uri.var.host}/store");
        assertAccepted("https://{uri.var.host}:8080/store?x={uri.var.id}");
        assertAccepted("http://{uri.var.subdomain}." + ALLOWED_HOST + "/store");
    }

    @Test
    public void testAllowListedHostWithTemplatedPathIsAccepted() throws Exception {
        // Allow-listing the concrete host keeps a templated-path endpoint working.
        allowBackendHost();
        assertAccepted("http://" + ALLOWED_HOST + "/{uri.var.path}");
        assertAccepted("http://" + ALLOWED_HOST + "/store?id={uri.var.id}");
    }

    @Test
    public void testUnparseableUrlIsRejectedWithAndWithoutTemplates() throws Exception {
        // A URL that cannot be parsed must fail closed in both forms; adding a template must not flip the outcome.
        denyBlockedHost();
        assertRejected("http://allowed.test\\@" + BLOCKED_HOST + "/{uri.var.id}", ExceptionCodes.MALFORMED_URL);
        assertRejected("http://allowed.test\\@" + BLOCKED_HOST + "/path", ExceptionCodes.MALFORMED_URL);
        assertRejected("not a url at all", ExceptionCodes.MALFORMED_URL);
        assertRejected("/relative/path", ExceptionCodes.MALFORMED_URL);
    }

    @Test
    public void testPlainBlockedUrlIsStillRejected() throws Exception {
        // Regression guard for the untemplated path.
        denyBlockedHost();
        assertRejected("http://" + BLOCKED_HOST + "/latest/meta-data", ExceptionCodes.UNTRUSTED_URL);
        assertRejected("http://" + BLOCKED_HOST, ExceptionCodes.UNTRUSTED_URL);
        assertRejected("http://user:secret@" + BLOCKED_HOST + "/path", ExceptionCodes.UNTRUSTED_URL);
    }

    @Test
    public void testOnlyTheRequestHostIsConsidered() throws Exception {
        // A blocked host appearing inside the query string is not the host being contacted.
        allowBackendHost();
        assertAccepted("http://" + ALLOWED_HOST + "/redirect?next=http://" + BLOCKED_HOST + "/{uri.var.id}");
    }

    @Test
    public void testNonResolvableSchemesAreSkipped() throws Exception {
        denyBlockedHost();
        assertAccepted("jms:/queue?transport.jms.ConnectionFactoryJNDIName=QueueConnectionFactory");
        assertAccepted("consul(http://" + BLOCKED_HOST + "/service)");
    }

    @Test
    public void testBlankUrlIsSkipped() throws Exception {
        denyBlockedHost();
        assertAccepted(null);
        assertAccepted("");
        assertAccepted("   ");
    }

    @Test
    public void testDisabledPolicyAcceptsUrlsThatCannotBeParsed() throws Exception {
        // Nothing is enforced while disabled, so an unparseable URL must not be rejected. The hosts list is
        // populated to show it is the disabled flag, not an empty policy, that lets these URLs pass.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", TENANT);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", false);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityMode", "deny");
        Whitebox.setInternalState(APIUtil.class, "networkSecurityHosts",
                Collections.singletonList(BLOCKED_HOST));
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", false);

        assertAccepted("not a url at all");
        assertAccepted("http://allowed.test\\@" + BLOCKED_HOST + "/{uri.var.id}");
        assertAccepted("http://" + BLOCKED_HOST + "/latest?param={uri.var.id}");
    }

    @Test
    public void testHostContainingTheTemplateMarkerTextIsStillValidated() throws Exception {
        // The placeholder used while parsing must not be something a host can spell out in order to look
        // like a template and so escape the policy. The host is denied by name, so no lookup is involved:
        // were it mistaken for a template the check would be skipped and nothing would be thrown.
        String markerHost = "wso2urltemplatemarker." + BLOCKED_HOST;
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", TENANT);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", true);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityMode", "deny");
        Whitebox.setInternalState(APIUtil.class, "networkSecurityHosts",
                Collections.singletonList(markerHost));
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", false);

        assertRejected("http://" + markerHost + "/path", ExceptionCodes.UNTRUSTED_URL);
        assertRejected("http://" + markerHost + "/path?x={uri.var.id}", ExceptionCodes.UNTRUSTED_URL);
        assertRejected("http://" + markerHost + ":{uri.var.port}/path", ExceptionCodes.UNTRUSTED_URL);
    }

    @Test
    public void testUnreadableAuthorityWithoutATemplateIsRejected() throws Exception {
        // An authority that cannot be read as a host and port is only split by hand when a template made it
        // that way. A genuinely invalid port must not be silently discarded to expose an allowed host.
        allowBackendHost();
        assertRejected("http://" + ALLOWED_HOST + ":bad/{uri.var.id}", ExceptionCodes.MALFORMED_URL);
        assertRejected("http://" + ALLOWED_HOST + ":bad/path", ExceptionCodes.MALFORMED_URL);
    }

    @Test
    public void testTemplatedPortAfterAnIpv6LiteralIsValidated() throws Exception {
        // Substituting a template can stop the URL parsing at all. The address is still concrete, so it has
        // to be found and checked rather than the URL being called malformed.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", TENANT);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", true);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityMode", "deny");
        Whitebox.setInternalState(APIUtil.class, "networkSecurityHosts",
                Collections.singletonList("[2001:db8::1]"));
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", false);

        assertRejected("http://[2001:db8::1]:{uri.var.port}/path", ExceptionCodes.UNTRUSTED_URL);
    }

    @Test
    public void testTemplatedIpv6LiteralIsSkipped() throws Exception {
        // The address is only known at runtime, so it cannot be resolved to a decision and is skipped.
        denyBlockedHost();
        assertAccepted("http://[{uri.var.host}]/path");
    }

    @Test
    public void testWholeUrlTemplateIsSkipped() throws Exception {
        // A template standing for the entire URL names no host, so it gets the same treatment as any other
        // parameterized host rather than being rejected as malformed.
        denyBlockedHost();
        assertAccepted("{uri.var.endpoint}");
        assertAccepted("{uri.var.endpoint}/orders");
    }

    @Test
    public void testTemplatedUrlBehindALeadingSchemeIsSkipped() throws Exception {
        // The documented legacy-encoding form carries the whole URL in a template behind a scheme.
        denyBlockedHost();
        assertAccepted("legacy-encoding:{uri.var.APIurl}");
    }

    @Test
    public void testWildcardDenyMatchesTemplatedUrl() throws Exception {
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", TENANT);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", true);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityMode", "deny");
        Whitebox.setInternalState(APIUtil.class, "networkSecurityHosts", Arrays.asList("*.internal.test"));
        Whitebox.setInternalState(APIUtil.class, "networkSecurityBlockPrivateAccess", false);

        assertRejected("http://metadata.internal.test/latest?x={uri.var.id}", ExceptionCodes.UNTRUSTED_URL);
    }
}
