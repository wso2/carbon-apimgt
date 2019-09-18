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

import org.wso2.carbon.apimgt.impl.alertmgt.exception.AlertManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

/**
 * Alert Configuration Manager class. This class is used to get the required implementation of AlertConfigurator.
 * */
public class AlertConfigManager {

    private static AlertConfigManager alertConfigManager = null;

    private AlertConfigManager() throws AlertManagementException {
        if (!APIUtil.isAnalyticsEnabled()) {
            throw new AlertManagementException("Analytics Not Enabled");
        }
    }

    public static AlertConfigManager getInstance() throws AlertManagementException {
        if (alertConfigManager == null) {
             alertConfigManager = new AlertConfigManager();
        }
        return alertConfigManager;
    }

    /**
     * Method to get the alert configuration impl class based on the agent.
     *
     * @param agent : The alert subscribing agent. (subscriber/ publisher/ admin-dashboard)
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
