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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.TestUtils;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;

/**
 * ApplicationRegistrationWSWorkflowExecutor test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiMgtDAO.class, KeyManagerHolder.class})
public class ApplicationRegistrationWSWorkflowExecutorTest {

    private ApplicationRegistrationWSWorkflowExecutor applicationRegistrationWSWorkflowExecutor;

    @Before
    public void init() {
        applicationRegistrationWSWorkflowExecutor = new ApplicationRegistrationWSWorkflowExecutor();
        ApiMgtDAO apiMgtDAO = TestUtils.getApiMgtDAO();
    }

    @Test
    public void testExecutingApplicationRegistrationWorkflow() {
        WorkflowDTO workflowDTO = new WorkflowDTO();

        try {
            applicationRegistrationWSWorkflowExecutor.execute(workflowDTO);
        } catch (WorkflowException e) {
            e.printStackTrace();
        }
    }

}
