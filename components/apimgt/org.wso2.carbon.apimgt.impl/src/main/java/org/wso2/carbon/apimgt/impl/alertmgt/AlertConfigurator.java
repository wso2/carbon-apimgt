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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.List;
import java.util.Map;

/**
 * This interface defines the base methods for alert configuration.
 * This interface should be implemented by the agents which the alerts can be subscribed.
 * Agents: publisher, subscriber, admin
 * */
public abstract class AlertConfigurator {

    private static final Log log = LogFactory.getLog(AlertConfigurator.class);

    /**
     * Method to get the supported alert types. Based on the implementation of the agent.
     * @param agent : The agent name
     * @return A list of support alert types.
     * */
    public List<AlertTypeDTO> getSupportedAlertTypes(String agent) throws APIManagementException {
        Map<Integer, String> supportedAlertTypes = APIUtil.getAllAlertTypeByStakeHolder(agent);
        if (log.isDebugEnabled()) {
            log.debug("Retrieving supported alert types by " + agent);
        }
        return AlertMgtUtils.toAlertTypeDTO(supportedAlertTypes);
    };

    /**
     * Get the list of subscribed alert types by a user through an agent.
     *
     * @param userName : The username of the user who has subscribed.
     * @param agent : The agent which the user is subscribed. (subscriber/ publisher)
     * @return A list of alertTypes subscribed by the user.
     * */
    public List<Integer> getSubscribedAlerts(String userName, String agent) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving subscribed alerts by the user: " + userName + " of agent: " + agent);
        }
        return APIUtil.getSavedAlertTypesIdsByUserNameAndStakeHolder(userName, agent);
    };

    /**
     * Get the list of subscribed email addresses types by a user through an agent.
     *
     * @param userName : The username of the user who has subscribed.
     * @param agent : The agent which the user is subscribed. (subscriber/ publisher)
     * @return The list of email addresses subscribed by the user.
     * */
    public List<String> getSubscribedEmailAddresses(String userName, String agent) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving subscribed email address list by user: " + userName + " of agent: "
                    + agent);
        }
        return APIUtil.retrieveSavedEmailList(userName, agent);
    };

    /**
     * Method to add subscription to the provided alert types.
     *
     * @param userName : The username of the user, who is subscribing.
     * @param emailsList : The list of emails which needs to be subscribed.
     * @param alertTypeDTOList: The list of Alert types which needs to be subscribed.
     * */
    public abstract void subscribe(String userName, List<String> emailsList, List<AlertTypeDTO> alertTypeDTOList)
            throws APIManagementException;

    /**
     * Unsubscribe a particular user from all the alert types.
     *
     * @param userName : The name of the user who needs to be unsubscribed.
     * */
    public abstract void unsubscribe(String userName) throws APIManagementException;

    /**
     * Add configuration to the provided Alert type.
     *
     * @param userName: The name of the user
     * @param alertName: The name of the alert type.
     * @param configProperties: The configuration properties.
     * */
    public abstract void addAlertConfiguration(String userName, String alertName, Map<String, String> configProperties)
            throws APIManagementException;

    /**
     * Get the existing configurations of the provided alert type.
     *
     * @param userName: The name of the user.
     * @param alertName: The alert type name.
     * @return The list of configuration properties of the alert.
     * */
    public abstract List<Map<String, String>> getAlertConfiguration(String userName, String alertName)
            throws APIManagementException;

    /**
     * Remove provided configuration from the alert type.
     *
     * @param userName: The name of the user.
     * @param alertName: The alert type name
     * @param configProperties: The properties that should be removed from the alert config.
     * */
    public abstract void removeAlertConfiguration(String userName, String alertName, Map<String, String> configProperties)
            throws APIManagementException;
}
