/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;

/**
 * This class provides the definition of the cluster message which is initiated from the
 * web service call from publisher node
 */
public class StatUpdateClusterMessage extends ClusteringMessage {

    private static final Log log = LogFactory.getLog(StatUpdateClusterMessage.class);
    private Boolean statUpdateStatus;
    private String receiverUrl;
    private String user;
    private String password;

    public StatUpdateClusterMessage(Boolean statUpdateStatus, String receiverUrl, String user, String password) {
        this.statUpdateStatus = statUpdateStatus;
        this.receiverUrl = receiverUrl;
        this.user = user;
        this.password = password;
    }

    @Override
    public ClusteringCommand getResponse() {
        return null;
    }

    @Override
    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {

        //update the service variable, a boolean variable representing the stat data publishing in the node
        APIManagerAnalyticsConfiguration instanceOfAPIAnalytics = APIManagerAnalyticsConfiguration.getInstance();
        instanceOfAPIAnalytics.setAnalyticsEnabled(statUpdateStatus);

        // Only change Data publishing information only if they are set
        if (receiverUrl != null && !receiverUrl.isEmpty() &&
            user != null && !user.isEmpty() &&
            password != null && !password.isEmpty()) {
            instanceOfAPIAnalytics.setDasReceiverUrlGroups(receiverUrl);
            instanceOfAPIAnalytics.setDasReceiverServerUser(user);
            instanceOfAPIAnalytics.setDasReceiverServerPassword(password);
        }

        if (log.isDebugEnabled()) {
            log.debug("Updated Stat publishing status to : " + statUpdateStatus);
        }

    }
}
