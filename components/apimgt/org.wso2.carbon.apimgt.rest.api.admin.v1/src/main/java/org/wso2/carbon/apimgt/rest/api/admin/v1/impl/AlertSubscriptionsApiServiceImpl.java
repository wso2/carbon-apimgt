package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.alertmgt.AdminAlertConfigurator;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertConfigManager;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertMgtConstants;
import org.wso2.carbon.apimgt.impl.alertmgt.exception.AlertManagementException;
import org.wso2.carbon.apimgt.impl.dto.AlertInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.AlertSubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertTypeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertsSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.AlertsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AlertSubscriptionsApiServiceImpl implements AlertSubscriptionsApiService {

    private static final Log log = LogFactory.getLog(AlertSubscriptionsApiServiceImpl.class);

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

    @Override public Response subscribeToAlerts(AlertsSubscriptionDTO body, MessageContext messageContext)
            throws APIManagementException {

        //Validate for empty list of emails
        List<String> emailsList = body.getEmailList();
        if (emailsList == null || emailsList.size() == 0) {
            RestApiUtil.handleBadRequest("Email list cannot be empty", log);
        }
        //Validate for empty list of alerts
        List<AlertTypeDTO> alertDTOs = body.getAlerts();
        if (alertDTOs == null || alertDTOs.size() == 0) {
            RestApiUtil.handleBadRequest("Alert list should not be empty", log);
        }

        String fullyQualifiedUsername = getFullyQualifiedUsername(RestApiUtil.getLoggedInUsername());
        try {
            AdminAlertConfigurator adminAlertConfigurator = (AdminAlertConfigurator) AlertConfigManager.getInstance()
                    .getAlertConfigurator(AlertMgtConstants.ADMIN_DASHBOARD_AGENT);
            List<org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO> alertTypesToSubscribe= AlertsMappingUtil.fromAlertTypeDTOListToAlertTypeList(alertDTOs);
            adminAlertConfigurator.subscribe(fullyQualifiedUsername, emailsList, alertTypesToSubscribe);
            return Response.status(Response.Status.CREATED).entity(null).build();
        } catch (AlertManagementException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("API Manager analytics is not Enabled").build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while subscribing to alert types", e, log);
        }
        return null;
    }

    private String getFullyQualifiedUsername(String username) {
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(RestApiUtil.getLoggedInUserTenantDomain())) {
            return username + "@" + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        return username;
    }
}
