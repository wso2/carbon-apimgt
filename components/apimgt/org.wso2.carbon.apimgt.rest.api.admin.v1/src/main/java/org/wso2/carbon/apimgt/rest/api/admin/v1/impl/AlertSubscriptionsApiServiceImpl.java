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
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertMgtConstants;
import org.wso2.carbon.apimgt.impl.alertmgt.exception.AlertManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.AlertSubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl.AlertSubscriptionCommonImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertTypeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertsSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionAlertSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionAlertSubscriptionListDTO;

import javax.ws.rs.core.Response;
import java.util.List;

public class AlertSubscriptionsApiServiceImpl implements AlertSubscriptionsApiService {

    /**
     * Retrieves all the admin alert subscriptions of the logged in user
     *
     * @param messageContext
     * @return
     */
    public Response getSubscribedAlertTypes(MessageContext messageContext) throws APIManagementException {
        try {
            AlertsSubscriptionDTO alertsSubscriptionDTO = AlertSubscriptionCommonImpl.getSubscribedAlertTypes();
            return Response.status(Response.Status.OK).entity(alertsSubscriptionDTO).build();
        } catch (AlertManagementException e) {
            return Response.status(Response.Status.NO_CONTENT).entity("API Manager analytics is not enabled").build();
        }
    }

    /**
     * Subscribes the logged in user for requested admin alert types
     *
     * @param body
     * @param messageContext
     * @return
     */
    @Override
    public Response subscribeToAlerts(AlertsSubscriptionDTO body, MessageContext messageContext)
            throws APIManagementException {

        //Validate for empty list of emails
        List<String> emailsList = body.getEmailList();
        if (emailsList == null || emailsList.isEmpty()) {
            throw new APIManagementException("Email list cannot be empty", ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        //Validate for empty list of alerts
        List<AlertTypeDTO> subscribingAlertDTOs = body.getAlerts();
        if (subscribingAlertDTOs == null || subscribingAlertDTOs.isEmpty()) {
            throw new APIManagementException("Alert list should not be empty", ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        try {
            AlertsSubscriptionDTO subscribedAlerts = AlertSubscriptionCommonImpl
                    .subscribeToAlerts(subscribingAlertDTOs, emailsList);
            return Response.status(Response.Status.OK).entity(subscribedAlerts).build();
        } catch (AlertManagementException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("API Manager analytics is not Enabled").build();
        }
    }

    /**
     * Unsubscribe the user from all the admin alert types
     *
     * @param messageContext
     * @return
     */
    @Override
    public Response unsubscribeAllAlerts(MessageContext messageContext) throws APIManagementException {

        try {
            AlertSubscriptionCommonImpl.unsubscribeAllAlerts();
        } catch (AlertManagementException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Analytics not Enabled").build();
        }
        return Response.status(Response.Status.OK).build();
    }

    /**
     * Retrieve a list of bot detection alert subscriptions
     *
     * @param messageContext CXF Message Context
     * @return list of bot detection alert subscriptions
     * @throws APIManagementException if an error occurs when retrieving bot detection alert subscriptions
     */
    @Override
    public Response getBotDetectionAlertSubscriptions(MessageContext messageContext) throws APIManagementException {
        BotDetectionAlertSubscriptionListDTO listDTO = AlertSubscriptionCommonImpl.getBotDetectionAlertSubscriptions();
        return Response.ok().entity(listDTO).build();
    }

    /**
     * Subscribe for bot detection alerts
     *
     * @param body           email to be registered for the subscription
     * @param messageContext CXF Message Context
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
        BotDetectionAlertSubscriptionDTO alertSubscriptionDTO = AlertSubscriptionCommonImpl
                .subscribeForBotDetectionAlerts(email);
        return Response.ok(alertSubscriptionDTO).build();
    }

    /**
     * Unsubscribe from bot detection alerts
     *
     * @param uuid           uuid of the subscription
     * @param messageContext CXF Message Context
     * @return 200 OK response if the subscription is deleted successfully
     * @throws APIManagementException if an error occurs when un-subscribing from bot detection alerts
     */
    @Override
    public Response unsubscribeFromBotDetectionAlerts(String uuid, MessageContext messageContext)
            throws APIManagementException {

        AlertSubscriptionCommonImpl.unsubscribeFromBotDetectionAlerts(uuid);
        return Response.ok().build();
    }
}
