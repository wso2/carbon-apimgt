/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.axiom.om.OMElement;
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
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

/**
 * WS workflow executor for subscription delete action
 * cancelPendingSubscriptionWorkflows
 */
public class CancelPendingSubscriptionWorkflowExecutor extends SubscriptionDeletionSimpleWorkflowExecutor {

    private String serviceEndpoint;

    private String username;

    private String password;

    private String contentType;

    private static final Log log = LogFactory.getLog(ApplicationCreationWSWorkflowExecutor.class);

    @Override
    public void execute(WorkflowDTO workflowDTO) throws WorkflowException {
        try {
            ServiceClient client = new ServiceClient(ServiceReferenceHolder.getInstance()
                    .getContextService().getClientConfigContext(), null);
            Options options = new Options();
            options.setAction("http://workflow.subscription.apimgt.carbon.wso2.org/cancel");
            options.setTo(new EndpointReference(serviceEndpoint));

            if (contentType != null) {
                options.setProperty(Constants.Configuration.MESSAGE_TYPE, contentType);
            } else {
                options.setProperty(Constants.Configuration.MESSAGE_TYPE,
                        HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
            }

            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();

            // Assumes authentication is required if username and password is given
            if (username != null && password != null) {
                auth.setUsername(username);
                auth.setPassword(password);
                auth.setPreemptiveAuthentication(true);
                List<String> authSchemes = new ArrayList<String>();
                authSchemes.add(HttpTransportProperties.Authenticator.BASIC);
                auth.setAuthSchemes(authSchemes);

                if(contentType == null){
                    options.setProperty(Constants.Configuration.MESSAGE_TYPE, HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
                }
                options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE,
                        auth);
                options.setManageSession(true);
            }

            client.setOptions(options);

            SubscriptionWorkflowDTO appWorkFlowDTO = (SubscriptionWorkflowDTO) workflowDTO;
            String payload = "<wor:CancelSubscriptionApprovalWorkflowProcessRequest " +
                    "           xmlns:wor=\"http://workflow.subscription.apimgt.carbon.wso2.org\">\n" +
                    "           <wor:workflowExtRef>$1</wor:workflowExtRef>\n" +
                    "        </wor:CancelSubscriptionApprovalWorkflowProcessRequest>";

            payload = payload.replace("$1", appWorkFlowDTO.getExternalWorkflowReference());
            client.fireAndForget(AXIOMUtil.stringToOM(payload));
            // call complete method here since there are no callbacks to fire complete method
            complete(workflowDTO);
        } catch (AxisFault axisFault) {
            log.error("Error sending out message", axisFault);
            throw new WorkflowException("Error sending out message", axisFault);
        } catch (XMLStreamException e) {
            log.error("Error converting String to OMElement", e);
            throw new WorkflowException("Error converting String to OMElement", e);
        }
    }

    @Override
    public void complete(WorkflowDTO workflowDTO) throws WorkflowException {
        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();

        workflowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workflowDTO);
        try {
            workflowDTO.setStatus(WorkflowStatus.APPROVED);
            apiMgtDAO.updateWorkflowStatus(workflowDTO);
            publishEvents(workflowDTO);
        } catch (APIManagementException e) {
            throw new WorkflowException("Error while updating workflow", e);
        }
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
