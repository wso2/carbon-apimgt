/*
 *
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.keymgt.handlers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;


@RunWith(PowerMockRunner.class)
@PrepareForTest({DefaultKeyValidationHandler.class, TokenValidationContext.class, APIKeyValidationInfoDTO.class,
        SubscriptionDataStore.class, SubscriptionDataHolder.class, PrivilegedCarbonContext.class})

public class DefaultKeyValidationHandlerTest extends DefaultKeyValidationHandler {

    private final String USER_NAME = "admin";
    private final String API_CONTEXT = "/apicontext/1.0.0";
    private final String API_NAME = "test_api";
    private final String API_VERSION = "1.0.0";
    private final String DEFAULT_API_VERSION = "_default_1.0.0";
    private final String SUBSCRIBER = "subscriber";
    private final String RESOURCE = "/test";
    private final String TENANT_DOMAIN = "carbon.super";
    private final String HTTP_VERB = "GET";
    private final String APPLICATION_NAME = "foo_PRODUCTION";
    private final String APPLICATION_ID = "1";
    private final int APP_ID = 1;
    private final String SCOPES = "subscriber";
    private final String ACCESS_TOKEN = "ca19a540f544777860e44e75f605d927";
    private final String TIER = "unlimited";
    private PrivilegedCarbonContext privilegedCarbonContext;
    private SubscriptionDataHolder subscriptionDataHolder;
    private SubscriptionDataStore tenantSubscriptionStore;

    @Before
    public void setup() throws Exception {
        System.setProperty(CARBON_HOME, "");
        privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        subscriptionDataHolder = Mockito.mock(SubscriptionDataHolder.class);
        tenantSubscriptionStore = Mockito.mock(SubscriptionDataStore.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(SubscriptionDataHolder.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
    }

    @Test
    public void testValidateScopes() throws APIKeyMgtException {

        API api = new API();
        api.setApiId(1);
        api.setApiProvider(USER_NAME);
        api.setApiName(API_NAME);
        api.setApiVersion(API_VERSION);
        api.setContext(API_CONTEXT);
        URLMapping urlMapping = new URLMapping();
        urlMapping.addScope(SCOPES);
        urlMapping.setHttpMethod(HTTP_VERB);
        urlMapping.setUrlPattern(RESOURCE);
        api.addResource(urlMapping);

        Map<String, API> apiMap = new HashMap<>();
        String key = API_CONTEXT + ":" + API_VERSION;
        apiMap.put(key, api);

        APIKeyValidationInfoDTO dto = new APIKeyValidationInfoDTO();
        dto.setSubscriber(SUBSCRIBER);
        dto.setApplicationName(APPLICATION_NAME);
        dto.setApplicationId(APPLICATION_ID);
        dto.setApplicationTier(TIER);
        Set<String> scopeSet = new HashSet<>();
        scopeSet.add(SCOPES);
        dto.setScopes(scopeSet);
        dto.setSubscriberTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        dto.setUserType(APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION);

        // TokenValidationContext for non default API
        TokenValidationContext param1 = new TokenValidationContext();
        param1.setValidationInfoDTO(dto);
        param1.setContext(API_CONTEXT);
        param1.setVersion(API_VERSION);
        param1.setAccessToken(ACCESS_TOKEN);
        param1.setMatchingResource(RESOURCE);
        param1.setHttpVerb(HTTP_VERB);

        // TokenValidationContext for default API version
        TokenValidationContext param2 = new TokenValidationContext();
        param2.setValidationInfoDTO(dto);
        param2.setContext(API_CONTEXT);
        param2.setVersion(DEFAULT_API_VERSION);
        param2.setAccessToken(ACCESS_TOKEN);
        param2.setMatchingResource(RESOURCE);
        param2.setHttpVerb(HTTP_VERB);

        Mockito.when(SubscriptionDataHolder.getInstance()).thenReturn(subscriptionDataHolder);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        Mockito.when(subscriptionDataHolder.getTenantSubscriptionStore(eq(TENANT_DOMAIN)))
                .thenReturn(tenantSubscriptionStore);
        Mockito.when(tenantSubscriptionStore.getApiByContextAndVersion(eq(API_CONTEXT), eq(API_VERSION)))
                .thenReturn(api);

        DefaultKeyValidationHandler defaultKeyValidationHandler = new DefaultKeyValidationHandler();
        boolean isScopeValidated = defaultKeyValidationHandler.validateScopes(param1);
        boolean isScopeValidated_default = defaultKeyValidationHandler.validateScopes(param2);

        Assert.assertTrue("Scope validation fails for API " + API_NAME, isScopeValidated);
        Assert.assertTrue("Scope validation fails for default API " + API_NAME, isScopeValidated_default);

    }
    
    @Test
    public void testInvalidSubscription() throws APIKeyMgtException {
        DefaultKeyValidationHandler defaultKeyValidationHandler = new DefaultKeyValidationHandler();
        API api = new API();
        api.setApiId(1);
        api.setApiProvider(USER_NAME);
        api.setApiName(API_NAME);
        api.setApiVersion(API_VERSION);
        api.setContext(API_CONTEXT);
        URLMapping urlMapping = new URLMapping();
        urlMapping.addScope(SCOPES);
        urlMapping.setHttpMethod(HTTP_VERB);
        urlMapping.setUrlPattern(RESOURCE);
        api.addResource(urlMapping);
        Mockito.when(SubscriptionDataHolder.getInstance()).thenReturn(subscriptionDataHolder);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        Mockito.when(subscriptionDataHolder.getTenantSubscriptionStore(eq(TENANT_DOMAIN)))
                .thenReturn(tenantSubscriptionStore);
        Mockito.when(tenantSubscriptionStore.getApiByContextAndVersion(eq(API_CONTEXT), eq(API_VERSION)))
        .thenReturn(api);
        ApplicationKeyMapping key = new ApplicationKeyMapping();
        Mockito.when(tenantSubscriptionStore.getKeyMappingByKeyAndKeyManager("xxxxxx", "default"))
                .thenReturn(key);
        key.setApplicationId(APP_ID);
        Application application = new Application();
        application.setId(APP_ID);
        application.setName(APPLICATION_NAME);
        application.setSubName(SUBSCRIBER);
        Mockito.when(tenantSubscriptionStore.getApplicationById(eq(application.getId()))).thenReturn(application);
        APIKeyValidationInfoDTO info = defaultKeyValidationHandler.validateSubscription(API_CONTEXT, API_VERSION,
                "xxxxxx", "default");
        Assert.assertEquals("Invalid error message status code ",
                APIConstants.KeyValidationStatus.API_AUTH_RESOURCE_FORBIDDEN, info.getValidationStatus());
        Assert.assertEquals(APPLICATION_NAME, info.getApplicationName());
        Assert.assertEquals(SUBSCRIBER, info.getSubscriber());

    }
}
