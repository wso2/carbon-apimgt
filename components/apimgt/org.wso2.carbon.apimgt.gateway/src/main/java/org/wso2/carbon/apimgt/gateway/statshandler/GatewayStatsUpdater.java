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

package org.wso2.carbon.apimgt.gateway.statshandler;

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.internal.APIManagerComponent;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.StatClusterMessageUtil;
import org.wso2.carbon.bam.service.data.publisher.conf.EventingConfigData;
import org.wso2.carbon.bam.service.data.publisher.services.ServiceDataPublisherAdmin;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.AbstractAdmin;

/**
 * This class contains the methods to update stat publishing status in gateway and send cluster message to other
 * nodes in the same domain
 */
public class GatewayStatsUpdater extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(GatewayStatsUpdater.class);

    /**
     * This method updates the stat publishing status in gateway domain
     *
     * @param receiverUrl      event receiver url
     * @param user             username for the event receiver
     * @param password         password for the event receiver
     * @param statUpdateStatus status of the stat publishing state
     */
    public void updateStatPublishGateway(String receiverUrl, String user, String password, Boolean statUpdateStatus) {

        log.debug("Updating stats publishing status in Gateway.");

        //updating stat publishing at the receiver node, self update
        APIManagerAnalyticsConfiguration analyticsConfiguration = APIManagerAnalyticsConfiguration.getInstance();
        analyticsConfiguration.setAnalyticsEnabled(statUpdateStatus);
        log.debug("Updated stats publishing status in Gateway to : " + statUpdateStatus);

        ServiceDataPublisherAdmin serviceDataPublisherAdmin = APIManagerComponent.getDataPublisherAdminService();
        EventingConfigData eventingConfigData = null;
        if (serviceDataPublisherAdmin != null) {
            eventingConfigData = serviceDataPublisherAdmin.getEventingConfigData();
        }

        try {
            if (eventingConfigData != null) {
                //config values are updated if the stats publishing is true
                if (statUpdateStatus) {
                    log.debug("Updating values related to stats publishing status.");
                    //values related to stats publishing status are only updated if all of them are non-empty
                    if (!(receiverUrl.isEmpty()) && !(user.isEmpty()) && !(password.isEmpty())) {
                        analyticsConfiguration.setBamServerUrlGroups(receiverUrl);
                        analyticsConfiguration.setBamServerUser(user);
                        analyticsConfiguration.setBamServerPassword(password);

                        eventingConfigData.setUrl(receiverUrl);
                        eventingConfigData.setUserName(user);
                        eventingConfigData.setPassword(password);
                        log.debug("BAMServerURL : " + receiverUrl + " , BAMServerUserName : " + user + " , " +
                                "BAMServerPassword : " + password);
                        APIUtil.addBamServerProfile
                                (receiverUrl, user, password, MultitenantConstants.SUPER_TENANT_ID);
                    }
                }
                //eventingConfigData must be updated irrespective of the value of statUpdateStatus
                eventingConfigData.setServiceStatsEnable(statUpdateStatus);

                //this may throw an Exception
                serviceDataPublisherAdmin.configureEventing(eventingConfigData);
            }
            //send the cluster message to other nodes in the cluster to update stats publishing status
            ClusteringAgent clusteringAgent = this.getConfigContext().getAxisConfiguration().getClusteringAgent();
            if (clusteringAgent != null) {
                log.debug("Sending cluster message to Gateway domain to update stats publishing status.");
                clusteringAgent.sendMessage(new StatClusterMessageUtil(statUpdateStatus), true);
            }
        } catch (ClusteringFault clusteringFault) {
            //error is only logged because initial gateway has modified the status
            log.error("Failed to send cluster message to Gateway domain and update stats publishing status.");
        } catch (APIManagementException e) {
            log.error("Error occurred while adding BAMServerProfile");
        } catch (Exception e) {
            log.error("Error occurred while updating EventingConfiguration");
        }
    }
}