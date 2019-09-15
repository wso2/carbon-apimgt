package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertConfigManager;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertConfigurator;
import org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.AlertsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertConfigDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertConfigListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertsInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.AlertsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AlertsApiServiceImpl implements AlertsApiService {

    private AlertConfigurator storeAlertConfigurator = null;
    private static final Log log = LogFactory.getLog(ApiProductsApiServiceImpl.class);

    @Override
    public Response addAlertConfig(AlertConfigListDTO body, MessageContext messageContext) {
        storeAlertConfigurator = getAlertConfigurator();
        String tenantAwareUserName = getTenantAwareUserName();
        for (AlertConfigDTO alertConfigDTO : body.getConfig()) {
            Properties configurationProperties = new Properties();
            configurationProperties.setProperty("apiName", alertConfigDTO.getApiName());
            configurationProperties.setProperty("apiVersion", alertConfigDTO.getApiVersion());
            configurationProperties.setProperty("applicationId", alertConfigDTO.getApplicationId());
            configurationProperties
                    .setProperty("thresholdRequestCountPerMin", String.valueOf(alertConfigDTO.getRequestCount()));
            try {
                storeAlertConfigurator.addAlertConfiguration(tenantAwareUserName, null, configurationProperties);
            } catch (APIManagementException e) {
                RestApiUtil.handleInternalServerError("Error while adding configuration for alert type", e, log);
            }
        }
        return Response.status(Response.Status.OK).build();
    }

    @Override
    public Response deleteAlertConfig(AlertConfigListDTO body, MessageContext messageContext) {
        storeAlertConfigurator = getAlertConfigurator();
        String tenantAwareUserName = getTenantAwareUserName();

        for (AlertConfigDTO alertConfigDTO : body.getConfig()) {
            Properties configurationProperties = new Properties();
            configurationProperties.setProperty("apiName", alertConfigDTO.getApiName());
            configurationProperties.setProperty("apiVersion", alertConfigDTO.getApiVersion());
            configurationProperties.setProperty("applicationId", alertConfigDTO.getApplicationId());
            configurationProperties
                    .setProperty("thresholdRequestCountPerMin", String.valueOf(alertConfigDTO.getRequestCount()));
            try {
                storeAlertConfigurator.removeAlertConfiguration(tenantAwareUserName, null, configurationProperties);
            } catch (APIManagementException e) {
                RestApiUtil.handleInternalServerError("Error while removing configuration for alert type", e, log);
            }
        }
        return Response.status(Response.Status.OK).build();
    }

    @Override
    public Response getAlertConfigs(MessageContext messageContext) {
        storeAlertConfigurator = getAlertConfigurator();
        String tenantAwareUserName = getTenantAwareUserName();
        try {
            List<Properties> alertConfigList = storeAlertConfigurator.getAlertConfiguration(tenantAwareUserName, null);
            List<AlertConfigDTO> alertConfigDTOList = new ArrayList<>();
            for (Properties alertConfig : alertConfigList) {
                AlertConfigDTO alertConfigDTO = AlertsMappingUtil.toAlertConfigDTO(alertConfig);
                alertConfigDTOList.add(alertConfigDTO);
            }
            return Response.status(Response.Status.OK).entity(alertConfigDTOList).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving alert configurations", e, log);
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    public Response getStoreAlertTypes(MessageContext messageContext) {
        storeAlertConfigurator = getAlertConfigurator();
        try {
            List<AlertTypeDTO> alertTypes = storeAlertConfigurator.getSupportedAlertTypes();
            return Response.status(Response.Status.OK).entity(alertTypes).build();
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Internal Server Error Occurred while retrieving alert types", e, log);
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response getSubscribedAlertTypes(MessageContext messageContext) {
        storeAlertConfigurator = getAlertConfigurator();
        String tenantAwareUserName = getTenantAwareUserName();

        try {
            List<Integer> subscribedAlertTypes = storeAlertConfigurator.getSubscribedAlerts(tenantAwareUserName);
            List<AlertTypeDTO> alertTypes = storeAlertConfigurator.getSupportedAlertTypes();
            AlertsInfoDTO alertsInfoDTO = new AlertsInfoDTO();
            List<AlertDTO> alertDTOS = new ArrayList<>();

            for (Integer alertId : subscribedAlertTypes) {
                for (AlertTypeDTO alertTypeDTO : alertTypes) {
                    if (alertId.equals(alertTypeDTO.getId())) {
                        AlertDTO alertDTO = new AlertDTO();
                        alertDTO.setId(alertId);
                        alertDTO.setName(alertTypeDTO.getName());
                        List<AlertConfigDTO> alertConfigDTOList = new ArrayList<>();
                        if (alertTypeDTO.isConfigurable()) {
                            List<Properties> configurationList = storeAlertConfigurator
                                    .getAlertConfiguration(tenantAwareUserName, null);
                            for (Properties properties : configurationList) {
                                AlertConfigDTO alertConfigDTO = AlertsMappingUtil.toAlertConfigDTO(properties);
                                alertConfigDTOList.add(alertConfigDTO);
                            }
                            alertDTO.setConfiguration(alertConfigDTOList);
                        }
                        alertDTOS.add(alertDTO);
                    }
                }
            }
            List<String> subscribedEmails = storeAlertConfigurator.getSubscribedEmailAddresses(tenantAwareUserName);
            alertsInfoDTO.setAlerts(alertDTOS);
            alertsInfoDTO.setEmailList(subscribedEmails);
            return Response.status(Response.Status.OK).entity(alertsInfoDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Internal Error occurred", e, log);
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response subscribeToAlerts(AlertsInfoDTO body, MessageContext messageContext) {
        storeAlertConfigurator = getAlertConfigurator();
        String tenantAwareUserName = getTenantAwareUserName();

        List<String> emailsList = body.getEmailList();
        if (emailsList == null || emailsList.size() == 0) {
            RestApiUtil.handleBadRequest("Email list should not be empty", log);
        }
        List<AlertDTO> alertsToSubscribe = body.getAlerts();
        if (alertsToSubscribe == null || alertsToSubscribe.size() == 0) {
            RestApiUtil.handleBadRequest("Alert list should not be empty", log);
        }
        List<AlertTypeDTO> alertTypesToSubscribe = new ArrayList<>();
        for (AlertDTO alertDTO : alertsToSubscribe) {
            AlertTypeDTO alertTypeDTO = new AlertTypeDTO();
            alertTypeDTO.setName(alertDTO.getName());
            alertTypeDTO.setId(alertDTO.getId());
            alertTypesToSubscribe.add(alertTypeDTO);
            if (alertDTO.getConfiguration().size() > 0) {
                for (AlertConfigDTO alertConfigDTO : alertDTO.getConfiguration()) {
                    Properties configurationProperties = AlertsMappingUtil.toAlertConfigProperties(alertConfigDTO);
                    try {
                        storeAlertConfigurator
                                .addAlertConfiguration(tenantAwareUserName, null, configurationProperties);
                    } catch (APIManagementException e) {
                        RestApiUtil.handleInternalServerError(
                                "Error while adding Alert Configuration for Alert " + alertDTO.getName(), e, log);
                    }
                }
            }
        }

        try {
            storeAlertConfigurator.subscribe(tenantAwareUserName, emailsList, alertTypesToSubscribe);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while subscribing to alert types", e, log);
        }
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    @Override
    public Response unsubscribeAllAlerts(MessageContext messageContext) {
        storeAlertConfigurator = getAlertConfigurator();
        String tenantAwareUserName = getTenantAwareUserName();
        try {
            storeAlertConfigurator.unsubscribe(tenantAwareUserName);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Internal Server Error occurred while un subscribing from alerts", e,
                    log);
        }
        return Response.status(Response.Status.OK).build();
    }

    /**
     * Method to get the alert configurator class.
     * */
    private AlertConfigurator getAlertConfigurator() {
        try {
            AlertConfigManager alertConfigManager = AlertConfigManager.getInstance();
            return storeAlertConfigurator = alertConfigManager.getAlertConfigurator("subscriber");
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Analytics Not Configured", e, log);
        }
        return null;
    }

    /**
     * Get the user name with the tenant domain.
     *
     * @return User name with tenant domain.
     * */
    private String getTenantAwareUserName() {
        String userName = RestApiUtil.getLoggedInUsername();
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            return userName + "@" + tenantDomain;
        }
        return userName;
    }
}
