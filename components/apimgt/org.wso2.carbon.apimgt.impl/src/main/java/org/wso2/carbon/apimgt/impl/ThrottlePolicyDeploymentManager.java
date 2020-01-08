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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.throttling.GlobalThrottleEngineClient;
import org.wso2.carbon.apimgt.impl.utils.APIGatewayAdminClient;


import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
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
     * This method will be used to deploy policy to Global policy engine.
     *
     * @param policy     Policy string to be deployed.
     * @throws APIManagementException
     */
    public void deployPolicyToGlobalCEP(String policy) throws APIManagementException {
        try {
            globalThrottleEngineClient.deployExecutionPlan(policy);
        } catch (Exception e) {
            log.error("Error while deploying policy to global policy server." + e.getMessage());
        }
    }

    /**
     * This method will be usde to update an existing policy
     *
     * @param policyName policy name of the policy to be updated.
     * @param policy Policy string to be updated.
     * @throws APIManagementException
     */
    public void updatePolicyToGlobalCEP(String policyName, String policy) throws APIManagementException {
        try {
            globalThrottleEngineClient.updateExecutionPlan(policyName, policy);
        } catch (Exception e) {
            log.error("Error while updating policy to global policy server." + e.getMessage());
            throw new APIManagementException(e);
        }
    }

    /**
     * Undeploy policy from global CEP.
     *
     * @param policyName name of the policy file to be deleted.
     * @throws APIManagementException
     */
    public void undeployPolicyFromGlobalCEP(String policyName) throws APIManagementException {
        try {
            globalThrottleEngineClient.deleteExecutionPlan(policyName);
        } catch (Exception e) {
            log.error("Error while undeploying policy from global policy server." + e.getMessage());
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
            for(String policyName : policyNames) {
                globalThrottleEngineClient.deleteExecutionPlan(policyName);
            }
    }

    /**
     * Returns true if the passed execution plan is valid
     *
     * @param executionPlan
     * @return boolean
     */
    public boolean validateExecutionPlan(String executionPlan){
        return globalThrottleEngineClient.validateExecutionPlan(executionPlan);
    }
}
