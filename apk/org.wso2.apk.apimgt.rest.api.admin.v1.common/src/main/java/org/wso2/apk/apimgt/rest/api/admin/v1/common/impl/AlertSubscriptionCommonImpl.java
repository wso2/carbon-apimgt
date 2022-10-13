/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.rest.api.admin.v1.common.impl;

import org.wso2.apk.apimgt.api.APIAdmin;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.botDataAPI.BotDetectionData;
import org.wso2.apk.apimgt.impl.APIAdminImpl;
import org.wso2.apk.apimgt.impl.alertmgt.AdminAlertConfigurator;
import org.wso2.apk.apimgt.impl.alertmgt.AlertConfigManager;
import org.wso2.apk.apimgt.impl.alertmgt.AlertMgtConstants;
import org.wso2.apk.apimgt.impl.alertmgt.exception.AlertManagementException;
import org.wso2.apk.apimgt.rest.api.admin.v1.common.utils.mappings.AlertsMappingUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.common.utils.mappings.BotDetectionMappingUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.AlertTypeDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.AlertsSubscriptionDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.BotDetectionAlertSubscriptionDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.BotDetectionAlertSubscriptionListDTO;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AlertSubscriptionCommonImpl {

    private AlertSubscriptionCommonImpl() {
    }

    /**
     * Retrieves all the admin alert subscriptions of the logged-in user
     *
     * @return Alert subscription details
     * @throws APIManagementException   When an internal error occurs
     * @throws AlertManagementException When an alert related error occurs.
     */
    public static AlertsSubscriptionDTO getSubscribedAlertTypes() throws APIManagementException,
            AlertManagementException {
        String fullyQualifiedUsername = RestApiCommonUtil.getLoggedInUsername();

        AdminAlertConfigurator adminAlertConfigurator = (AdminAlertConfigurator) AlertConfigManager.getInstance()
                .getAlertConfigurator(AlertMgtConstants.ADMIN_DASHBOARD_AGENT);
        List<Integer> subscribedAlertTypes = adminAlertConfigurator.getSubscribedAlerts(fullyQualifiedUsername);
        List<org.wso2.apk.apimgt.impl.dto.AlertTypeDTO> supportedAlertTypeDTOS = adminAlertConfigurator
                .getSupportedAlertTypes();

        //Filter out the subscribed alerts
        List<org.wso2.apk.apimgt.impl.dto.AlertTypeDTO> subscribedAlertsList = supportedAlertTypeDTOS.stream()
                .filter(supportedAlertTypes -> subscribedAlertTypes.stream()
                        .anyMatch(subscribedAlerts -> supportedAlertTypes.getId().equals(subscribedAlerts)))
                .collect(Collectors.toList());

        //Retrieve the list of subscribed emails
        List<String> subscribedEmails = adminAlertConfigurator.getSubscribedEmailAddresses(fullyQualifiedUsername);
        AlertsSubscriptionDTO alertsSubscriptionDTO = new AlertsSubscriptionDTO();
        alertsSubscriptionDTO
                .setAlerts(AlertsMappingUtil.fromAlertTypesListToAlertTypeDTOList(subscribedAlertsList));
        alertsSubscriptionDTO.setEmailList(subscribedEmails);
        return alertsSubscriptionDTO;
    }

    /**
     * Subscribes the logged-in user for requested admin alert types
     *
     * @param subscribingAlertDTOs Subscription request details
     * @param emailsList           List of emails
     * @return Alert subscription details
     * @throws APIManagementException   When an internal error occurs
     * @throws AlertManagementException When an alert related error occurs.
     */
    public static AlertsSubscriptionDTO subscribeToAlerts(List<AlertTypeDTO> subscribingAlertDTOs,
                                                          List<String> emailsList)
            throws APIManagementException, AlertManagementException {
        String fullyQualifiedUsername = RestApiCommonUtil.getLoggedInUsername();

        AdminAlertConfigurator adminAlertConfigurator = (AdminAlertConfigurator) AlertConfigManager.getInstance()
                .getAlertConfigurator(AlertMgtConstants.ADMIN_DASHBOARD_AGENT);
        //Retrieve the supported alert types
        List<org.wso2.apk.apimgt.impl.dto.AlertTypeDTO> supportedAlertTypes = adminAlertConfigurator
                .getSupportedAlertTypes();
        Map<String, org.wso2.apk.apimgt.impl.dto.AlertTypeDTO> supportedAlertTypesMap = supportedAlertTypes
                .stream().collect(Collectors
                        .toMap(org.wso2.apk.apimgt.impl.dto.AlertTypeDTO::getName, alertType -> alertType));
        List<org.wso2.apk.apimgt.impl.dto.AlertTypeDTO> alertTypesToSubscribe = new ArrayList<>();

        //Validate the request alerts against supported alert types
        for (AlertTypeDTO subscribingAlertDTO : subscribingAlertDTOs) {
            if (supportedAlertTypesMap.containsKey(subscribingAlertDTO.getName())) {
                alertTypesToSubscribe.add(supportedAlertTypesMap.get(subscribingAlertDTO.getName()));
            } else {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.UNSUPPORTED_ALERT_TYPE,
                        subscribingAlertDTO.getName()));
            }
        }
        adminAlertConfigurator.subscribe(fullyQualifiedUsername, emailsList, alertTypesToSubscribe);
        AlertsSubscriptionDTO subscribedAlerts = new AlertsSubscriptionDTO();
        subscribedAlerts.setAlerts(AlertsMappingUtil.fromAlertTypesListToAlertTypeDTOList(alertTypesToSubscribe));
        subscribedAlerts.setEmailList(emailsList);
        return subscribedAlerts;
    }

    /**
     * Unsubscribe the user from all the admin alert types
     *
     * @throws APIManagementException   When an internal error occurs
     * @throws AlertManagementException When an alert related error occurs.
     */
    public static void unsubscribeAllAlerts() throws APIManagementException, AlertManagementException {
        String fullyQualifiedUsername = RestApiCommonUtil.getLoggedInUsername();
        AdminAlertConfigurator adminAlertConfigurator = (AdminAlertConfigurator) AlertConfigManager.getInstance()
                .getAlertConfigurator(AlertMgtConstants.ADMIN_DASHBOARD_AGENT);
        adminAlertConfigurator.unsubscribe(fullyQualifiedUsername);
    }

    /**
     * Retrieve a list of bot detection alert subscriptions
     *
     * @return Bot detection subscription details list
     * @throws APIManagementException When an internal error occurs
     */
    public static BotDetectionAlertSubscriptionListDTO getBotDetectionAlertSubscriptions()
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        List<BotDetectionData> botDetectionDataList = apiAdmin.getBotDetectionAlertSubscriptions();
        return BotDetectionMappingUtil.fromAlertSubscriptionListToListDTO(botDetectionDataList);
    }

    /**
     * Subscribe for bot detection alerts
     *
     * @param email Email to subscribe to alerts
     * @return Bot detection subscription details
     * @throws APIManagementException When an internal error occurs
     */
    public static BotDetectionAlertSubscriptionDTO subscribeForBotDetectionAlerts(String email)
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        BotDetectionData alertSubscription =
                apiAdmin.getBotDetectionAlertSubscription(AlertMgtConstants.BOT_DETECTION_EMAIL_FIELD, email);
        if (alertSubscription != null) {
            throw new APIManagementException(ExceptionCodes
                    .from(ExceptionCodes.ALREADY_SUBSCRIBED_FOR_BOT_ALERTS, email));
        }
        apiAdmin.addBotDetectionAlertSubscription(email);
        BotDetectionData newAlertSubscription =
                apiAdmin.getBotDetectionAlertSubscription(AlertMgtConstants.BOT_DETECTION_EMAIL_FIELD, email);
        return BotDetectionMappingUtil.fromAlertSubscriptionToDTO(newAlertSubscription);
    }

    /**
     * Unsubscribe from bot detection alerts
     *
     * @param uuid Subscription UUID
     * @throws APIManagementException When an internal error occurs
     */
    public static void unsubscribeFromBotDetectionAlerts(String uuid) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        BotDetectionData alertSubscription = apiAdmin.getBotDetectionAlertSubscription("uuid", uuid);
        if (alertSubscription == null) {
            throw new APIManagementException(ExceptionCodes
                    .from(ExceptionCodes.BOT_DETECTION_SUBSCRIPTION_NOT_FOUND, uuid));
        }
        apiAdmin.deleteBotDetectionAlertSubscription(uuid);
    }

}
