/*
 *  Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com/).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.common.gateway.proxy;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.common.gateway.configdto.HttpClientConfigurationDTO;

public class ExtendedProxyRoutePlannerTest {

    private static final String PROXY_HOST = "proxy.example.com";
    private static final int PROXY_PORT = 3128;
    private static final String PROXY_PROTOCOL = "http";
    private static final HttpRequest REQUEST = new BasicHttpRequest("GET", "/");
    private static final HttpContext CONTEXT = new BasicHttpContext();

    private ExtendedProxyRoutePlanner createPlanner(String[] nonProxyHosts, String[] targetProxyHosts) {
        HttpClientConfigurationDTO config = new HttpClientConfigurationDTO.Builder()
                .withConnectionParams(100, 10, -1, -1)
                .withProxy(PROXY_HOST, PROXY_PORT, null, null, PROXY_PROTOCOL,
                        nonProxyHosts, targetProxyHosts)
                .build();
        HttpHost proxyHost = new HttpHost(PROXY_HOST, PROXY_PORT, PROXY_PROTOCOL);
        return new ExtendedProxyRoutePlanner(proxyHost, config);
    }

    @Test
    public void testGlobWildcardInNonProxyHostsDoesNotThrow() {
        ExtendedProxyRoutePlanner planner = createPlanner(
                new String[]{"*.internal.example.com"}, new String[]{});
        HttpHost result = planner.determineProxy(
                new HttpHost("api.internal.example.com", 443, "https"), REQUEST, CONTEXT);
        Assert.assertNull("*.internal.example.com should match api.internal.example.com", result);
    }

    @Test
    public void testGlobWildcardInTargetProxyHostsDoesNotThrow() {
        ExtendedProxyRoutePlanner planner = createPlanner(
                new String[]{}, new String[]{"*.external.example.com"});
        HttpHost result = planner.determineProxy(
                new HttpHost("api.external.example.com", 443, "https"), REQUEST, CONTEXT);
        Assert.assertNotNull("*.external.example.com should match — use proxy", result);
    }

    @Test
    public void testNonProxyHostsExactMatch() {
        ExtendedProxyRoutePlanner planner = createPlanner(
                new String[]{"localhost"}, new String[]{});
        HttpHost result = planner.determineProxy(
                new HttpHost("localhost", 80, "http"), REQUEST, CONTEXT);
        Assert.assertNull("localhost should bypass proxy", result);
    }

    @Test
    public void testNonProxyHostsNoMatch() {
        ExtendedProxyRoutePlanner planner = createPlanner(
                new String[]{"internal.example.com"}, new String[]{});
        HttpHost result = planner.determineProxy(
                new HttpHost("external.example.com", 443, "https"), REQUEST, CONTEXT);
        Assert.assertNotNull("external.example.com should use proxy", result);
    }

    @Test
    public void testNonProxyHostsWildcardAll() {
        ExtendedProxyRoutePlanner planner = createPlanner(
                new String[]{"*"}, new String[]{});
        HttpHost result = planner.determineProxy(
                new HttpHost("anything.example.com", 443, "https"), REQUEST, CONTEXT);
        Assert.assertNull("* should bypass proxy for all hosts", result);
    }

    @Test
    public void testNonProxyHostsCaseInsensitiveMatch() {
        ExtendedProxyRoutePlanner planner = createPlanner(
                new String[]{"*.INTERNAL.com"}, new String[]{});
        HttpHost result = planner.determineProxy(
                new HttpHost("API.internal.COM", 443, "https"), REQUEST, CONTEXT);
        Assert.assertNull("Host should match nonProxyHosts regardless of case", result);
    }

    @Test
    public void testNonProxyHostsMultipleEntries() {
        ExtendedProxyRoutePlanner planner = createPlanner(
                new String[]{"localhost", "*.internal.com", "10.0.*"}, new String[]{});

        Assert.assertNull("localhost should match",
                planner.determineProxy(new HttpHost("localhost", 80, "http"), REQUEST, CONTEXT));
        Assert.assertNull("api.internal.com should match",
                planner.determineProxy(new HttpHost("api.internal.com", 443, "https"), REQUEST, CONTEXT));
        Assert.assertNull("10.0.1.5 should match",
                planner.determineProxy(new HttpHost("10.0.1.5", 80, "http"), REQUEST, CONTEXT));
        Assert.assertNotNull("external.com should NOT match",
                planner.determineProxy(new HttpHost("external.com", 443, "https"), REQUEST, CONTEXT));
    }

    @Test
    public void testNonProxyHostsEmpty() {
        ExtendedProxyRoutePlanner planner = createPlanner(new String[]{}, new String[]{});
        HttpHost result = planner.determineProxy(
                new HttpHost("localhost", 80, "http"), REQUEST, CONTEXT);
        Assert.assertNotNull("Empty nonProxyHosts should use proxy", result);
    }

    @Test
    public void testNonProxyHostsNull() {
        ExtendedProxyRoutePlanner planner = createPlanner(null, null);
        HttpHost result = planner.determineProxy(
                new HttpHost("localhost", 80, "http"), REQUEST, CONTEXT);
        Assert.assertNotNull("Null nonProxyHosts should use proxy", result);
    }

    @Test
    public void testTargetProxyHostsExactMatch() {
        ExtendedProxyRoutePlanner planner = createPlanner(
                new String[]{}, new String[]{"api.external.com"});
        HttpHost result = planner.determineProxy(
                new HttpHost("api.external.com", 443, "https"), REQUEST, CONTEXT);
        Assert.assertNotNull("api.external.com matches targetProxyHosts — use proxy", result);
    }

    @Test
    public void testTargetProxyHostsNoMatch() {
        ExtendedProxyRoutePlanner planner = createPlanner(
                new String[]{}, new String[]{"*.external.com"});
        HttpHost result = planner.determineProxy(
                new HttpHost("localhost", 80, "http"), REQUEST, CONTEXT);
        Assert.assertNull("localhost doesn't match targetProxyHosts — bypass proxy", result);
    }

    @Test
    public void testTargetProxyHostsWildcardAll() {
        ExtendedProxyRoutePlanner planner = createPlanner(
                new String[]{}, new String[]{"*"});
        HttpHost result = planner.determineProxy(
                new HttpHost("anything.example.com", 443, "https"), REQUEST, CONTEXT);
        Assert.assertNotNull("* targetProxyHosts should proxy all hosts", result);
    }

    @Test
    public void testTargetProxyHostsCaseInsensitiveMatch() {
        ExtendedProxyRoutePlanner planner = createPlanner(
                new String[]{}, new String[]{"*.EXTERNAL.com"});
        HttpHost result = planner.determineProxy(
                new HttpHost("API.external.COM", 443, "https"), REQUEST, CONTEXT);
        Assert.assertNotNull("Host should match targetProxyHosts regardless of case", result);
    }

    @Test
    public void testNonProxyHostsBracketPatternDoesNotThrow() {
        ExtendedProxyRoutePlanner planner = createPlanner(
                new String[]{"host[1].internal.com"}, new String[]{});
        HttpHost result = planner.determineProxy(
                new HttpHost("anything.example.com", 443, "https"), REQUEST, CONTEXT);
        Assert.assertNotNull("Pattern with literal brackets should not crash; host should use proxy", result);
    }

    @Test
    public void testNonProxyHostsBracketPatternMatchesLiteral() {
        ExtendedProxyRoutePlanner planner = createPlanner(
                new String[]{"host[1].internal.com"}, new String[]{});
        HttpHost result = planner.determineProxy(
                new HttpHost("host[1].internal.com", 443, "https"), REQUEST, CONTEXT);
        Assert.assertNull("Literal bracketed host should match the same bracketed pattern", result);
    }

    @Test
    public void testNonProxyHostsRegexMetacharsDoNotThrow() {
        ExtendedProxyRoutePlanner planner = createPlanner(
                new String[]{"foo?+.example.(com)"}, new String[]{});
        HttpHost result = planner.determineProxy(
                new HttpHost("api.example.com", 443, "https"), REQUEST, CONTEXT);
        Assert.assertNotNull("Pattern with regex metacharacters should not crash; host should use proxy", result);
    }

    @Test
    public void testNonProxyHostsTakesPrecedenceOverTargetProxyHosts() {
        ExtendedProxyRoutePlanner planner = createPlanner(
                new String[]{"localhost"}, new String[]{"localhost"});
        HttpHost result = planner.determineProxy(
                new HttpHost("localhost", 80, "http"), REQUEST, CONTEXT);
        Assert.assertNull("nonProxyHosts should take precedence over targetProxyHosts", result);
    }

    @Test
    public void testBothConfiguredDifferentHosts() {
        ExtendedProxyRoutePlanner planner = createPlanner(
                new String[]{"localhost"}, new String[]{"*.external.com"});

        Assert.assertNull("localhost matches nonProxyHosts — bypass",
                planner.determineProxy(new HttpHost("localhost", 80, "http"), REQUEST, CONTEXT));
        Assert.assertNotNull("api.external.com matches targetProxyHosts — proxy",
                planner.determineProxy(new HttpHost("api.external.com", 443, "https"), REQUEST, CONTEXT));
        Assert.assertNull("unknown.com doesn't match targetProxyHosts — bypass",
                planner.determineProxy(new HttpHost("unknown.com", 443, "https"), REQUEST, CONTEXT));
    }
}
