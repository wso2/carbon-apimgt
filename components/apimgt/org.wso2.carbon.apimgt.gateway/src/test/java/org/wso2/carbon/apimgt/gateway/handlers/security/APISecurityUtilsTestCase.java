/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class for APISecurityUtils
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ServiceReferenceHolder.class)
public class APISecurityUtilsTestCase {

    public void testSetAuthenticationContext() {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfiguration apiMgtConfig = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiMgtConfig);
        Mockito.when(apiMgtConfig.getFirstProperty(APIConstants.API_KEY_VALIDATOR_CLIENT_TYPE)).thenReturn("WSClient");
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        AuthenticationContext authenticationContext = Mockito.mock(AuthenticationContext.class);
        Mockito.when(authenticationContext.getKeyType()).thenReturn("keyType");

        APISecurityUtils.setAuthenticationContext(messageContext, authenticationContext, "abc");
        //test when caller token is not null
        Mockito.when(authenticationContext.getCallerToken()).thenReturn("callertoken");
        Mockito.when(messageContext.getProperty(APIConstants.API_KEY_TYPE)).thenReturn("keyType");
//        Axis2MessageContext axis2MessageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Map transportHeaders = new HashMap();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(transportHeaders);

        APISecurityUtils.setAuthenticationContext(messageContext, authenticationContext, "abc");

        Assert.assertEquals(APISecurityUtils.getAuthenticationContext(messageContext).getCallerToken(),
                "callertoken");

        Assert.assertEquals("keyType", messageContext.getProperty(APIConstants.API_KEY_TYPE));

        //test for IllegalStateException
        String API_AUTH_CONTEXT = "__API_AUTH_CONTEXT";
        Mockito.when(authenticationContext.getCallerToken()).thenReturn("newCallerToken");
        Mockito.when(messageContext.getProperty(API_AUTH_CONTEXT)).thenReturn("abc");
        APISecurityUtils.setAuthenticationContext(messageContext, authenticationContext, "abc");

        Assert.assertEquals(APISecurityUtils.getAuthenticationContext(messageContext).getCallerToken(),
                "newCallerToken");
    }

}
