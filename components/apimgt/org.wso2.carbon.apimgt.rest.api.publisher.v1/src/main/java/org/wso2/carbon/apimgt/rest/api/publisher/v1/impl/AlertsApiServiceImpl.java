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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertConfigManager;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertConfigurator;
import org.wso2.carbon.apimgt.impl.alertmgt.exception.AlertManagementException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.AlertsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AlertConfigDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.PublisherAlertsAPIUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.AlertsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlertsApiServiceImpl implements AlertsApiService {

    private AlertConfigurator publisherAlertConfigurator = null;
    private static final Log log = LogFactory.getLog(AlertsApiServiceImpl.class);
    private static final String AGENT = "publisher";
    private static final String API_NAME = "apiName";
    private static final String API_VERSION = "apiVersion";

    @Override
    public Response addAlertConfig(String alertType, String configurationId, Map<String, String> body,
            MessageContext messageContext) throws APIManagementException {
        String tenantAwareUserName = RestApiCommonUtil.getLoggedInUsername();
        PublisherAlertsAPIUtils.validateConfigParameters(configurationId);
        if (body == null) {
            RestApiUtil.handleBadRequest("Configuration should not be empty", log);
        }
        try {
            publisherAlertConfigurator = AlertConfigManager.getInstance().getAlertConfigurator(AGENT);
            publisherAlertConfigurator.addAlertConfiguration(tenantAwareUserName, alertType, body);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while adding configuration for alert type", e, log);
        } catch (AlertManagementException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Analytics not Enabled").build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @Override
    public Response deleteAlertConfig(String alertType, String configurationId,
            MessageContext messageContext) throws APIManagementException {
        String tenantAwareUserName = RestApiCommonUtil.getLoggedInUsername();
        PublisherAlertsAPIUtils.validateConfigParameters(configurationId);
        try {
            publisherAlertConfigurator = AlertConfigManager.getInstance().getAlertConfigurator(AGENT);
            publisherAlertConfigurator.removeAlertConfiguration(tenantAwareUserName, alertType,
                    AlertsMappingUtil.configIdToMap(configurationId));
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while removing configuration for alert type", e, log);
        } catch (AlertManagementException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Analytics not Enabled").build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @Override
    public Response getAllAlertConfigs(String alertType, MessageContext messageContext) {
        String userName = RestApiCommonUtil.getLoggedInUsername();
        try {
            Map<String, List<String>> allowedAPIVersionInfo = AlertsMappingUtil.getAllowedAPIInfo();
            publisherAlertConfigurator = AlertConfigManager.getInstance().getAlertConfigurator(AGENT);
            List<Map<String, String>> alertConfigList = publisherAlertConfigurator
                    .getAlertConfiguration(userName, alertType);
            List<AlertConfigDTO> alertConfigDTOList = new ArrayList<>();
            for (Map<String, String> alertConfig : alertConfigList) {
                List<String> allowedVersions = allowedAPIVersionInfo.get(alertConfig.get(API_NAME));
                if (allowedVersions != null) {
                    String configVersion = alertConfig.get(API_VERSION);
                    for (String version : allowedVersions) {
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
            return Response.status(Response.Status.BAD_REQUEST).entity("Analytics not Enabled").build();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
