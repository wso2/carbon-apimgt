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
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;

/**
 * ApplicationRegistrationSimpleWorkflowExecutor test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiMgtDAO.class, KeyManagerHolder.class})
public class ApplicationRegistrationSimpleWorkflowExecutorTest {

    private ApplicationRegistrationSimpleWorkflowExecutor applicationRegistrationSimpleWorkflowExecutor;
    private ApiMgtDAO apiMgtDAO;
    private ApplicationRegistrationWorkflowDTO workflowDTO;
    private Application application;
    private KeyManager keyManager;
    private OAuthAppRequest oAuthAppRequest;
    private OAuthApplicationInfo oAuthApplicationInfo;

    @Before
    public void init() {
        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.mockStatic(KeyManagerHolder.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        keyManager = Mockito.mock(KeyManager.class);
        application = new Application("test", new Subscriber("testUser"));
        oAuthAppRequest = new OAuthAppRequest();
        oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthAppRequest.setOAuthApplicationInfo(oAuthApplicationInfo);
        workflowDTO = new ApplicationRegistrationWorkflowDTO();
        workflowDTO.setWorkflowReference("1");
        workflowDTO.setApplication(application);
        workflowDTO.setAppInfoDTO(oAuthAppRequest);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance()).thenReturn(keyManager);
        applicationRegistrationSimpleWorkflowExecutor = new ApplicationRegistrationSimpleWorkflowExecutor();

    }

    @Test
    public void testExecutingApplicationRegistrationWorkFlow() throws APIManagementException {
        PowerMockito.doNothing().when(apiMgtDAO).createApplicationRegistrationEntry(workflowDTO, false);
        oAuthApplicationInfo.setJsonString("{\"client_credentials\":\"Client Credentials\"}");
        Mockito.when(keyManager.createApplication(oAuthAppRequest)).thenReturn(oAuthApplicationInfo);

        try {
            applicationRegistrationSimpleWorkflowExecutor.execute(workflowDTO);
        } catch (WorkflowException e) {
            Assert.fail("Unexpected WorkflowException occurred while executing application registration simple " +
                    "workflow");
        }
    }

    @Test
    public void testFailureWhileExecutingApplicationRegistrationWorkFlow() throws APIManagementException {
        PowerMockito.doNothing().when(apiMgtDAO).createApplicationRegistrationEntry(workflowDTO, false);
        oAuthApplicationInfo.setJsonString("{\"client_credentials\":\"Client Credentials\"}");
        Mockito.when(keyManager.createApplication(oAuthAppRequest)).thenThrow(new APIManagementException(""));
        try {
            applicationRegistrationSimpleWorkflowExecutor.execute(workflowDTO);
            Assert.fail("Expected WorkflowException is not thrown while executing application registration simple " +
                    "workflow");
        } catch (WorkflowException e) {
           Assert.assertTrue(e.getMessage().contains("Error occurred while executing SubscriberKeyMgtClient."));
        }
    }

    @Test
    public void testGetWorkflowDetails(){
        try {
            applicationRegistrationSimpleWorkflowExecutor.getWorkflowDetails("random_string");
        } catch (WorkflowException e) {
            Assert.fail("Unexpected exception occurred while retriving workflow details");
        }
    }
}
