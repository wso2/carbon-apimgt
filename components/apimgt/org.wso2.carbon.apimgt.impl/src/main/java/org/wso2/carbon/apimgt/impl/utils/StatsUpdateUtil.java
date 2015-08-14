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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.statsupdate.stub.GatewayStatsUpdateServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * This class is used to initiate the stats publishing status in a distributed environment
 */
public class StatsUpdateUtil {

    private static final Log log = LogFactory.getLog(StatsUpdateUtil.class);
    public static StatsUpdateUtil instance = new StatsUpdateUtil();
    private Map<String, Environment> gatewyEnvironments;

    public static synchronized StatsUpdateUtil getInstance() {
        if (instance == null) {
            instance = new StatsUpdateUtil();
        }
        return instance;
    }

    /**
     * This method is used to initiate the web service calls and cluster messages related to stats publishing status
     *
     * @param receiverUrl   event receiver url
     * @param user          username of the event receiver
     * @param password      password of the event receiver
     * @param updatedStatus status of the stat publishing state
     * @throws APIManagementException if an error occurs while trying to update status and send cluster message
     */
    public void callStatupdateService(String receiverUrl, String user, String password, boolean updatedStatus)
            throws APIManagementException {

        try {
            gatewyEnvironments = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                    getAPIManagerConfiguration().getApiGatewayEnvironments();

            for (String gatewayEnvironmentName : gatewyEnvironments.keySet()) {
                Environment currentGatewayEnvironment = gatewyEnvironments.get(gatewayEnvironmentName);
                String gatewayServiceUrl = currentGatewayEnvironment.getServerURL();
                String gatewayUserName = currentGatewayEnvironment.getUserName();
                String gatewayPassword = currentGatewayEnvironment.getPassword();

                //get the stub and the call the admin service with the credentials
                GatewayStatsUpdateServiceStub stub =
                        new GatewayStatsUpdateServiceStub(gatewayServiceUrl + "GatewayStatsUpdateService");
                StatUpdateStorePublisherDomain storePublisherMessageAgent = new StatUpdateStorePublisherDomain();
                ServiceClient gatewayServiceClient = stub._getServiceClient();
                CarbonUtils.setBasicAccessSecurityHeaders(gatewayUserName, gatewayPassword, gatewayServiceClient);

                //send an empty string if at least one mandatory parameter is null
                if (receiverUrl == null || user == null || password == null) {
                    receiverUrl = "";
                    user = "";
                    password = "";
                }
                stub.updateStatPublishGateway(receiverUrl, user, password, updatedStatus);

                //send cluster message to publisher-store domain
                storePublisherMessageAgent.updateStatsPublishStore(updatedStatus);
            }
        } catch (AxisFault axisFault) {
            log.error("Error in accessing Statsupdate web service. " + axisFault);
            throw new APIManagementException("Error in accessing Statsupdate web service.");
        } catch (RemoteException e) {
            log.error("Error in updating Stats publish status in Gataways. " + e);
            throw new APIManagementException("Error in updating Stats publish status in Gataways.");
        }
    }
}