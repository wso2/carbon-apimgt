/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.AWSUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

/**
 * Tests for the credential-mode branching added to {@link AWSSigV4Signer}: static credentials only
 * (unchanged behavior) vs. static credentials assuming an IAM role (new).
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(AWSUtil.class)
public class AWSSigV4SignerTest {

    private static final String ACCESS_KEY = "AKIA_TEST_ACCESS_KEY";
    private static final String SECRET_KEY = "test_secret_key";
    private static final String REGION = "us-east-1";
    private static final String SERVICE = "bedrock";
    private static final String ENDPOINT = "https://bedrock-runtime.us-east-1.amazonaws.com";
    private static final String ENDPOINT_HOST = "bedrock-runtime.us-east-1.amazonaws.com";
    private static final String ROLE_ARN = "arn:aws:iam::123456789012:role/BedrockInvokeRole";
    private static final String ROLE_REGION = "us-east-1";
    private static final String ROLE_EXTERNAL_ID = "external-id-123";

    private AWSSigV4Signer newConfiguredSigner() {
        AWSSigV4Signer signer = new AWSSigV4Signer();
        signer.setAccessKey(ACCESS_KEY);
        signer.setSecretKey(SECRET_KEY);
        signer.setRegion(REGION);
        signer.setService(SERVICE);
        signer.setEndpoint(ENDPOINT);
        return signer;
    }

    private MessageContext buildGetMessageContext() {
        SynapseConfiguration synCfg = new SynapseConfiguration();
        org.apache.axis2.context.MessageContext axisMsgCtx = new org.apache.axis2.context.MessageContext();
        axisMsgCtx.setIncomingTransportName("http");
        axisMsgCtx.setProperty(Constants.Configuration.HTTP_METHOD, "GET");
        axisMsgCtx.setProperty(Constants.Configuration.CONTENT_TYPE, "application/json");
        axisMsgCtx.setProperty(RESTConstants.REST_FULL_REQUEST_PATH, "/model/foo/converse");
        axisMsgCtx.setProperty(NhttpConstants.REST_URL_POSTFIX, "/model/foo/converse");
        axisMsgCtx.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, new HashMap<String, String>());
        // Skips real message building, since this context has no live transport-in stream.
        axisMsgCtx.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext cfgCtx = new ConfigurationContext(axisConfig);
        return new Axis2MessageContext(axisMsgCtx, synCfg, new Axis2SynapseEnvironment(cfgCtx, synCfg));
    }

    // ---------------------------------------------------------------------
    // init() validation
    // ---------------------------------------------------------------------

    @Test
    public void testInitRequiresCoreFields() {
        AWSSigV4Signer signer = new AWSSigV4Signer();
        try {
            signer.init(mock(SynapseEnvironment.class));
            fail("Expected SynapseException for missing core fields");
        } catch (SynapseException e) {
            assertTrue(e.getMessage().contains("Region, Service and Endpoint are required"));
        }
    }

    @Test
    public void testInitRejectsStoredModeWithoutKeys() {
        // Stored-credentials mode (the default) still requires access/secret keys.
        AWSSigV4Signer signer = new AWSSigV4Signer();
        signer.setRegion(REGION);
        signer.setService(SERVICE);
        signer.setEndpoint(ENDPOINT);
        try {
            signer.init(mock(SynapseEnvironment.class));
            fail("Expected SynapseException for stored mode without keys");
        } catch (SynapseException e) {
            assertTrue(e.getMessage().contains("Access Key and Secret Key are required for stored-credentials mode"));
        }
    }

    @Test
    public void testInitAllowsEnvironmentModeWithoutKeys() {
        // Environment mode resolves credentials from the runtime (EC2 instance profile / EKS IRSA),
        // so no static access/secret keys are required at configuration time.
        AWSSigV4Signer signer = new AWSSigV4Signer();
        signer.setRegion(REGION);
        signer.setService(SERVICE);
        signer.setEndpoint(ENDPOINT);
        signer.setAuthType("environment");
        signer.init(mock(SynapseEnvironment.class)); // should not throw
        signer.destroy();
    }

    @Test
    public void testInitAllowsStaticConfigWithoutRole() {
        // Existing behavior must be unaffected: no role fields configured, no exception.
        newConfiguredSigner().init(mock(SynapseEnvironment.class));
    }

    @Test
    public void testInitRejectsRoleArnWithoutRoleRegion() {
        AWSSigV4Signer signer = newConfiguredSigner();
        signer.setRoleArn(ROLE_ARN);
        try {
            signer.init(mock(SynapseEnvironment.class));
            fail("Expected SynapseException for Role ARN without Role Region");
        } catch (SynapseException e) {
            assertTrue(e.getMessage().contains("Role ARN and Role Region must be provided together"));
        }
    }

    @Test
    public void testInitRejectsRoleRegionWithoutRoleArn() {
        AWSSigV4Signer signer = newConfiguredSigner();
        signer.setRoleRegion(ROLE_REGION);
        try {
            signer.init(mock(SynapseEnvironment.class));
            fail("Expected SynapseException for Role Region without Role ARN");
        } catch (SynapseException e) {
            assertTrue(e.getMessage().contains("Role ARN and Role Region must be provided together"));
        }
    }

    @Test
    public void testInitAllowsRoleArnAndRoleRegionTogether() {
        AWSSigV4Signer signer = newConfiguredSigner();
        signer.setRoleArn(ROLE_ARN);
        signer.setRoleRegion(ROLE_REGION);
        signer.init(mock(SynapseEnvironment.class)); // should not throw
    }

    // ---------------------------------------------------------------------
    // mediate() branching: static-only vs. assumed-role
    // ---------------------------------------------------------------------

    @Test
    public void testMediateUsesPlainSignatureWhenNoRoleConfigured() throws Exception {
        PowerMockito.mockStatic(AWSUtil.class);
        Map<String, String> fakeHeaders = new HashMap<>();
        fakeHeaders.put("Authorization", "AWS4-HMAC-SHA256 Credential=" + ACCESS_KEY + "/...");
        PowerMockito.when(AWSUtil.generateAWSSignature(anyString(), anyString(), anyString(), anyString(), any(),
                anyString(), anyString(), anyString(), anyString(), isNull(), any())).thenReturn(fakeHeaders);

        AWSSigV4Signer signer = newConfiguredSigner();
        signer.init(mock(SynapseEnvironment.class));

        boolean result = signer.mediate(buildGetMessageContext());

        assertTrue(result);
        PowerMockito.verifyStatic(AWSUtil.class, times(1));
        AWSUtil.generateAWSSignature(eq(ENDPOINT_HOST), eq("GET"), eq(SERVICE), anyString(), any(), anyString(),
                eq(ACCESS_KEY), eq(SECRET_KEY), eq(REGION), isNull(), any());
        PowerMockito.verifyStatic(AWSUtil.class, times(0));
        AWSUtil.generateAWSSignatureUsingAssumeRole(anyString(), anyString(), anyString(), anyString(), any(),
                anyString(), anyString(), anyString(), anyString(), any(), anyString(), anyString(), any(), any());
    }

    @Test
    public void testMediateUsesAssumeRoleVariantWhenRoleConfigured() throws Exception {
        PowerMockito.mockStatic(AWSUtil.class);
        Map<String, String> fakeHeaders = new HashMap<>();
        fakeHeaders.put("Authorization", "AWS4-HMAC-SHA256 Credential=temporary/...");
        fakeHeaders.put("x-amz-security-token", "temporary-session-token");
        PowerMockito.when(AWSUtil.generateAWSSignatureUsingAssumeRole(anyString(), anyString(), anyString(),
                anyString(), any(), anyString(), anyString(), anyString(), anyString(), any(), anyString(),
                anyString(), any(), any())).thenReturn(fakeHeaders);

        AWSSigV4Signer signer = newConfiguredSigner();
        signer.setRoleArn(ROLE_ARN);
        signer.setRoleRegion(ROLE_REGION);
        signer.setRoleExternalId(ROLE_EXTERNAL_ID);
        signer.init(mock(SynapseEnvironment.class));

        boolean result = signer.mediate(buildGetMessageContext());

        assertTrue(result);
        PowerMockito.verifyStatic(AWSUtil.class, times(1));
        AWSUtil.generateAWSSignatureUsingAssumeRole(eq(ENDPOINT_HOST), eq("GET"), eq(SERVICE), anyString(), any(),
                anyString(), eq(ACCESS_KEY), eq(SECRET_KEY), eq(REGION), isNull(), eq(ROLE_ARN), eq(ROLE_REGION),
                eq(ROLE_EXTERNAL_ID), any());
        PowerMockito.verifyStatic(AWSUtil.class, times(0));
        AWSUtil.generateAWSSignature(anyString(), anyString(), anyString(), anyString(), any(), anyString(),
                anyString(), anyString(), anyString(), any(), any());
    }

    @Test
    public void testMediateWrapsAssumeRoleFailureAsSynapseException() throws Exception {
        PowerMockito.mockStatic(AWSUtil.class);
        PowerMockito.when(AWSUtil.generateAWSSignatureUsingAssumeRole(anyString(), anyString(), anyString(),
                anyString(), any(), anyString(), anyString(), anyString(), anyString(), any(), anyString(),
                anyString(), any(), any())).thenThrow(new APIManagementException("Failed to assume role: 403: AccessDenied"));

        AWSSigV4Signer signer = newConfiguredSigner();
        signer.setRoleArn(ROLE_ARN);
        signer.setRoleRegion(ROLE_REGION);
        signer.init(mock(SynapseEnvironment.class));

        try {
            signer.mediate(buildGetMessageContext());
            fail("Expected SynapseException when AssumeRole fails");
        } catch (SynapseException e) {
            assertTrue(e.getMessage().contains("Error while signing the request with AWS SigV4"));
            assertTrue(e.getCause() instanceof APIManagementException);
        }
    }
}
