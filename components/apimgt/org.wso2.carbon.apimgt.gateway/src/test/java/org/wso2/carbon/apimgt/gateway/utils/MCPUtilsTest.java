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

package org.wso2.carbon.apimgt.gateway.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.gateway.TestUtils;
import org.wso2.carbon.apimgt.gateway.exception.McpException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.mcp.transformer.model.ResolvedRequest;

/**
 * Unit tests for {@link MCPUtils#processRequestBody(MessageContext, ResolvedRequest, boolean, String)}.
 *
 * <p>Regression coverage for issue #5214: a tool whose schema declares no {@code requestBody}
 * (hasBody == false) left the inbound MCP JSON-RPC envelope untouched, so it was relayed to the
 * backend verbatim instead of being stripped. Per RFC 9110 a POST request body is optional, so the
 * "no body" case must still clear whatever the client originally sent, replacing it with an empty
 * JSON object rather than leaving no payload at all (leaving no payload breaks the outbound JSON
 * message formatter — see the "Deviations" note in the fix report for this issue).</p>
 */
public class MCPUtilsTest {

    private static final String INBOUND_MCP_ENVELOPE =
            "{\"method\":\"tools/call\",\"params\":{\"name\":\"post_hhh\",\"arguments\":{}},"
                    + "\"jsonrpc\":\"2.0\",\"id\":12}";

    private org.apache.axis2.context.MessageContext axis2ContextOf(MessageContext messageContext) {
        return ((Axis2MessageContext) messageContext).getAxis2MessageContext();
    }

    /**
     * Builds a real (non-mocked) message context with a default SOAP envelope attached — required
     * for {@link JsonUtil#getNewJsonPayload} to attach a JSON payload correctly.
     */
    private MessageContext newMessageContext() throws Exception {
        MessageContext messageContext = TestUtils.getMessageContext("/echo-mcp", "1.0.0");
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        axis2ContextOf(messageContext).setEnvelope(envelope);
        return messageContext;
    }

    /**
     * Simulates the state of the message before {@code processRequestBody} runs: the raw inbound
     * MCP envelope is attached as the current JSON payload, exactly as the gateway would have it
     * before any transformation.
     */
    private MessageContext messageContextWithInboundEnvelope() throws Exception {
        MessageContext messageContext = newMessageContext();
        JsonUtil.getNewJsonPayload(axis2ContextOf(messageContext), INBOUND_MCP_ENVELOPE, true, true);
        return messageContext;
    }

    /**
     * Core regression test (issue #19163): when the tool's schema declares no request body, the
     * inbound MCP JSON-RPC envelope must not be forwarded to the backend. The outbound payload must
     * be an empty JSON object, not the envelope and not an absent/unset payload.
     */
    @Test
    public void testNoBodyReplacesEnvelopeWithEmptyObject() throws Exception {
        MessageContext messageContext = messageContextWithInboundEnvelope();
        org.apache.axis2.context.MessageContext axis2MC = axis2ContextOf(messageContext);
        Assert.assertTrue("precondition: envelope must be attached before processing",
                JsonUtil.hasAJsonPayload(axis2MC));

        MCPUtils.processRequestBody(messageContext, new ResolvedRequest(), false, null);

        Assert.assertTrue("a no-body tool must still leave a well-formed JSON payload "
                + "(an absent payload breaks the outbound message formatter)", JsonUtil.hasAJsonPayload(axis2MC));
        String outboundPayload = JsonUtil.jsonPayloadToString(axis2MC);
        Assert.assertEquals("{}", outboundPayload.trim());
        Assert.assertFalse("outbound payload must not contain the JSON-RPC envelope",
                outboundPayload.contains("jsonrpc"));
    }

    /**
     * Same as above but with a content type that would be REJECTED on the hasBody=true path (see
     * {@link #testHasBodyWithUnsupportedContentTypeThrows()}), to prove the no-body path genuinely
     * ignores content type entirely rather than merely happening to pass with a JSON-compatible
     * value.
     */
    @Test
    public void testNoBodyReplacesEnvelopeRegardlessOfContentType() throws Exception {
        MessageContext messageContext = messageContextWithInboundEnvelope();
        org.apache.axis2.context.MessageContext axis2MC = axis2ContextOf(messageContext);

        MCPUtils.processRequestBody(messageContext, new ResolvedRequest(),
false, "text/plain");

        Assert.assertEquals("{}", JsonUtil.jsonPayloadToString(axis2MC).trim());
    }

    /**
     * Edge case: hasBody == false when there is no JSON payload attached at all. Must still result
     * in a well-formed empty payload, not throw.
     */
    @Test
    public void testNoBodyWhenNoPayloadPresentSetsEmptyObject() throws Exception {
        MessageContext messageContext = newMessageContext();
        org.apache.axis2.context.MessageContext axis2MC = axis2ContextOf(messageContext);
        Assert.assertFalse(JsonUtil.hasAJsonPayload(axis2MC));

        MCPUtils.processRequestBody(messageContext, new ResolvedRequest(), false, null);

        Assert.assertTrue(JsonUtil.hasAJsonPayload(axis2MC));
        Assert.assertEquals("{}", JsonUtil.jsonPayloadToString(axis2MC).trim());
    }

    /**
     * Regression guard for the pre-existing, correct behaviour: when the tool DOES declare a
     * request body, the inbound envelope must be replaced with the resolved payload (not left
     * as-is, and not simply cleared to {@code {}}). This path was not functionally changed by the
     * fix (only a variable-scope hoist), but is pinned here so a future change can't silently break
     * it.
     */
    @Test
    public void testHasBodyReplacesEnvelopeWithResolvedPayload() throws Exception {
        MessageContext messageContext = messageContextWithInboundEnvelope();
        org.apache.axis2.context.MessageContext axis2MC = axis2ContextOf(messageContext);

        JsonObject body = new JsonObject();
        body.addProperty("dsdsd", "dedede");
        ResolvedRequest resolvedRequest = new ResolvedRequest();
        resolvedRequest.setBody(body);

        MCPUtils.processRequestBody(messageContext, resolvedRequest, true, APIConstants.APPLICATION_JSON_MEDIA_TYPE);

        Assert.assertTrue(JsonUtil.hasAJsonPayload(axis2MC));
        String outboundPayload = JsonUtil.jsonPayloadToString(axis2MC);
        Assert.assertEquals(new Gson().toJson(body), new Gson().fromJson(outboundPayload, JsonObject.class).toString());
        Assert.assertFalse("outbound payload must not contain the JSON-RPC envelope",
                outboundPayload.contains("jsonrpc"));
        Assert.assertFalse("outbound payload must not contain the JSON-RPC method",
                outboundPayload.contains("tools/call"));
    }

    /**
     * When hasBody is true but the resolver produced a null body, the outbound payload defaults to
     * an empty JSON object rather than leaving the inbound envelope or throwing.
     */
    @Test
    public void testHasBodyWithNullResolvedBodyDefaultsToEmptyObject() throws Exception {
        MessageContext messageContext = messageContextWithInboundEnvelope();
        org.apache.axis2.context.MessageContext axis2MC = axis2ContextOf(messageContext);

        ResolvedRequest resolvedRequest = new ResolvedRequest();
        resolvedRequest.setBody(null);

        MCPUtils.processRequestBody(messageContext, resolvedRequest, true, APIConstants.APPLICATION_JSON_MEDIA_TYPE);

        Assert.assertTrue(JsonUtil.hasAJsonPayload(axis2MC));
        Assert.assertEquals("{}", JsonUtil.jsonPayloadToString(axis2MC).trim());
    }

    /**
     * hasBody == true with an unsupported (non-JSON) content type must fail loudly instead of
     * silently forwarding the envelope or the unsupported payload. Unchanged by the fix.
     */
    @Test
    public void testHasBodyWithUnsupportedContentTypeThrows() throws Exception {
        MessageContext messageContext = messageContextWithInboundEnvelope();
        ResolvedRequest resolvedRequest = new ResolvedRequest();
        resolvedRequest.setBody(new JsonObject());

        try {
            MCPUtils.processRequestBody(messageContext, resolvedRequest, true, "text/plain");
            Assert.fail("expected McpException for unsupported content type");
        } catch (McpException e) {
            Assert.assertTrue(String.valueOf(e.getData()).contains("Unsupported content type"));
        }
    }
}
