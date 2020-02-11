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
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Caching.class, APIUtil.class})
public class PermissionBasedScopeIssuerTestCase {

    private CacheManager cacheManager = Mockito.mock(CacheManager.class);
    private RealmService realmService = Mockito.mock(RealmService.class);
    private TenantManager tenantManager = Mockito.mock(TenantManager.class);
    private Cache cache = Mockito.mock(Cache.class);
    private UserRealm userRealm = Mockito.mock(UserRealm.class);
    private AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);

    @Test
    public void testGetPrefix() throws Exception {

        String ISSUER_PREFIX = "perm";
        PermissionBasedScopeIssuer permissionBasedScopeIssuer = new PermissionBasedScopeIssuer();
        Assert.assertEquals(ISSUER_PREFIX, permissionBasedScopeIssuer.getPrefix());
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
        Mockito.when(cacheManager.getCache(APIConstants.REST_API_SCOPE_CACHE)).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PermissionBasedScopeIssuer permissionBasedScopeIssuer =
                new PermissionBasedScopesIssuerWrapper(cacheManager, realmService, apiMgtDAO);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext msgContext = new OAuthTokenReqMessageContext(tokenDTO);
        msgContext.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        msgContext.setAuthorizedUser(authenticatedUser);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = permissionBasedScopeIssuer.getScopes(msgContext, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));

    }

    @Test
    public void testGetScopesWhenAppScopesAreEmpty() throws Exception {

        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(APIConstants.REST_API_SCOPE_CACHE)).thenReturn(cache);
        Map<String, String> restAPIScopesOfCurrentTenant = new HashMap<String, String>();
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(restAPIScopesOfCurrentTenant);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Map<String, String> appScopes = new HashMap<String, String>();
        Mockito.when(apiMgtDAO.getScopeRolesOfApplication(Mockito.anyString())).thenReturn(appScopes);
        PermissionBasedScopeIssuer permissionBasedScopeIssuer =
                new PermissionBasedScopesIssuerWrapper(cacheManager, realmService, apiMgtDAO);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext msgContext = new OAuthTokenReqMessageContext(tokenDTO);
        msgContext.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        msgContext.setAuthorizedUser(authenticatedUser);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = permissionBasedScopeIssuer.getScopes(msgContext, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));

    }

    @Test
    public void testGetScopesHandleException() throws Exception {

        Mockito.when(cacheManager.getCache(APIConstants.REST_API_SCOPE_CACHE)).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PermissionBasedScopeIssuer permissionBasedScopeIssuer =
                new PermissionBasedScopesIssuerWrapper(cacheManager, realmService, apiMgtDAO);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext msgContext = new OAuthTokenReqMessageContext(tokenDTO);
        msgContext.setScope(new String[]{"scope 1", "scope 2"});
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");
        Mockito.doThrow(APIManagementException.class).when(apiMgtDAO).getScopeRolesOfApplication("clientId");

        Assert.assertEquals(null, permissionBasedScopeIssuer.getScopes(msgContext, whiteListedScopes));

    }

    @Test
    public void testGetAuthorizedScopes() throws Exception {

        Mockito.when(cacheManager.getCache(APIConstants.REST_API_SCOPE_CACHE)).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId("carbon.super")).thenReturn(-1234);
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);

        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get("carbon.super")).thenReturn(restAPIScopes);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PermissionBasedScopeIssuer permissionBasedScopeIssuer =
                new PermissionBasedScopesIssuerWrapper(cacheManager, realmService, apiMgtDAO);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext msgContext = new OAuthTokenReqMessageContext(tokenDTO);
        msgContext.setScope(new String[]{"scope 1", "api_view"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        msgContext.setAuthorizedUser(authenticatedUser);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = permissionBasedScopeIssuer.getScopes(msgContext, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));

    }

    @Test
    public void testGetAuthorizedScopesWhenTenantISMinusOne() throws Exception {

        Mockito.when(cacheManager.getCache(APIConstants.REST_API_SCOPE_CACHE)).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId("carbon.super")).thenReturn(-1);
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);

        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get("carbon.super")).thenReturn(restAPIScopes);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PermissionBasedScopeIssuer permissionBasedScopeIssuer =
                new PermissionBasedScopesIssuerWrapper(cacheManager, realmService, apiMgtDAO);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext msgContext = new OAuthTokenReqMessageContext(tokenDTO);
        msgContext.setScope(new String[]{"scope 1", "api_view"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        msgContext.setAuthorizedUser(authenticatedUser);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = permissionBasedScopeIssuer.getScopes(msgContext, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));

    }

    @Test
    public void testGetAuthorizedScopesWhenTenantISZero() throws Exception {

        Mockito.when(cacheManager.getCache(APIConstants.REST_API_SCOPE_CACHE)).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId("carbon.super")).thenReturn(0);
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);

        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get("carbon.super")).thenReturn(restAPIScopes);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PermissionBasedScopeIssuer permissionBasedScopeIssuer =
                new PermissionBasedScopesIssuerWrapper(cacheManager, realmService, apiMgtDAO);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext msgContext = new OAuthTokenReqMessageContext(tokenDTO);
        msgContext.setScope(new String[]{"scope 1", "api_view"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        msgContext.setAuthorizedUser(authenticatedUser);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = permissionBasedScopeIssuer.getScopes(msgContext, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));

    }

    @Test
    public void testGetAuthorizedScopesWhenUserRealmIsNotNull() throws Exception {

        Mockito.when(userRealm.getAuthorizationManager()).thenReturn(authorizationManager);
        Mockito.when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        Mockito.when(cacheManager.getCache(APIConstants.REST_API_SCOPE_CACHE)).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId("carbon.super")).thenReturn(0);
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);

        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get("carbon.super")).thenReturn(restAPIScopes);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PermissionBasedScopeIssuer permissionBasedScopeIssuer =
                new PermissionBasedScopesIssuerWrapper(cacheManager, realmService, apiMgtDAO);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext msgContext = new OAuthTokenReqMessageContext(tokenDTO);
        msgContext.setScope(new String[]{"scope 1", "api_view"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        msgContext.setAuthorizedUser(authenticatedUser);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = permissionBasedScopeIssuer.getScopes(msgContext, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));

    }

    @Test
    public void testGetAuthorizedScopesWhenUserStoreIsNull() throws Exception {

        Mockito.when(userRealm.getAuthorizationManager()).thenReturn(authorizationManager);
        Mockito.when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        Mockito.when(cacheManager.getCache(APIConstants.REST_API_SCOPE_CACHE)).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId("carbon.super")).thenReturn(0);
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(authorizationManager.isUserAuthorized(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString())).thenReturn(true);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get("carbon.super")).thenReturn(restAPIScopes);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PermissionBasedScopeIssuer permissionBasedScopeIssuer =
                new PermissionBasedScopesIssuerWrapper(cacheManager, realmService, apiMgtDAO);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext msgContext = new OAuthTokenReqMessageContext(tokenDTO);
        msgContext.setScope(new String[]{"scope 1", "api_view"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        msgContext.setAuthorizedUser(authenticatedUser);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> scopes = permissionBasedScopeIssuer.getScopes(msgContext, whiteListedScopes);
        Assert.assertEquals(1, scopes.size());
        Assert.assertEquals("api_view", scopes.get(0));
    }

    @Test
    public void testGetAuthorizedScopesWhenAuthorizedManagerIsNull() throws Exception {

        Mockito.when(userRealm.getAuthorizationManager()).thenReturn(null);
        Mockito.when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        Mockito.when(cacheManager.getCache(APIConstants.REST_API_SCOPE_CACHE)).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId("carbon.super")).thenReturn(0);
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);

        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get("carbon.super")).thenReturn(restAPIScopes);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PermissionBasedScopeIssuer permissionBasedScopeIssuer =
                new PermissionBasedScopesIssuerWrapper(cacheManager, realmService, apiMgtDAO);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext msgContext = new OAuthTokenReqMessageContext(tokenDTO);
        msgContext.setScope(new String[]{"scope 1", "api_view"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        msgContext.setAuthorizedUser(authenticatedUser);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = permissionBasedScopeIssuer.getScopes(msgContext, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));

    }

    @Test
    public void testGetAuthorizedScopesWhenAppPermissionLengthIsZero() throws Exception {

        Mockito.when(userRealm.getAuthorizationManager()).thenReturn(null);
        Mockito.when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        Mockito.when(cacheManager.getCache(APIConstants.REST_API_SCOPE_CACHE)).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId("carbon.super")).thenReturn(0);
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);

        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        restAPIScopes.put("", "");
        Mockito.when(cache.get("carbon.super")).thenReturn(restAPIScopes);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PermissionBasedScopeIssuer permissionBasedScopeIssuer =
                new PermissionBasedScopesIssuerWrapper(cacheManager, realmService, apiMgtDAO);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext msgContext = new OAuthTokenReqMessageContext(tokenDTO);
        msgContext.setScope(new String[]{"", "api_view"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        msgContext.setAuthorizedUser(authenticatedUser);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> scopes = permissionBasedScopeIssuer.getScopes(msgContext, whiteListedScopes);
        Assert.assertEquals(1, scopes.size());
        Assert.assertEquals("", scopes.get(0));

    }

    @Test
    public void testGetAuthorizedScopesForAppScopes() throws Exception {

        Mockito.when(userRealm.getAuthorizationManager()).thenReturn(null);
        Mockito.when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        Mockito.when(cacheManager.getCache(APIConstants.REST_API_SCOPE_CACHE)).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId("carbon.super")).thenReturn(0);
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);

        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get("carbon.super")).thenReturn(restAPIScopes);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PermissionBasedScopeIssuer permissionBasedScopeIssuer =
                new PermissionBasedScopesIssuerWrapper(cacheManager, realmService, apiMgtDAO);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext msgContext = new OAuthTokenReqMessageContext(tokenDTO);
        msgContext.setScope(new String[]{"scope 1", "api_view"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        msgContext.setAuthorizedUser(authenticatedUser);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("api_view");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = permissionBasedScopeIssuer.getScopes(msgContext, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));

    }

    @Test
    public void testGetAuthorizedScopesForWhiteListedScopes() throws Exception {

        Mockito.when(userRealm.getAuthorizationManager()).thenReturn(null);
        Mockito.when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        Mockito.when(cacheManager.getCache(APIConstants.REST_API_SCOPE_CACHE)).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId("carbon.super")).thenReturn(0);
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);

        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get("carbon.super")).thenReturn(restAPIScopes);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PermissionBasedScopeIssuer permissionBasedScopeIssuer =
                new PermissionBasedScopesIssuerWrapper(cacheManager, realmService, apiMgtDAO);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext msgContext = new OAuthTokenReqMessageContext(tokenDTO);
        msgContext.setScope(new String[]{"scope 4", "api_view"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        msgContext.setAuthorizedUser(authenticatedUser);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("api_view");
        whiteListedScopes.add("scope 4");

        List<String> scopes = permissionBasedScopeIssuer.getScopes(msgContext, whiteListedScopes);
        Assert.assertEquals(1, scopes.size());
        Assert.assertEquals("scope 4", scopes.get(0));

    }

    @Test
    public void testGetAuthorizedScopesForUserStoreException() throws Exception {

        Mockito.when(cacheManager.getCache(APIConstants.REST_API_SCOPE_CACHE)).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.doThrow(UserStoreException.class).when(tenantManager).getTenantId(Mockito.anyString());

        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get("carbon.super")).thenReturn(restAPIScopes);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PermissionBasedScopeIssuer permissionBasedScopeIssuer =
                new PermissionBasedScopesIssuerWrapper(cacheManager, realmService, apiMgtDAO);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext msgContext = new OAuthTokenReqMessageContext(tokenDTO);
        msgContext.setScope(new String[]{"scope 4", "api_view"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        msgContext.setAuthorizedUser(authenticatedUser);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("api_view");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = permissionBasedScopeIssuer.getScopes(msgContext, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));

    }
}