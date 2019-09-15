/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.impl.alertmgt;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

public class AlertConfigManager {

    private static AlertConfigManager alertConfigManager = null;

    private AlertConfigManager() throws APIManagementException {
        if (!APIUtil.isAnalyticsEnabled()) {
            throw new APIManagementException("Analytics Not Enabled");
        }
    }

    public static AlertConfigManager getInstance() throws APIManagementException {
        if (alertConfigManager == null) {
             alertConfigManager = new AlertConfigManager();
        }
        return alertConfigManager;
    }

    /**
     * Method to get the alert configuration impl class based on the agent.
     * */
    public AlertConfigurator getAlertConfigurator(String agent) {
        switch (agent) {
        case "subscriber":
            return new StoreAlertConfigurator();
        case "publisher":
            return new PublisherAlertConfigurator();
        default:
            return new AdminAlertConfigurator();
        }
    }
}
