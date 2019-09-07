/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.tasks;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.config.ConfigManager;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.AccessTokenDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.OAuthApplicationInfoDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.HttpRequestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.TokenUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.util.mapping.throttling.AdvancedThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.TestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.HashMap;

import static org.mockito.Matchers.any;

/**
 * ThrottlingSynchronizer Task Test Class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({PrivilegedCarbonContext.class, TokenUtil.class, TenantAxisUtils.class, ServiceReferenceHolder.class,
        TenantAxisUtils.class, APIManagerFactory.class, HttpClients.class, ConfigManager.class, HttpRequestUtil.class,
        AdvancedThrottlePolicyMappingUtil.class, APIUtil.class})
public class ThrottlingSyncTaskTest {
    @Before
    public void setUp() throws Exception {
        TestUtil testUtil = new TestUtil();
        testUtil.setupCarbonHome();
    }

    @Test
    public void execute() throws Exception {
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.doReturn("8jona1j@googl.igg.biz").when(privilegedCarbonContext).getUsername();

        PowerMockito.mockStatic(TokenUtil.class);
        OAuthApplicationInfoDTO oAuthDto = Mockito.mock(OAuthApplicationInfoDTO.class);

        PowerMockito.when(TokenUtil.registerClient()).thenReturn(oAuthDto);
        Mockito.doReturn("AAHHGFHNNNN").when(oAuthDto)
                .getClientId();
        Mockito.doReturn("AJHJJJKLLO").when(oAuthDto)
                .getClientSecret();

        AccessTokenDTO accessTknDTO = Mockito.mock(AccessTokenDTO.class);
        PowerMockito.when(TokenUtil.generateAccessToken(any(String.class), any(char[].class), any(String.class)))
                .thenReturn(accessTknDTO);
        Mockito.doReturn("ssfhhh-jenfho-wfembj").when(accessTknDTO)
                .getAccessToken();

        ServiceReferenceHolder serviceDataHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService apimConfigServ = Mockito.mock(APIManagerConfigurationService.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceDataHolder);
        PowerMockito.when(serviceDataHolder.getAPIManagerConfigurationService()).thenReturn(apimConfigServ);
        APIManagerConfiguration apimConfig = Mockito.mock(APIManagerConfiguration.class);
        Mockito.doReturn(apimConfig).when(apimConfigServ).getAPIManagerConfiguration();
        Mockito.doReturn("amandaj@wso2.com").when(apimConfig)
                .getFirstProperty("APIKeyValidator.Username");

        ConfigurationContextService configContextServ = Mockito.mock(ConfigurationContextService.class);
        ConfigurationContext configContext = Mockito.mock(ConfigurationContext.class);
        PowerMockito.when(serviceDataHolder.getConfigContextService()).thenReturn(configContextServ);
        PowerMockito.when(configContextServ.getServerConfigContext()).thenReturn(configContext);

        PowerMockito.mockStatic(TenantAxisUtils.class);

        Policy[] policies = new Policy[3];
        policies[0] = new Policy("Gold");
        policies[1] = new Policy("Bronze");
        policies[2] = new Policy("Unlimited");

        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);
        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        Mockito.doReturn(apiProvider).when(apiManagerFactory).getAPIProvider(any(String.class));
        Mockito.doReturn(policies).when(apiProvider).getPolicies(any(String.class), any(String.class));

        PowerMockito.mockStatic(ConfigManager.class);
        ConfigManager configManager = Mockito.mock(ConfigManager.class);
        PowerMockito.when(ConfigManager.getConfigManager()).thenReturn(configManager);

        PowerMockito.mockStatic(APIUtil.class);
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        PowerMockito.when(APIUtil.getHttpClient(Mockito.anyInt(), Mockito.anyString())).thenReturn(httpClient);

        Mockito.when(configManager.getProperty(any(String.class)))
                .thenReturn("https://api.cloud.wso2.com/api/am/admin/v0.14/throttling/policies/subscription",
                        "https://api.cloud.wso2.com/api/am/admin/v0.14/throttling/policies/application",
                        "https://api.cloud.wso2.com/api/am/admin/v0.14/throttling/policies/advanced");

        String subPolicies = "{\"count\":2,\"list\":[{\"policyId\":\"8e73b2b4-76c2-4a0f-9520-087337395ce6\"," +
                "\"policyName\":\"Gold\",\"displayName\":\"Gold\",\"description\":" +
                "\"Allows 5000 requests per minute\",\"isDeployed\":true,\"defaultLimit\":" +
                "{\"type\":\"RequestCountLimit\",\"timeUnit\":\"min\",\"unitTime\":1,\"requestCount\":5000}," +
                "\"rateLimitCount\":0,\"rateLimitTimeUnit\":null,\"customAttributes\":[]," +
                "\"stopOnQuotaReach\":true,\"billingPlan\":\"FREE\"}," +
                "{\"policyId\":\"c78f89c6-1b5a-4d66-9033-29dc28f8fb66\",\"policyName\":\"Silver\"," +
                "\"displayName\":\"Silver\",\"description\":\"Allows 2000 requests per minute\"," +
                "\"isDeployed\":true,\"defaultLimit\":{\"type\":\"RequestCountLimit\",\"timeUnit\":\"min\"," +
                "\"unitTime\":1,\"requestCount\":2000},\"rateLimitCount\":0,\"rateLimitTimeUnit\":null," +
                "\"customAttributes\":[],\"stopOnQuotaReach\":true,\"billingPlan\":\"FREE\"}]}";

        String appPolicies = "{\"count\":2,\"list\":[{\"policyId\":\"cec243db-51cc-4198-8391-b75651805c46\"," +
                "\"policyName\":\"50PerMin\",\"displayName\":\"50PerMin\",\"description\":" +
                "\"Allows 50 request per minute\",\"isDeployed\":true,\"defaultLimit\":" +
                "{\"type\":\"RequestCountLimit\",\"timeUnit\":\"min\",\"unitTime\":1,\"requestCount\":50}}," +
                "{\"policyId\":\"bc7c6e78-3adc-4b52-aff4-603bec352f64\",\"policyName\":" +
                "\"20PerMin\",\"displayName\":\"20PerMin\",\"description\":\"Allows 20 request per minute\"," +
                "\"isDeployed\":true,\"defaultLimit\":{\"type\":\"RequestCountLimit\",\"timeUnit\":\"min\"," +
                "\"unitTime\":1,\"requestCount\":20}}]}";

        String advancedPolicies = "{\"count\":2,\"list\":[{\"policyId\":\"273080cb-b225-4622-810b-4c8025ee6fca\"," +
                "\"policyName\":\"50KPerMin\",\"displayName\":\"50KPerMin\",\"description\":" +
                "\"Allows 50000 requests per minute\",\"isDeployed\":true,\"defaultLimit\":" +
                "{\"type\":\"RequestCountLimit\",\"timeUnit\":\"min\",\"unitTime\":1,\"requestCount\":50000}}," +
                "{\"policyId\":\"98335d16-2348-4954-aefd-b81aaf9436a5\",\"policyName\":\"20KPerMin\"," +
                "\"displayName\":\"20KPerMin\",\"description\":\"Allows 20000 requests per minute\"," +
                "\"isDeployed\":true,\"defaultLimit\":{\"type\":\"RequestCountLimit\",\"timeUnit\":\"min\"," +
                "\"unitTime\":1,\"requestCount\":20000}}]}";

        String advancedPoliciesInfo1 = "{\"policyId\":\"273080cb-b225-4622-810b-4c8025ee6fca\",\"policyName\":" +
                "\"50KPerMin\",\"displayName\":\"50KPerMin\",\"description\":" +
                "\"Allows 50000 requests per minute\",\"isDeployed\":true,\"defaultLimit\":" +
                "{\"type\":\"RequestCountLimit\",\"timeUnit\":\"min\",\"unitTime\":1,\"requestCount\":50000}," +
                "\"conditionalGroups\":[]}";

        String advancedPoliciesInfo2 = "{\"policyId\":\"98335d16-2348-4954-aefd-b81aaf9436a5\"," +
                "\"policyName\":\"20KPerMin\",\"displayName\":\"20KPerMin\",\"description\":" +
                "\"Allows 20000 requests per minute\",\"isDeployed\":true,\"defaultLimit\":" +
                "{\"type\":\"RequestCountLimit\",\"timeUnit\":\"min\",\"unitTime\":1,\"requestCount\":20000}," +
                "\"conditionalGroups\":[]}";

        String blacklistPolicies = "{\"count\":0,\"list\":[]}";

        PowerMockito.mockStatic(HttpRequestUtil.class);
        PowerMockito.when(HttpRequestUtil.executeHTTPMethodWithRetry(any(HttpClient.class), any(HttpGet.class),
                any(Integer.class))).thenReturn(subPolicies, appPolicies, advancedPolicies,
                advancedPoliciesInfo1, advancedPoliciesInfo2, blacklistPolicies);

        PowerMockito.mockStatic(AdvancedThrottlePolicyMappingUtil.class);
        APIPolicy apiPolicy = Mockito.mock(APIPolicy.class);
        PowerMockito.when(AdvancedThrottlePolicyMappingUtil.
                fromAdvancedPolicyDTOToPolicy(any(AdvancedThrottlePolicyDTO.class))).thenReturn(apiPolicy);

        ThrottlingSyncTask throttlingSynchronizerTask = new ThrottlingSyncTask();
        throttlingSynchronizerTask.execute();
    }

    @Test
    public void initTest() throws Exception {
        ThrottlingSyncTask throttlingSynchronizerTask = new ThrottlingSyncTask();
        throttlingSynchronizerTask.init();
    }

    @Test
    public void setPropertiesTest() throws Exception {
        ThrottlingSyncTask throttlingSynchronizerTask = new ThrottlingSyncTask();
        throttlingSynchronizerTask.setProperties(new HashMap<String, String>());
    }
}
