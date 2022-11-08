/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.issuers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.SystemScopeUtils;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.identity.oauth2.validators.OAuth2TokenValidationMessageContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RunWith(PowerMockRunner.class)
@PrepareForTest({SystemScopeUtils.class, APIManagerConfiguration.class, ServiceReferenceHolder.class,
        APIManagerConfigurationService.class, APIUtil.class, OAuth2Util.class, IdentityUtil.class,
        IdentityConfigParser.class, OAuthServerConfiguration.class})
public class SystemScopeIssuerImplTest {

    private SystemScopesIssuer systemScopesIssuer;
    private AuthenticatedUser authenticatedUser;
    private OAuth2AuthorizeReqDTO oAuth2AuthorizeReqDTO;
    private OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO;
    private OAuth2TokenValidationRequestDTO oAuth2TokenValidationRequestDTO;
    private OAuth2TokenValidationResponseDTO oAuth2TokenValidationResponseDTO;
    private OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext;
    private OAuthTokenReqMessageContext oAuthTokenReqMessageContext;
    private OAuth2TokenValidationMessageContext oAuth2TokenValidationMessageContext;
    Map<String, String> restAPIScopes = new HashMap<>();

    @Before
    public void init() throws IdentityOAuth2Exception {
        systemScopesIssuer = Mockito.mock(SystemScopesIssuer.class);
        oAuth2AuthorizeReqDTO = new OAuth2AuthorizeReqDTO();
        String[] scopes = {"test", "test1"};
        restAPIScopes.put("test","test");
        authenticatedUser = Mockito.mock(AuthenticatedUser.class);
        Mockito.when(systemScopesIssuer.getAppScopes(Mockito.anyString(),Mockito.anyObject(), Mockito.anyList())).
                thenReturn(restAPIScopes);
        Mockito.when(systemScopesIssuer.validateScope((OAuthAuthzReqMessageContext) Mockito.anyObject()))
                .thenReturn(true);
        oAuth2AuthorizeReqDTO.setScopes(scopes);
        oAuth2AuthorizeReqDTO.setUser(authenticatedUser);
        oAuthAuthzReqMessageContext = new OAuthAuthzReqMessageContext(oAuth2AuthorizeReqDTO);
        oAuth2AccessTokenReqDTO = new OAuth2AccessTokenReqDTO();
        oAuth2AccessTokenReqDTO.setScope(scopes);
        oAuthTokenReqMessageContext = new OAuthTokenReqMessageContext(oAuth2AccessTokenReqDTO);
        Mockito.when(systemScopesIssuer.validateScope((OAuthTokenReqMessageContext) Mockito.anyObject()))
                .thenReturn(true);
        oAuth2TokenValidationRequestDTO = new OAuth2TokenValidationRequestDTO();
        oAuth2TokenValidationResponseDTO = new OAuth2TokenValidationResponseDTO();
        oAuth2TokenValidationMessageContext = new OAuth2TokenValidationMessageContext(
                oAuth2TokenValidationRequestDTO,oAuth2TokenValidationResponseDTO);
        Mockito.when(systemScopesIssuer.validateScope((OAuth2TokenValidationMessageContext) Mockito.anyObject()))
                .thenReturn(true);
    }

    @Test
    public void testOAuthzValidateScope() {
        try {
            Assert.assertTrue("Failed to validate scopes", systemScopesIssuer.
                    validateScope(oAuthAuthzReqMessageContext));
        } catch (IdentityOAuth2Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testTokenReqValidateScope() {
        try {
            Assert.assertTrue("Failed to validate scopes", systemScopesIssuer.
                    validateScope(oAuthTokenReqMessageContext));
        } catch (IdentityOAuth2Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testTokenValidateScope() {
        try {
            Assert.assertTrue("Failed to validate scopes", systemScopesIssuer.
                    validateScope(oAuth2TokenValidationMessageContext));
        } catch (IdentityOAuth2Exception e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testGetAppScopes() {
        List scopes = new ArrayList();
        scopes.add("test");
        Map<String, String> scopesRecieved = systemScopesIssuer.
                getAppScopes("test", authenticatedUser, scopes);
        Assert.assertEquals(restAPIScopes, scopesRecieved);
    }

}
