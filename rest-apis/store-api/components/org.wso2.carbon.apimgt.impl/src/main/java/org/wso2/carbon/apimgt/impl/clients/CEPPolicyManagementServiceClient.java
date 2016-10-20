/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

/**
 * This class will be used to handle deploying throttling policies for central CEP server
 */
public class CEPPolicyManagementServiceClient {

    private static final Log log = LogFactory.getLog(CEPPolicyManagementServiceClient.class);
    private boolean debugEnabled = log.isErrorEnabled();
    private String username;

    public CEPPolicyManagementServiceClient() throws APIManagementException {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration();
        String serviceURL = config.getFirstProperty(APIConstants.CPS_SERVER_URL);
        username = config.getFirstProperty(APIConstants.CPS_SERVER_USERNAME);

        if (serviceURL == null) {
            throw new APIManagementException("Required connection details for the central policy server not provided");
        }
        try {

            ConfigurationContext ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
            //Initialize the client here
        } catch (AxisFault axisFault) {
            throw new APIManagementException("Error while initializing central policy client", axisFault);
        }
    }

    /**
     * This method is used to add policy to central policy server
     *
     * @param policy Policy to be added
     */
    public void addPolicy(Policy policy) {
        //Add policy implementation is going
    }
}
