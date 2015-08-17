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

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

/**
 * This class contains the functions to update stat publishing in Store-Publisher domain
 */
public class StatUpdateStorePublisherDomain {

    private static final Log log = LogFactory.getLog(StatUpdateStorePublisherDomain.class);

    /**
     * This method updates the stat publishing status in Store-Publisher domain by using a cluster message
     *
     * @param statUpdateStatus status of the stat publishing state
     */
    public void updateStatsPublishStore(boolean statUpdateStatus) {

        log.debug("Started updating Stats publishing status to : " + statUpdateStatus);

        ClusteringAgent clusteringAgent = ServiceReferenceHolder.getContextService().getServerConfigContext().
                                            getAxisConfiguration().getClusteringAgent();

        if (clusteringAgent != null) {
            //changing stat publishing status at other nodes via a cluster message
            try {
                clusteringAgent.sendMessage(new StatUpdateClusterMessage(statUpdateStatus), true);
            } catch (ClusteringFault clusteringFault) {
                //error is only logged because initially gateway has modified the status
                log.error("Failed to send cluster message to Publisher/Store domain " +
                        "and update stats publishing status.");
            }
            log.debug("Successfully updated Stats publishing status to : " + statUpdateStatus);
        }
    }
}
