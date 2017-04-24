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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.ETagUtils;

import java.util.List;

public class PolicyDAOImplIT extends DAOIntegrationTestBase {

    @Test
    public void testFingerprintAfterUpdatingAPIPolicy() throws Exception {
        APIPolicy policy = SampleTestObjectCreator.createDefaultAPIPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        policyDAO.addPolicy(APIMgtConstants.ThrottlePolicyConstants.API_LEVEL, policy);
        String fingerprintBeforeUpdatingPolicy = ETagUtils
                .generateETag(policyDAO.getLastUpdatedTimeOfAPIPolicy(policy.getPolicyName()));
        Assert.assertNotNull(fingerprintBeforeUpdatingPolicy);
        //todo: complete rest of the flow after PolicyDAO supports updating Policies
    }

    @Test
    public void testFingerprintAfterUpdatingApplicationPolicy() throws Exception {
        ApplicationPolicy policy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        policyDAO.addPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL, policy);
        String fingerprintBeforeUpdatingPolicy = ETagUtils
                .generateETag(policyDAO.getLastUpdatedTimeOfApplicationPolicy(policy.getPolicyName()));
        Assert.assertNotNull(fingerprintBeforeUpdatingPolicy);
        //todo: complete rest of the flow after PolicyDAO supports updating Policies
    }

    @Test
    public void testFingerprintAfterUpdatingSubscriptionPolicy() throws Exception {
        SubscriptionPolicy policy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        policyDAO.addPolicy(APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL, policy);
        String fingerprintBeforeUpdatingPolicy = ETagUtils
                .generateETag(policyDAO.getLastUpdatedTimeOfSubscriptionPolicy(policy.getPolicyName()));
        Assert.assertNotNull(fingerprintBeforeUpdatingPolicy);
        //todo: complete rest of the flow after PolicyDAO supports updating Policies
    }

    @Test (description = "Add, Get and Delete an API policy")
    public void testAddGetAndDeleteApiPolicy() throws Exception {
        APIPolicy policy = SampleTestObjectCreator.createDefaultAPIPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addPolicy(APIMgtConstants.ThrottlePolicyConstants.API_LEVEL, policy);
        //get added policy
        Policy addedPolicy = policyDAO.getPolicy(policy.getUserLevel(), policy.getPolicyName());
        Assert.assertNotNull(addedPolicy);
        Assert.assertEquals(addedPolicy.getPolicyName(), policy.getPolicyName());
        //delete policy
        policyDAO.deletePolicy(policy.getPolicyName(), policy.getUserLevel());
        //get policy after deletion
        Policy policyAfterDeletion = policyDAO.getPolicy(policy.getUserLevel(), policy.getPolicyName());
        Assert.assertNull(policyAfterDeletion);
    }

    @Test(description = "Add, Get and Delete an Application policy")
    public void testAddGetAndDeleteApplicationPolicy() throws Exception {
        ApplicationPolicy policy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL, policy);
        //get added policy
        Policy addedPolicy = policyDAO
                .getPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL, policy.getPolicyName());
        Assert.assertNotNull(addedPolicy);
        Assert.assertEquals(addedPolicy.getPolicyName(), policy.getPolicyName());
        //delete policy
        policyDAO.deletePolicy(policy.getPolicyName(), APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL);
        //get policy after deletion
        Policy policyAfterDeletion = policyDAO
                .getPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL, policy.getPolicyName());
        Assert.assertNull(policyAfterDeletion);
    }

    @Test(description = "Get API Policies")
    public void testGetAPIPolicies() throws Exception {
        APIPolicy policy = SampleTestObjectCreator.createDefaultAPIPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addPolicy(APIMgtConstants.ThrottlePolicyConstants.API_LEVEL, policy);
        List<Policy> policyList = policyDAO.getPolicies(APIMgtConstants.ThrottlePolicyConstants.API_LEVEL);
        Assert.assertNotNull(policyList);
    }

    @Test(description = "Get Application Policies")
    public void testGetApplicationPolicies() throws Exception {
        ApplicationPolicy policy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL, policy);
        List<Policy> policyList = policyDAO.getPolicies(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL);
        Assert.assertNotNull(policyList);
    }

    @Test(description = "Get Subscription Policies")
    public void testGetSubscriptionPolicies() throws Exception {
        SubscriptionPolicy policy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addPolicy(APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL, policy);
        List<Policy> policyList = policyDAO.getPolicies(APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL);
        Assert.assertNotNull(policyList);
    }

    @Test(description = "Add,Get and Delete Subscription Policies")
    public void testAddGetDeleteSubscriptionPolicies() throws Exception {
        SubscriptionPolicy policy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();
        policy.setUuid("3d253272-25b3-11e7-93ae-92361f002671");
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addPolicy(APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL, policy);
        //get added policy
        Policy addedPolicy = policyDAO.
                getPolicy(APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL, policy.getPolicyName());
        Assert.assertNotNull(addedPolicy);
        Assert.assertEquals(addedPolicy.getPolicyName(), policy.getPolicyName());
        //delete policy
        policyDAO.deletePolicy(policy.getPolicyName(),
                APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL);
        //get policy after deletion
        Policy policyAfterDeletion = policyDAO.getPolicy(
                APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL, policy.getPolicyName());
        Assert.assertNull(policyAfterDeletion);


    }

    @Test(description = "Get API Policies with bandwidth limit")
    public void testGetAPIPolicyWithBandwidthLimit() throws Exception {
        APIPolicy policy = SampleTestObjectCreator.createDefaultAPIPolicyWithBandwidthLimit();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addPolicy(APIMgtConstants.ThrottlePolicyConstants.API_LEVEL, policy);
        Policy policyAdded = policyDAO.getPolicy(APIMgtConstants.ThrottlePolicyConstants.API_LEVEL,
                policy.getPolicyName());
        Assert.assertNotNull(policyAdded);
        Assert.assertEquals(policyAdded.getPolicyName(), policy.getPolicyName());

    }



}
