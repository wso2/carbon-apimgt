
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
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.ETagUtils;

import java.util.List;

public class PolicyDAOImplIT extends DAOIntegrationTestBase {

    @Test
    public void testFingerprintAfterUpdatingAPIPolicy() throws Exception {
        APIPolicy policy = SampleTestObjectCreator.createDefaultAPIPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        policyDAO.addApiPolicy(policy);
        String fingerprintBeforeUpdatingPolicy = ETagUtils
                .generateETag(policyDAO.getLastUpdatedTimeOfThrottlingPolicy(APIMgtAdminService.PolicyLevel.api,
                        policy.getPolicyName()));
        Assert.assertNotNull(fingerprintBeforeUpdatingPolicy);
        //todo: complete rest of the flow after PolicyDAO supports updating Policies
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
        //todo: complete rest of the flow after PolicyDAO supports updating Policies
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
        //todo: complete rest of the flow after PolicyDAO supports updating Policies
    }

    @Test (description = "Add, Get and Delete an API policy", expectedExceptions = APIMgtDAOException.class)
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
        Policy policyAfterDeletion = policyDAO.getApiPolicy(policy.getPolicyName());
        Assert.assertNull(policyAfterDeletion);
    }

    @Test(description = "Add, Get and Delete an Application policy", expectedExceptions = APIMgtDAOException.class)
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
        Policy policyAfterDeletion = policyDAO.getApplicationPolicy(policy.getPolicyName());
        Assert.assertNull(policyAfterDeletion);
    }

    @Test(description = "Get API Policies")
    public void testGetAPIPolicies() throws Exception {
        APIPolicy policy = SampleTestObjectCreator.createDefaultAPIPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addApiPolicy(policy);
        List<APIPolicy> policyList = policyDAO.getApiPolicies();
        Assert.assertNotNull(policyList);
    }

    @Test(description = "Get Application Policies")
    public void testGetApplicationPolicies() throws Exception {
        ApplicationPolicy policy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addApplicationPolicy(policy);
        List<ApplicationPolicy> policyList = policyDAO.getApplicationPolicies();
        Assert.assertNotNull(policyList);
    }
    @Test(description = "Get Subscription Policies")
    public void testGetSubscriptionPolicies() throws Exception {
        SubscriptionPolicy policy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        //add policy
        policyDAO.addSubscriptionPolicy(policy);
        List<SubscriptionPolicy> policyList = policyDAO.getSubscriptionPolicies();
        Assert.assertNotNull(policyList);
    }

    @Test(description = "Add,Get and Delete Subscription Policies", expectedExceptions = APIMgtDAOException.class)
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
        Policy policyAfterDeletion = policyDAO.getSubscriptionPolicy(policy.getPolicyName());
        Assert.assertNull(policyAfterDeletion);

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
}
