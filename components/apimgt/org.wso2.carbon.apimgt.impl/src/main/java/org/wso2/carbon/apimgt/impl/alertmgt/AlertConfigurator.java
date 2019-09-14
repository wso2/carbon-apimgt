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
import org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO;

import java.util.List;
import java.util.Properties;

/**
 * This interface defines the base methods for alert configuration.
 * This interface should be implemented by the agents which the alerts can be subscribed.
 * Agents: publisher, subscriber, admin
 * */
public interface AlertConfigurator {

    /**
     * Method to get the supported alert types. Based on the implementation of the agent.
     *
     * @return A list of support alert types.
     * */
    public List<AlertTypeDTO> getSupportedAlertTypes() throws APIManagementException;

    /**
     * Get the list of subscribed alert types by a user through an agent.
     *
     * @param userName : The username of the user who has subscribed.
     * @return A list of alertTypes subscribed by the user.
     * */
    public List<AlertTypeDTO> getSubscribedAlerts(String userName) throws APIManagementException;

    /**
     * Method to add subscription to the provided alert types.
     *
     * @param userName : The username of the user, who is subscribing.
     * @param emailsList : The list of emails which needs to be subscribed.
     * @param alertTypeDTOList: The list of Alert types which needs to be subscribed.
     * */
    public void subscribe(String userName, List<String> emailsList, List<AlertTypeDTO> alertTypeDTOList);

    /**
     * Unsubscribe a particular user from all the alert types.
     *
     * @param userName : The name of the user who needs to be unsubscribed.
     * */
    public void unsubscribe(String userName);

    /**
     * Add configuration to the provided Alert type.
     *
     * @param userName: The name of the user
     * @param alertName: The name of the alert type.
     * @param configProperties: The configuration properties list.
     * */
    public void addAlertConfiguration(String userName, String alertName, List<Properties> configProperties);

    /**
     * Get the existing configurations of the provided alert type.
     *
     * @param userName: The name of the user.
     * @param alertName: The alert type name.
     * @return The list of configuration properties of the alert.
     * */
    public List<Properties> getAlertConfiguration(String userName, String alertName);

    /**
     * Remove provided configuration from the alert type.
     *
     * @param userName: The name of the user.
     * @param alertName: The alert type name
     * @param configProperties: The properties that should be removed from the alert config.
     * */
    public void removeAlertConfiguration(String userName, String alertName, List<Properties> configProperties);

}
