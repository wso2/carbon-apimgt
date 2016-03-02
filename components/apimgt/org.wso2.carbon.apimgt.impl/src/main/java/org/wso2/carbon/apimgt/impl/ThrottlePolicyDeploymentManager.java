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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

public class ThrottlePolicyDeploymentManager {
    private static final Log log = LogFactory.getLog(ThrottlePolicyDeploymentManager.class);
    private static ThrottlePolicyDeploymentManager instance;

    private ThrottlePolicyDeploymentManager() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        // get the gateway manager related configurations
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
    public void deployPolicyToGlobalCEP(String policy) throws APIManagementException {
        try {
            OMElement element = AXIOMUtil.stringToOM(policy);
            String elegibilityQuery = element.getFirstChildWithName(new QName(APIConstants.ELIGIBILITY_QUERY_ELEM))
                    .getText();
            String decisionQuery = element.getFirstChildWithName(new QName(APIConstants.DECISION_QUERY_ELEM))
                    .getText();
            //deploy to cep
        } catch (XMLStreamException e) {
            String msg = "Error while parsing the policy to get the eligibility query: ";
            log.error(msg , e);
            throw new APIManagementException(msg);
        }
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
            // TODO call gatewaymanager admin service to deploy.

            // Temp. save in local location
            writeToFile(policy, fileName);
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
    
    private void writeToFile(String content, String fileName) throws IOException {
        File file = new File(APIConstants.POLICY_FILE_FOLDER); // WSO2Carbon_Home/repository/deployment/server/throttle-config
        if (!file.exists()) { // if directory doesn't exist, make onee
            file.mkdir();
        }
        File writeFile = new File(APIConstants.POLICY_FILE_LOCATION + fileName); // file folder+/
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(writeFile);
            if (!writeFile.exists()) {
                writeFile.createNewFile();
            }
            byte[] contentInBytes = content.getBytes();
            fos.write(contentInBytes);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            log.error("Error occurred writing to file: " + fileName, e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                log.error("Error occurred closing file output stream", e);
            }
        }
    }
}
