/*
 *
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
 *
 */
package org.wso2.carbon.apimgt.impl.utils;

import com.google.common.net.InetAddresses;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.api.model.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.ApiMgtDAOMockCreator;
import org.wso2.carbon.apimgt.impl.ServiceReferenceHolderMockCreator;
import org.wso2.carbon.apimgt.impl.TestUtils;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.Notifier;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;


@RunWith(PowerMockRunner.class)
@PrepareForTest({LogFactory.class, ApiMgtDAO.class, ServiceReferenceHolder.class, APIManagerConfigurationService.class, APIManagerConfiguration.class, APIUtil.class})
public class APIUtilTierTest {
    private static byte[] tenantConf;
    private final String[] validTierNames = {"Gold", "Silver", "Bronze", "Platinum", "Medium", "100PerMinute", "50PerMinute", APIConstants.UNLIMITED_TIER};
    private final String[] tiersReturned = {"policy1", "gold", APIConstants.UNLIMITED_TIER};

    @Before
    public void setup() throws IOException {
        System.setProperty("carbon.home", "");

        File siteConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("tenant-conf.json").getFile());

        tenantConf = FileUtils.readFileToString(siteConfFile).getBytes();
    }

    /**
     * Test whether the APIUtil properly converts the billing plan and the custom attributes in the SubscriptionPolicy
     * when constructing the Tier object.
     */
    @Test
    public void testBillingPlanAndCustomAttr() throws Exception {
        SubscriptionPolicy policy = new SubscriptionPolicy("JUnitTest");
        JSONArray jsonArray = new JSONArray();

        JSONObject json1 = new JSONObject();
        json1.put("name", "key1");
        json1.put("value", "value1");
        jsonArray.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("name", "key2");
        json2.put("value", "value2");
        jsonArray.add(json2);

        policy.setCustomAttributes(jsonArray.toJSONString().getBytes());
        policy.setBillingPlan("FREE");

        Tier tier = new Tier("JUnitTest");

        APIUtil.setBillingPlanAndCustomAttributesToTier(policy, tier);

        Assert.assertTrue("Expected FREE but received " + tier.getTierPlan(), "FREE".equals(tier.getTierPlan()));

        if ("key1".equals(tier.getTierAttributes().get("name"))) {
            Assert.assertTrue("Expected to have 'value1' as the value of 'key1' but found " +
                            tier.getTierAttributes().get("value"),
                    tier.getTierAttributes().get("value").equals("value1"));
        }
        if ("key2".equals(tier.getTierAttributes().get("name"))) {
            Assert.assertTrue("Expected to have 'value2' as the value of 'key2' but found " +
                            tier.getTierAttributes().get("value"),
                    tier.getTierAttributes().get("value").equals("value2"));
        }
    }

    @Test
    public void testGetAvailableTiersWithExistingTier() throws Exception {
        Map<String, Tier> definedTiers = getDefinedTiers();

        // Select valid tier names to be assigned to the API
        Set<Tier> expectedTiers = getRoundRobinTierString(validTierNames);

        String apiName = "testApi";

        Set<Tier> availableTiers = APIUtil.getAvailableTiers(definedTiers, getTiersAsString(expectedTiers), apiName);

        Assert.assertEquals("Expected tiers do not match", expectedTiers, availableTiers);
    }

    @Test
    public void testGetAvailableTiersWithNonExisting() throws Exception {
        Map<String, Tier> definedTiers = getDefinedTiers();

        // Select valid tier names to be assigned to the API
        Set<Tier> expectedTiers = getRoundRobinTierString(validTierNames);

        Set<Tier> assignedTiers = new HashSet<Tier>(expectedTiers);
        assignedTiers.add(new Tier("Bogus"));

        String apiName = "testApi";

        Set<Tier> availableTiers = APIUtil.getAvailableTiers(definedTiers, getTiersAsString(assignedTiers), apiName);
        Assert.assertEquals("Expected tiers do not match", expectedTiers, availableTiers);

    }

    @Test
    public void testGetTiersFromSubscriptionPolicies() throws Exception {
        String policyLevel = PolicyConstants.POLICY_LEVEL_SUB;
        int tenantId = 1;

        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(tenantId);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();

        ServiceReferenceHolderMockCreator serviceReferenceHolderMockCreator =
                daoMockHolder.getServiceReferenceHolderMockCreator();

        serviceReferenceHolderMockCreator.initRegistryServiceMockCreator(true, tenantConf);

        SubscriptionPolicy[] policies = generateSubscriptionPolicies(tiersReturned);
        Mockito.when(apiMgtDAO.getSubscriptionPolicies(tenantId)).thenReturn(policies);


        Map<String, Tier> tiersFromPolicies = APIUtil.getTiersFromPolicies(policyLevel, tenantId);

        Mockito.verify(apiMgtDAO, Mockito.only()).getSubscriptionPolicies(tenantId);

        for (SubscriptionPolicy policy : policies) {
            Tier tier = tiersFromPolicies.get(policy.getPolicyName());
            Assert.assertNotNull(tier);
            Assert.assertEquals(policy.getPolicyName(), tier.getName());
            Assert.assertEquals(policy.getBillingPlan(), tier.getTierPlan());
            Assert.assertEquals(policy.getDescription(), tier.getDescription());

        }

    }

    @Test
    public void testGetTiersFromApiPolicies() throws Exception {
        String policyLevel = PolicyConstants.POLICY_LEVEL_API;
        int tenantId = 1;

        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(tenantId);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();

        ServiceReferenceHolderMockCreator serviceReferenceHolderMockCreator =
                daoMockHolder.getServiceReferenceHolderMockCreator();

        serviceReferenceHolderMockCreator.initRegistryServiceMockCreator(true, tenantConf);

        APIPolicy[] policies = generateApiPolicies(tiersReturned);
        Mockito.when(apiMgtDAO.getAPIPolicies(tenantId)).thenReturn(policies);

        Map<String, Tier> tiersFromPolicies = APIUtil.getTiersFromPolicies(policyLevel, tenantId);

        Mockito.verify(apiMgtDAO, Mockito.only()).getAPIPolicies(tenantId);

        for (APIPolicy policy : policies) {
            Tier tier = tiersFromPolicies.get(policy.getPolicyName());
            Assert.assertNotNull(tier);
            Assert.assertEquals(policy.getPolicyName(), tier.getName());
            Assert.assertEquals(policy.getDescription(), tier.getDescription());
        }
    }

    @Test
    public void testGetTiersFromAppPolicies() throws Exception {
        String policyLevel = PolicyConstants.POLICY_LEVEL_APP;
        int tenantId = 1;

        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(tenantId);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();

        ServiceReferenceHolderMockCreator serviceReferenceHolderMockCreator =
                daoMockHolder.getServiceReferenceHolderMockCreator();

        serviceReferenceHolderMockCreator.initRegistryServiceMockCreator(true, tenantConf);

        ApplicationPolicy[] policies = generateAppPolicies(tiersReturned);
        Mockito.when(apiMgtDAO.getApplicationPolicies(tenantId)).thenReturn(policies);

        Map<String, Tier> tiersFromPolicies = APIUtil.getTiersFromPolicies(policyLevel, tenantId);

        Mockito.verify(apiMgtDAO, Mockito.only()).getApplicationPolicies(tenantId);

        for (ApplicationPolicy policy : policies) {
            Tier tier = tiersFromPolicies.get(policy.getPolicyName());
            Assert.assertNotNull(tier);
            Assert.assertEquals(policy.getPolicyName(), tier.getName());
            Assert.assertEquals(policy.getDescription(), tier.getDescription());
        }
    }

    @Test
    public void testGetTiersFromApiPoliciesBandwidth() throws Exception {
        String policyLevel = PolicyConstants.POLICY_LEVEL_API;
        int tenantId = 1;

        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(tenantId);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();

        ServiceReferenceHolderMockCreator serviceReferenceHolderMockCreator =
                daoMockHolder.getServiceReferenceHolderMockCreator();

        serviceReferenceHolderMockCreator.initRegistryServiceMockCreator(true, tenantConf);

        APIPolicy[] policies = generateApiPoliciesBandwidth(tiersReturned);
        Mockito.when(apiMgtDAO.getAPIPolicies(tenantId)).thenReturn(policies);

        Map<String, Tier> tiersFromPolicies = APIUtil.getTiersFromPolicies(policyLevel, tenantId);

        Mockito.verify(apiMgtDAO, Mockito.only()).getAPIPolicies(tenantId);

        for (APIPolicy policy : policies) {
            Tier tier = tiersFromPolicies.get(policy.getPolicyName());
            Assert.assertNotNull(tier);
            Assert.assertEquals(policy.getPolicyName(), tier.getName());
            Assert.assertEquals(policy.getDescription(), tier.getDescription());
        }
    }

    @Test
    public void testAddDefaultSuperTenantAdvancedThrottlePoliciesAppLevel() throws Exception {
        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(1);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();

        String[] appPolicies = new String[]{APIConstants.DEFAULT_APP_POLICY_FIFTY_REQ_PER_MIN,
                APIConstants.DEFAULT_APP_POLICY_TWENTY_REQ_PER_MIN,
                APIConstants.DEFAULT_APP_POLICY_TEN_REQ_PER_MIN, APIConstants.DEFAULT_APP_POLICY_UNLIMITED};

        for (String policy : appPolicies) {
            Mockito.when(
                    apiMgtDAO.isPolicyExist(eq(PolicyConstants.POLICY_LEVEL_APP), eq(MultitenantConstants.SUPER_TENANT_ID),
                            eq(policy))).thenReturn(false);
        }

        try {
            APIUtil.addDefaultSuperTenantAdvancedThrottlePolicies();
            Mockito.verify(apiMgtDAO, Mockito.times(appPolicies.length)).addApplicationPolicy(Mockito.any(ApplicationPolicy.class));
        } catch (APIManagementException e) {
            Assert.assertTrue("Exception thrown", false);
        }
    }

    @Test
    public void testAddDefaultSuperTenantAdvancedThrottlePoliciesAppLevelAlreadyAdded() throws Exception {
        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(1);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();


        String[] appPolicies = new String[]{APIConstants.DEFAULT_APP_POLICY_FIFTY_REQ_PER_MIN,
                APIConstants.DEFAULT_APP_POLICY_TWENTY_REQ_PER_MIN,
                APIConstants.DEFAULT_APP_POLICY_TEN_REQ_PER_MIN, APIConstants.DEFAULT_APP_POLICY_UNLIMITED};


        // If policy added already
        for (String policy : appPolicies) {
            Mockito.when(
                    apiMgtDAO.isPolicyExist(eq(PolicyConstants.POLICY_LEVEL_APP), eq(MultitenantConstants.SUPER_TENANT_ID),
                            eq(policy))).thenReturn(true);
        }

        try {
            APIUtil.addDefaultSuperTenantAdvancedThrottlePolicies();
            Mockito.verify(apiMgtDAO, Mockito.never()).addApplicationPolicy(Mockito.any(ApplicationPolicy.class));
        } catch (APIManagementException e) {
            Assert.assertTrue("Exception thrown", false);
        }
    }

    @Test
    public void testAddDefaultSuperTenantAdvancedThrottlePoliciesSubLevel() throws Exception {
        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(1);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();

        String[] subPolicies = new String[]{APIConstants.DEFAULT_SUB_POLICY_GOLD, APIConstants.DEFAULT_SUB_POLICY_SILVER,
                APIConstants.DEFAULT_SUB_POLICY_BRONZE, APIConstants.DEFAULT_SUB_POLICY_UNAUTHENTICATED,
                APIConstants.DEFAULT_SUB_POLICY_UNLIMITED, APIConstants.DEFAULT_SUB_POLICY_ASYNC_GOLD,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_SILVER, APIConstants.DEFAULT_SUB_POLICY_ASYNC_BRONZE,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_UNLIMITED, APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_GOLD,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_SILVER, APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_BRONZE,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_UNLIMITED};

        for (String policy : subPolicies) {
            Mockito.when(
                    apiMgtDAO.isPolicyExist(eq(PolicyConstants.POLICY_LEVEL_SUB), eq(MultitenantConstants.SUPER_TENANT_ID),
                            eq(policy))).thenReturn(false);
        }

        try {
            APIUtil.addDefaultSuperTenantAdvancedThrottlePolicies();
            Mockito.verify(apiMgtDAO, Mockito.times(subPolicies.length)).addSubscriptionPolicy(Mockito.any(SubscriptionPolicy.class));
        } catch (APIManagementException e) {
            Assert.assertTrue("Exception thrown", false);
        }
    }


    @Test
    public void testAddDefaultSuperTenantAdvancedThrottlePoliciesSubLevelAlreadyAdded() throws Exception {
        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(1);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();

        String[] subPolicies = new String[]{APIConstants.DEFAULT_SUB_POLICY_GOLD, APIConstants.DEFAULT_SUB_POLICY_SILVER,
                APIConstants.DEFAULT_SUB_POLICY_BRONZE, APIConstants.DEFAULT_SUB_POLICY_UNAUTHENTICATED,
                APIConstants.DEFAULT_SUB_POLICY_UNLIMITED, APIConstants.DEFAULT_SUB_POLICY_ASYNC_GOLD,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_SILVER, APIConstants.DEFAULT_SUB_POLICY_ASYNC_BRONZE,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_UNLIMITED, APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_GOLD,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_SILVER, APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_BRONZE,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_UNLIMITED};

        for (String policy : subPolicies) {
            Mockito.when(
                    apiMgtDAO.isPolicyExist(eq(PolicyConstants.POLICY_LEVEL_SUB), eq(MultitenantConstants.SUPER_TENANT_ID),
                            eq(policy))).thenReturn(true);
        }

        try {
            APIUtil.addDefaultSuperTenantAdvancedThrottlePolicies();
            Mockito.verify(apiMgtDAO, Mockito.never()).addSubscriptionPolicy(Mockito.any(SubscriptionPolicy.class));
        } catch (APIManagementException e) {
            Assert.assertTrue("Exception thrown", false);
        }
    }

    @Test
    public void testAddDefaultSuperTenantAdvancedThrottlePoliciesApiLevel() throws Exception {
        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(1);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();

        String[] apiPolicies = new String[]{APIConstants.DEFAULT_API_POLICY_FIFTY_THOUSAND_REQ_PER_MIN,
                APIConstants.DEFAULT_API_POLICY_TWENTY_THOUSAND_REQ_PER_MIN,
                APIConstants.DEFAULT_API_POLICY_TEN_THOUSAND_REQ_PER_MIN, APIConstants.DEFAULT_API_POLICY_UNLIMITED};

        for (String policy : apiPolicies) {
            Mockito.when(
                    apiMgtDAO.isPolicyExist(eq(PolicyConstants.POLICY_LEVEL_API), eq(MultitenantConstants.SUPER_TENANT_ID),
                            eq(policy))).thenReturn(false);
        }

        try {
            APIUtil.addDefaultSuperTenantAdvancedThrottlePolicies();
            Mockito.verify(apiMgtDAO, Mockito.times(apiPolicies.length)).addAPIPolicy(Mockito.any(APIPolicy.class));
        } catch (APIManagementException e) {
            Assert.assertTrue("Exception thrown", false);
        }
    }

    @Test
    public void testAddDefaultSuperTenantAdvancedThrottlePoliciesApiLevelAlreadyAdded() throws Exception {
        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(1);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();

        String[] apiPolicies = new String[]{APIConstants.DEFAULT_API_POLICY_FIFTY_THOUSAND_REQ_PER_MIN,
                APIConstants.DEFAULT_API_POLICY_TWENTY_THOUSAND_REQ_PER_MIN,
                APIConstants.DEFAULT_API_POLICY_TEN_THOUSAND_REQ_PER_MIN, APIConstants.DEFAULT_API_POLICY_UNLIMITED};

        for (String policy : apiPolicies) {
            Mockito.when(
                    apiMgtDAO.isPolicyExist(eq(PolicyConstants.POLICY_LEVEL_API), eq(MultitenantConstants.SUPER_TENANT_ID),
                            eq(policy))).thenReturn(true);
        }

        try {
            APIUtil.addDefaultSuperTenantAdvancedThrottlePolicies();
            Mockito.verify(apiMgtDAO, Mockito.never()).addAPIPolicy(Mockito.any(APIPolicy.class));
        } catch (APIManagementException e) {
            Assert.assertTrue("Exception thrown", false);
        }
    }

    @Test
    public void testAddDefaultTenantAdvancedThrottlePoliciesAppLevel() throws Exception {
        int tenantId = 1;
        String tenantDomain = "test.com";

        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(tenantId);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();

        String[] appPolicies = new String[]{APIConstants.DEFAULT_APP_POLICY_FIFTY_REQ_PER_MIN,
                APIConstants.DEFAULT_APP_POLICY_TWENTY_REQ_PER_MIN,
                APIConstants.DEFAULT_APP_POLICY_TEN_REQ_PER_MIN, APIConstants.DEFAULT_APP_POLICY_UNLIMITED};

        for (String policy : appPolicies) {
            Mockito.when(
                    apiMgtDAO.isPolicyExist(eq(PolicyConstants.POLICY_LEVEL_APP), eq(tenantId),
                            eq(policy))).thenReturn(false);
            Mockito.when(
                    apiMgtDAO.isPolicyDeployed(eq(PolicyConstants.POLICY_LEVEL_APP), eq(tenantId),
                            eq(policy))).thenReturn(false);
        }
        mockPolicyRetrieval(apiMgtDAO);
        APIUtil.addDefaultTenantAdvancedThrottlePolicies(tenantDomain, tenantId);
        Mockito.verify(apiMgtDAO, Mockito.times(appPolicies.length)).
                addApplicationPolicy(Mockito.any(ApplicationPolicy.class));
    }


    @Test
    public void testAddDefaultTenantAdvancedThrottlePoliciesAppLevelAlreadyAdded() throws Exception {
        int tenantId = 1;
        String tenantDomain = "test.com";

        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(tenantId);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();

        String[] appPolicies = new String[]{APIConstants.DEFAULT_APP_POLICY_FIFTY_REQ_PER_MIN,
                APIConstants.DEFAULT_APP_POLICY_TWENTY_REQ_PER_MIN,
                APIConstants.DEFAULT_APP_POLICY_TEN_REQ_PER_MIN, APIConstants.DEFAULT_APP_POLICY_UNLIMITED};

        for (String policy : appPolicies) {
            Mockito.when(
                    apiMgtDAO.isPolicyExist(eq(PolicyConstants.POLICY_LEVEL_APP), eq(tenantId),
                            eq(policy))).thenReturn(true);
            Mockito.when(
                    apiMgtDAO.isPolicyDeployed(eq(PolicyConstants.POLICY_LEVEL_APP), eq(tenantId),
                            eq(policy))).thenReturn(true);
        }

        APIUtil.addDefaultTenantAdvancedThrottlePolicies(tenantDomain, tenantId);
        Mockito.verify(apiMgtDAO, Mockito.never()).
                addApplicationPolicy(Mockito.any(ApplicationPolicy.class));
        Mockito.verify(apiMgtDAO, Mockito.never()).
                setPolicyDeploymentStatus(eq(PolicyConstants.POLICY_LEVEL_APP), Mockito.anyString(), eq(tenantId), eq(true));
    }


    @Test
    public void testAddDefaultTenantAdvancedThrottlePoliciesSubLevel() throws Exception {
        int tenantId = 1;
        String tenantDomain = "test.com";

        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(tenantId);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();

        mockPolicyRetrieval(apiMgtDAO);

        String[] policies = new String[]{APIConstants.DEFAULT_SUB_POLICY_GOLD, APIConstants.DEFAULT_SUB_POLICY_SILVER,
                APIConstants.DEFAULT_SUB_POLICY_BRONZE, APIConstants.DEFAULT_SUB_POLICY_UNAUTHENTICATED,
                APIConstants.DEFAULT_SUB_POLICY_UNLIMITED, APIConstants.DEFAULT_SUB_POLICY_ASYNC_GOLD,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_SILVER, APIConstants.DEFAULT_SUB_POLICY_ASYNC_BRONZE,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_UNLIMITED, APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_GOLD,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_SILVER, APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_BRONZE,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_UNLIMITED};

        for (String policy : policies) {
            Mockito.when(
                    apiMgtDAO.isPolicyExist(eq(PolicyConstants.POLICY_LEVEL_SUB), eq(tenantId),
                            eq(policy))).thenReturn(false);
            Mockito.when(
                    apiMgtDAO.isPolicyDeployed(eq(PolicyConstants.POLICY_LEVEL_SUB), eq(tenantId),
                            eq(policy))).thenReturn(false);
        }

        APIUtil.addDefaultTenantAdvancedThrottlePolicies(tenantDomain, tenantId);
        Mockito.verify(apiMgtDAO, Mockito.times(policies.length)).
                addSubscriptionPolicy(Mockito.any(SubscriptionPolicy.class));
    }

    @Test
    public void testAddDefaultTenantAdvancedThrottlePoliciesSubLevelAlreadyAdded() throws Exception {
        int tenantId = 1;
        String tenantDomain = "test.com";

        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(tenantId);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();

        mockPolicyRetrieval(apiMgtDAO);

        String[] policies = new String[]{APIConstants.DEFAULT_SUB_POLICY_GOLD, APIConstants.DEFAULT_SUB_POLICY_SILVER,
                APIConstants.DEFAULT_SUB_POLICY_BRONZE, APIConstants.DEFAULT_SUB_POLICY_UNAUTHENTICATED,
                APIConstants.DEFAULT_SUB_POLICY_UNLIMITED, APIConstants.DEFAULT_SUB_POLICY_ASYNC_GOLD,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_SILVER, APIConstants.DEFAULT_SUB_POLICY_ASYNC_BRONZE,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_UNLIMITED, APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_GOLD,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_SILVER, APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_BRONZE,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_UNLIMITED};

        for (String policy : policies) {
            Mockito.when(
                    apiMgtDAO.isPolicyExist(eq(PolicyConstants.POLICY_LEVEL_SUB), eq(tenantId),
                            eq(policy))).thenReturn(true);
            Mockito.when(
                    apiMgtDAO.isPolicyDeployed(eq(PolicyConstants.POLICY_LEVEL_SUB), eq(tenantId),
                            eq(policy))).thenReturn(true);
        }

        APIUtil.addDefaultTenantAdvancedThrottlePolicies(tenantDomain, tenantId);
        Mockito.verify(apiMgtDAO, Mockito.never()).
                addSubscriptionPolicy(Mockito.any(SubscriptionPolicy.class));
        Mockito.verify(apiMgtDAO, Mockito.never()).
                setPolicyDeploymentStatus(eq(PolicyConstants.POLICY_LEVEL_SUB), Mockito.anyString(), eq(tenantId), eq(true));
    }

    @Test
    public void testAddDefaultTenantAdvancedThrottlePoliciesApiLevel() throws Exception {
        int tenantId = 1;
        String tenantDomain = "test.com";

        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(tenantId);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();

        mockPolicyRetrieval(apiMgtDAO);

        String[] policies = new String[]{APIConstants.DEFAULT_API_POLICY_FIFTY_THOUSAND_REQ_PER_MIN,
                APIConstants.DEFAULT_API_POLICY_TWENTY_THOUSAND_REQ_PER_MIN,
                APIConstants.DEFAULT_API_POLICY_TEN_THOUSAND_REQ_PER_MIN, APIConstants.DEFAULT_API_POLICY_UNLIMITED};

        for (String policy : policies) {
            Mockito.when(
                    apiMgtDAO.isPolicyExist(eq(PolicyConstants.POLICY_LEVEL_API), eq(tenantId),
                            eq(policy))).thenReturn(false);
            Mockito.when(
                    apiMgtDAO.isPolicyDeployed(eq(PolicyConstants.POLICY_LEVEL_API), eq(tenantId),
                            eq(policy))).thenReturn(false);
        }

        APIUtil.addDefaultTenantAdvancedThrottlePolicies(tenantDomain, tenantId);
        Mockito.verify(apiMgtDAO, Mockito.times(policies.length)).
                addAPIPolicy(Mockito.any(APIPolicy.class));
    }

    @Test
    public void testAddDefaultTenantAdvancedThrottlePoliciesApiLevelAlreadyAdded() throws Exception {
        int tenantId = 1;
        String tenantDomain = "test.com";

        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(tenantId);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();

        mockPolicyRetrieval(apiMgtDAO);

        String[] policies = new String[]{APIConstants.DEFAULT_API_POLICY_FIFTY_THOUSAND_REQ_PER_MIN,
                APIConstants.DEFAULT_API_POLICY_TWENTY_THOUSAND_REQ_PER_MIN,
                APIConstants.DEFAULT_API_POLICY_TEN_THOUSAND_REQ_PER_MIN, APIConstants.DEFAULT_API_POLICY_UNLIMITED};

        for (String policy : policies) {
            Mockito.when(
                    apiMgtDAO.isPolicyExist(eq(PolicyConstants.POLICY_LEVEL_API), eq(tenantId),
                            eq(policy))).thenReturn(true);
            Mockito.when(
                    apiMgtDAO.isPolicyDeployed(eq(PolicyConstants.POLICY_LEVEL_API), eq(tenantId),
                            eq(policy))).thenReturn(true);
        }

        APIUtil.addDefaultTenantAdvancedThrottlePolicies(tenantDomain, tenantId);
        Mockito.verify(apiMgtDAO, Mockito.never()).
                addAPIPolicy(Mockito.any(APIPolicy.class));
        Mockito.verify(apiMgtDAO, Mockito.never()).
                setPolicyDeploymentStatus(eq(PolicyConstants.POLICY_LEVEL_API), Mockito.anyString(), eq(tenantId), eq(true));
    }

    @Test
    public void testGetAllTiers() throws APIManagementException, RegistryException {
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        ThrottleProperties throttleProperties = Mockito.mock(ThrottleProperties.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        Mockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        Mockito.when(amConfig.getThrottleProperties()).thenReturn(throttleProperties);
        Mockito.when(throttleProperties.isEnableUnlimitedTier()).thenReturn(true);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry()).thenReturn(userRegistry);

        SubscriptionPolicy policies[] = new SubscriptionPolicy[3];
        policies[0] = TestUtils.getUniqueSubscriptionPolicyWithBandwidthLimit();
        policies[1] = TestUtils.getUniqueSubscriptionPolicyWithRequestCountLimit();
        policies[2] = TestUtils.getUniqueSubscriptionPolicyWithBandwidthLimit();
        Mockito.when(apiMgtDAO.getSubscriptionPolicies(Mockito.anyInt())).thenReturn(policies);

        //IsEnabled true scenario
        Assert.assertEquals(3, APIUtil.getAllTiers().size());

    }

    @Test
    public void TestIPToLong() {
        String ipString = InetAddresses.fromInteger(new Random().nextInt()).getHostAddress();
        long ipLong = APIUtil.ipToLong(ipString);
        Assert.assertEquals(ipString, longToIp(ipLong));
    }

    private String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
    }

    private SubscriptionPolicy[] generateSubscriptionPolicies(String[] policyNames) {
        List<SubscriptionPolicy> policyList = new ArrayList<SubscriptionPolicy>();

        for (String policyName : policyNames) {
            SubscriptionPolicy policy = new SubscriptionPolicy(policyName);
            policy.setBillingPlan("FREE");
            policy.setStopOnQuotaReach(true);
            policy.setRateLimitCount(1000);
            policy.setRateLimitTimeUnit("PerMinute");

            QuotaPolicy quotaPolicy = new QuotaPolicy();
            RequestCountLimit countLimit = new RequestCountLimit();
            countLimit.setRequestCount(123);
            quotaPolicy.setLimit(countLimit);

            policy.setDefaultQuotaPolicy(quotaPolicy);
            policy.setDescription(policyName);
            policyList.add(policy);
        }

        SubscriptionPolicy[] array = {};
        return policyList.toArray(array);
    }

    private APIPolicy[] generateApiPolicies(String[] policyNames) {
        List<APIPolicy> policyList = new ArrayList<APIPolicy>();

        for (String policyName : policyNames) {
            APIPolicy policy = new APIPolicy(policyName);

            QuotaPolicy quotaPolicy = new QuotaPolicy();
            RequestCountLimit countLimit = new RequestCountLimit();
            countLimit.setRequestCount(123);
            quotaPolicy.setLimit(countLimit);

            policy.setDefaultQuotaPolicy(quotaPolicy);
            policy.setDescription(policyName);
            policyList.add(policy);
        }

        APIPolicy[] array = {};
        return policyList.toArray(array);
    }

    private APIPolicy[] generateApiPoliciesBandwidth(String[] policyNames) {
        List<APIPolicy> policyList = new ArrayList<APIPolicy>();

        for (String policyName : policyNames) {
            APIPolicy policy = new APIPolicy(policyName);

            QuotaPolicy quotaPolicy = new QuotaPolicy();
            BandwidthLimit bandwidthLimit = new BandwidthLimit();
            bandwidthLimit.setDataAmount(1000);
            bandwidthLimit.setDataUnit("seconds");
            quotaPolicy.setLimit(bandwidthLimit);

            policy.setDefaultQuotaPolicy(quotaPolicy);
            policy.setDescription(policyName);
            policyList.add(policy);
        }

        APIPolicy[] array = {};
        return policyList.toArray(array);
    }

    private ApplicationPolicy[] generateAppPolicies(String[] policyNames) {
        List<ApplicationPolicy> policyList = new ArrayList<ApplicationPolicy>();

        for (String policyName : policyNames) {
            ApplicationPolicy policy = new ApplicationPolicy(policyName);

            QuotaPolicy quotaPolicy = new QuotaPolicy();
            RequestCountLimit countLimit = new RequestCountLimit();
            countLimit.setRequestCount(123);
            quotaPolicy.setLimit(countLimit);

            policy.setDefaultQuotaPolicy(quotaPolicy);
            policy.setDescription(policyName);
            policyList.add(policy);
        }

        ApplicationPolicy[] array = {};
        return policyList.toArray(array);
    }

    private Set<Tier> getRoundRobinTierString(String[] values) {
        Set<Tier> tiers = new HashSet<Tier>();
        for (int i = 0; i < values.length; ++i) {
            if (i % 2 == 0) {
                tiers.add(new Tier(values[i]));
            }
        }

        return tiers;
    }

    private Map<String, Tier> getDefinedTiers() {
        Map<String, Tier> definedTiers = new HashMap<String, Tier>();

        for (String tierName : validTierNames) {
            definedTiers.put(tierName, new Tier(tierName));
        }

        return definedTiers;
    }

    private String getTiersAsString(Set<Tier> tiers) {
        StringBuilder builder = new StringBuilder();

        for (Tier tier : tiers) {
            builder.append(tier.getName());
            builder.append("||");
        }

        return builder.toString();
    }

    private void mockPolicyRetrieval(ApiMgtDAO apiMgtDAO) throws Exception {
        QuotaPolicy quotaPolicy = Mockito.mock(QuotaPolicy.class);
        ApplicationPolicy applicationPolicy = Mockito.mock(ApplicationPolicy.class);
        SubscriptionPolicy subscriptionPolicy = Mockito.mock(SubscriptionPolicy.class);
        APIPolicy apiPolicy = Mockito.mock(APIPolicy.class);
        Mockito.when(applicationPolicy.getDefaultQuotaPolicy()).thenReturn(quotaPolicy);
        Mockito.when(subscriptionPolicy.getDefaultQuotaPolicy()).thenReturn(quotaPolicy);
        Mockito.when(apiPolicy.getDefaultQuotaPolicy()).thenReturn(quotaPolicy);
        Mockito.when(
                apiMgtDAO.getApplicationPolicy(anyString(), anyInt()))
                .thenReturn(applicationPolicy);
        Mockito.when(
                apiMgtDAO.getSubscriptionPolicy(anyString(), anyInt()))
                .thenReturn(subscriptionPolicy);
        Mockito.when(
                apiMgtDAO.getAPIPolicy(anyString(), anyInt()))
                .thenReturn(apiPolicy);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        Map<String, List<Notifier>> notifierMap = Mockito.mock(Map.class);
        List<Notifier> notifierList = new ArrayList<>();
        Mockito.when(notifierMap.get(any())).thenReturn(notifierList);
        Mockito.when(serviceReferenceHolder.getNotifiersMap()).thenReturn(notifierMap);

        APIManagerConfigurationService apiManagerConfigurationService =
                Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ThrottleProperties throttleProperties = Mockito.mock(ThrottleProperties.class);
        Map<String, Long> defaultLimits = new HashMap<>();
        Mockito.when(throttleProperties.getDefaultThrottleTierLimits()).thenReturn(defaultLimits);
        Mockito.when(apiManagerConfiguration.getThrottleProperties()).thenReturn(throttleProperties);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
    }
}
