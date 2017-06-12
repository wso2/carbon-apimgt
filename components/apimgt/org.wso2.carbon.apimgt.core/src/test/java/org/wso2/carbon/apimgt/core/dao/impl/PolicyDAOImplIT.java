
/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.TestUtil;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.BlockConditionAlreadyExistsException;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.core.models.PolicyValidationData;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.CustomPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.ETagUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PolicyDAOImplIT extends DAOIntegrationTestBase {

    private static final Logger log = LoggerFactory.getLogger(PolicyDAOImplIT.class);

    @Test
    public void testFingerprintAfterUpdatingAPIPolicy() throws Exception {
        APIPolicy policy = SampleTestObjectCreator.createDefaultAPIPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        policyDAO.addApiPolicy(policy);
        String fingerprintBeforeUpdatingPolicy = ETagUtils
                .generateETag(policyDAO.getLastUpdatedTimeOfThrottlingPolicy(APIMgtAdminService.PolicyLevel.api,
                        policy.getPolicyName()));
        Assert.assertNotNull(fingerprintBeforeUpdatingPolicy);
        APIPolicy updatedAPIPolicy = SampleTestObjectCreator.updateAPIPolicy(policy);
        Thread.sleep(100L);
        policyDAO.updateApiPolicy(updatedAPIPolicy);
        String fingerprintAfterUpdatingPolicy = ETagUtils
                .generateETag(policyDAO.getLastUpdatedTimeOfThrottlingPolicy(APIMgtAdminService.PolicyLevel.api,
                        updatedAPIPolicy.getPolicyName()));
        Assert.assertNotNull(fingerprintAfterUpdatingPolicy);
        Assert.assertNotEquals(fingerprintBeforeUpdatingPolicy, fingerprintAfterUpdatingPolicy, "Policy "
                + "fingerprint expected to be different before and after updating for policy: "
                + policy.getPolicyName());
        updatedAPIPolicy.setUuid(null);
        //test for exception
        try {
            policyDAO.updateApiPolicy(updatedAPIPolicy);
            Assert.fail("Exception expected, but not thrown.");
        } catch (APIMgtDAOException ex) {
            Assert.assertEquals(ex.getMessage(),
                    "Policy uuid is not found, unable to update policy: " + updatedAPIPolicy.getPolicyName());
        }
    }

    @Test
    public void testFingerprintAfterUpdatingApplicationPolicy() throws Exception {
        ApplicationPolicy policy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        policyDAO.addApplicationPolicy(policy);
        String fingerprintBeforeUpdatingPolicy = ETagUtils
                .generateETag(policyDAO.getLastUpdatedTimeOfThrottlingPolicy(APIMgtAdminService.PolicyLevel.application,
                        policy.getPolicyName()));
        Assert.assertNotNull(fingerprintBeforeUpdatingPolicy);
        ApplicationPolicy updatedPolicy = SampleTestObjectCreator.updateApplicationPolicy(policy);
        Thread.sleep(100L);
        policyDAO.updateApplicationPolicy(updatedPolicy);
        String fingerprintAfterUpdatingPolicy = ETagUtils
                .generateETag(policyDAO.getLastUpdatedTimeOfThrottlingPolicy(APIMgtAdminService.PolicyLevel.application,
                        updatedPolicy.getPolicyName()));
        Assert.assertNotNull(fingerprintAfterUpdatingPolicy);
        Assert.assertNotEquals(fingerprintBeforeUpdatingPolicy, fingerprintAfterUpdatingPolicy, "Policy "
                + "fingerprint expected to be different before and after updating for policy: "
                + policy.getPolicyName());
        //Test for exception uuid null
        updatedPolicy.setUuid(null);
        try {
            policyDAO.updateApplicationPolicy(updatedPolicy);
            Assert.fail("Exception expected, but not thrown.");
        } catch (APIMgtDAOException ex) {
            Assert.assertEquals(ex.getMessage(),
                    "Policy uuid is not found, unable to update policy: " + updatedPolicy.getPolicyName());
        }
    }

    @Test
    public void testFingerprintAfterUpdatingSubscriptionPolicy() throws Exception {
        SubscriptionPolicy policy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        policyDAO.addSubscriptionPolicy(policy);
        String fingerprintBeforeUpdatingPolicy = ETagUtils
                .generateETag(policyDAO.getLastUpdatedTimeOfThrottlingPolicy(APIMgtAdminService.PolicyLevel
                                .subscription, policy.getPolicyName()));
        Assert.assertNotNull(fingerprintBeforeUpdatingPolicy);
        SubscriptionPolicy updatedPolicy = SampleTestObjectCreator.updateSubscriptionPolicy(policy);
        Thread.sleep(100L);
        policyDAO.updateSubscriptionPolicy(updatedPolicy);
        String fingerprintAfterUpdatingPolicy = ETagUtils
                .generateETag(policyDAO.getLastUpdatedTimeOfThrottlingPolicy(APIMgtAdminService.PolicyLevel
                        .subscription, updatedPolicy.getPolicyName()));
        Assert.assertNotNull(fingerprintAfterUpdatingPolicy);
        Assert.assertNotEquals(fingerprintBeforeUpdatingPolicy, fingerprintAfterUpdatingPolicy, TestUtil.printDiff
                (fingerprintBeforeUpdatingPolicy, fingerprintAfterUpdatingPolicy));

        updatedPolicy.setUuid(null);
        try {
            policyDAO.updateSubscriptionPolicy(updatedPolicy);
            Assert.fail("Exception expected, but not thrown.");
        } catch (APIMgtDAOException ex) {
            Assert.assertEquals(ex.getMessage(),
                    "Policy uuid is not found, unable to update policy: " + updatedPolicy.getPolicyName());
        }
    }

    @Test (description = "Add, Get and Delete an API policy")
    public void testAddGetAndDeleteApiPolicy() throws Exception {
        APIPolicy policy = SampleTestObjectCreator.createDefaultAPIPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addApiPolicy(policy);
        //get added policy
        Policy addedPolicy = policyDAO.getApiPolicy(policy.getPolicyName());
        Assert.assertNotNull(addedPolicy);
        Assert.assertEquals(addedPolicy.getPolicyName(), policy.getPolicyName());
        //delete policy
        policyDAO.deletePolicy(APIMgtAdminService.PolicyLevel.api, policy.getPolicyName());
        //get policy after deletion
        try {
            policyDAO.getApiPolicy(policy.getPolicyName());
            Assert.fail("Exception expected, but not thrown.");
        } catch (APIMgtDAOException ex) {
            Assert.assertEquals(ex.getMessage(), "API Policy not found for name: " + addedPolicy.getPolicyName());
        }

        try {
            policyDAO.getApiPolicyByUuid(policy.getUuid());
            Assert.fail("Exception expected, but not thrown.");
        } catch (APIMgtDAOException ex) {
            Assert.assertEquals(ex.getMessage(), "API Policy not found for id: " + addedPolicy.getUuid());
        }
    }

    @Test(description = "Add, Get and Delete an Application policy")
    public void testAddGetAndDeleteApplicationPolicy() throws Exception {
        ApplicationPolicy policy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addApplicationPolicy(policy);
        //get added policy
        Policy addedPolicy = policyDAO
                .getApplicationPolicy(policy.getPolicyName());
        Assert.assertNotNull(addedPolicy);
        Assert.assertEquals(addedPolicy.getPolicyName(), policy.getPolicyName());
        //delete policy
        policyDAO.deletePolicy(APIMgtAdminService.PolicyLevel.application, policy.getPolicyName());
        //get policy after deletion
        try {
            policyDAO.getApplicationPolicy(policy.getPolicyName());
            Assert.fail("Exception expected, but not thrown.");
        } catch (APIMgtDAOException ex) {
            Assert.assertEquals(ex.getMessage(), "Application Policy not found for name: " +
                    addedPolicy.getPolicyName());
        }

        try {
            policyDAO.getApplicationPolicyByUuid(policy.getUuid());
            Assert.fail("Exception expected, but not thrown.");
        } catch (APIMgtDAOException ex) {
            Assert.assertEquals(ex.getMessage(), "Application Policy not found for id: " + addedPolicy.getUuid());
        }
    }

    @Test(description = "Add,Get and Delete Subscription Policies")
    public void testAddGetDeleteSubscriptionPolicies()
            throws Exception {
        SubscriptionPolicy policy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();
        policy.setUuid("3d253272-25b3-11e7-93ae-92361f002671");
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addSubscriptionPolicy(policy);
        //get added policy
        Policy addedPolicy = policyDAO.getSubscriptionPolicy(policy.getPolicyName());
        Assert.assertNotNull(addedPolicy);
        Assert.assertEquals(addedPolicy.getPolicyName(), policy.getPolicyName());
        //delete policy
        policyDAO.deletePolicy(APIMgtAdminService.PolicyLevel.subscription, policy.getPolicyName());
        //get policy after deletion
        try {
            policyDAO.getSubscriptionPolicy(policy.getPolicyName());
            Assert.fail("Exception expected, but not thrown.");
        } catch (APIMgtDAOException ex) {
            Assert.assertEquals(ex.getMessage(), "Subscription Policy not found for name: "
                    + addedPolicy.getPolicyName());
        }
        //test for exception: retrieving not available policy
        try {
            policyDAO.getSubscriptionPolicyByUuid(policy.getUuid());
            Assert.fail("Exception expected, but not thrown.");
        } catch (APIMgtDAOException ex) {
            Assert.assertEquals(ex.getMessage(),
                    "Subscription Policy not found for id: " + addedPolicy.getUuid());
        }
    }

    @Test(description = "Get API Policies")
    public void testGetAPIPolicies() throws Exception {
        APIPolicy policy = SampleTestObjectCreator.createDefaultAPIPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addApiPolicy(policy);
        List<APIPolicy> policyList = policyDAO.getApiPolicies();
        Assert.assertNotNull(policyList);
        Assert.assertNotNull(policyDAO.getApiPolicy(policy.getPolicyName()), "Retrieving API policy by name failed "
                + "for policy with name: " + policy.getPolicyName());
        Assert.assertNotNull(policyDAO.getApiPolicyByUuid(policy.getUuid()), "Retrieving API policy by id failed for "
                + "policy with id: " + policy.getUuid());
    }

    @Test(description = "Get Application Policies")
    public void testGetApplicationPolicies() throws Exception {
        ApplicationPolicy policy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addApplicationPolicy(policy);
        List<ApplicationPolicy> policyList = policyDAO.getApplicationPolicies();
        Assert.assertNotNull(policyList);
        Assert.assertNotNull(policyDAO.getApplicationPolicy(policy.getPolicyName()), "Retrieving Application policy by "
                + "name failed for policy with name: " + policy.getPolicyName());
        Assert.assertNotNull(policyDAO.getApplicationPolicyByUuid(policy.getUuid()), "Retrieving Application policy "
                + "by id failed for policy with id: " + policy.getUuid());
    }
    @Test(description = "Get Subscription Policies")
    public void testGetSubscriptionPolicies() throws Exception {
        SubscriptionPolicy policy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addSubscriptionPolicy(policy);
        List<SubscriptionPolicy> policyList = policyDAO.getSubscriptionPolicies();
        Assert.assertNotNull(policyList);
        Assert.assertNotNull(policyDAO.getSubscriptionPolicy(policy.getPolicyName()), "Retrieving Subscription policy "
                + "by name failed for policy with name: " + policy.getPolicyName());
        Assert.assertNotNull(policyDAO.getSubscriptionPolicyByUuid(policy.getUuid()), "Retrieving Subscription policy "
                + "by id failed for policy with id: " + policy.getUuid());
    }

    @Test(description = "Get API Policies with bandwidth limit")
    public void testGetAPIPolicyWithBandwidthLimit()
            throws Exception {
        APIPolicy policy = SampleTestObjectCreator.createDefaultAPIPolicyWithBandwidthLimit();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addApiPolicy(policy);
        Policy policyAdded = policyDAO.getApiPolicy(policy.getPolicyName());
        Assert.assertNotNull(policyAdded);
        Assert.assertEquals(policyAdded.getPolicyName(), policy.getPolicyName());
    }

    @Test(description = "policy exists test")
    public void testPolicyExists () throws Exception {
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();

        ApplicationPolicy applicationPolicy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        policyDAO.addApplicationPolicy(applicationPolicy);
        Assert.assertTrue(policyDAO.policyExists(APIMgtAdminService.PolicyLevel.application,
                applicationPolicy.getPolicyName()), "Application policy with name: " + applicationPolicy.getPolicyName()
                + " does not exist");
        policyDAO.deletePolicyByUuid(APIMgtAdminService.PolicyLevel.application, applicationPolicy.getUuid());
        Assert.assertFalse(policyDAO.policyExists(APIMgtAdminService.PolicyLevel.application,
                applicationPolicy.getPolicyName()), "Deleted Application policy with name: "
                + applicationPolicy.getPolicyName() + " still exists");

        SubscriptionPolicy subscriptionPolicy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();
        policyDAO.addSubscriptionPolicy(subscriptionPolicy);
        Assert.assertTrue(policyDAO.policyExists(APIMgtAdminService.PolicyLevel.subscription,
                subscriptionPolicy.getPolicyName()), "Subscription policy with name: "
                + subscriptionPolicy.getPolicyName() + " does not exist");
        policyDAO.deletePolicyByUuid(APIMgtAdminService.PolicyLevel.subscription, subscriptionPolicy.getUuid());
        Assert.assertFalse(policyDAO.policyExists(APIMgtAdminService.PolicyLevel.subscription,
                subscriptionPolicy.getPolicyName()), "Deleted Subscription policy with name: "
                + subscriptionPolicy.getPolicyName() + " still exists");

        APIPolicy apiPolicy = SampleTestObjectCreator.createDefaultAPIPolicy();
        policyDAO.addApiPolicy(apiPolicy);
        Assert.assertTrue(policyDAO.policyExists(APIMgtAdminService.PolicyLevel.api, apiPolicy.getPolicyName()), "API"
                + " policy with name: " + apiPolicy.getPolicyName() + " does not exist");
        policyDAO.deletePolicyByUuid(APIMgtAdminService.PolicyLevel.api, apiPolicy.getUuid());
        Assert.assertFalse(policyDAO.policyExists(APIMgtAdminService.PolicyLevel.api, apiPolicy.getPolicyName()),
                "Deleted API policy with name: " + apiPolicy.getPolicyName() + " still exists");
    }

    @Test(description = "Add, Get, Delete block condition")
    public void testAddGetUpdateDeleteBlockConditions() throws Exception {

        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();

        BlockConditions blockConditionsIP = SampleTestObjectCreator
                .createDefaultBlockCondition(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_IP);
        BlockConditions blockConditionsIpRange = SampleTestObjectCreator
                .createDefaultBlockCondition(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE);
        BlockConditions blockConditionsApi = SampleTestObjectCreator
                .createDefaultBlockCondition(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_API);
        BlockConditions blockConditionsApp = SampleTestObjectCreator
                .createDefaultBlockCondition(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_APPLICATION);
        BlockConditions blockConditionsUser = SampleTestObjectCreator
                .createDefaultBlockCondition(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_USER);

        String uuidIp = policyDAO.addBlockConditions(blockConditionsIP);
        String uuidIpRange = policyDAO.addBlockConditions(blockConditionsIpRange);
        String uuidApi = policyDAO.addBlockConditions(blockConditionsApi);
        String uuidApp = policyDAO.addBlockConditions(blockConditionsApp);
        String uuidUser = policyDAO.addBlockConditions(blockConditionsUser);

        BlockConditions blockConditionsAddedIP = policyDAO.getBlockConditionByUUID(uuidIp);
        BlockConditions blockConditionsAddedIpRange = policyDAO.getBlockConditionByUUID(uuidIpRange);
        BlockConditions blockConditionsAddedApi = policyDAO.getBlockConditionByUUID(uuidApi);
        BlockConditions blockConditionsAddedApp = policyDAO.getBlockConditionByUUID(uuidApp);
        BlockConditions blockConditionsAddedUser = policyDAO.getBlockConditionByUUID(uuidUser);

        Assert.assertEquals(blockConditionsIP.getConditionValue(), blockConditionsAddedIP.getConditionValue());
        Assert.assertEquals(blockConditionsApi.getConditionValue(), blockConditionsAddedApi.getConditionValue());
        Assert.assertEquals(blockConditionsApp.getConditionValue(), blockConditionsAddedApp.getConditionValue());
        Assert.assertEquals(blockConditionsUser.getConditionValue(), blockConditionsAddedUser.getConditionValue());
        Assert.assertEquals(blockConditionsIpRange.getStartingIP(), blockConditionsAddedIpRange.getStartingIP());

        Assert.assertTrue(policyDAO.updateBlockConditionStateByUUID(uuidIp, true));
        Assert.assertTrue(policyDAO.getBlockConditionByUUID(uuidIp).isEnabled());
        Assert.assertTrue(policyDAO.getBlockConditions().size() == 5);

        Assert.assertTrue(policyDAO.deleteBlockConditionByUuid(uuidIp));
        Assert.assertTrue(policyDAO.deleteBlockConditionByUuid(uuidIpRange));
        Assert.assertTrue(policyDAO.deleteBlockConditionByUuid(uuidApi));
        Assert.assertTrue(policyDAO.deleteBlockConditionByUuid(uuidApp));
        Assert.assertTrue(policyDAO.deleteBlockConditionByUuid(uuidUser));

    }

    @Test
    public void testAddGetUpdateDeleteCustomPolicy() throws Exception {
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        CustomPolicy customPolicy = SampleTestObjectCreator.createDefaultCustomPolicy();
        String uuid = policyDAO.addCustomPolicy(customPolicy);

        CustomPolicy policyAdded = policyDAO.getCustomPolicyByUuid(uuid);
        Assert.assertEquals(customPolicy.getSiddhiQuery(), policyAdded.getSiddhiQuery());

        policyAdded.setDescription("updated custom policy");
        policyDAO.updateCustomPolicy(policyAdded);
        Assert.assertEquals(policyDAO.getCustomPolicyByUuid(uuid).getDescription() , "updated custom policy");

        policyDAO.deleteCustomPolicy(uuid);
        CustomPolicy policyDeletion = policyDAO.getCustomPolicyByUuid(uuid);
        Assert.assertNull(policyDeletion);
    }

    @Test
    public void testGetAllPolicies() throws Exception {
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        int size = policyDAO.getAllPolicies().size();
        APIPolicy apiPolicy = SampleTestObjectCreator.createDefaultAPIPolicy();
        ApplicationPolicy applicationPolicy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        SubscriptionPolicy subscriptionPolicy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();
        policyDAO.addApiPolicy(apiPolicy);
        policyDAO.addApplicationPolicy(applicationPolicy);
        policyDAO.addSubscriptionPolicy(subscriptionPolicy);
        Set<PolicyValidationData> policyValidationData = policyDAO.getAllPolicies();
        Assert.assertTrue(policyValidationData.size() == size + 3);
    }


    @Test
    public void testGetCustomPolicies() throws Exception {
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        int size = policyDAO.getCustomPolicies().size();
        CustomPolicy customPolicy = SampleTestObjectCreator.createDefaultCustomPolicy();
        policyDAO.addCustomPolicy(customPolicy);
        Assert.assertTrue(policyDAO.getCustomPolicies().size() ==  size + 1);
    }

    @Test
    public void testGetPoliciesByLevel() throws Exception {
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        int policySize;
        //api policy by level
        policySize = policyDAO.getPoliciesByLevel(APIMgtAdminService.PolicyLevel.api).size();
        APIPolicy apiPolicy = SampleTestObjectCreator.createDefaultAPIPolicy();
        policyDAO.addApiPolicy(apiPolicy);
        Assert.assertTrue(policyDAO.getPoliciesByLevel(APIMgtAdminService.PolicyLevel.api).size() == policySize + 1);
        Assert.assertEquals(policyDAO
                .getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api, apiPolicy.getPolicyName())
                .getUuid(), apiPolicy.getUuid());
        Assert.assertEquals(
                policyDAO.getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api, apiPolicy.getPolicyName())
                        .getUuid(), apiPolicy.getUuid());

        //application policy by level
        policySize = policyDAO.getPoliciesByLevel(APIMgtAdminService.PolicyLevel.application).size();
        ApplicationPolicy applicationPolicy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        policyDAO.addApplicationPolicy(applicationPolicy);
        Assert.assertTrue(
                policyDAO.getPoliciesByLevel(APIMgtAdminService.PolicyLevel.application).size() == policySize + 1);
        Assert.assertEquals(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application,
                applicationPolicy.getPolicyName()).getUuid(), applicationPolicy.getUuid());
        Assert.assertEquals(policyDAO.getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application,
                applicationPolicy.getPolicyName()).getUuid(), applicationPolicy.getUuid());

        //subscription policy by level
        policySize = policyDAO.getPoliciesByLevel(APIMgtAdminService.PolicyLevel.subscription).size();
        SubscriptionPolicy subscriptionPolicy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();
        policyDAO.addSubscriptionPolicy(subscriptionPolicy);
        Assert.assertTrue(
                policyDAO.getPoliciesByLevel(APIMgtAdminService.PolicyLevel.subscription).size() == policySize + 1);
        Assert.assertEquals(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription,
                subscriptionPolicy.getPolicyName()).getUuid(), subscriptionPolicy.getUuid());
        Assert.assertEquals(policyDAO.getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription,
                subscriptionPolicy.getPolicyName()).getUuid(), subscriptionPolicy.getUuid());
        //When policy is not in the DB
        try {
            policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api, "test");
        } catch (APIMgtResourceNotFoundException ex) {
            Assert.assertEquals(ex.getMessage(),
                    "Policy " + APIMgtAdminService.PolicyLevel.api + "Couldn't found " + "test");
        }
    }

    @Test
    public void testGetPolicyByLevelAndUUUID() throws Exception {
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        APIPolicy apiPolicy = SampleTestObjectCreator.createDefaultAPIPolicy();
        ApplicationPolicy applicationPolicy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        SubscriptionPolicy subscriptionPolicy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();

        policyDAO.addApiPolicy(apiPolicy);
        policyDAO.addApplicationPolicy(applicationPolicy);
        policyDAO.addSubscriptionPolicy(subscriptionPolicy);

        Assert.assertEquals(policyDAO.getPolicyByLevelAndUUID(APIMgtAdminService.PolicyLevel.api, apiPolicy.getUuid())
                .getPolicyName(), apiPolicy.getPolicyName());
        Assert.assertEquals(policyDAO
                .getPolicyByLevelAndUUID(APIMgtAdminService.PolicyLevel.application, applicationPolicy.getUuid())
                .getPolicyName(), applicationPolicy.getPolicyName());
        Assert.assertEquals(policyDAO
                .getPolicyByLevelAndUUID(APIMgtAdminService.PolicyLevel.subscription, subscriptionPolicy.getUuid())
                .getPolicyName(), subscriptionPolicy.getPolicyName());
    }

    @Test
    public void testValidityOfBlockCondition() throws Exception {
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        BlockConditions blockConditionIPRange = SampleTestObjectCreator
                .createDefaultBlockCondition(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE);
        BlockConditions blockConditionAPI = SampleTestObjectCreator
                .createDefaultBlockCondition(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_API);
        BlockConditions blockConditionsApp = SampleTestObjectCreator
                .createDefaultBlockCondition(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_APPLICATION);

        //Making starting IP > ending IP
        blockConditionIPRange.setStartingIP("12.34.13.12");
        blockConditionIPRange.setEndingIP("10.32.44.32");
        //Giving invalid API context
        blockConditionAPI.setConditionValue("invalid");
        //giving invalid app name and invalid UUID
        String appArray[] = blockConditionsApp.getConditionValue().split(":");
        UUID appUuid = UUID.randomUUID();
        String appName = appArray[1];
        blockConditionsApp.setConditionValue(appUuid + ":" + appName);

        try {
            policyDAO.addBlockConditions(blockConditionIPRange);
        } catch (APIMgtDAOException ex) {
            Assert.assertEquals(ex.getMessage(),
                    "Couldn't Save Block Condition Due to Invalid IP Range -> Starting IP : " + blockConditionIPRange
                            .getStartingIP() + " EndingIP : " + blockConditionIPRange.getEndingIP());
        }
        try {
            policyDAO.addBlockConditions(blockConditionAPI);
        } catch (APIMgtDAOException ex) {
            Assert.assertEquals(ex.getMessage(),
                    "Couldn't Save Block Condition Due to Invalid API Context : " + blockConditionAPI
                            .getConditionValue());
        }

        try {
            policyDAO.addBlockConditions(blockConditionsApp);
        } catch (APIMgtDAOException ex) {
            Assert.assertEquals(ex.getMessage(),
                    "Couldn't Save Block Condition Due to Invalid Application : " + appName + ", UUID :"
                            + appUuid);
        }
        //Making IP block condition valid and add twice to check if it shows already existing
        blockConditionIPRange.setEndingIP("29.23.12.12");
        policyDAO.addBlockConditions(blockConditionIPRange);
        try {
            policyDAO.addBlockConditions(blockConditionIPRange);
        } catch (BlockConditionAlreadyExistsException ex) {
            Assert.assertEquals(ex.getMessage(),
                    "Condition with type: " + blockConditionIPRange.getConditionType() + ", value: "
                            + blockConditionIPRange.getConditionValue() + " already exists");
        }
    }
}
