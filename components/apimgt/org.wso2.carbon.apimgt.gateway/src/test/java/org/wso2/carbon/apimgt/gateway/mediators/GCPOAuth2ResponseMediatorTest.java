/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.TargetResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit tests for {@link GCPOAuth2ResponseMediator}: on a backend 401 it invalidates the exact GCP token provider
 * published on the message context by {@link GCPOAuth2Mediator}; on any other status (or when nothing is
 * published) it is a no-op and never touches the token cache.
 */
public class GCPOAuth2ResponseMediatorTest {

    private static final String TARGET_RESPONSE_PROPERTY = "pass-through.Target-Response";

    private GCPOAuth2ResponseMediator mediator;
    private Axis2MessageContext synapseCtx;
    private org.apache.axis2.context.MessageContext axis2Ctx;

    @Before
    public void setUp() {

        mediator = new GCPOAuth2ResponseMediator();
        synapseCtx = Mockito.mock(Axis2MessageContext.class);
        axis2Ctx = Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(synapseCtx.getAxis2MessageContext()).thenReturn(axis2Ctx);
    }

    private void stubBackendStatus(int status) {

        TargetResponse response = Mockito.mock(TargetResponse.class);
        Mockito.when(response.getStatus()).thenReturn(status);
        Mockito.when(axis2Ctx.getProperty(TARGET_RESPONSE_PROPERTY)).thenReturn(response);
    }

    private GCPAccessTokenProvider stubProviderOnContext() {

        GCPAccessTokenProvider provider = Mockito.mock(GCPAccessTokenProvider.class);
        Mockito.when(synapseCtx.getProperty(GCPOAuth2Mediator.GCP_TOKEN_PROVIDER_PROPERTY)).thenReturn(provider);
        return provider;
    }

    @Test
    public void testInvalidatesTokenOnBackend401() {

        stubBackendStatus(401);
        GCPAccessTokenProvider provider = stubProviderOnContext();

        Assert.assertTrue(mediator.mediate(synapseCtx));
        Mockito.verify(provider).invalidate();
    }

    @Test
    public void testDoesNotInvalidateOnSuccessResponse() {

        stubBackendStatus(200);
        GCPAccessTokenProvider provider = stubProviderOnContext();

        Assert.assertTrue(mediator.mediate(synapseCtx));
        Mockito.verify(provider, Mockito.never()).invalidate();
    }

    @Test
    public void testDoesNotInvalidateOnForbidden() {

        // 403 is a permission problem re-minting cannot fix, so the token is left cached (only 401 evicts).
        stubBackendStatus(403);
        GCPAccessTokenProvider provider = stubProviderOnContext();

        Assert.assertTrue(mediator.mediate(synapseCtx));
        Mockito.verify(provider, Mockito.never()).invalidate();
    }

    @Test
    public void test401WithNoProviderOnContextIsNoOp() {

        // Keyless/other flows may not publish a provider; a 401 must then be a safe no-op, never an NPE.
        stubBackendStatus(401);

        Assert.assertTrue(mediator.mediate(synapseCtx));
    }

    @Test
    public void testMissingTargetResponseIsNoOp() {

        // No backend response object on the context (e.g. a fault before the call) - nothing to act on.
        GCPAccessTokenProvider provider = stubProviderOnContext();

        Assert.assertTrue(mediator.mediate(synapseCtx));
        Mockito.verify(provider, Mockito.never()).invalidate();
    }
}
