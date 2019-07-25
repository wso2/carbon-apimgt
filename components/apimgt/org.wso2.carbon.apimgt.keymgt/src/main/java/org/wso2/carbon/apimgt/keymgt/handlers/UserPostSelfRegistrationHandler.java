/*
 *Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.carbon.apimgt.keymgt.handlers;

import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.core.handler.InitConfig;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryServerException;
import org.wso2.carbon.identity.recovery.util.Utils;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.api.UserStoreException;
import java.util.List;
import static java.util.Arrays.asList;

/**
 * This class handler adds a new user role to a self registered user.
 */
public class UserPostSelfRegistrationHandler extends AbstractEventHandler {

    //Role Constants
    private static final String SUBSCRIBER_ROLE = "Internal/subscriber";
    private static final String SELF_SIGNUP_ROLE = "Internal/selfsignup";
    private static final int HIGH_PRIORITY = 250;

    @Override
    public String getName() {
        return "userPostSelfRegistration";
    }

    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        String tenantDomain = (String) event.getEventProperties()
                .get(IdentityEventConstants.EventProperty.TENANT_DOMAIN);
        String userName = (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.USER_NAME);

        //The handler should be called ss a post add user event.
        if (IdentityEventConstants.Event.POST_ADD_USER.equals(event.getEventName())) {
            try {
                executeUserRegistrationWorkflow(tenantDomain, userName);
            } catch (IdentityRecoveryServerException e) {
                throw new IdentityEventException("Error while adding custom roles to the user", e);
            }
        }
    }

    /**
     * This method adds new role to the existing user roles
     * @param tenantDomain tenant domain extracted from the event
     * @param userName username extracted from the event
     * @throws org.wso2.carbon.identity.recovery.IdentityRecoveryServerException when unable to retrieve
     * userStoreManager instance
     */
    private void executeUserRegistrationWorkflow(String tenantDomain, String userName)
            throws org.wso2.carbon.identity.recovery.IdentityRecoveryServerException {
        try {
            //Realm service is used for user management tasks
            RealmService realmService = APIKeyMgtDataHolder.getRealmService();
            UserStoreManager userStoreManager;
            try {
                userStoreManager = realmService.getTenantUserRealm(IdentityTenantUtil.getTenantId(tenantDomain))
                        .getUserStoreManager();
            } catch (UserStoreException e) {
                throw Utils
                        .handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_UNEXPECTED, userName,
                                e);
            }
            //Start a tenant flow
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(IdentityTenantUtil.getTenantId(tenantDomain));
            carbonContext.setTenantDomain(tenantDomain);

            if (userStoreManager.isExistingUser(userName)) {
                List<String> roleList = asList(userStoreManager.getRoleListOfUser(userName));
                //User should have selfSignup role. Checking whether the user is in the new role
                if (roleList.contains(SELF_SIGNUP_ROLE) && !roleList.contains(SUBSCRIBER_ROLE)) {
                    WorkflowExecutor userSignUpWFExecutor = WorkflowExecutorFactory.getInstance()
                            .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_USER_SIGNUP);

                    //initiate a new signup workflow
                    WorkflowDTO signUpWFDto = new WorkflowDTO();
                    signUpWFDto.setWorkflowReference(userName);
                    signUpWFDto.setStatus(WorkflowStatus.CREATED);
                    signUpWFDto.setCreatedTime(System.currentTimeMillis());
                    signUpWFDto.setTenantDomain(tenantDomain);
                    signUpWFDto.setTenantId(IdentityTenantUtil.getTenantId(tenantDomain));
                    signUpWFDto.setExternalWorkflowReference(userSignUpWFExecutor.generateUUID());
                    signUpWFDto.setWorkflowType(WorkflowConstants.WF_TYPE_AM_USER_SIGNUP);
                    signUpWFDto.setCallbackUrl(userSignUpWFExecutor.getCallbackURL());
                    userSignUpWFExecutor.execute(signUpWFDto);

                }
            }
        } catch (UserStoreException | WorkflowException e) {
            throw Utils
                    .handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_UNEXPECTED, userName, e);
        } finally {
            Utils.clearArbitraryProperties();
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void init(InitConfig configuration) throws IdentityRuntimeException {
        super.init(configuration);
    }

    @Override
    public int getPriority(MessageContext messageContext) {
        return HIGH_PRIORITY;
    }
}
