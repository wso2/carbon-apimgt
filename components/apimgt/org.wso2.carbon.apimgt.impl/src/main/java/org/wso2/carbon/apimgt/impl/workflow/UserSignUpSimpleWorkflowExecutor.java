/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.util.List;

public class UserSignUpSimpleWorkflowExecutor extends UserSignUpWorkflowExecutor {

    private static final Log log = LogFactory.getLog(UserSignUpSimpleWorkflowExecutor.class);

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_USER_SIGNUP;
    }

    @Override
    public void execute(WorkflowDTO workflowDTO) throws WorkflowException {
        log.info("Executing User SignUp Workflow");
        complete(workflowDTO);
    }

    @Override
    public void complete(WorkflowDTO workflowDTO) throws WorkflowException {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String serverURL = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
        String adminUsername = config.getFirstProperty(APIConstants.AUTH_MANAGER_USERNAME);
        String adminPassword = config.getFirstProperty(APIConstants.AUTH_MANAGER_PASSWORD);
        if (serverURL == null || adminUsername == null || adminPassword == null) {
            throw new WorkflowException("Required parameter missing to connect to the" +
                    " authentication manager");
        }

        String role = UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR+config.getFirstProperty(APIConstants.SELF_SIGN_UP_ROLE);
        if (role.equals(UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR)) {
            throw new WorkflowException("Subscriber role undefined for self registration");
        }

        try{
            /* update users role list with SELF_SIGN_UP_ROLE role */
            updateRolesOfUser(serverURL, adminUsername, adminPassword, workflowDTO.getWorkflowReference(), role);
        }catch(Exception e){
            throw new WorkflowException("Error while assigning role to user", e);

        }

    }


    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException{
        return null;
    }

}
