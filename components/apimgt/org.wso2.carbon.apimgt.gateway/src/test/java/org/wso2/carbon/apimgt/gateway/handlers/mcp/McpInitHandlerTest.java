/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.mcp;

import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the MCP-Protocol-Version header gate in {@link McpInitHandler}.
 *
 * A request declaring a protocol revision this gateway does not implement is answered with a bare
 * HTTP 400 before authentication, so that the client falls back to the initialize handshake. Every
 * published revision up to the newest one the gateway implements must be let through instead, since
 * the revision is then settled by that handshake.
 */
public class McpInitHandlerTest {

    /**
     * Every published MCP revision must be accepted. 2024-11-05 and 2025-03-26 regressed at update
     * level 48, when they were left out of SUPPORTED_PROTOCOL_VERSION_HEADERS and started being
     * rejected with a bare 400.
     */
    @Test
    public void testPublishedProtocolVersionsAreAccepted() throws Exception {
        String[] published = {
                APIConstants.MCP.PROTOCOL_VERSION_2024_NOVEMBER,
                APIConstants.MCP.PROTOCOL_VERSION_2025_MARCH,
                APIConstants.MCP.PROTOCOL_VERSION_2025_JUNE,
                APIConstants.MCP.PROTOCOL_VERSION_2025_NOVEMBER,
        };
        for (String version : published) {
            Assert.assertTrue("Published MCP protocol revision " + version + " must be accepted",
                    isSupported(version));
        }
    }

    /**
     * A revision newer than the gateway implements is rejected, which is what makes a client
     * supporting both fall back to the initialize handshake.
     */
    @Test
    public void testNewerProtocolVersionIsRejected() throws Exception {
        Assert.assertFalse("A revision newer than the gateway implements must be rejected",
                isSupported("2026-07-28"));
    }

    /**
     * A request that does not carry the header is left to the initialize handshake.
     */
    @Test
    public void testAbsentHeaderIsAccepted() throws Exception {
        Assert.assertTrue("A request without the header must be accepted",
                isSupported(null));
    }

    /**
     * A header carrying no usable revision declares nothing the gateway can act on and is rejected
     * like any other unsupported value.
     */
    @Test
    public void testEmptyAndMalformedValuesAreRejected() throws Exception {
        Assert.assertFalse("An empty value must be rejected", isSupported(""));
        Assert.assertFalse("A blank value must be rejected", isSupported("   "));
        Assert.assertFalse("A malformed value must be rejected", isSupported("not-a-version"));
    }

    /**
     * Surrounding whitespace is tolerated, so a client padding the value is not rejected.
     */
    @Test
    public void testSupportedValueIsTrimmed() throws Exception {
        Assert.assertTrue("A padded but supported revision must be accepted",
                isSupported(" " + APIConstants.MCP.PROTOCOL_VERSION_2024_NOVEMBER + " "));
    }

    /**
     * HTTP header names are case insensitive, so the header is matched ignoring its letter case.
     */
    @Test
    public void testHeaderNameIsMatchedIgnoringCase() throws Exception {
        for (String headerName : new String[] {"MCP-Protocol-Version", "mcp-protocol-version",
                "MCP-PROTOCOL-VERSION"}) {
            Assert.assertFalse("Header " + headerName + " must be read regardless of its letter case",
                    isSupported(headerName, "2026-07-28"));
        }
    }

    private boolean isSupported(String version) throws Exception {
        return isSupported(APIConstants.MCP.MCP_PROTOCOL_VERSION_HEADER, version);
    }

    /**
     * Invokes the handler's protocol version check for a request carrying the given header.
     *
     * @param headerName name the header is sent with, to cover case insensitive matching
     * @param version    declared revision, or null to send no header at all
     * @return whether the request is allowed past the version gate
     */
    private boolean isSupported(String headerName, String version) throws Exception {
        Map<String, String> headers = new HashMap<>();
        if (version != null) {
            headers.put(headerName, version);
        }
        org.apache.axis2.context.MessageContext axis2MC = new org.apache.axis2.context.MessageContext();
        axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
        Axis2MessageContext messageContext = mock(Axis2MessageContext.class);
        when(messageContext.getAxis2MessageContext()).thenReturn(axis2MC);

        Method method = McpInitHandler.class.getDeclaredMethod("isSupportedMCPProtocolVersion",
                org.apache.synapse.MessageContext.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(new McpInitHandler(), messageContext);
    }
}
