/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.ApplicationUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Arrays;

public abstract class AbstractApplicationRegistrationWorkflowExecutor extends WorkflowExecutor{

    private static final Log log = LogFactory.getLog(AbstractApplicationRegistrationWorkflowExecutor.class);
    
    public String getWorkflowType(){
       return WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION;
    }

    public WorkflowResponse execute(WorkflowDTO workFlowDTO) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.debug("Executing AbstractApplicationRegistrationWorkflowExecutor...");
        }
        ApiMgtDAO dao = ApiMgtDAO.getInstance();
        try {
            //dao.createApplicationRegistrationEntry((ApplicationRegistrationWorkflowDTO) workFlowDTO, false);
            ApplicationRegistrationWorkflowDTO appRegDTO;
            if (workFlowDTO instanceof ApplicationRegistrationWorkflowDTO) {
                appRegDTO = (ApplicationRegistrationWorkflowDTO)workFlowDTO;
            }else{
                String message = "Invalid workflow type found";
                log.error(message);
                throw new WorkflowException(message);
            }
            dao.createApplicationRegistrationEntry(appRegDTO,false);
           // appRegDTO.getAppInfoDTO().saveDTO();
            super.execute(workFlowDTO);
        } catch (APIManagementException e) {
            log.error("Error while creating Application Registration entry.", e);
            throw new WorkflowException("Error while creating Application Registration entry.", e);
        }
        return new GeneralWorkflowResponse();
    }

    public WorkflowResponse complete(WorkflowDTO workFlowDTO) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.debug("Completing AbstractApplicationRegistrationWorkflowExecutor...");
        }
        super.complete(workFlowDTO);
        ApiMgtDAO dao = ApiMgtDAO.getInstance();
        try {
            String status = null;
            if ("CREATED".equals(workFlowDTO.getStatus().toString())) {
                status = APIConstants.AppRegistrationStatus.REGISTRATION_CREATED;
            } else if ("REJECTED".equals(workFlowDTO.getStatus().toString())) {
                status = APIConstants.AppRegistrationStatus.REGISTRATION_REJECTED;
            } else if ("APPROVED".equals(workFlowDTO.getStatus().toString())) {
                status = APIConstants.AppRegistrationStatus.REGISTRATION_APPROVED;
            }

            ApplicationRegistrationWorkflowDTO regWorkFlowDTO;
            if (workFlowDTO instanceof ApplicationRegistrationWorkflowDTO) {
                regWorkFlowDTO = (ApplicationRegistrationWorkflowDTO)workFlowDTO;
            } else {
                String message = "Invalid workflow type found";
                log.error(message);
                throw new WorkflowException(message);
            }
            dao.populateAppRegistrationWorkflowDTO(regWorkFlowDTO);

            dao.updateApplicationRegistration(status, regWorkFlowDTO.getKeyType(),
                    regWorkFlowDTO.getApplication().getId(),regWorkFlowDTO.getKeyManager());

        } catch (APIManagementException e) {
            log.error("Error while completing Application Registration entry.", e);
            throw new WorkflowException("Error while completing Application Registration entry.", e);
        }
        return new GeneralWorkflowResponse();
    }

    /**
     * This method will create a oAuth client at oAuthServer.
     * and will create a mapping with APIM using consumerKey
     * @param workflowDTO
     * @throws APIManagementException
     */
    protected void generateKeysForApplication(ApplicationRegistrationWorkflowDTO workflowDTO) throws
                                                                                              APIManagementException {
        if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
            dogenerateKeysForApplication(workflowDTO);
        }
    }

    public static void dogenerateKeysForApplication(ApplicationRegistrationWorkflowDTO workflowDTO)
            throws APIManagementException{
        log.debug("Registering Application and creating an Access Token... ");
        Application application = workflowDTO.getApplication();
        Subscriber subscriber = application.getSubscriber();
        ApiMgtDAO dao = ApiMgtDAO.getInstance();
        if (subscriber == null || workflowDTO.getAllowedDomains() == null) {
            dao.populateAppRegistrationWorkflowDTO(workflowDTO);
        }

        try {
            //get new key manager
            String tenantDomain = MultitenantUtils.getTenantDomain(subscriber.getName());
            String keyManagerName = workflowDTO.getKeyManager();
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance(tenantDomain, keyManagerName);
            if (keyManager == null){
                throw new APIManagementException("Key Manager " + keyManagerName + " not configured");
            }
            workflowDTO.getAppInfoDTO().getOAuthApplicationInfo()
                       .setClientName(application.getName());

            // set applications attributes to the oAuthApplicationInfo
            workflowDTO.getAppInfoDTO().getOAuthApplicationInfo()
                    .putAllAppAttributes(application.getApplicationAttributes());

            //createApplication on oAuthorization server.
            OAuthApplicationInfo oAuthApplication = keyManager.createApplication(workflowDTO.getAppInfoDTO());

            //update associateApplication
            ApplicationUtils
                    .updateOAuthAppAssociation(application, workflowDTO.getKeyType(), oAuthApplication, keyManagerName);

            //change create application status in to completed.
            dao.updateApplicationRegistration(APIConstants.AppRegistrationStatus.REGISTRATION_COMPLETED,
                    workflowDTO.getKeyType(), workflowDTO.getApplication().getId(), keyManagerName);

            workflowDTO.setApplicationInfo(oAuthApplication);
            AccessTokenInfo tokenInfo;
            Object enableTokenGeneration = keyManager.getKeyManagerConfiguration()
                    .getParameter(APIConstants.KeyManager.ENABLE_TOKEN_GENERATION);
                if (enableTokenGeneration != null && (Boolean) enableTokenGeneration &&
                        oAuthApplication.getJsonString().contains(APIConstants.GRANT_TYPE_CLIENT_CREDENTIALS)) {
                    AccessTokenRequest tokenRequest = ApplicationUtils.createAccessTokenRequest(keyManager,
                            oAuthApplication, null);
                    tokenInfo = keyManager.getNewApplicationAccessToken(tokenRequest);
                } else {
                    tokenInfo = new AccessTokenInfo();
                    tokenInfo.setAccessToken("");
                    tokenInfo.setValidityPeriod(0L);
                    String[] noScopes = new String[] {"N/A"};
                    tokenInfo.setScope(noScopes);
                    oAuthApplication.addParameter("tokenScope", Arrays.toString(noScopes));
                }
            workflowDTO.setAccessTokenInfo(tokenInfo);
        } catch (Exception e) {
            APIUtil.handleException("Error occurred while executing SubscriberKeyMgtClient.", e);
        }
    }

}
