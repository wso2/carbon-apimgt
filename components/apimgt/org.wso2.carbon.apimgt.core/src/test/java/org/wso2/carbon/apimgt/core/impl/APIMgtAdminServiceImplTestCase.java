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
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class APIMgtAdminServiceImplTestCase {

    private static final Integer LIMIT = 2;
    private static final String API_VERSION = "1.0.0";
    private static final String API_CONTEXT = "/testContext";
    private static final String POLICY_LEVEL = "policyLevel";
    private static final String POLICY_NAME = "policyName";

    @Test(description = "Get api subscriptions")
    public void testGetAPISubscriptions() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        APIMgtAdminServiceImpl adminService = newAPIMgtAdminServiceImplforAPISubscriptionDAO(apiSubscriptionDAO);
        when(apiSubscriptionDAO.getAPISubscriptionsOfAPIForValidation(LIMIT))
                .thenReturn(new ArrayList<>());
        adminService.getAPISubscriptions(LIMIT);
        verify(apiSubscriptionDAO, times(1)).getAPISubscriptionsOfAPIForValidation(LIMIT);
    }

    @Test(description = "Get api subscriptions of API")
    public void testGetAPISubscriptionsOfApi() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        APIMgtAdminServiceImpl adminService = newAPIMgtAdminServiceImplforAPISubscriptionDAO(apiSubscriptionDAO);
        when(apiSubscriptionDAO.getAPISubscriptionsOfAPIForValidation(API_CONTEXT, API_VERSION))
                .thenReturn(new ArrayList<SubscriptionValidationData>());
        adminService.getAPISubscriptionsOfApi(API_CONTEXT, API_VERSION);
        verify(apiSubscriptionDAO, times(1)).getAPISubscriptionsOfAPIForValidation(API_CONTEXT, API_VERSION);
    }

    @Test(description = "Get policy")
    public void testGetPolicy() throws APIManagementException {
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = newAPIMgtAdminServiceImplforPolicyDAO(policyDAO);
        Policy policy = mock(Policy.class);
        when(policyDAO.getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, POLICY_NAME))
                .thenReturn(policy);
        adminService.getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, POLICY_NAME);
        verify(policyDAO, times(1)).getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, POLICY_NAME);
    }

    @Test(description = "Get all policies by level")
    public void testGetAllPoliciesByLevel() throws APIManagementException {
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = newAPIMgtAdminServiceImplforPolicyDAO(policyDAO);
        Policy policy = mock(Policy.class);
        List<Policy> policyList = new ArrayList<>();
        policyList.add(policy);
        when(policyDAO.getPoliciesByLevel(APIMgtAdminService.PolicyLevel.application)).thenReturn(policyList);
        adminService.getPoliciesByLevel(APIMgtAdminService.PolicyLevel.application);
        verify(policyDAO, times(1)).getPoliciesByLevel(APIMgtAdminService.PolicyLevel.application);
    }

    @Test(description = "Add policy")
    public void testAddPolicy() throws APIManagementException {
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = newAPIMgtAdminServiceImplforPolicyDAO(policyDAO);
        APIPolicy policy = mock(APIPolicy.class);
        adminService.addApiPolicy(policy);
        verify(policyDAO, times(1)).addApiPolicy(
                policy);
    }

    @Test(description = "Get API Info")
    public void testGetAPIInfo() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIMgtAdminServiceImpl adminService = newAPIMgtAdminServiceImplforApiDAO(apiDAO);
        List<API> apiList = SampleTestObjectCreator.createMockAPIList();
        when(apiDAO.getAPIs()).thenReturn(apiList);
        adminService.getAPIInfo();
        verify(apiDAO, times(1)).getAPIs();
    }

    @Test(description = "Delete a label")
    public void testDeleteLabel() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        APIMgtAdminServiceImpl adminService = newAPIMgtAdminServiceImplforLabelDAO(labelDAO);
        Label label = SampleTestObjectCreator.createLabel("Public").build();
        String labelId = label.getId();
        adminService.deleteLabel(labelId);
        Mockito.verify(labelDAO, Mockito.times(1)).deleteLabel(labelId);
    }

    @Test(description = "Exception when deleting a label", expectedExceptions = APIManagementException.class)
    public void testDeleteLabelException() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        APIMgtAdminServiceImpl adminService = newAPIMgtAdminServiceImplforLabelDAO(labelDAO);
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
        APIMgtAdminServiceImpl adminService = newAPIMgtAdminServiceImplforLabelDAO(labelDAO);
        adminService.registerGatewayLabels(labels, "false");
        Mockito.verify(labelDAO, Mockito.times(1)).addLabels(labels);
    }

    @Test(description = "Exception when registering gateway labels", expectedExceptions = APIManagementException.class)
    public void testRegisterGatewayLabelsException() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        List<Label> labels = new ArrayList<>();
        Label label = SampleTestObjectCreator.createLabel("testLabel1").build();
        labels.add(label);
        APIMgtAdminServiceImpl adminService = newAPIMgtAdminServiceImplforLabelDAO(labelDAO);
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
        APIMgtAdminServiceImpl adminService = newAPIMgtAdminServiceImplforLabelDAO(labelDAO);
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
        APIMgtAdminServiceImpl adminService = newAPIMgtAdminServiceImplforLabelDAO(labelDAO);
        adminService.registerGatewayLabels(labels, "true");
        Mockito.verify(labelDAO, Mockito.times(1)).addLabels(labels);
        Mockito.verify(labelDAO, Mockito.times(1)).updateLabel(label1);
    }

    private APIMgtAdminServiceImpl newAPIMgtAdminServiceImplforApiDAO(ApiDAO apiDAO) {
        return new APIMgtAdminServiceImpl(null, null, apiDAO, null);
    }

    private APIMgtAdminServiceImpl newAPIMgtAdminServiceImplforPolicyDAO(PolicyDAO policyDAO) {
        return new APIMgtAdminServiceImpl(null, policyDAO, null, null);
    }

    private APIMgtAdminServiceImpl newAPIMgtAdminServiceImplforAPISubscriptionDAO(APISubscriptionDAO
                                                                                          apiSubscriptionDAO) {
        return new APIMgtAdminServiceImpl(apiSubscriptionDAO, null, null, null);
    }

    private APIMgtAdminServiceImpl newAPIMgtAdminServiceImplforLabelDAO(LabelDAO labelDAO) {
        return new APIMgtAdminServiceImpl(null, null, null, labelDAO);
    }

}
