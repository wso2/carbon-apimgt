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
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ThreatProtectionDAO;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThreatProtectionDAOImplIT extends DAOIntegrationTestBase {

    @Test
    public void testAddPolicy() throws Exception {
        ThreatProtectionPolicy policy = SampleTestObjectCreator.createUniqueThreatProtectionPolicy();
        ThreatProtectionDAO dao = DAOFactory.getThreatProtectionDAO();
        dao.addPolicy(policy);

        ThreatProtectionPolicy fromDb = dao.getPolicy(policy.getUuid());
        Assert.assertEquals(fromDb.getUuid(), policy.getUuid());
        Assert.assertEquals(fromDb.getName(), policy.getName());
        Assert.assertEquals(fromDb.getType(), policy.getType());
        Assert.assertEquals(fromDb.getPolicy(), policy.getPolicy());
    }

    @Test
    public void testGetPolicies() throws Exception {
        List<ThreatProtectionPolicy> policyList = new ArrayList<>();
        policyList.add(SampleTestObjectCreator.createUniqueThreatProtectionPolicy());
        policyList.add(SampleTestObjectCreator.createUniqueThreatProtectionPolicy());
        policyList.add(SampleTestObjectCreator.createUniqueThreatProtectionPolicy());

        ThreatProtectionDAO dao = DAOFactory.getThreatProtectionDAO();
        for (ThreatProtectionPolicy policy: policyList) {
            dao.addPolicy(policy);
        }

        int matchedCount = 0;
        List<ThreatProtectionPolicy> fromDb = dao.getPolicies();
        for (ThreatProtectionPolicy policyFromDb: fromDb) {
            for (ThreatProtectionPolicy originalPolicy: policyList) {
                if (originalPolicy.getUuid().equals(policyFromDb.getUuid())) {
                    matchedCount += 1;
                }
            }
        }

        Assert.assertEquals(matchedCount, policyList.size());
    }

    @Test
    public void testPolicyExists() throws Exception {
        ThreatProtectionPolicy policy = SampleTestObjectCreator.createUniqueThreatProtectionPolicy();
        ThreatProtectionDAO dao = DAOFactory.getThreatProtectionDAO();
        dao.addPolicy(policy);
        Assert.assertEquals(dao.isPolicyExists(policy.getUuid()), true);

        ThreatProtectionPolicy policy2 = SampleTestObjectCreator.createUniqueThreatProtectionPolicy();
        Assert.assertEquals(dao.isPolicyExists(policy2.getUuid()), false);
    }

    @Test
    public void testUpdatePolicy() throws Exception {
        ThreatProtectionPolicy policy = SampleTestObjectCreator.createUniqueThreatProtectionPolicy();
        ThreatProtectionDAO dao = DAOFactory.getThreatProtectionDAO();
        dao.addPolicy(policy);

        //make some changes, except UUID
        policy.setName("Changed name");
        policy.setPolicy("{}");
        policy.setType("XML");

        dao.updatePolicy(policy);
        ThreatProtectionPolicy fromDb = dao.getPolicy(policy.getUuid());

        Assert.assertEquals(fromDb.getUuid(), policy.getUuid());
        Assert.assertEquals(fromDb.getName(), policy.getName());
        Assert.assertEquals(fromDb.getType(), policy.getType());
        Assert.assertEquals(fromDb.getPolicy(), policy.getPolicy());
    }

    @Test
    public void testGetThreatProtectionPolicyIdsForApi() throws Exception {
        ThreatProtectionPolicy policy = SampleTestObjectCreator.createUniqueThreatProtectionPolicy();
        ThreatProtectionDAO threatProtectionDAO = DAOFactory.getThreatProtectionDAO();
        threatProtectionDAO.addPolicy(policy);

        API.APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();

        Set<String> policyIds = new HashSet<>();
        policyIds.add(policy.getUuid());
        builder = builder.threatProtectionPolicies(policyIds);
        ApiDAO dao = DAOFactory.getApiDAO();
        dao.addAPI(builder.build());

        Set<String> fromDb = threatProtectionDAO.getThreatProtectionPolicyIdsForApi(builder.getId());

        Assert.assertEquals(fromDb.toArray()[0].toString(), policy.getUuid());
    }
}
