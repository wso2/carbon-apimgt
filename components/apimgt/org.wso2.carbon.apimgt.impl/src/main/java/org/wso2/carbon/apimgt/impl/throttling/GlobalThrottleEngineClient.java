/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.throttling;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.log4j.Logger;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub;
import org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto;


import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class GlobalThrottleEngineClient {
    private AuthenticationAdminStub authenticationAdminStub = null;
    private static final Logger log = Logger.getLogger(GlobalThrottleEngineClient.class);
    ThrottleProperties.PolicyDeployer policyDeployerConfiguration = ServiceReferenceHolder.getInstance()
            .getAPIManagerConfigurationService().getAPIManagerConfiguration().getThrottleProperties()
            .getPolicyDeployer();

    private String login() throws RemoteException, LoginAuthenticationExceptionException, MalformedURLException {
        authenticationAdminStub = new AuthenticationAdminStub(policyDeployerConfiguration.getServiceUrl() +
                "AuthenticationAdmin");
        String sessionCookie = null;

        if (authenticationAdminStub.login(policyDeployerConfiguration.getUsername(), policyDeployerConfiguration.getPassword(),
                new URL(policyDeployerConfiguration.getServiceUrl()).getHost())) {
            ServiceContext serviceContext = authenticationAdminStub._getServiceClient().getLastOperationContext()
                    .getServiceContext();
            sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        }
        return sessionCookie;
    }

    /**
     * Checks the validity of a execution plan
     *
     * @param executionPlan
     * @return boolean
     */
    public boolean validateExecutionPlan(String executionPlan){

        ServiceClient serviceClient;
        Options options;
        String result = null;
        try {
            String sessionCookie = login();
            EventProcessorAdminServiceStub eventProcessorAdminServiceStub = new EventProcessorAdminServiceStub
                    (policyDeployerConfiguration.getServiceUrl() + "EventProcessorAdminService");

            serviceClient = eventProcessorAdminServiceStub._getServiceClient();
            options = serviceClient.getOptions();
            options.setManageSession(true);
            options.setProperty(HTTPConstants.COOKIE_STRING, sessionCookie);

            result = eventProcessorAdminServiceStub.validateExecutionPlan(executionPlan);

        } catch (RemoteException e) {
            return false;
        } catch (MalformedURLException e) {
            return false;
        } catch (LoginAuthenticationExceptionException e) {
            return false;
        }

        if("success".equalsIgnoreCase(result)){
            return true;
        }
        return false;
    }
    
    /**
     * 1. Check validity of execution plan
     * 2. If execution plan exist with same name edit it
     * 3. Else deploy new execution plan
     *
     * @param name          Name of execution plan
     * @param executionPlan execution query plan
     * @param sessionCookie session cookie to use established connection
     * @throws RemoteException
     */
    private void deploy(String name, String executionPlan, String sessionCookie) throws RemoteException {
        ServiceClient serviceClient;
        Options options;

        EventProcessorAdminServiceStub eventProcessorAdminServiceStub = new EventProcessorAdminServiceStub
                (policyDeployerConfiguration.getServiceUrl() + "EventProcessorAdminService");
        serviceClient = eventProcessorAdminServiceStub._getServiceClient();
        options = serviceClient.getOptions();
        options.setManageSession(true);
        options.setProperty(HTTPConstants.COOKIE_STRING, sessionCookie);

        eventProcessorAdminServiceStub.validateExecutionPlan(executionPlan);
        ExecutionPlanConfigurationDto[] executionPlanConfigurationDtos = eventProcessorAdminServiceStub
                .getAllActiveExecutionPlanConfigurations();
        boolean isUpdateRequest = false;
        if (executionPlanConfigurationDtos != null) {
            for (ExecutionPlanConfigurationDto executionPlanConfigurationDto : executionPlanConfigurationDtos) {
                if (executionPlanConfigurationDto.getName().trim().equals(name)) {
                    eventProcessorAdminServiceStub.editActiveExecutionPlan(executionPlan, name);
                    isUpdateRequest = true;
                    break;
                }
            }
        }
        if (!isUpdateRequest) {
            eventProcessorAdminServiceStub.deployExecutionPlan(executionPlan);
        }

    }


    private void logout() throws RemoteException, LogoutAuthenticationExceptionException {
        authenticationAdminStub.logout();
    }


    public void deployExecutionPlan(String name, String executionPlan)
            throws Exception {
        try {
            String sessionID = login();
            deploy(name, executionPlan, sessionID);
        } catch (Throwable e) {
            throw new Exception("Error in deploying policy \n" + executionPlan + "\nin global " +
                    "throttling engine", e);
        } finally {
            try {
                logout();
            } catch (RemoteException e) {
                log.error("Error when logging out from global throttling engine. " + e.getMessage(), e);
            } catch (LogoutAuthenticationExceptionException e) {
                log.error("Error when logging out from global throttling engine. " + e.getMessage(), e);
            }
        }
    }

    /**
     * This method will be used to delete single execution plan.
     * @param name execution plan name to be deleted.
     */
    public void deleteExecutionPlan(String name) {
        ServiceClient serviceClient;
        Options options;
        String sessionID = null;
        try {
            sessionID = login();
        } catch (RemoteException e) {
            log.error("Error while connecting to login central policy management server" + e.getMessage());
        } catch (LoginAuthenticationExceptionException e) {
            log.error("Error while connecting to login central policy management server, " +
                    "Check user name and password"
                    + e.getMessage());
        } catch (MalformedURLException e) {
            log.error("Error while connecting to login central policy management server, check URL" +
                    e.getMessage());
        }
        EventProcessorAdminServiceStub eventProcessorAdminServiceStub = null;
        try {
            eventProcessorAdminServiceStub = new EventProcessorAdminServiceStub
                    (policyDeployerConfiguration.getServiceUrl() + "EventProcessorAdminService");
            serviceClient = eventProcessorAdminServiceStub._getServiceClient();
            options = serviceClient.getOptions();
            options.setManageSession(true);
            options.setProperty(HTTPConstants.COOKIE_STRING, sessionID);
            eventProcessorAdminServiceStub.undeployActiveExecutionPlan(name);
        } catch (AxisFault axisFault) {
            log.error("Error while connecting to login central policy management server to delete " +
                    "execution plan." + axisFault);
        } catch (RemoteException e) {
            log.error("Error while connecting to login central policy management server to delete " +
                    "execution plan." + e.getMessage());
        }

    }
}
