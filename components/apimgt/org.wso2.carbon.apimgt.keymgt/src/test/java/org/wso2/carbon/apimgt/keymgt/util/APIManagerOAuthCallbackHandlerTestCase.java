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

package org.wso2.carbon.apimgt.keymgt.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.ScopesIssuer;
import org.wso2.carbon.apimgt.keymgt.issuers.AbstractScopesIssuer;
import org.wso2.carbon.apimgt.keymgt.issuers.RoleBasedScopesIssuer;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth.callback.OAuthCallback;

import javax.security.auth.callback.Callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {APIKeyMgtDataHolder.class, ServiceReferenceHolder.class})
public class APIManagerOAuthCallbackHandlerTestCase {

    @Test
    public void testCanHandle() throws Exception {

        Callback[] callbacks = {};
        APIManagerOAuthCallbackHandler handler = new APIManagerOAuthCallbackHandler();
        Assert.assertEquals(true, handler.canHandle(callbacks));
    }

    @Test
    public void testHandleForAccessDelegationAuthz() throws Exception {

        OAuthCallback oAuthCallback = new OAuthCallback(new AuthenticatedUser(), "client", OAuthCallback.
                OAuthCallbackType.ACCESS_DELEGATION_AUTHZ);
        Callback[] callbacks = {oAuthCallback};
        APIManagerOAuthCallbackHandler handler = new APIManagerOAuthCallbackHandler();
        handler.handle(callbacks);
        Assert.assertEquals(true, oAuthCallback.isAuthorized());
    }

    @Test
    public void testHandleForAccessDelegationTokens() throws Exception {

        OAuthCallback oAuthCallback = new OAuthCallback(new AuthenticatedUser(), "client", OAuthCallback.
                OAuthCallbackType.ACCESS_DELEGATION_TOKEN);
        Callback[] callbacks = {oAuthCallback};
        APIManagerOAuthCallbackHandler handler = new APIManagerOAuthCallbackHandler();
        handler.handle(callbacks);
        Assert.assertEquals(true, oAuthCallback.isAuthorized());
    }

    @Test
    public void testHandleForScopeValidationAuthzWhenScopesNull() throws Exception {

        OAuthCallback oAuthCallback = new OAuthCallback(new AuthenticatedUser(), "client", OAuthCallback.
                OAuthCallbackType.SCOPE_VALIDATION_AUTHZ);
        Callback[] callbacks = {oAuthCallback};
        APIManagerOAuthCallbackHandler handler = new APIManagerOAuthCallbackHandler();
        PowerMockito.mockStatic(APIKeyMgtDataHolder.class);
        AbstractScopesIssuer mockIssuer = Mockito.mock(AbstractScopesIssuer.class);
        Map<String, AbstractScopesIssuer> scopesIssuerMap = new HashMap<String, AbstractScopesIssuer>();
        scopesIssuerMap.put("wso2", mockIssuer);
        BDDMockito.given(APIKeyMgtDataHolder.getScopesIssuers()).willReturn(scopesIssuerMap);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(null);
        ScopesIssuer.loadInstance(Collections.<String>emptyList());
        handler.handle(callbacks);

        String[] scopes = oAuthCallback.getApprovedScope();
        Assert.assertEquals(1, scopes.length);
        Assert.assertEquals(APIConstants.OAUTH2_DEFAULT_SCOPE, scopes[0]);
        Assert.assertEquals(true, oAuthCallback.isValidScope());
    }

    @Test
    public void testHandleForScopeValidationAuthzWhenScopesNotNull() throws Exception {

        OAuthCallback oAuthCallback = new OAuthCallback(new AuthenticatedUser(), "client", OAuthCallback.
                OAuthCallbackType.SCOPE_VALIDATION_AUTHZ);
        String[] scopesList = {"wso2:default", "wso2:test"};
        List<String> authorizedScopes = Arrays.asList(scopesList);
        List<String> whiteListScopes = new ArrayList<>();
        oAuthCallback.setRequestedScope(scopesList);
        Callback[] callbacks = {oAuthCallback};
        APIManagerOAuthCallbackHandler handler = new APIManagerOAuthCallbackHandler();
        PowerMockito.mockStatic(APIKeyMgtDataHolder.class);
        RoleBasedScopesIssuer mockIssuer = Mockito.mock(RoleBasedScopesIssuer.class);
        Map<String, AbstractScopesIssuer> scopesIssuerMap = new HashMap<String, AbstractScopesIssuer>();
        scopesIssuerMap.put("wso2", mockIssuer);
        BDDMockito.given(APIKeyMgtDataHolder.getScopesIssuers()).willReturn(scopesIssuerMap);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(null);
        ScopesIssuer.loadInstance(whiteListScopes);
        Mockito.when(mockIssuer.getScopes(oAuthCallback,whiteListScopes)).thenReturn(authorizedScopes);
        handler.handle(callbacks);

        String[] scopes = oAuthCallback.getApprovedScope();
        Assert.assertEquals(2, scopes.length);
        Arrays.sort(scopes);
        Arrays.sort(scopesList);
        Assert.assertArrayEquals(scopesList, scopes);
        Assert.assertEquals(true, oAuthCallback.isValidScope());
    }

    @Test
    public void testHandleForScopeValidationTokenWhenScopesNull() throws Exception {

        OAuthCallback oAuthCallback = new OAuthCallback(new AuthenticatedUser(), "client", OAuthCallback.
                OAuthCallbackType.SCOPE_VALIDATION_TOKEN);
        Callback[] callbacks = {oAuthCallback};
        APIManagerOAuthCallbackHandler handler = new APIManagerOAuthCallbackHandler();
        handler.handle(callbacks);

        String[] scopes = oAuthCallback.getApprovedScope();
        Assert.assertEquals(1, scopes.length);
        Assert.assertEquals(APIConstants.OAUTH2_DEFAULT_SCOPE, scopes[0]);
        Assert.assertEquals(true, oAuthCallback.isValidScope());
    }

    @Test
    public void testHandleForScopeValidationTokenWhenScopesNotNull() throws Exception {

        OAuthCallback oAuthCallback = new OAuthCallback(new AuthenticatedUser(), "client", OAuthCallback.
                OAuthCallbackType.SCOPE_VALIDATION_TOKEN);
        String[] scopesList = {"default", "test"};
        oAuthCallback.setRequestedScope(scopesList);
        Callback[] callbacks = {oAuthCallback};
        APIManagerOAuthCallbackHandler handler = new APIManagerOAuthCallbackHandler();
        handler.handle(callbacks);

        String[] scopes = oAuthCallback.getApprovedScope();
        Assert.assertEquals(2, scopes.length);
        Arrays.sort(scopes);
        Arrays.sort(scopesList);
        Assert.assertArrayEquals(scopesList, scopes);
        Assert.assertEquals(true, oAuthCallback.isValidScope());
    }

    @Test
    public void testHandleForScopeValidationAuthzWhenScopesHaveOneElement() throws Exception {

        OAuthCallback oAuthCallback = new OAuthCallback(new AuthenticatedUser(), "client", OAuthCallback.
                OAuthCallbackType.SCOPE_VALIDATION_AUTHZ);
        String[] scopesList = {};
        oAuthCallback.setRequestedScope(scopesList);
        Callback[] callbacks = {oAuthCallback};
        APIManagerOAuthCallbackHandler handler = new APIManagerOAuthCallbackHandler();
        PowerMockito.mockStatic(APIKeyMgtDataHolder.class);
        AbstractScopesIssuer mockIssuer = Mockito.mock(AbstractScopesIssuer.class);
        Map<String, AbstractScopesIssuer> scopesIssuerMap = new HashMap<String, AbstractScopesIssuer>();
        scopesIssuerMap.put("wso2", mockIssuer);
        BDDMockito.given(APIKeyMgtDataHolder.getScopesIssuers()).willReturn(scopesIssuerMap);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(null);
        ScopesIssuer.loadInstance(Collections.<String>emptyList());
        handler.handle(callbacks);
        String[] scopes = oAuthCallback.getApprovedScope();
        Assert.assertEquals(1, scopes.length);
        Assert.assertEquals(APIConstants.OAUTH2_DEFAULT_SCOPE, scopes[0]);
        Assert.assertEquals(true, oAuthCallback.isValidScope());
    }

    @Test
    public void testHandleForScopeValidationTokenWhenScopesHaveOneElement() throws Exception {

        OAuthCallback oAuthCallback = new OAuthCallback(new AuthenticatedUser(), "client", OAuthCallback.
                OAuthCallbackType.SCOPE_VALIDATION_TOKEN);
        String[] scopesList = {};
        oAuthCallback.setRequestedScope(scopesList);
        Callback[] callbacks = {oAuthCallback};
        APIManagerOAuthCallbackHandler handler = new APIManagerOAuthCallbackHandler();
        handler.handle(callbacks);

        String[] scopes = oAuthCallback.getApprovedScope();
        Assert.assertEquals(1, scopes.length);
        Assert.assertEquals(APIConstants.OAUTH2_DEFAULT_SCOPE, scopes[0]);
        Assert.assertEquals(true, oAuthCallback.isValidScope());
    }
}