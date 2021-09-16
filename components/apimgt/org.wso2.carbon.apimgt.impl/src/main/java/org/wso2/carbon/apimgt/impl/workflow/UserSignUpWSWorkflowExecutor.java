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

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.UserRegistrationConfigDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.SelfSignUpUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;

public class UserSignUpWSWorkflowExecutor extends UserSignUpWorkflowExecutor {
    private static final Log log = LogFactory.getLog(UserSignUpWSWorkflowExecutor.class);
    private String serviceEndpoint;
    private String username;
    private char[] password;
    private String contentType;

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_USER_SIGNUP;
    }

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Executing User SignUp Webservice Workflow for " + workflowDTO.getWorkflowReference());
        }

        try {
            String action = WorkflowConstants.REGISTER_USER_WS_ACTION;
            ServiceClient client = getClient(action);

            //get the default empty payload
            String payload = WorkflowConstants.REGISTER_USER_PAYLOAD;

            String callBackURL = workflowDTO.getCallbackUrl();
            String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(workflowDTO.getWorkflowReference());

            payload = payload.replace("$1", tenantAwareUserName);
            payload = payload.replace("$2", workflowDTO.getTenantDomain());
            payload = payload.replace("$3", workflowDTO.getExternalWorkflowReference());
            payload = payload.replace("$4", callBackURL != null ? callBackURL : "?");

            client.fireAndForget(AXIOMUtil.stringToOM(payload));
            super.execute(workflowDTO);
        } catch (AxisFault axisFault) {
            log.error("Error sending out message", axisFault);
            throw new WorkflowException("Error sending out message", axisFault);
        } catch (XMLStreamException e) {
            log.error("Error converting String to OMElement", e);
            throw new WorkflowException("Error converting String to OMElement", e);
        }
        return new GeneralWorkflowResponse();
    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {

//        workflowDTO.setStatus(workflowDTO.getStatus());
        workflowDTO.setUpdatedTime(System.currentTimeMillis());

        if (log.isDebugEnabled()) {
            log.debug("User Sign Up [Complete] Workflow Invoked. Workflow ID : " +
                    workflowDTO.getExternalWorkflowReference() + "Workflow State : " +
                    workflowDTO.getStatus());
        }               

        super.complete(workflowDTO);


        String tenantDomain = workflowDTO.getTenantDomain();
        try {

            UserRegistrationConfigDTO signupConfig = SelfSignUpUtil.getSignupConfiguration(tenantDomain);


            String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(workflowDTO.getWorkflowReference());

            if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
                try {
                    updateRolesOfUser(tenantAwareUserName,
                            SelfSignUpUtil.getRoleNames(signupConfig), tenantDomain);
                } catch (Exception e) {

                    // updateRolesOfUser throws generic Exception. Therefore generic Exception is caught
                    throw new WorkflowException("Error while assigning role to user", e);
                }
            } else {
                try {
                    /* Remove created user */
                    deleteUser(tenantDomain, tenantAwareUserName);
                } catch (Exception e) {
                    throw new WorkflowException("Error while deleting the user", e);
                }
            }
        } catch (APIManagementException e1) {
            throw new WorkflowException("Error while accessing signup configuration", e1);
        }
        return new GeneralWorkflowResponse();
    }

    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {
        String errorMsg;

        super.cleanUpPendingTask(workflowExtRef);
        try {
            String action = WorkflowConstants.DELETE_USER_WS_ACTION;
            ServiceClient client = getClient(action);

            String payload = "<p:CancelUserSignupProcessRequest " +
                    "        xmlns:p=\"http://workflow.registeruser.apimgt.carbon.wso2.org\">" +
                    "           <p:workflowRef>" + workflowExtRef + "</p:workflowRef>" +
                    "        </p:CancelUserSignupProcessRequest>";

            client.fireAndForget(AXIOMUtil.stringToOM(payload));
        } catch (AxisFault axisFault) {
            errorMsg = "Error sending out cancel pending user signup approval process message. Cause: " + axisFault
                    .getMessage();
            throw new WorkflowException(errorMsg, axisFault);
        } catch (XMLStreamException e) {
            errorMsg = "Error converting cancel user signup String to OMElement. Cause: " + e.getMessage();
            throw new WorkflowException(errorMsg, e);
        }
    }

    /**
     * Retrieves configured ServiceClient for communication with external services
     *
     * @param action web service action to use
     * @return configured service client
     * @throws AxisFault
     */
    public ServiceClient getClient(String action) throws AxisFault {
        ServiceClient client = new ServiceClient(
                ServiceReferenceHolder.getInstance().getContextService().getClientConfigContext(), null);
        Options options = new Options();
        options.setAction(action);
        options.setTo(new EndpointReference(serviceEndpoint));

        if (contentType != null) {
            options.setProperty(Constants.Configuration.MESSAGE_TYPE, contentType);
        } else {
            options.setProperty(Constants.Configuration.MESSAGE_TYPE, HTTPConstants.MEDIA_TYPE_TEXT_XML);
        }

        HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();

        // Assumes authentication is required if username and password is given
        if (username != null && !username.isEmpty() && password != null && password.length != 0) {
            auth.setUsername(username);
            auth.setPassword(String.valueOf(password));
            auth.setPreemptiveAuthentication(true);
            List<String> authSchemes = new ArrayList<String>();
            authSchemes.add(HttpTransportProperties.Authenticator.BASIC);
            auth.setAuthSchemes(authSchemes);

            if (contentType == null) {
                options.setProperty(Constants.Configuration.MESSAGE_TYPE, HTTPConstants.MEDIA_TYPE_TEXT_XML);
            }
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
            options.setManageSession(true);
        }
        client.setOptions(options);

        return client;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
