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

package org.wso2.carbon.apimgt.core.impl;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.PolicyValidationData;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.CustomPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class APIMgtAdminServiceImplTestCase {

    private static final Integer LIMIT = 2;
    private static final String API_VERSION = "1.0.0";
    private static final String API_ID = "erbde56e-4512-498d-b6dc-85a6f1f8b058";
    private static final String API_CONTEXT = "/testContext";
    private static final String POLICY_NAME = "policyName";
    private static final String POLICY_ID = "drbde46a-4512-498e-b6dr-85a5f1f8b055";
    private static final String BLOCK_CONDITION_TYPE = "Test_condition_type";

    @Test(description = "Get api subscriptions")
    public void testGetAPISubscriptions() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(apiSubscriptionDAO);
        Mockito.when(apiSubscriptionDAO.getAPISubscriptionsOfAPIForValidation(LIMIT)).thenReturn(new ArrayList<>());
        adminService.getAPISubscriptions(LIMIT);
        Mockito.verify(apiSubscriptionDAO, Mockito.times(1)).getAPISubscriptionsOfAPIForValidation(LIMIT);
    }

    @Test(description = "Get api subscriptions of API")
    public void testGetAPISubscriptionsOfApi() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(apiSubscriptionDAO);
        Mockito.when(apiSubscriptionDAO.getAPISubscriptionsOfAPIForValidation(API_CONTEXT, API_VERSION))
                .thenReturn(new ArrayList<SubscriptionValidationData>());
        adminService.getAPISubscriptionsOfApi(API_CONTEXT, API_VERSION);
        Mockito.verify(apiSubscriptionDAO, Mockito.times(1))
                .getAPISubscriptionsOfAPIForValidation(API_CONTEXT, API_VERSION);
    }

    @Test(description = "Get policy by level and name")
    public void testGetPolicy() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        Policy policy = Mockito.mock(Policy.class);
        Mockito.when(policyDAO.getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, POLICY_NAME))
                .thenReturn(policy);
        adminService.getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, POLICY_NAME);
        Mockito.verify(policyDAO, Mockito.times(1))
                .getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, POLICY_NAME);

        //Error path
        Mockito.when(policyDAO.getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, POLICY_NAME))
                .thenThrow(APIMgtDAOException.class);
        try {
            adminService.getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, POLICY_NAME);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Couldn't retrieve Throttle Policy with level: " + APIMgtAdminService.PolicyLevel.application.name()
                            + ", name: " + POLICY_NAME);
        }
    }

    @Test(description = "Get all policies by level")
    public void testGetAllPoliciesByLevel() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        Policy policy = Mockito.mock(Policy.class);
        List<Policy> policyList = new ArrayList<>();
        policyList.add(policy);
        Mockito.when(policyDAO.getPoliciesByLevel(APIMgtAdminService.PolicyLevel.application)).thenReturn(policyList);
        adminService.getPoliciesByLevel(APIMgtAdminService.PolicyLevel.application);
        Mockito.verify(policyDAO, Mockito.times(1)).getPoliciesByLevel(APIMgtAdminService.PolicyLevel.application);

        //Error path
        Mockito.when(policyDAO.getPoliciesByLevel(APIMgtAdminService.PolicyLevel.application))
                .thenThrow(APIMgtDAOException.class);
        try {
            adminService.getPoliciesByLevel(APIMgtAdminService.PolicyLevel.application);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Couldn't retrieve Throttle Policies with level: " + APIMgtAdminService.PolicyLevel.application
                            .name());
        }
    }

    @Test(description = "Add policy")
    public void testAddPolicy() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO, apiGateway);
        APIPolicy policy = SampleTestObjectCreator.createDefaultAPIPolicy();
        adminService.addApiPolicy(policy);
        Mockito.verify(policyDAO, Mockito.times(1)).addApiPolicy(policy);

        //Error path
        Mockito.doThrow(APIMgtDAOException.class).when(policyDAO).addApiPolicy(policy);
        try {
            adminService.addApiPolicy(policy);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't add API policy for uuid: " + policy.getUuid());
        }
    }

    @Test(description = "Add policy when policy id is null")
    public void testAddPolicyWhenPolicyIdNull() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO, apiGateway);
        APIPolicy policy = SampleTestObjectCreator.createDefaultAPIPolicy();
        policy.setUuid(null);
        adminService.addApiPolicy(policy);
        Mockito.verify(policyDAO, Mockito.times(1)).addApiPolicy(policy);
    }

    @Test(description = "Delete a label")
    public void testDeleteLabel() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(labelDAO);
        Label label = SampleTestObjectCreator.createLabel("Public").build();
        String labelId = label.getId();
        adminService.deleteLabel(labelId);
        Mockito.verify(labelDAO, Mockito.times(1)).deleteLabel(labelId);
    }

    @Test(description = "Exception when deleting a label", expectedExceptions = APIManagementException.class)
    public void testDeleteLabelException() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(labelDAO);
        Label label = SampleTestObjectCreator.createLabel("Public").build();
        String labelId = label.getId();
        Mockito.doThrow(new APIMgtDAOException("Error occurred while deleting label [labelId] " + labelId))
                .when(labelDAO).deleteLabel(labelId);
        adminService.deleteLabel(labelId);
        Mockito.verify(labelDAO, Mockito.times(1)).deleteLabel(labelId);
    }

    @Test(description = "Register gateway labels")
    public void testRegisterGatewayLabels() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        List<Label> labels = new ArrayList<>();
        Label label1 = SampleTestObjectCreator.createLabel("testLabel1").build();
        Label label2 = SampleTestObjectCreator.createLabel("testLabel2").build();
        labels.add(label1);
        List<String> labelNames = new ArrayList<>();
        labelNames.add(label1.getName());
        List<Label> existingLabels = new ArrayList<>();
        existingLabels.add(label1);
        existingLabels.add(label2);
        Mockito.when(labelDAO.getLabelsByName(labelNames)).thenReturn(existingLabels);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(labelDAO);
        adminService.registerGatewayLabels(labels, "false");
        Mockito.verify(labelDAO, Mockito.times(1)).addLabels(labels);
    }

    @Test(description = "Exception when registering gateway labels", expectedExceptions = APIManagementException.class)
    public void testRegisterGatewayLabelsException() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        List<Label> labels = new ArrayList<>();
        Label label = SampleTestObjectCreator.createLabel("testLabel1").build();
        labels.add(label);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(labelDAO);
        Mockito.doThrow(new APIMgtDAOException("Error occurred while adding label information")).when(labelDAO)
                .addLabels(labels);
        adminService.registerGatewayLabels(labels, "false");
    }

    @Test(description = "Register gateway labels when overwriteLabels value is null")
    public void testRegisterGatewayLabelsWhenOverwriteLabelsNull() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        List<Label> labels = new ArrayList<>();
        Label label1 = SampleTestObjectCreator.createLabel("testLabel1").build();
        labels.add(label1);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(labelDAO);
        adminService.registerGatewayLabels(labels, null);
        Mockito.verify(labelDAO, Mockito.times(1)).addLabels(labels);
    }

    @Test(description = "Register gateway labels when overwriteLabels value is true")
    public void testRegisterGatewayLabelsWhenOverwriteLabelsTrue() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        List<Label> labels = new ArrayList<>();
        Label label1 = SampleTestObjectCreator.createLabel("testLabel1").build();
        labels.add(label1);
        List<String> labelNames = new ArrayList<>();
        labelNames.add(label1.getName());
        List<Label> existingLabels = new ArrayList<>();
        existingLabels.add(label1);
        Mockito.when(labelDAO.getLabelsByName(labelNames)).thenReturn(existingLabels);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(labelDAO);
        adminService.registerGatewayLabels(labels, "true");
        Mockito.verify(labelDAO, Mockito.times(1)).addLabels(labels);
        Mockito.verify(labelDAO, Mockito.times(1)).updateLabel(label1);
    }

    @Test(description = "Test add application policy")
    public void testAddApplicationPolicy() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO, apiGateway);
        ApplicationPolicy policy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        adminService.addApplicationPolicy(policy);
        Mockito.verify(policyDAO, Mockito.times(1)).addApplicationPolicy(policy);

        //Error path
        Mockito.doThrow(APIMgtDAOException.class).when(policyDAO).addApplicationPolicy(policy);
        try {
            adminService.addApplicationPolicy(policy);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't add Application for uuid: " + policy.getUuid());
        }
    }

    @Test(description = "Test add application policy when the policy ID is null")
    public void testAddApplicationPolicyWhenPolicyIdNull() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO, apiGateway);
        ApplicationPolicy policy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        policy.setUuid(null);
        adminService.addApplicationPolicy(policy);
        Mockito.verify(policyDAO, Mockito.times(1)).addApplicationPolicy(policy);
    }

    @Test(description = "Test add subscription policy")
    public void testAddSubscriptionPolicy() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO, apiGateway);
        SubscriptionPolicy policy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();
        adminService.addSubscriptionPolicy(policy);
        Mockito.verify(policyDAO, Mockito.times(1)).addSubscriptionPolicy(policy);

        //Error path
        Mockito.doThrow(APIMgtDAOException.class).when(policyDAO).addSubscriptionPolicy(policy);
        try {
            adminService.addSubscriptionPolicy(policy);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't add Subscription policy for uuid: " + policy.getUuid());
        }
    }

    @Test(description = "Test add subscription policy when the policy ID is null")
    public void testAddSubscriptionPolicyWhenPolicyIdNull() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO, apiGateway);
        SubscriptionPolicy policy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();
        policy.setUuid(null);
        adminService.addSubscriptionPolicy(policy);
        Mockito.verify(policyDAO, Mockito.times(1)).addSubscriptionPolicy(policy);
    }

    @Test(description = "Test update API policy")
    public void testUpdateApiPolicy() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO, apiGateway);
        APIPolicy apiPolicy = SampleTestObjectCreator.createDefaultAPIPolicy();
        adminService.updateApiPolicy(apiPolicy);
        Mockito.verify(policyDAO, Mockito.times(1)).updateApiPolicy(apiPolicy);

        //Error path
        Mockito.doThrow(APIMgtDAOException.class).when(policyDAO).updateApiPolicy(apiPolicy);
        try {
            adminService.updateApiPolicy(apiPolicy);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't update API policy for uuid: " + apiPolicy.getUuid());
        }
    }

    @Test(description = "Test update subscription policy")
    public void testUpdateSubscriptionPolicy() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO, apiGateway);
        SubscriptionPolicy subscriptionPolicy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();
        adminService.updateSubscriptionPolicy(subscriptionPolicy);
        Mockito.verify(policyDAO, Mockito.times(1)).updateSubscriptionPolicy(subscriptionPolicy);

        //Error path
        Mockito.doThrow(APIMgtDAOException.class).when(policyDAO).updateSubscriptionPolicy(subscriptionPolicy);
        try {
            adminService.updateSubscriptionPolicy(subscriptionPolicy);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Couldn't update Subscription policy for uuid: " + subscriptionPolicy.getUuid());
        }
    }

    @Test(description = "Test update application policy")
    public void testUpdateApplicationPolicy() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO, apiGateway);
        ApplicationPolicy applicationPolicy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        adminService.updateApplicationPolicy(applicationPolicy);
        Mockito.verify(policyDAO, Mockito.times(1)).updateApplicationPolicy(applicationPolicy);

        //Error path
        Mockito.doThrow(APIMgtDAOException.class).when(policyDAO).updateApplicationPolicy(applicationPolicy);
        try {
            adminService.updateApplicationPolicy(applicationPolicy);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Couldn't update Application policy for uuid: " + applicationPolicy.getUuid());
        }
    }

    @Test(description = "Test delete policy")
    public void testDeletePolicy() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO, apiGateway);
        APIPolicy apiPolicy = SampleTestObjectCreator.createDefaultAPIPolicy();
        Mockito.when(policyDAO.getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api, apiPolicy.getPolicyName()))
                .thenReturn(apiPolicy);
        adminService.deletePolicy(apiPolicy.getPolicyName(), APIMgtAdminService.PolicyLevel.api);
        Mockito.verify(policyDAO, Mockito.times(1))
                .getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.api, apiPolicy.getPolicyName());
        Mockito.verify(policyDAO, Mockito.times(1))
                .deletePolicy(APIMgtAdminService.PolicyLevel.api, apiPolicy.getPolicyName());

        //Error path
        Mockito.doThrow(APIMgtDAOException.class).when(policyDAO)
                .deletePolicy(APIMgtAdminService.PolicyLevel.api, apiPolicy.getPolicyName());
        try {
            adminService.deletePolicy(apiPolicy.getPolicyName(), APIMgtAdminService.PolicyLevel.api);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Couldn't delete policy with name: " + apiPolicy.getPolicyName() + ", level: "
                            + APIMgtAdminService.PolicyLevel.api);
        }
    }

    @Test(description = "Test delete policy by UUID")
    public void testDeletePolicyByUuid() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO, apiGateway);
        APIPolicy apiPolicy = SampleTestObjectCreator.createDefaultAPIPolicy();
        adminService.deletePolicyByUuid(apiPolicy.getUuid(), APIMgtAdminService.PolicyLevel.api);

        //Error path
        Mockito.doThrow(APIMgtDAOException.class).when(policyDAO)
                .deletePolicyByUuid(APIMgtAdminService.PolicyLevel.api, apiPolicy.getUuid());
        try {
            adminService.deletePolicyByUuid(apiPolicy.getUuid(), APIMgtAdminService.PolicyLevel.api);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't delete policy with id: " + apiPolicy.getUuid() + ", level: "
                    + APIMgtAdminService.PolicyLevel.api);
        }
    }

    @Test(description = "Test getting API policy")
    public void testGetApiPolicy() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        APIPolicy apiPolicy = SampleTestObjectCreator.createDefaultAPIPolicy();
        Mockito.when(policyDAO.getApiPolicy(apiPolicy.getPolicyName())).thenReturn(apiPolicy);
        adminService.getApiPolicy(apiPolicy.getPolicyName());
        Mockito.verify(policyDAO, Mockito.times(1)).getApiPolicy(apiPolicy.getPolicyName());

        //Error path
        Mockito.when(policyDAO.getApiPolicy(apiPolicy.getPolicyName())).thenThrow(APIMgtDAOException.class);
        try {
            adminService.getApiPolicy(apiPolicy.getPolicyName());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't retrieve API policy with name: " + apiPolicy.getPolicyName());
        }
    }

    @Test(description = "Test getting Application policy")
    public void testGetApplicationPolicy() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        ApplicationPolicy applicationPolicy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        Mockito.when(policyDAO.getApplicationPolicy(applicationPolicy.getPolicyName())).thenReturn(applicationPolicy);
        adminService.getApplicationPolicy(applicationPolicy.getPolicyName());
        Mockito.verify(policyDAO, Mockito.times(1)).getApplicationPolicy(applicationPolicy.getPolicyName());

        //Error path
        Mockito.when(policyDAO.getApplicationPolicy(applicationPolicy.getPolicyName()))
                .thenThrow(APIMgtDAOException.class);
        try {
            adminService.getApplicationPolicy(applicationPolicy.getPolicyName());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Couldn't retrieve Application policy with name: " + applicationPolicy.getPolicyName());
        }
    }

    @Test(description = "Test getting Subscription policy")
    public void testGetSubscriptionPolicy() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        SubscriptionPolicy subscriptionPolicy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();
        Mockito.when(policyDAO.getSubscriptionPolicy(subscriptionPolicy.getPolicyName()))
                .thenReturn(subscriptionPolicy);
        adminService.getSubscriptionPolicy(subscriptionPolicy.getPolicyName());
        Mockito.verify(policyDAO, Mockito.times(1)).getSubscriptionPolicy(subscriptionPolicy.getPolicyName());

        //Error path
        Mockito.when(policyDAO.getSubscriptionPolicy(subscriptionPolicy.getPolicyName()))
                .thenThrow(APIMgtDAOException.class);
        try {
            adminService.getSubscriptionPolicy(subscriptionPolicy.getPolicyName());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Couldn't retrieve Subscription policy with name: " + subscriptionPolicy.getPolicyName());
        }
    }

    @Test(description = "Test getting API policy by UUID")
    public void testGetApiPolicyByUuid() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        APIPolicy apiPolicy = SampleTestObjectCreator.createDefaultAPIPolicy();
        Mockito.when(policyDAO.getApiPolicyByUuid(apiPolicy.getUuid())).thenReturn(apiPolicy);
        adminService.getApiPolicyByUuid(apiPolicy.getUuid());
        Mockito.verify(policyDAO, Mockito.times(1)).getApiPolicyByUuid(apiPolicy.getUuid());

        //Error path
        Mockito.when(policyDAO.getApiPolicyByUuid(apiPolicy.getUuid())).thenThrow(APIMgtDAOException.class);
        try {
            adminService.getApiPolicyByUuid(apiPolicy.getUuid());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't retrieve API policy with id: " + apiPolicy.getUuid());
        }
    }

    @Test(description = "Test getting Application policy by UUID")
    public void testGetApplicationPolicyByUuid() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        ApplicationPolicy applicationPolicy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        Mockito.when(policyDAO.getApplicationPolicyByUuid(applicationPolicy.getUuid())).thenReturn(applicationPolicy);
        adminService.getApplicationPolicyByUuid(applicationPolicy.getUuid());
        Mockito.verify(policyDAO, Mockito.times(1)).getApplicationPolicyByUuid(applicationPolicy.getUuid());

        //Error path
        Mockito.when(policyDAO.getApplicationPolicyByUuid(applicationPolicy.getUuid()))
                .thenThrow(APIMgtDAOException.class);
        try {
            adminService.getApplicationPolicyByUuid(applicationPolicy.getUuid());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Couldn't retrieve Application policy with id: " + applicationPolicy.getUuid());
        }
    }

    @Test(description = "Test getting Subscription policy by UUID")
    public void testGetSubscriptionPolicyByUuid() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        SubscriptionPolicy subscriptionPolicy = SampleTestObjectCreator.createDefaultSubscriptionPolicy();
        Mockito.when(policyDAO.getSubscriptionPolicyByUuid(subscriptionPolicy.getUuid()))
                .thenReturn(subscriptionPolicy);
        adminService.getSubscriptionPolicyByUuid(subscriptionPolicy.getUuid());
        Mockito.verify(policyDAO, Mockito.times(1)).getSubscriptionPolicyByUuid(subscriptionPolicy.getUuid());

        //Error path
        Mockito.when(policyDAO.getSubscriptionPolicyByUuid(subscriptionPolicy.getUuid()))
                .thenThrow(APIMgtDAOException.class);
        try {
            adminService.getSubscriptionPolicyByUuid(subscriptionPolicy.getUuid());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Couldn't retrieve Subscription policy with id: " + subscriptionPolicy.getUuid());
        }
    }

    @Test(description = "Test getting all API Policies")
    public void testGetApiPolicies() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        adminService.getApiPolicies();
        Mockito.verify(policyDAO, Mockito.times(1)).getApiPolicies();

        //Error path
        Mockito.when(policyDAO.getApiPolicies()).thenThrow(APIMgtDAOException.class);
        try {
            adminService.getApiPolicies();
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't retrieve API policies");
        }
    }

    @Test(description = "Test getting all Application Policies")
    public void testGetApplicationPolicies() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        adminService.getApplicationPolicies();
        Mockito.verify(policyDAO, Mockito.times(1)).getApplicationPolicies();

        //Error path
        Mockito.when(policyDAO.getApplicationPolicies()).thenThrow(APIMgtDAOException.class);
        try {
            adminService.getApplicationPolicies();
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't retrieve Application policies");
        }
    }

    @Test(description = "Test getting all Subscription Policies")
    public void testGetSubscriptionPolicies() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        adminService.getSubscriptionPolicies();
        Mockito.verify(policyDAO, Mockito.times(1)).getSubscriptionPolicies();

        //Error path
        Mockito.when(policyDAO.getSubscriptionPolicies()).thenThrow(APIMgtDAOException.class);
        try {
            adminService.getSubscriptionPolicies();
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't retrieve Subscription policies");
        }
    }

    @Test(description = "Test getting API gateway service configuration")
    public void testGetAPIGatewayServiceConfig() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(apiDAO);
        adminService.getAPIGatewayServiceConfig(API_ID);
        Mockito.verify(apiDAO, Mockito.times(1)).getGatewayConfigOfAPI(API_ID);

        //Error path
        Mockito.when(apiDAO.getGatewayConfigOfAPI(API_ID)).thenThrow(APIMgtDAOException.class);
        try {
            adminService.getAPIGatewayServiceConfig(API_ID);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't retrieve gateway configuration for apiId " + API_ID);
        }
    }

    @Test(description = "Test getting all resources for API")
    public void testGetAllResourcesForApi() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(apiDAO);
        adminService.getAllResourcesForApi(API_CONTEXT, API_VERSION);
        Mockito.verify(apiDAO, Mockito.times(1)).getResourcesOfApi(API_CONTEXT, API_VERSION);

        //Error path
        Mockito.when(apiDAO.getResourcesOfApi(API_CONTEXT, API_VERSION)).thenThrow(APIMgtDAOException.class);
        try {
            adminService.getAllResourcesForApi(API_CONTEXT, API_VERSION);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't retrieve resources for Api Name: " + API_CONTEXT);
        }
    }

    @Test(description = "Test getting APIs by status")
    public void testGetAPIsByStatus() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(apiDAO);
        adminService.getAPIsByStatus(new ArrayList<>(), APIStatus.CREATED.getStatus());
        Mockito.verify(apiDAO, Mockito.times(1)).getAPIsByStatus(new ArrayList<>(), APIStatus.CREATED.getStatus());

        //Error path
        //When gateway labels are null
        try {
            adminService.getAPIsByStatus(null, APIStatus.CREATED.getStatus());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Gateway labels cannot be null");
        }

        //Error path
        //When status is null
        try {
            adminService.getAPIsByStatus(new ArrayList<>(), null);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Status cannot be null");
        }

        //Error path
        //APIMgtDAOException
        Mockito.when(apiDAO.getAPIsByStatus(new ArrayList<>(), APIStatus.CREATED.getStatus()))
                .thenThrow(APIMgtDAOException.class);
        try {
            adminService.getAPIsByStatus(new ArrayList<>(), APIStatus.CREATED.getStatus());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while getting the API list in given states");
        }
    }

    @Test(description = "Test getting APIs by Gateway label")
    public void testGetAPIsByGatewayLabel() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        List<String> gatewayLabels = new ArrayList<>();
        gatewayLabels.add("Label1");
        List<API> apiListExpected = new ArrayList<>();
        API api = SampleTestObjectCreator.createDefaultAPI().labels(new HashSet<>(gatewayLabels)).build();
        apiListExpected.add(api);
        Mockito.when(apiDAO.getAPIsByGatewayLabel(gatewayLabels)).thenReturn(apiListExpected);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(apiDAO);
        List<API> apiListReturned = adminService.getAPIsByGatewayLabel(gatewayLabels);
        Assert.assertEquals(apiListReturned, apiListExpected);

        //Error path
        //When gateway labels are null
        try {
            adminService.getAPIsByGatewayLabel(null);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Gateway labels cannot be null");
        }

        //Error path
        //Error while getting the APIs
        Mockito.when(apiDAO.getAPIsByGatewayLabel(gatewayLabels)).thenThrow(APIMgtDAOException.class);
        try {
            adminService.getAPIsByGatewayLabel(gatewayLabels);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while getting the API list in given gateway labels");
        }
    }

    @Test(description = "Test getting all applications")
    public void testGetAllApplications() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(applicationDAO);
        List<Application> applicationListExpected = new ArrayList<>();
        Application application = SampleTestObjectCreator.createDefaultApplication();
        applicationListExpected.add(application);
        Mockito.when(applicationDAO.getAllApplications()).thenReturn(applicationListExpected);
        List<Application> applicationListReturned = adminService.getAllApplications();
        Assert.assertEquals(applicationListReturned, applicationListExpected);

        //Error path
        Mockito.when(applicationDAO.getAllApplications()).thenThrow(APIMgtDAOException.class);
        try {
            adminService.getAllApplications();
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while getting the Application list");
        }
    }

    @Test(description = "Test getting all endpoints")
    public void testGetAllEndpoints() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(apiDAO);
        List<Endpoint> endpointListExpected = new ArrayList<>();
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        endpointListExpected.add(endpoint);
        Mockito.when(apiDAO.getEndpoints()).thenReturn(endpointListExpected);
        List<Endpoint> endpointListReturned = adminService.getAllEndpoints();
        Assert.assertEquals(endpointListReturned, endpointListExpected);

        //Error path
        Mockito.when(apiDAO.getEndpoints()).thenThrow(APIMgtDAOException.class);
        try {
            adminService.getAllEndpoints();
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while getting the Endpoint list");
        }
    }

    @Test(description = "Test getting the endpoint gateway configuration")
    public void testGetEndpointGatewayConfig() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(apiDAO);
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        Mockito.when(apiDAO.getEndpointConfig(endpoint.getId())).thenReturn(endpoint.getEndpointConfig());
        String endpointConfigReturned = adminService.getEndpointGatewayConfig(endpoint.getId());
        Assert.assertEquals(endpointConfigReturned, endpoint.getEndpointConfig());

        //Error path
        Mockito.when(apiDAO.getEndpointConfig(endpoint.getId())).thenThrow(APIMgtDAOException.class);
        try {
            adminService.getEndpointGatewayConfig(endpoint.getId());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while getting the Endpoint Configuration");
        }
    }

    @Test(description = "Test getting all policies")
    public void testGetAllPolicies() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        Set<PolicyValidationData> policyValidationDataSetExpected = new HashSet<>();
        PolicyValidationData policyValidationData = new PolicyValidationData(POLICY_ID, POLICY_NAME, true);
        policyValidationDataSetExpected.add(policyValidationData);
        Mockito.when(policyDAO.getAllPolicies()).thenReturn(policyValidationDataSetExpected);
        Set<PolicyValidationData> policyValidationDataSetReturned = adminService.getAllPolicies();
        Assert.assertEquals(policyValidationDataSetReturned, policyValidationDataSetExpected);

        //Error path
        Mockito.when(policyDAO.getAllPolicies()).thenThrow(APIMgtDAOException.class);
        try {
            adminService.getAllPolicies();
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while retrieving policies");
        }
    }

    @Test(description = "Test adding block condition")
    public void testAddBlockCondition() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO, apiGateway);
        BlockConditions blockConditions = SampleTestObjectCreator.createDefaultBlockCondition(BLOCK_CONDITION_TYPE);
        String uuid = adminService.addBlockCondition(blockConditions);
        Assert.assertNotNull(uuid);

        //Error path
        Mockito.when(policyDAO.addBlockConditions(blockConditions)).thenThrow(APIMgtDAOException.class);
        try {
            adminService.addBlockCondition(blockConditions);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Couldn't add block condition with condition type: " + blockConditions.getConditionType()
                            + ", condition value: " + blockConditions.getConditionValue());
        }
    }

    @Test(description = "Test updating block condition state by uuid")
    public void testUpdateBlockConditionStateByUUID() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO, apiGateway);
        BlockConditions blockConditions = SampleTestObjectCreator.createDefaultBlockCondition(BLOCK_CONDITION_TYPE);
        Mockito.when(policyDAO.updateBlockConditionStateByUUID(blockConditions.getUuid(), true)).thenReturn(true);
        Boolean statusTrue = adminService.updateBlockConditionStateByUUID(blockConditions.getUuid(), true);
        Assert.assertTrue(statusTrue);

        //Error path
        //Failure updating
        Mockito.when(policyDAO.updateBlockConditionStateByUUID(blockConditions.getUuid(), true)).thenReturn(false);
        Boolean statusFalse = adminService.updateBlockConditionStateByUUID(blockConditions.getUuid(), true);
        Assert.assertFalse(statusFalse);

        //Error path
        //APIMgtDAOException
        Mockito.when(policyDAO.updateBlockConditionStateByUUID(blockConditions.getUuid(), true))
                .thenThrow(APIMgtDAOException.class);
        try {
            adminService.updateBlockConditionStateByUUID(blockConditions.getUuid(), true);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Couldn't update block condition with UUID: " + blockConditions.getUuid() + ", state: " + true);
        }
    }

    @Test(description = "Test deleting block condition by uuid")
    public void testDeleteBlockConditionByUuid() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO, apiGateway);
        BlockConditions blockConditions = SampleTestObjectCreator.createDefaultBlockCondition(BLOCK_CONDITION_TYPE);
        Mockito.when(policyDAO.deleteBlockConditionByUuid(blockConditions.getUuid())).thenReturn(true);
        Boolean statusTrue = adminService.deleteBlockConditionByUuid(blockConditions.getUuid());
        Assert.assertTrue(statusTrue);

        //Error path
        //Failure deleting
        Mockito.when(policyDAO.deleteBlockConditionByUuid(blockConditions.getUuid())).thenReturn(false);
        Boolean statusFalse = adminService.deleteBlockConditionByUuid(blockConditions.getUuid());
        Assert.assertFalse(statusFalse);

        //Error path
        //APIMgtDAOException
        Mockito.when(policyDAO.deleteBlockConditionByUuid(blockConditions.getUuid()))
                .thenThrow(APIMgtDAOException.class);
        try {
            adminService.deleteBlockConditionByUuid(blockConditions.getUuid());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Couldn't delete block condition with UUID: " + blockConditions.getUuid());
        }
    }

    @Test(description = "Test getting block conditions")
    public void testGetBlockConditions() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        List<BlockConditions> blockConditionsListExpected = new ArrayList<>();
        BlockConditions blockConditions = SampleTestObjectCreator.createDefaultBlockCondition(BLOCK_CONDITION_TYPE);
        blockConditionsListExpected.add(blockConditions);
        Mockito.when(policyDAO.getBlockConditions()).thenReturn(blockConditionsListExpected);
        List<BlockConditions> blockConditionsListReturned = adminService.getBlockConditions();
        Assert.assertEquals(blockConditionsListReturned, blockConditionsListExpected);

        //Error path
        Mockito.when(policyDAO.getBlockConditions()).thenThrow(APIMgtDAOException.class);
        try {
            adminService.getBlockConditions();
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't get list of block conditions.");
        }
    }

    @Test(description = "Test getting block condition by uuid")
    public void testGetBlockConditionByUUID() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        BlockConditions blockConditions = SampleTestObjectCreator.createDefaultBlockCondition(BLOCK_CONDITION_TYPE);
        Mockito.when(policyDAO.getBlockConditionByUUID(blockConditions.getUuid())).thenReturn(blockConditions);
        BlockConditions blockConditionsReturned = adminService.getBlockConditionByUUID(blockConditions.getUuid());
        Assert.assertEquals(blockConditionsReturned, blockConditions);

        //Error path
        Mockito.when(policyDAO.getBlockConditionByUUID(blockConditions.getUuid())).thenThrow(APIMgtDAOException.class);
        try {
            adminService.getBlockConditionByUUID(blockConditions.getUuid());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't get block condition by UUID: " + blockConditions.getUuid());
        }
    }

    @Test(description = "Test adding a custom rule")
    public void testAddCustomRule() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        CustomPolicy customPolicy = SampleTestObjectCreator.createDefaultCustomPolicy();
        Mockito.when(policyDAO.addCustomPolicy(customPolicy)).thenReturn(customPolicy.getUuid());
        String uuid = adminService.addCustomRule(customPolicy);
        Assert.assertEquals(uuid, customPolicy.getUuid());

        //Error path
        Mockito.when(policyDAO.addCustomPolicy(customPolicy)).thenThrow(APIMgtDAOException.class);
        try {
            adminService.addCustomRule(customPolicy);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(),
                    "Couldn't add custom policy with policy name: " + customPolicy.getPolicyName());
        }
    }

    @Test(description = "Test updating a custom rule")
    public void testUpdateCustomRule() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        CustomPolicy customPolicy = SampleTestObjectCreator.createDefaultCustomPolicy();
        adminService.updateCustomRule(customPolicy);

        //Error path
        Mockito.doThrow(APIMgtDAOException.class).when(policyDAO).updateCustomPolicy(customPolicy);
        try {
            adminService.updateCustomRule(customPolicy);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't update custom policy with UUID: " + customPolicy.getUuid());
        }
    }

    @Test(description = "Test deleting a custom rule")
    public void testDeleteCustomRule() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        CustomPolicy customPolicy = SampleTestObjectCreator.createDefaultCustomPolicy();
        adminService.deleteCustomRule(customPolicy.getUuid());

        //Error path
        Mockito.doThrow(APIMgtDAOException.class).when(policyDAO).deleteCustomPolicy(customPolicy.getUuid());
        try {
            adminService.deleteCustomRule(customPolicy.getUuid());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't delete custom policy with UUID: " + customPolicy.getUuid());
        }
    }

    @Test(description = "Test getting custom rules")
    public void testGetCustomRules() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        List<CustomPolicy> customPolicyListExpected = new ArrayList<>();
        CustomPolicy customPolicy = SampleTestObjectCreator.createDefaultCustomPolicy();
        customPolicyListExpected.add(customPolicy);
        Mockito.when(policyDAO.getCustomPolicies()).thenReturn(customPolicyListExpected);
        List<CustomPolicy> customPolicyListReturned = adminService.getCustomRules();
        Assert.assertEquals(customPolicyListReturned, customPolicyListExpected);

        //Error path
        Mockito.when(policyDAO.getCustomPolicies()).thenThrow(APIMgtDAOException.class);
        try {
            adminService.getCustomRules();
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't get list of custom policy.");
        }
    }

    @Test(description = "Test getting custom rule by uuid")
    public void testGetCustomRuleByUUID() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = getAPIMgtAdminServiceImpl(policyDAO);
        CustomPolicy customPolicy = SampleTestObjectCreator.createDefaultCustomPolicy();
        Mockito.when(policyDAO.getCustomPolicyByUuid(customPolicy.getUuid())).thenReturn(customPolicy);
        CustomPolicy customPolicyReturned = adminService.getCustomRuleByUUID(customPolicy.getUuid());
        Assert.assertEquals(customPolicyReturned, customPolicy);

        //Error path
        Mockito.when(policyDAO.getCustomPolicyByUuid(customPolicy.getUuid())).thenThrow(APIMgtDAOException.class);
        try {
            adminService.getCustomRuleByUUID(customPolicy.getUuid());
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Couldn't get custom policy by UUID: " + customPolicy.getUuid());
        }
    }

    private APIMgtAdminServiceImpl getAPIMgtAdminServiceImpl(ApiDAO apiDAO) {
        return new APIMgtAdminServiceImpl(null, null, apiDAO, null, null, null, null);
    }

    private APIMgtAdminServiceImpl getAPIMgtAdminServiceImpl(ApplicationDAO applicationDAO) {
        return new APIMgtAdminServiceImpl(null, null, null, null, applicationDAO, null, null);
    }

    private APIMgtAdminServiceImpl getAPIMgtAdminServiceImpl(PolicyDAO policyDAO) {
        return new APIMgtAdminServiceImpl(null, policyDAO, null, null, null, null, null);
    }

    private APIMgtAdminServiceImpl getAPIMgtAdminServiceImpl(APISubscriptionDAO apiSubscriptionDAO) {
        return new APIMgtAdminServiceImpl(apiSubscriptionDAO, null, null, null, null, null, null);
    }

    private APIMgtAdminServiceImpl getAPIMgtAdminServiceImpl(LabelDAO labelDAO) {
        return new APIMgtAdminServiceImpl(null, null, null, labelDAO, null, null, null);
    }

    private APIMgtAdminServiceImpl getAPIMgtAdminServiceImpl(PolicyDAO policyDAO, APIGateway apiGateway) {
        return new APIMgtAdminServiceImpl(null, policyDAO, null, null, null, apiGateway, null);
    }
}
