/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.workflow;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;

/**
 * SubscriptionDeletionSimpleWorkflowExecutor test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiMgtDAO.class})
public class SubscriptionDeletionSimpleWorkflowExecutorTest {
    private SubscriptionDeletionSimpleWorkflowExecutor subscriptionDeletionSimpleWorkflowExecutor;
    private ApiMgtDAO apiMgtDAO;
    private SubscriptionWorkflowDTO subscriptionWorkflowDTO;

    @Before
    public void init() {
        PowerMockito.mockStatic(ApiMgtDAO.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        subscriptionWorkflowDTO = new SubscriptionWorkflowDTO();
        subscriptionWorkflowDTO.setApiProvider("testUser");
        subscriptionWorkflowDTO.setApiName("weatherAPI");
        subscriptionWorkflowDTO.setApiVersion("v1");
        subscriptionDeletionSimpleWorkflowExecutor = new SubscriptionDeletionSimpleWorkflowExecutor();
    }

    @Test
    public void testRetrievingWorkflowType(){
        Assert.assertEquals(subscriptionDeletionSimpleWorkflowExecutor.getWorkflowType(), "AM_SUBSCRIPTION_DELETION");
    }

    @Test
    public void testExecutingSubscriptionDeletionWorkFlow() throws APIManagementException {
        PowerMockito.doNothing().when(apiMgtDAO).removeSubscription((APIIdentifier) Mockito.anyObject(), Mockito.anyInt());
        try {
            Assert.assertNotNull(subscriptionDeletionSimpleWorkflowExecutor.execute(subscriptionWorkflowDTO));
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while executing subscription deletion simple " +
                    "workflow");
        }
    }

    @Test
    public void testFailureWhileExecutingSubscriptionDeletionWorkFlow() throws APIManagementException {
        PowerMockito.doThrow(new APIManagementException("Error occurred while removing subscriptions")).when(apiMgtDAO)
                .removeSubscription((APIIdentifier) Mockito.anyObject(), Mockito.anyInt());
        try {
           subscriptionDeletionSimpleWorkflowExecutor.execute(subscriptionWorkflowDTO);
           Assert.fail("Expected WorkflowException is not thrown while executing subscription deletion workflow");
        } catch (WorkflowException e) {
            Assert.assertTrue(e.getMessage().contains("Could not complete subscription deletion workflow for api: " +
                    subscriptionWorkflowDTO.getApiName()));
        }
    }

    @Test
    public void testGetWorkflowDetails(){
        try {
            subscriptionDeletionSimpleWorkflowExecutor.getWorkflowDetails("random_string");
        } catch (WorkflowException e) {
            Assert.fail("Unexpected exception occurred while retriving workflow details");
        }
    }

}
