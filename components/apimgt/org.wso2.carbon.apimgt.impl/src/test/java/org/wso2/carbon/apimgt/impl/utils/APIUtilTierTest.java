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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
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
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RunWith(PowerMockRunner.class)
@PrepareForTest({LogFactory.class, ApiMgtDAO.class, ServiceReferenceHolder.class, APIManagerConfigurationService.class, APIManagerConfiguration.class, PrivilegedCarbonContext.class})
public class APIUtilTierTest {
    private final String[] validTierNames = {"Gold", "Silver", "Bronze", "Platinum", "Medium", "100PerMinute", "50PerMinute", APIConstants.UNLIMITED_TIER};
    private final String[] tiersReturned = {"policy1", "gold", APIConstants.UNLIMITED_TIER};
    private static byte[] tenantConf;

    @BeforeClass
    public static void setup() throws IOException {
        System.setProperty("carbon.home", "");
        //String tenantConfPath =  "test" + File.pathSeparatorChar + "resources" +File.pathSeparator +
        //"org" + File.pathSeparator + "wso2" + File.pathSeparator + "carbon" + File.pathSeparator +
        //        "apimgt" + File.pathSeparator + "impl" + File.pathSeparator + "utils" + File.pathSeparator + "tenant-conf.json";
        //tenantConf = IOUtils.toString(APIUtilTierTest.class.getClassLoader().getResourceAsStream(tenantConfPath), "UTF-8");

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

        if("key1".equals(tier.getTierAttributes().get("name"))){
            Assert.assertTrue("Expected to have 'value1' as the value of 'key1' but found " +
                            tier.getTierAttributes().get("value"),
                    tier.getTierAttributes().get("value").equals("value1"));
        }
        if("key2".equals(tier.getTierAttributes().get("name"))){
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

        PrivilegedCarbonContext carbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        //Mockito.when(PrivilegedCarbonContext.startTenantFlow().thenReturn(log);
        PowerMockito.doNothing().when(PrivilegedCarbonContext.class, "startTenantFlow");
        PowerMockito.doReturn(carbonContext).when(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext");

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
}
