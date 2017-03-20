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
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.ETagUtils;

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
}
