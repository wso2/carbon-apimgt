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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionCreationWSWorkflowExecutor extends WorkflowExecutor{

    private static final Log log = LogFactory.getLog(SubscriptionCreationWSWorkflowExecutor.class);

    private String serviceEndpoint;

    private String username;

    private String password;

    private String contentType;

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }

    @Override
    public void execute(WorkflowDTO workflowDTO) throws WorkflowException{

        try {
            ServiceClient client = new ServiceClient(ServiceReferenceHolder.getInstance()
                    .getContextService().getClientConfigContext(), null);
            Options options = new Options();
            options.setAction("http://workflow.subscription.apimgt.carbon.wso2.org/initiate");
            options.setTo(new EndpointReference(serviceEndpoint));
            if(contentType != null){
                options.setProperty(Constants.Configuration.MESSAGE_TYPE, contentType);
            }

            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();

            //Consider this as a secured service if username and password are not null. Unsecured if not.
            if(username != null && password != null){
                auth.setUsername(username);
                auth.setPassword(password);
                auth.setPreemptiveAuthentication(true);
                List<String> authSchemes = new ArrayList<String>();
                authSchemes.add(HttpTransportProperties.Authenticator.BASIC);
                auth.setAuthSchemes(authSchemes);

                if(contentType == null){
                    options.setProperty(Constants.Configuration.MESSAGE_TYPE, HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
                }
                options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
                options.setManageSession(true);
            }

            client.setOptions(options);

            String payload = "<wor:SubscriptionApprovalWorkFlowProcessRequest xmlns:wor=\"http://workflow.subscription.apimgt.carbon.wso2.org\">\n" +
                    "         <wor:apiName>$1</wor:apiName>\n" +
                    "         <wor:apiVersion>$2</wor:apiVersion>\n" +
                    "         <wor:apiContext>$3</wor:apiContext>\n" +
                    "         <wor:apiProvider>$4</wor:apiProvider>\n" +
                    "         <wor:subscriber>$5</wor:subscriber>\n" +
                    "         <wor:applicationName>$6</wor:applicationName>\n" +
                    "         <wor:tierName>$7</wor:tierName>\n" +
                    "         <wor:workflowExternalRef>$8</wor:workflowExternalRef>\n" +
                    "         <wor:callBackURL>$9</wor:callBackURL>\n" +
                    "      </wor:SubscriptionApprovalWorkFlowProcessRequest>";

            SubscriptionWorkflowDTO subsWorkflowDTO = (SubscriptionWorkflowDTO)workflowDTO;
            String callBackURL = subsWorkflowDTO.getCallbackUrl();

            payload = payload.replace("$1", subsWorkflowDTO.getApiName()) ;
            payload = payload.replace("$2", subsWorkflowDTO.getApiVersion());
            payload = payload.replace("$3", subsWorkflowDTO.getApiContext());
            payload = payload.replace("$4", subsWorkflowDTO.getApiProvider());
            payload = payload.replace("$5", subsWorkflowDTO.getSubscriber());
            payload = payload.replace("$6", subsWorkflowDTO.getApplicationName());
            payload = payload.replace("$7", subsWorkflowDTO.getTierName());
            payload = payload.replace("$8", subsWorkflowDTO.getExternalWorkflowReference());
            payload = payload.replace("$9", callBackURL != null ? callBackURL : "?");

            client.fireAndForget(AXIOMUtil.stringToOM(payload));

            super.execute(workflowDTO);
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

        workflowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workflowDTO);
        log.info("Subscription Creation [Complete] Workflow Invoked. Workflow ID : " + workflowDTO.getExternalWorkflowReference() + "Workflow State : "+ workflowDTO.getStatus());

        if(WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())){
            ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
            try {
                apiMgtDAO.updateSubscriptionStatus(Integer.parseInt(workflowDTO.getWorkflowReference()),
                        APIConstants.SubscriptionStatus.UNBLOCKED);
            } catch (APIManagementException e) {
                log.error("Could not complete subscription creation workflow", e);
                throw new WorkflowException("Could not complete subscription creation workflow", e);
            }
        }else if(WorkflowStatus.REJECTED.equals(workflowDTO.getStatus())){
            ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
            try {
                apiMgtDAO.updateSubscriptionStatus(Integer.parseInt(workflowDTO.getWorkflowReference()),
                        APIConstants.SubscriptionStatus.REJECTED);
            } catch (APIManagementException e) {
                log.error("Could not complete subscription creation workflow", e);
                throw new WorkflowException("Could not complete subscription creation workflow", e);
            }
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
