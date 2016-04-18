/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
*
*/
package org.wso2.carbon.apimgt.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.throttling.GlobalThrottleEngineClient;
import org.wso2.carbon.apimgt.impl.utils.APIGatewayAdminClient;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub;
import org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public class ThrottlePolicyDeploymentManager {
    private static final Log log = LogFactory.getLog(ThrottlePolicyDeploymentManager.class);
    private static ThrottlePolicyDeploymentManager instance;
    private Map<String, Environment> environments;
    private GlobalThrottleEngineClient globalThrottleEngineClient = new GlobalThrottleEngineClient();

    private ThrottlePolicyDeploymentManager() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        environments = config.getApiGatewayEnvironments();

    }

    public static synchronized ThrottlePolicyDeploymentManager getInstance() {
        if (instance == null) {
            instance = new ThrottlePolicyDeploymentManager();
        }
        return instance;
    }

    /**
     * Deploy policy in the global even processor
     * @param policy
     * @throws APIManagementException
     */
    public void deployPolicyToGlobalCEP(String policyName, String policy) throws APIManagementException {

        try {
            globalThrottleEngineClient.deployExecutionPlan(policyName, policy);
        } catch (Exception e) {
            log.error(e);
        }
        /*try {
            OMElement element = AXIOMUtil.stringToOM(policy);
            String elegibilityQuery = element.getFirstChildWithName(new QName(APIConstants.ELIGIBILITY_QUERY_ELEM))
                    .getText();
            String decisionQuery = element.getFirstChildWithName(new QName(APIConstants.DECISION_QUERY_ELEM))
                    .getText();
            String fileName = element.getAttributeValue(new QName(APIConstants.POLICY_NAME_ELEM));
            //deploy to cep
            String policyQuery = elegibilityQuery + "\n" + decisionQuery;
            if(log.isDebugEnabled()){
                log.debug("deploy policy to global event processor : \n" + policyQuery );
            }
            deployPolicyInGlobalThrottleEngine(fileName,policyQuery);
            //throttler.deployGlobalThrottlingPolicy(fileName, policyQuery);
        } catch (XMLStreamException e) {
            String msg = "Error while parsing the policy to get the eligibility query: ";
            log.error(msg , e);
            throw new APIManagementException(msg);
        }*/
    }
    
    /**
     * Undeploy policy from global CEP. 
     * @param policies names of the policy files.
     * @throws APIManagementException
     */
    public void undeployPolicyFromGlobalCEP(List<String> policies) throws APIManagementException {
        //TODO
    }

    /**
     * deploy policy in the gateway manager
     * @param policy
     * @throws APIManagementException
     */
    public void deployPolicyToGatewayManager(String policy) throws APIManagementException {
        OMElement element;
        try {
            element = AXIOMUtil.stringToOM(policy);
            String fileName = element.getAttributeValue(new QName(APIConstants.POLICY_NAME_ELEM));
            for (Map.Entry<String, Environment> environment : environments.entrySet()) {
                if(log.isDebugEnabled()){
                    log.debug("deploy policy to gateway : " + environment.getValue().getName());
                }
                APIGatewayAdminClient client = new APIGatewayAdminClient(null , environment.getValue());
                client.deployPolicy(policy, fileName);
            }
     
        } catch (XMLStreamException e) {
            String msg = "Error while parsing the policy to get the eligibility query: ";
            log.error(msg , e);
            throw new APIManagementException(msg);
        } catch (IOException e) {
            String msg = "Error while deploying the policy in gateway manager: ";
            log.error(msg , e);
            throw new APIManagementException(msg);
        }
        
    }

    /**
     * Undeploy policy from the gateway manager nodes
     *
     * @param policyNames
     * @return
     * @throws APIManagementException
     */
    public void undeployPolicyFromGatewayManager(String[] policyNames) throws APIManagementException {

        for (Map.Entry<String, Environment> environment : environments.entrySet()) {
            if (log.isDebugEnabled()) {
                log.debug("undeploy policy from gateway : " + environment.getValue().getName());
            }
            APIGatewayAdminClient client;
            try {
                client = new APIGatewayAdminClient(null, environment.getValue());
                client.undeployPolicy(policyNames);
            } catch (AxisFault axisFault) {
                String msg = "Error occurred when undeploying from gateway " + environment.getValue().getName();
                log.error(msg, axisFault);
                throw new APIManagementException(msg);
            }
        }

    }

    private boolean deployPolicyInGlobalThrottleEngine(String executionPlan, String name) throws
            APIManagementException {
        ServiceClient serviceClient;
        Options options;
        AuthenticationAdminStub authenticationAdminStub;
        EventProcessorAdminServiceStub eventProcessorAdminServiceStub;
        ThrottleProperties.PolicyDeployer policyDeployerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService
                        ().getAPIManagerConfiguration().getThrottleProperties().getPolicyDeployer();
        try {
            authenticationAdminStub = new AuthenticationAdminStub(policyDeployerConfiguration.getServiceUrl() +
                    "AuthenticationAdmin");
            String sessionCookie = null;

            if (authenticationAdminStub.login(policyDeployerConfiguration.getUsername(), policyDeployerConfiguration
                    .getPassword(), new URL(policyDeployerConfiguration.getServiceUrl()).getHost())) {
                ServiceContext serviceContext = authenticationAdminStub._getServiceClient().getLastOperationContext()
                        .getServiceContext();
                sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            }
            if (sessionCookie != null) {
                eventProcessorAdminServiceStub = new EventProcessorAdminServiceStub("/EventProcessorAdminService");
                serviceClient = eventProcessorAdminServiceStub._getServiceClient();
                options = serviceClient.getOptions();
                options.setManageSession(true);
                options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);

                eventProcessorAdminServiceStub.validateExecutionPlan(executionPlan);
                ExecutionPlanConfigurationDto[] executionPlanConfigurationDtos = eventProcessorAdminServiceStub
                        .getAllActiveExecutionPlanConfigurations();
                boolean isUpdateRequest = false;
                for (ExecutionPlanConfigurationDto executionPlanConfigurationDto : executionPlanConfigurationDtos) {
                    if (executionPlanConfigurationDto.getName().equals(name)) {
                        eventProcessorAdminServiceStub.editActiveExecutionPlan(executionPlan, name);
                        isUpdateRequest = true;
                        break;
                    }
                }
                if (!isUpdateRequest) {
                    eventProcessorAdminServiceStub.deployExecutionPlan(executionPlan);
                }

            }
        } catch (AxisFault axisFault) {
            log.error("Couldn't deploy the policy", axisFault);
        } catch (RemoteException e) {
            log.error("Couldn't connect to  the global policy engine", e);
        } catch (LoginAuthenticationExceptionException e) {
            log.error("Couldn't log into  the global policy engine", e);
        } catch (MalformedURLException e) {
            log.error("Couldn't resolve the hostname of global policy engine", e);
        }
        return false;
    }
}
