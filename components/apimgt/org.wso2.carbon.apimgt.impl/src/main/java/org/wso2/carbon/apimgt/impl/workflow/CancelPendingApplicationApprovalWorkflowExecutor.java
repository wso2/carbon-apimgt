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
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple workflow executor for application delete action
 */
public class CancelPendingApplicationApprovalWorkflowExecutor extends ApplicationDeletionSimpleWorkflowExecutor {

    private String serviceEndpoint;

    private String username;

    private String password;

    private String contentType;

    private static final Log log = LogFactory.getLog(ApplicationCreationWSWorkflowExecutor.class);

    @Override
    public void execute(WorkflowDTO workflowDTO) throws WorkflowException {
        ApplicationWorkflowDTO applicationWorkflowDTO = (ApplicationWorkflowDTO) workflowDTO;
        removeSubscriptionProcessesByApplication(applicationWorkflowDTO.getApplication());
        removeRegistrationProcessByApplicationId(applicationWorkflowDTO.getApplication().getId());
        try {
            Map<String, String> args = new HashMap<String, String>();
            args.put("action", "http://workflow.application.apimgt.carbon.wso2.org/cancel");
            args.put("username", username);
            args.put("password", password);
            args.put("serviceEndpoint", serviceEndpoint);
            args.put("contentType", contentType);
            ServiceClient client = getClient(args);

            String payload = "<p:CancelApplicationApprovalWorkflowProcessRequest " +
                    "        xmlns:p=\"http://workflow.application.apimgt.carbon.wso2.org\">\n" +
                    "           <p:workflowRef>" + applicationWorkflowDTO.getExternalWorkflowReference() +
                    "</p:workflowRef>\n" +
                    "        </p:CancelApplicationApprovalWorkflowProcessRequest>";

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

    /**
     * Removes and application's registration processes created at the BPS
     *
     * @param applicationId of the application
     */
    private void removeRegistrationProcessByApplicationId(int applicationId) throws WorkflowException {
        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
        ApplicationRegistrationWSWorkflowExecutor applicationRegistrationWFExecutor =
                (ApplicationRegistrationWSWorkflowExecutor) WorkflowExecutorFactory.getInstance().
                        getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
        String subContentType = applicationRegistrationWFExecutor.getContentType();
        String subUsername = applicationRegistrationWFExecutor.getUsername();
        String subPassword = applicationRegistrationWFExecutor.getPassword();
        String subServiceEndPoint = applicationRegistrationWFExecutor.getServiceEndpoint();

        try {
            Set<String> registrationIDs = apiMgtDAO.getRegistrationWFReferencesByApplicationId(applicationId);

            Map<String, String> args = new HashMap<String, String>();
            args.put("action", "http://workflow.application.apimgt.carbon.wso2.org/cancel");
            args.put("username", subUsername);
            args.put("password", subPassword);
            args.put("serviceEndpoint", subServiceEndPoint);
            args.put("contentType", subContentType);
            ServiceClient client = getClient(args);

            for (String registration : registrationIDs) {
                try {
                    String payload = "  <p:CancelApplicationRegistrationWorkflowProcessRequest " +
                            "           xmlns:p=\"http://workflow.application.apimgt.carbon.wso2.org\">\n" +
                            "               <p:workflowRef>" + registration + "</p:workflowRef>\n" +
                            "           </p:CancelApplicationRegistrationWorkflowProcessRequest>";
                    client.fireAndForget(AXIOMUtil.stringToOM(payload));
                } catch (AxisFault axisFault) {
                    log.error("Error sending out message", axisFault);
                    throw new WorkflowException("Error sending out message", axisFault);
                } catch (XMLStreamException e) {
                    log.error("Error converting String to OMElement", e);
                    throw new WorkflowException("Error converting String to OMElement", e);
                }
            }
        } catch (APIManagementException e) {
            log.error(e.getMessage(), e);
            throw new WorkflowException(e.getMessage(), e);
        } catch (AxisFault axisFault) {
            log.error("Error sending out message", axisFault);
            throw new WorkflowException("Error sending out message", axisFault);
        }
    }

    /**
     * Removes an application's pending subscription processes from BPS
     *
     * @param application Application which has the subscriptions to be removed
     * @throws WorkflowException
     */
    private void removeSubscriptionProcessesByApplication(Application application) throws WorkflowException {
        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
        CancelPendingSubscriptionWorkflowExecutor cancelPendingSubscriptionExecutor =
                (CancelPendingSubscriptionWorkflowExecutor) WorkflowExecutorFactory.getInstance().
                        getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION);
        String subContentType = cancelPendingSubscriptionExecutor.getContentType();
        String subUsername = cancelPendingSubscriptionExecutor.getUsername();
        String subPassword = cancelPendingSubscriptionExecutor.getPassword();
        String subServiceEndPoint = cancelPendingSubscriptionExecutor.getServiceEndpoint();
        try {
            Set<Integer> pendingSubscriptions = apiMgtDAO.getPendingSubscriptionsByApplicationId(application.getId());
            Map<String, String> args = new HashMap<String, String>();
            args.put("action", "http://workflow.subscription.apimgt.carbon.wso2.org/cancel");
            args.put("username", subUsername);
            args.put("password", subPassword);
            args.put("serviceEndpoint", subServiceEndPoint);
            args.put("contentType", subContentType);
            ServiceClient client = getClient(args);

            for (int subscription : pendingSubscriptions) {
                String workflowExtRef = null;
                try {
                    workflowExtRef = apiMgtDAO.getExternalWorkflowReferenceForSubscription(subscription);
                    String payload = "  <wor:CancelSubscriptionApprovalWorkflowProcessRequest " +
                            "           xmlns:wor=\"http://workflow.subscription.apimgt.carbon.wso2.org\">\n" +
                            "               <wor:workflowExtRef>" + workflowExtRef + "</wor:workflowExtRef>\n" +
                            "           </wor:CancelSubscriptionApprovalWorkflowProcessRequest>";
                    client.fireAndForget(AXIOMUtil.stringToOM(payload));
                } catch (AxisFault axisFault) {
                    log.error("Error sending out message", axisFault);
                    throw new WorkflowException("Error sending out message", axisFault);
                } catch (XMLStreamException e) {
                    log.error("Error converting String to OMElement", e);
                    throw new WorkflowException("Error converting String to OMElement", e);
                }
            }
        } catch (APIManagementException e) {
            log.error(e.getMessage(), e);
            throw new WorkflowException(e.getMessage(), e);
        } catch (AxisFault axisFault) {
            log.error("Error sending out message", axisFault);
            throw new WorkflowException("Error sending out message", axisFault);
        }
    }

    public ServiceClient getClient(Map<String, String> args) throws AxisFault {
        ServiceClient client = new ServiceClient(ServiceReferenceHolder.getInstance()
                .getContextService().getClientConfigContext(), null);
        Options options = new Options();
        options.setAction(args.get("action"));
        options.setTo(new EndpointReference(args.get("serviceEndpoint")));

        if (args.get("contentType") != null) {
            options.setProperty(Constants.Configuration.MESSAGE_TYPE, args.get("contentType"));
        } else {
            options.setProperty(Constants.Configuration.MESSAGE_TYPE,
                    HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
        }

        HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();

        // Assumes authentication is required if username and password is given
        if (args.get("username") != null && args.get("password") != null) {
            auth.setUsername(args.get("username"));
            auth.setPassword(args.get("password"));
            auth.setPreemptiveAuthentication(true);
            List<String> authSchemes = new ArrayList<String>();
            authSchemes.add(HttpTransportProperties.Authenticator.BASIC);
            auth.setAuthSchemes(authSchemes);

            if (args.get("contentType") == null) {
                options.setProperty(Constants.Configuration.MESSAGE_TYPE, HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
            }
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE,
                    auth);
            options.setManageSession(true);
        }

        client.setOptions(options);

        return client;
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
