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

import org.apache.http.HttpStatus;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.AzureUmiTokenProvider;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.apache.synapse.transport.passthru.util.RelayUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link AzureUMIMediator}.
 * <p>
 * PowerMock is used to:
 * <ul>
 *   <li>Stub the static {@link ServiceReferenceHolder#getInstance()} call</li>
 *   <li>Intercept {@code new AzureUmiTokenProvider()} and inject a mock</li>
 *   <li>Stub the static {@link Utils#send} and {@link RelayUtils#discardRequestMessage} calls</li>
 * </ul>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, AzureUMIMediator.class, Utils.class, RelayUtils.class})
public class AzureUMIMediatorTest {

    private static final String SCOPE = "https://ai.azure.com/.default";
    private static final String TEST_TOKEN = "test-bearer-token-xyz";

    private AzureUMIMediator mediator;
    private Axis2MessageContext synapseCtx;
    private org.apache.axis2.context.MessageContext axis2Ctx;
    private AzureUmiTokenProvider mockProvider;

    @Before
    public void setUp() throws Exception {
        mediator = new AzureUMIMediator();

        // Message context mocks
        synapseCtx = Mockito.mock(Axis2MessageContext.class);
        axis2Ctx = Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(synapseCtx.getAxis2MessageContext()).thenReturn(axis2Ctx);

        // ServiceReferenceHolder → APIManagerConfiguration chain
        ServiceReferenceHolder holder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService configService =
                Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration config = Mockito.mock(APIManagerConfiguration.class);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(holder);
        Mockito.when(holder.getApiManagerConfigurationService()).thenReturn(configService);
        Mockito.when(configService.getAPIManagerConfiguration()).thenReturn(config);
        Mockito.when(config.getFirstProperty(APIConstants.AI.AZURE_UMI_SCOPE)).thenReturn(SCOPE);

        // AzureUmiTokenProvider mock
        mockProvider = Mockito.mock(AzureUmiTokenProvider.class);
        Mockito.when(mockProvider.getAccessToken()).thenReturn(TEST_TOKEN);
        PowerMockito.whenNew(AzureUmiTokenProvider.class).withNoArguments().thenReturn(mockProvider);

        // Static utilities
        PowerMockito.mockStatic(Utils.class);
        PowerMockito.mockStatic(RelayUtils.class);
    }

    // -------------------------------------------------------------------------
    // mediate() — happy path
    // -------------------------------------------------------------------------

    @Test
    public void testMediate_successfulTokenInjection_returnsTrue() {
        Map<String, Object> headers = new HashMap<>();
        Mockito.when(axis2Ctx.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(headers);

        boolean result = mediator.mediate(synapseCtx);

        Assert.assertTrue("mediate() should return true on success", result);
        Assert.assertEquals(
                APIConstants.AUTHORIZATION_HEADER_DEFAULT + " header should carry Bearer token",
                APIConstants.AUTHORIZATION_BEARER + TEST_TOKEN,
                headers.get(APIConstants.AUTHORIZATION_HEADER_DEFAULT));
    }

    @Test
    public void testMediate_nullTransportHeaders_createsMapAndInjectsToken() {
        Mockito.when(axis2Ctx.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(null);

        boolean result = mediator.mediate(synapseCtx);

        Assert.assertTrue("mediate() should return true when headers map is created fresh", result);

        // Verify a new map was set on the axis2 context
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor =
                ArgumentCaptor.forClass((Class) Map.class);
        Mockito.verify(axis2Ctx).setProperty(
                Mockito.eq(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS),
                captor.capture());

        Assert.assertEquals(
                "Bearer token should be present in the newly created headers map",
                APIConstants.AUTHORIZATION_BEARER + TEST_TOKEN,
                captor.getValue().get(APIConstants.AUTHORIZATION_HEADER_DEFAULT));
    }

    // -------------------------------------------------------------------------
    // mediate() — token acquisition failure
    // -------------------------------------------------------------------------

    @Test
    public void testMediate_tokenProviderThrows_returnsFalseAndSends500() throws Exception {
        Mockito.when(axis2Ctx.getProperty(
                        org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(new HashMap<>());
        Mockito.when(mockProvider.getAccessToken())
                .thenThrow(new APIManagementException("Workload identity credential unavailable"));

        boolean result = mediator.mediate(synapseCtx);

        Assert.assertFalse("mediate() should return false on token acquisition failure", result);

        // Verify discard + 500 response
        PowerMockito.verifyStatic(RelayUtils.class);
        RelayUtils.discardRequestMessage(axis2Ctx);

        PowerMockito.verifyStatic(Utils.class);
        Utils.send(synapseCtx, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    // -------------------------------------------------------------------------
    // Scope propagation — scope from config is passed to provider.init()
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    public void testMediate_scopePropagatedToProviderInit() throws Exception {
        Map<String, Object> headers = new HashMap<>();
        Mockito.when(axis2Ctx.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(headers);

        mediator.mediate(synapseCtx);

        ArgumentCaptor<Map> scopeCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(mockProvider).init(scopeCaptor.capture());
        Assert.assertEquals(
                "Scope read from APIManagerConfiguration must be forwarded to provider.init()",
                SCOPE,
                scopeCaptor.getValue().get(APIConstants.AI.AZURE_UMI_SCOPE_KEY));
    }

    // -------------------------------------------------------------------------
    // Lazy initialisation — provider created once and reused
    // -------------------------------------------------------------------------

    @Test
    public void testMediate_tokenProviderInitialisedOnce_reusedAcrossRequests() throws Exception {
        Map<String, Object> headers = new HashMap<>();
        Mockito.when(axis2Ctx.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(headers);

        mediator.mediate(synapseCtx);
        mediator.mediate(synapseCtx);

        // constructor should have been called exactly once
        PowerMockito.verifyNew(AzureUmiTokenProvider.class, Mockito.times(1)).withNoArguments();
    }

    // -------------------------------------------------------------------------
    // destroy()
    // -------------------------------------------------------------------------

    @Test
    public void testDestroy_nullsTokenProvider_causingReinitOnNextMediate() throws Exception {
        Map<String, Object> headers = new HashMap<>();
        Mockito.when(axis2Ctx.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(headers);

        mediator.mediate(synapseCtx); // first call — creates provider
        mediator.destroy();           // clear provider
        mediator.mediate(synapseCtx); // second call — must create a new provider

        PowerMockito.verifyNew(AzureUmiTokenProvider.class, Mockito.times(2)).withNoArguments();
    }

    // -------------------------------------------------------------------------
    // init() / isContentAware()
    // -------------------------------------------------------------------------

    @Test
    public void testInit_doesNotThrow() {
        mediator.init(null); // SynapseEnvironment is unused; must not throw
    }

    @Test
    public void testIsContentAware_returnsFalse() {
        Assert.assertFalse(mediator.isContentAware());
    }
}