package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertConfigManager;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertConfigurator;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertMgtConstants;
import org.wso2.carbon.apimgt.impl.alertmgt.exception.AlertManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.AlertSubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertsInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.AlertsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

public class AlertSubscriptionsApiServiceImpl implements AlertSubscriptionsApiService {

    private static final String AGENT = AlertMgtConstants.ADMIN_DASHBOARD_AGENT;
    private static final Log log = LogFactory.getLog(AlertTypesApiServiceImpl.class);

    public Response getSubscribedAlertTypes(MessageContext messageContext) {

        String tenantAwareUserName = getFullyQualifiedUsername(RestApiUtil.getLoggedInUsername());

        try {
            AlertConfigurator adminAlertConfigurator = AlertConfigManager.getInstance().getAlertConfigurator(AGENT);
            List<Integer> subscribedAlertTypes = adminAlertConfigurator.getSubscribedAlerts(tenantAwareUserName, AGENT);
            List<org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO> supportedAlertTypeDTOS = adminAlertConfigurator
                    .getSupportedAlertTypes(AGENT);

            //Filter out the subscribed alerts
            List<org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO> subscribedAlertsList = supportedAlertTypeDTOS.stream()
                    .filter(supportedAlertTypes -> subscribedAlertTypes.stream()
                            .anyMatch(subscribedAlerts -> supportedAlertTypes.getId().equals(subscribedAlerts)))
                    .collect(Collectors.toList());
            //Retrieve the list of subscribed emails
            List<String> subscribedEmails = adminAlertConfigurator
                    .getSubscribedEmailAddresses(tenantAwareUserName, AGENT);
            AlertsInfoDTO alertsInfoDTO = new AlertsInfoDTO();
            alertsInfoDTO.setAlerts(AlertsMappingUtil.fromAlertTypesListToAlertTypeDTOList(subscribedAlertsList));
            alertsInfoDTO.setEmailList(subscribedEmails);
            return Response.status(Response.Status.OK).entity(alertsInfoDTO).build();
        } catch (AlertManagementException e) {
            return Response.status(Response.Status.NO_CONTENT).entity("APIM analytics is not enabled").build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Internal Error occurred", e, log);
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private String getFullyQualifiedUsername(String username) {
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(RestApiUtil.getLoggedInUserTenantDomain())) {
            return username + "@" + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        return username;
    }
}
