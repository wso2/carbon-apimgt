package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.alertmgt.AdminAlertConfigurator;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertConfigManager;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertMgtConstants;
import org.wso2.carbon.apimgt.impl.alertmgt.exception.AlertManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.AlertSubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertTypeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertsSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.AlertsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

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
