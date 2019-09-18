package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertConfigManager;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertConfigurator;
import org.wso2.carbon.apimgt.impl.alertmgt.exception.AlertManagementException;
import org.wso2.carbon.apimgt.rest.api.store.v1.AlertsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertConfigDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertConfigInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.AlertsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.utils.AlertsAPIUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlertsApiServiceImpl implements AlertsApiService {

    private AlertConfigurator storeAlertConfigurator = null;
    private static final Log log = LogFactory.getLog(ApiProductsApiServiceImpl.class);
    private static final String AGENT = "subscriber";

    @Override
    public Response addAlertConfig(String alertType, String configurationId, AlertConfigInfoDTO body,
            MessageContext messageContext) {
        String tenantAwareUserName = RestApiUtil.getLoggedInUsername();
        AlertsAPIUtils.validateConfigParameters(configurationId);
        if (body == null) {
            RestApiUtil.handleBadRequest("Configuration should not be empty", log);
        }
        try {
            storeAlertConfigurator = AlertConfigManager.getInstance().getAlertConfigurator(AGENT);
            storeAlertConfigurator.addAlertConfiguration(tenantAwareUserName, alertType,
                    AlertsMappingUtil.alertInfoDTOToMap(body));
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while adding configuration for alert type", e, log);
        } catch (AlertManagementException e) {
            RestApiUtil.handleInternalServerError("Analytics not enabled", e, log);
        }
        return Response.status(Response.Status.OK).build();
    }

    @Override
    public Response deleteAlertConfig(String alertType, String configurationId,
            MessageContext messageContext) {
        String tenantAwareUserName = RestApiUtil.getLoggedInUsername();
        AlertsAPIUtils.validateConfigParameters(configurationId);
        try {
            storeAlertConfigurator = AlertConfigManager.getInstance().getAlertConfigurator(AGENT);
            storeAlertConfigurator.removeAlertConfiguration(tenantAwareUserName, alertType,
                    AlertsMappingUtil.configIdToMap(configurationId));
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while removing configuration for alert type", e, log);
        } catch (AlertManagementException e) {
            RestApiUtil.handleInternalServerError("Analytics not enabled", e, log);
        }
        return Response.status(Response.Status.OK).build();
    }

    @Override
    public Response getAllAlertConfigs(String alertType, MessageContext messageContext) {
        String userName = RestApiUtil.getLoggedInUsername();
        try {
            storeAlertConfigurator = AlertConfigManager.getInstance().getAlertConfigurator(AGENT);
            List<Map<String, String>> alertConfigList = storeAlertConfigurator
                    .getAlertConfiguration(userName, alertType);
            List<AlertConfigDTO> alertConfigDTOList = new ArrayList<>();
            for (Map<String, String> alertConfig : alertConfigList) {
                AlertConfigDTO alertConfigDTO = AlertsMappingUtil.toAlertConfigDTO(alertConfig);
                alertConfigDTOList.add(alertConfigDTO);
            }
            return Response.status(Response.Status.OK).entity(alertConfigDTOList).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving alert configurations", e, log);
        } catch (AlertManagementException e) {
            RestApiUtil.handleInternalServerError("Analytics not enabled", e, log);
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
