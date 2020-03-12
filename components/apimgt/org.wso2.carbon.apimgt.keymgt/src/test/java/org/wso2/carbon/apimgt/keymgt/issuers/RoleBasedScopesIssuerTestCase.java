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

package org.wso2.carbon.apimgt.keymgt.issuers;

import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.handlers.ResourceConstants;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth.common.GrantType;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Caching.class, APIUtil.class})
public class RoleBasedScopesIssuerTestCase {

    private ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
    private RealmService realmService = Mockito.mock(RealmService.class);
    private CacheManager cacheManager = Mockito.mock(CacheManager.class);
    private TenantManager tenantManager = Mockito.mock(TenantManager.class);
    private Cache cache = Mockito.mock(Cache.class);
    private DefaultRealm defaultRealm = Mockito.mock(DefaultRealm.class);
    private AbstractUserStoreManager abstractUserStoreManager = Mockito.mock(AbstractUserStoreManager.class);

    @Test
    public void testGetPrefix() throws Exception {

        String ISSUER_PREFIX = "default";
        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuer();
        Assert.assertEquals(ISSUER_PREFIX, roleBasedScopesIssuer.getPrefix());
    }

    @Test
    public void testGetAllowedScopes() throws Exception {

        ArrayList<String> scopeSkipList = new ArrayList<String>();
        ArrayList<String> requestedScopes = new ArrayList<String>();
        scopeSkipList.add("scope 1");
        scopeSkipList.add("scope 2");
        requestedScopes.add("scope 1");
        requestedScopes.add("scope 3");
        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuer();
        List<String> authorizedScopes = roleBasedScopesIssuer.getAllowedScopes(scopeSkipList, requestedScopes);

        Assert.assertEquals(1, authorizedScopes.size());
        Assert.assertEquals("scope 1", authorizedScopes.get(0));
    }

    @Test
    public void testGetAllowedScopesWhenAuthorizedScopesEmpty() throws Exception {

        ArrayList<String> scopeSkipList = new ArrayList<String>();
        ArrayList<String> requestedScopes = new ArrayList<String>();
        scopeSkipList.add("scope 1");
        scopeSkipList.add("scope 2");
        requestedScopes.add("scope 3");
        requestedScopes.add("scope 4");
        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuer();
        List<String> authorizedScopes = roleBasedScopesIssuer.getAllowedScopes(scopeSkipList, requestedScopes);

        Assert.assertEquals(1, authorizedScopes.size());
        Assert.assertEquals("default", authorizedScopes.get(0));
    }

    @Test
    public void testGetScopes() throws Exception {

        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getTenantRESTAPIScopesConfig(Mockito.anyString())).thenReturn(new JSONObject());
        Map<String, String> scopes = new HashMap<String, String>();
        scopes.put("default", "default");
        PowerMockito.when(APIUtil.getRESTAPIScopesFromConfig(Mockito.any(), Mockito.any())).thenReturn(scopes);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Map<String, String> appScopes = new HashMap<String, String>();
        Mockito.when(apiMgtDAO.getScopeRolesOfApplication("clientId")).thenReturn(appScopes);
        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));
    }

    @Test
    public void testGetScopesWhenRestAPIScopesOfCurrentTenantIsNotNull() throws Exception {

        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));
    }

    @Test
    public void testGetScopesWhenAppScopesIsEmpty() throws Exception {

        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));
    }

    @Test
    public void testGetScopesWhenTenantISZero() throws Exception {

        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(0);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));
    }

    @Test
    public void testGetScopesWhenTenantISMinusOne() throws Exception {

        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(-1);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));
    }

    @Test
    public void testGetScopesForGrantType() throws Exception {

        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        tokenDTO.setGrantType(GrantType.SAML20_BEARER.toString());
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);
        System.setProperty(ResourceConstants.CHECK_ROLES_FROM_SAML_ASSERTION, "true");

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));
    }

    @Test
    public void testGetScopesForGrantTypeWhenSAML2NotEnabled() throws Exception {

        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Mockito.when(abstractUserStoreManager.getRoleListOfUser(Mockito.anyString())).thenReturn(new String[]{});
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        tokenDTO.setGrantType(GrantType.SAML20_BEARER.toString());
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);
        System.setProperty(ResourceConstants.CHECK_ROLES_FROM_SAML_ASSERTION, "false");

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));
    }

    @Test
    public void testGetScopesForUserStoreException() throws Exception {

        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.doThrow(UserStoreException.class).when(tenantManager).getTenantId(Mockito.anyString());
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));
    }

    @Test
    public void testGetScopesForRoles() throws Exception {

        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(abstractUserStoreManager.getRoleListOfUser(Mockito.anyString())).thenReturn
                (new String[]{"api_view"});
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "api_view"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> scopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, scopes.size());
        Assert.assertEquals("api_view", scopes.get(0));
    }

    @Test
    public void testGetScopesForRolesWhenRestAPIScopesNotNull() throws Exception {

        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(abstractUserStoreManager.getRoleListOfUser(Mockito.anyString())).thenReturn
                (new String[]{"api_view"});
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("", "");
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"", "api_view"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> scopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, scopes.size());
        Assert.assertEquals("", scopes.get(0));
    }

    @Test
    public void testGetScopesForAppScopes() throws Exception {

        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(abstractUserStoreManager.getRoleListOfUser(Mockito.anyString())).thenReturn
                (new String[]{"api_view"});
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "api_view"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 1");
        whiteListedScopes.add("scope 2");

        List<String> scopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(2, scopes.size());
    }

    @Test
    public void testGetScopesHandleException() throws Exception {

        Mockito.doThrow(APIManagementException.class).when(apiMgtDAO).getScopeRolesOfApplication(Mockito.anyString());
        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");
        Assert.assertEquals(null, roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes));
    }

    @Test
    public void testGetScopesOfRolesWithSpacesAndCases() throws Exception {
        AbstractUserStoreManager abstractUserStoreManager = Mockito.mock(AbstractUserStoreManager.class);
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getTenantRESTAPIScopesConfig(Mockito.anyString())).thenReturn(new JSONObject());
        Map<String, String> scopes = new HashMap<String, String>();
        scopes.put("default", "default");
        PowerMockito.when(APIUtil.getRESTAPIScopesFromConfig(Mockito.any(), Mockito.any())).thenReturn(scopes);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt())).thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        String[] roles = { "CaseRole", "space role" };
        Mockito.when(abstractUserStoreManager.getRoleListOfUser("caseuser@ADMIN.USER.STORE.DOMAIN")).thenReturn(roles);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[] { "caseScope1", "caseScope2", "spaceScope" });
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("caseuser");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Map<String, String> appScopes = new HashMap<String, String>();
        appScopes.put("caseScope1", "caserole");
        appScopes.put("caseScope2", "CaseRole");
        appScopes.put("spaceScope", "space role");
        Mockito.when(apiMgtDAO.getScopeRolesOfApplication("clientId")).thenReturn(appScopes);
        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService,
                apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> returnedScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(3, returnedScopes.size());
    }
}