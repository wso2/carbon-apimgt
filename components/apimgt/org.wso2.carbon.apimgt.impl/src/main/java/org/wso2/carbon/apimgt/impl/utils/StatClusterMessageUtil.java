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

import java.io.Serializable;

/**
 * This class provides the definition of the cluster message which is initiated from the
 * web service call from publisher node
 */
public class StatClusterMessageUtil extends ClusteringMessage implements Serializable  {

    private static final Log log = LogFactory.getLog(StatClusterMessageUtil.class);
    private Boolean statUpdateStatus;

    public StatClusterMessageUtil(Boolean statUpdateStatus) {
        this.statUpdateStatus = statUpdateStatus;
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
        log.debug("Updated Stat publishing status to : " + statUpdateStatus);

    }
}
