/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
import org.wso2.carbon.apimgt.rest.api.store.v1.utils.SubscriberAlertsAPIUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlertsApiServiceImpl implements AlertsApiService {

    private AlertConfigurator storeAlertConfigurator = null;
    private static final Log log = LogFactory.getLog(AlertsApiServiceImpl.class);
    private static final String AGENT = "subscriber";

    @Override
    public Response addAlertConfig(String alertType, String configurationId, AlertConfigInfoDTO body,
            MessageContext messageContext) {
        String tenantAwareUserName = RestApiUtil.getLoggedInUsername();
        SubscriberAlertsAPIUtils.validateConfigParameters(configurationId);
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
            Response.status(Response.Status.BAD_REQUEST).entity("Analytics not enabled").build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @Override
    public Response deleteAlertConfig(String alertType, String configurationId,
            MessageContext messageContext) {
        String tenantAwareUserName = RestApiUtil.getLoggedInUsername();
        SubscriberAlertsAPIUtils.validateConfigParameters(configurationId);
        try {
            storeAlertConfigurator = AlertConfigManager.getInstance().getAlertConfigurator(AGENT);
            storeAlertConfigurator.removeAlertConfiguration(tenantAwareUserName, alertType,
                    AlertsMappingUtil.configIdToMap(configurationId));
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while removing configuration for alert type", e, log);
        } catch (AlertManagementException e) {
            Response.status(Response.Status.BAD_REQUEST).entity("Analytics not enabled").build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @Override
    public Response getAllAlertConfigs(String alertType, MessageContext messageContext) {
        String userName = RestApiUtil.getLoggedInUsername();
        try {
            storeAlertConfigurator = AlertConfigManager.getInstance().getAlertConfigurator(AGENT);
            Map<String, List<String>> remainAPIVersionInfo = SubscriberAlertsAPIUtils.getConfigAPIs();
            List<Map<String, String>> alertConfigList = storeAlertConfigurator
                    .getAlertConfiguration(userName, alertType);
            List<AlertConfigDTO> alertConfigDTOList = new ArrayList<>();
            for (Map<String, String> alertConfig : alertConfigList) {
                String applicationId = alertConfig.get("applicationId");
                String applicationName = SubscriberAlertsAPIUtils.getApplicationNameById(Integer.parseInt(applicationId));
                List<String> remainVersions = remainAPIVersionInfo.get(alertConfig.get("apiName"));
                if (applicationName != null && remainVersions != null) {
                        String configVersion = alertConfig.get("apiVersion");
                        for (String version : remainVersions) {
                            if (configVersion.equals(version)) {
                                AlertConfigDTO alertConfigDTO = AlertsMappingUtil.toAlertConfigDTO(alertConfig);
                                alertConfigDTOList.add(alertConfigDTO);
                            }
                        }
                }
            }
            return Response.status(Response.Status.OK).entity(alertConfigDTOList).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving alert configurations", e, log);
        } catch (AlertManagementException e) {
            Response.status(Response.Status.BAD_REQUEST).entity("Analytics not enabled").build();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
