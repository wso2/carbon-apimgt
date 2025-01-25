/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.AMDefaultKeyManagerImpl;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerDto;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * This class tests the mediate method in the OneTimeTokenRevocationMediator with different attribute settings
 */
@PrepareForTest({OneTimeTokenRevocationMediator.class, GatewayUtils.class, KeyManagerHolder.class})
@RunWith(PowerMockRunner.class)
public class OneTimeTokenRevocationMediatorTest {

    private final Axis2MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
    private final KeyManagerDto keyManagerDto = Mockito.mock(KeyManagerDto.class);
    private final AMDefaultKeyManagerImpl keyManager = Mockito.mock(AMDefaultKeyManagerImpl.class);
    private final ExecutorService oneTimeExecutorService = Mockito.mock(ExecutorService.class);
    private final AuthenticationContext authContext = Mockito.mock(AuthenticationContext.class);
    private List<String> scopes = new ArrayList<>();
    private OneTimeTokenRevocationMediator mediator = Mockito.spy(new OneTimeTokenRevocationMediator());

    @Before
    public void init() {

        PowerMockito.mockStatic(GatewayUtils.class);
        PowerMockito.mockStatic(KeyManagerHolder.class);
        Mockito.mock(APISecurityUtils.class);

        Mockito.when(messageContext.getProperty(APISecurityUtils.API_AUTH_CONTEXT)).thenReturn(authContext);

        scopes.add("OTT");
        mediator.setScope("OTT");

        Mockito.when(GatewayUtils.getTenantDomain()).thenReturn("carbon.super");

        Mockito.when(authContext.getConsumerKey()).thenReturn("consumerKey");
        Mockito.when(authContext.getAccessToken()).thenReturn("testToken");
        Mockito.when(authContext.getIssuer()).thenReturn("https://localhost:9443/oauth2/token");
        Mockito.when(authContext.getRequestTokenScopes()).thenReturn(scopes);

        List<KeyManagerDto> keymanagerList = new ArrayList<KeyManagerDto>();
        keymanagerList.add(keyManagerDto);
        Mockito.when(KeyManagerHolder.getKeyManagerByIssuer(Mockito.anyString(), Mockito.anyString())).
                thenReturn(keymanagerList);
        Mockito.when(keyManagerDto.getKeyManager()).thenReturn(keyManager);
        Mockito.doNothing().when(oneTimeExecutorService).execute(() ->
                keyManagerDto.getKeyManager().revokeOneTimeToken(Mockito.anyString(), Mockito.anyString()));
    }

    /**
     * The happy path of the method with all attributes available
     */
    @Test
    public void testMediate() {

        Assert.assertTrue(mediator.mediate(messageContext));
        Mockito.verify(authContext, Mockito.times(1)).getConsumerKey();
    }

    /**
     * AuthContext is set to null
     */
    @Test
    public void testMediateWithNullAuthContext() {

        Mockito.when(messageContext.getProperty(APISecurityUtils.API_AUTH_CONTEXT)).thenReturn(null);
        Assert.assertTrue(mediator.mediate(messageContext));
        Mockito.verify(authContext, Mockito.times(0)).getConsumerKey();
    }

    /**
     * Issuer is set to null
     */
    @Test
    public void testMediateWithNullIssuer() {

        Mockito.when(authContext.getIssuer()).thenReturn(null);
        Assert.assertTrue(mediator.mediate(messageContext));
        Mockito.verify(authContext, Mockito.times(0)).getConsumerKey();
    }

    /**
     * KeyManagerDto is set to null
     */
    @Test
    public void testMediateWithNullKeyManagerDtoList() {

        Mockito.when(KeyManagerHolder.getKeyManagerByIssuer(Mockito.anyString(), Mockito.anyString())).
                thenReturn(null);
        Assert.assertTrue(mediator.mediate(messageContext));
        Mockito.verify(authContext, Mockito.times(0)).getConsumerKey();
    }

    /**
     * Scope variable in the mediator is set to null
     */
    @Test
    public void testMediateWithNullPolicyScope() {

        mediator.setScope(null);
        Assert.assertTrue(mediator.mediate(messageContext));
        Mockito.verify(authContext, Mockito.times(0)).getConsumerKey();
    }

    /**
     * JWT token does not have any scopes
     */
    @Test
    public void testMediateWithNullTokenScopes() {

        List<String> nullScopes = new ArrayList<>();
        Mockito.when(authContext.getRequestTokenScopes()).thenReturn(nullScopes);
        Assert.assertTrue(mediator.mediate(messageContext));
        Mockito.verify(authContext, Mockito.times(0)).getConsumerKey();
    }
}
