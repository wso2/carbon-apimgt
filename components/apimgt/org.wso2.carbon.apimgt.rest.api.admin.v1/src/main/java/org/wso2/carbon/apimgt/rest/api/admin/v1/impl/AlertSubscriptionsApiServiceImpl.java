/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.botDataAPI.BotDetectionData;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.alertmgt.AdminAlertConfigurator;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertConfigManager;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertMgtConstants;
import org.wso2.carbon.apimgt.impl.alertmgt.exception.AlertManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.AlertSubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertTypeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertsSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionAlertSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionAlertSubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.AlertsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.BotDetectionMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AlertSubscriptionsApiServiceImpl implements AlertSubscriptionsApiService {

    private static final Log log = LogFactory.getLog(AlertSubscriptionsApiServiceImpl.class);

    /**
     * Retrieves all the admin alert subscriptions of the logged in user
     *
     * @param messageContext
     * @return
     */
    public Response getSubscribedAlertTypes(MessageContext messageContext) {

        String fullyQualifiedUsername = getFullyQualifiedUsername(RestApiUtil.getLoggedInUsername());
        try {
            AdminAlertConfigurator adminAlertConfigurator = (AdminAlertConfigurator) AlertConfigManager.getInstance()
                    .getAlertConfigurator(AlertMgtConstants.ADMIN_DASHBOARD_AGENT);
            List<Integer> subscribedAlertTypes = adminAlertConfigurator.getSubscribedAlerts(fullyQualifiedUsername);
            List<org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO> supportedAlertTypeDTOS = adminAlertConfigurator
                    .getSupportedAlertTypes();

            //Filter out the subscribed alerts
            List<org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO> subscribedAlertsList = supportedAlertTypeDTOS.stream()
                    .filter(supportedAlertTypes -> subscribedAlertTypes.stream()
                            .anyMatch(subscribedAlerts -> supportedAlertTypes.getId().equals(subscribedAlerts)))
                    .collect(Collectors.toList());

            //Retrieve the list of subscribed emails
            List<String> subscribedEmails = adminAlertConfigurator.getSubscribedEmailAddresses(fullyQualifiedUsername);
            AlertsSubscriptionDTO alertsSubscriptionDTO = new AlertsSubscriptionDTO();
            alertsSubscriptionDTO
                    .setAlerts(AlertsMappingUtil.fromAlertTypesListToAlertTypeDTOList(subscribedAlertsList));
            alertsSubscriptionDTO.setEmailList(subscribedEmails);
            return Response.status(Response.Status.OK).entity(alertsSubscriptionDTO).build();
        } catch (AlertManagementException e) {
            return Response.status(Response.Status.NO_CONTENT).entity("API Manager analytics is not enabled").build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Internal Error occurred", e, log);
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    /**
     * Subscribes the logged in user for requested admin alert types
     *
     * @param body
     * @param messageContext
     * @return
     */
    @Override public Response subscribeToAlerts(AlertsSubscriptionDTO body, MessageContext messageContext) {

        //Validate for empty list of emails
        List<String> emailsList = body.getEmailList();
        if (emailsList == null || emailsList.size() == 0) {
            RestApiUtil.handleBadRequest("Email list cannot be empty", log);
        }
        //Validate for empty list of alerts
        List<AlertTypeDTO> subscribingAlertDTOs = body.getAlerts();
        if (subscribingAlertDTOs == null || subscribingAlertDTOs.size() == 0) {
            RestApiUtil.handleBadRequest("Alert list should not be empty", log);
        }

        String fullyQualifiedUsername = getFullyQualifiedUsername(RestApiUtil.getLoggedInUsername());
        try {

            AdminAlertConfigurator adminAlertConfigurator = (AdminAlertConfigurator) AlertConfigManager.getInstance()
                    .getAlertConfigurator(AlertMgtConstants.ADMIN_DASHBOARD_AGENT);
            //Retrieve the supported alert types
            List<org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO> supportedAlertTypes = adminAlertConfigurator
                    .getSupportedAlertTypes();
            Map<String, org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO> supportedAlertTypesMap = supportedAlertTypes
                    .stream().collect(Collectors
                            .toMap(org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO::getName, alertType -> alertType));
            List<org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO> alertTypesToSubscribe = new ArrayList<>();

            //Validate the request alerts against supported alert types
            for (AlertTypeDTO subscribingAlertDTO : subscribingAlertDTOs) {
                if (supportedAlertTypesMap.containsKey(subscribingAlertDTO.getName())) {
                    alertTypesToSubscribe.add(supportedAlertTypesMap.get(subscribingAlertDTO.getName()));
                } else {
                    RestApiUtil.handleBadRequest(
                            "Unsupported alert type : " + subscribingAlertDTO.getName() + " is provided.", log);
                    return null;
                }
            }
            adminAlertConfigurator.subscribe(fullyQualifiedUsername, emailsList, alertTypesToSubscribe);
            return Response.status(Response.Status.OK).build();
        } catch (AlertManagementException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("API Manager analytics is not Enabled").build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while subscribing to alert types", e, log);
        }
        return null;
    }

    /**
     * Unsubscribe the user from all the admin alert types
     *
     * @param messageContext
     * @return
     */
    @Override public Response unsubscribeAllAlerts(MessageContext messageContext) {

        String fullyQualifiedUsername = getFullyQualifiedUsername(RestApiUtil.getLoggedInUsername());
        try {
            AdminAlertConfigurator adminAlertConfigurator = (AdminAlertConfigurator) AlertConfigManager.getInstance()
                    .getAlertConfigurator(AlertMgtConstants.ADMIN_DASHBOARD_AGENT);
            adminAlertConfigurator.unsubscribe(fullyQualifiedUsername);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Internal Server Error occurred while un subscribing from alerts", e,
                    log);
        } catch (AlertManagementException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Analytics not Enabled").build();
        }
        return Response.status(Response.Status.OK).build();
    }

    /**
     * Retrieve a list of bot detection alert subscriptions
     *
     * @param messageContext
     * @return list of bot detection alert subscriptions
     * @throws APIManagementException if an error occurs when retrieving bot detection alert subscriptions
     */
    @Override
    public Response getBotDetectionAlertSubscriptions(MessageContext messageContext) throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        List<BotDetectionData> botDetectionDataList = apiAdmin.getBotDetectionAlertSubscriptions();
        BotDetectionAlertSubscriptionListDTO listDTO =
                BotDetectionMappingUtil.fromAlertSubscriptionListToListDTO(botDetectionDataList);
        return Response.ok().entity(listDTO).build();
    }

    /**
     * Subscribe for bot detection alerts
     *
     * @param body           email to be registered for the subscription
     * @param messageContext
     * @return alert subscription DTO containing the uuid of the subscription and the registered email
     * @throws APIManagementException if an error occurs when subscribing for bot detection alerts
     */
    @Override
    public Response subscribeForBotDetectionAlerts(BotDetectionAlertSubscriptionDTO body, MessageContext messageContext)
            throws APIManagementException {

        String email = body.getEmail();
        if (StringUtils.isBlank(email)) {
            String propertyName = AlertMgtConstants.BOT_DETECTION_EMAIL_FIELD;
            throw new APIManagementException(propertyName + " property value of payload cannot be blank",
                    ExceptionCodes.from(ExceptionCodes.BLANK_PROPERTY_VALUE, propertyName));
        }
        APIAdmin apiAdmin = new APIAdminImpl();
        BotDetectionData alertSubscription =
                apiAdmin.getBotDetectionAlertSubscription(AlertMgtConstants.BOT_DETECTION_EMAIL_FIELD, email);
        if (alertSubscription != null) {
            RestApiUtil.handleResourceAlreadyExistsError(
                    "Email: " + email + " has already been subscribed for bot detection alerts", log);
        }
        apiAdmin.addBotDetectionAlertSubscription(email);
        BotDetectionData newAlertSubscription =
                apiAdmin.getBotDetectionAlertSubscription(AlertMgtConstants.BOT_DETECTION_EMAIL_FIELD, email);
        BotDetectionAlertSubscriptionDTO alertSubscriptionDTO =
                BotDetectionMappingUtil.fromAlertSubscriptionToDTO(newAlertSubscription);
        return Response.ok(alertSubscriptionDTO).build();
    }

    /**
     * Unsubscribe from bot detection alerts
     *
     * @param uuid           uuid of the subscription
     * @param messageContext
     * @return 200 OK response if the subscription is deleted successfully
     * @throws APIManagementException if an error occurs when un-subscribing from bot detection alerts
     */
    @Override
    public Response unsubscribeFromBotDetectionAlerts(String uuid, MessageContext messageContext)
            throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        BotDetectionData alertSubscription = apiAdmin.getBotDetectionAlertSubscription("uuid", uuid);
        if (alertSubscription == null) {
            RestApiUtil.handleResourceNotFoundError(
                    "Bot detection alert subscription with uuid: " + uuid + " does not exist.", log);
        }
        apiAdmin.deleteBotDetectionAlertSubscription(uuid);
        return Response.ok().build();
    }

    /**
     *
     * Obtain the fully qualified username of the given user
     * @param username  tenant aware username
     * @return
     */
    private String getFullyQualifiedUsername(String username) {
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(RestApiUtil.getLoggedInUserTenantDomain())) {
            return username + "@" + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        return username;
    }
}
