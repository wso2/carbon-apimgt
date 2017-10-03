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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth.callback.OAuthCallback;

import javax.security.auth.callback.Callback;
import java.util.Arrays;

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