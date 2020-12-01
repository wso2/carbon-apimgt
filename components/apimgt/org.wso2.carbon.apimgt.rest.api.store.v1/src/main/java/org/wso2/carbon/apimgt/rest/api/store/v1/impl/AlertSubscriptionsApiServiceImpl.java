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
import org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.AlertSubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertConfigDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertsInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertsInfoResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.AlertsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.utils.SubscriberAlertsAPIUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlertSubscriptionsApiServiceImpl implements AlertSubscriptionsApiService {

    private AlertConfigurator storeAlertConfigurator = null;
    private static final Log log = LogFactory.getLog(AlertSubscriptionsApiServiceImpl.class);
    private static final String AGENT = "subscriber";

    @Override
    public Response getSubscribedAlertTypes(MessageContext messageContext) {

        String userName = RestApiCommonUtil.getLoggedInUsername();
        String tenantAwareUserName = SubscriberAlertsAPIUtils.getTenantAwareUserName(userName);

        try {
            storeAlertConfigurator = AlertConfigManager.getInstance().getAlertConfigurator(AGENT);
            List<Integer> subscribedAlertTypes = storeAlertConfigurator.getSubscribedAlerts(tenantAwareUserName, AGENT);
            List<org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO> alertTypes = storeAlertConfigurator
                    .getSupportedAlertTypes(AGENT);
            AlertsInfoDTO alertsInfoDTO = new AlertsInfoDTO();
            List<AlertDTO> alertDTOS = new ArrayList<>();

            for (Integer alertId : subscribedAlertTypes) {
                for (org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO alertTypeDTO : alertTypes) {
                    if (alertId.equals(alertTypeDTO.getId())) {
                        AlertDTO alertDTO = new AlertDTO();
                        alertDTO.setId(alertId);
                        alertDTO.setName(alertTypeDTO.getName());
                        List<AlertConfigDTO> alertConfigDTOList = new ArrayList<>();
                        if (alertTypeDTO.isConfigurable()) {
                            List<Map<String, String>> configurationList = storeAlertConfigurator
                                    .getAlertConfiguration(userName, null);
                            for (Map<String, String> properties : configurationList) {
                                AlertConfigDTO alertConfigDTO = AlertsMappingUtil.toAlertConfigDTO(properties);
                                alertConfigDTOList.add(alertConfigDTO);
                            }
                            alertDTO.setConfiguration(alertConfigDTOList);
                        }
                        alertDTOS.add(alertDTO);
                    }
                }
            }
            List<String> subscribedEmails = storeAlertConfigurator
                    .getSubscribedEmailAddresses(tenantAwareUserName, AGENT);
            alertsInfoDTO.setAlerts(alertDTOS);
            alertsInfoDTO.setEmailList(subscribedEmails);
            return Response.status(Response.Status.OK).entity(alertsInfoDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Internal Error occurred", e, log);
        } catch (AlertManagementException e) {
            Response.status(Response.Status.BAD_REQUEST).entity("Analytics not enabled").build();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response subscribeToAlerts(AlertsInfoDTO body, MessageContext messageContext) {
        try {
            storeAlertConfigurator = AlertConfigManager.getInstance().getAlertConfigurator(AGENT);
        } catch (AlertManagementException e) {
            Response.status(Response.Status.BAD_REQUEST).entity("Analytics not enabled").build();
        }
        String userName = RestApiCommonUtil.getLoggedInUsername();
        String tenantAwareUserName = SubscriberAlertsAPIUtils.getTenantAwareUserName(userName);

        List<String> emailsList = body.getEmailList();
        if (emailsList == null || emailsList.size() == 0) {
            RestApiUtil.handleBadRequest("Email list should not be empty", log);
        }
        List<AlertDTO> alertsToSubscribe = body.getAlerts();
        if (alertsToSubscribe == null || alertsToSubscribe.size() == 0) {
            RestApiUtil.handleBadRequest("Alert list should not be empty", log);
        }
        List<org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO> alertTypesToSubscribe = new ArrayList<>();
        AlertsInfoResponseDTO alertsInfoResponseDTO = new AlertsInfoResponseDTO();
        alertsInfoResponseDTO.setAlerts(body.getAlerts());
        alertsInfoResponseDTO.setEmailList(body.getEmailList());
        List<AlertConfigDTO> failedConfigList = new ArrayList<>();
        for (AlertDTO alertDTO : alertsToSubscribe) {
            org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO alertTypeDTO = new AlertTypeDTO();
            alertTypeDTO.setName(alertDTO.getName());
            alertTypeDTO.setId(alertDTO.getId());
            alertTypesToSubscribe.add(alertTypeDTO);
            if (alertDTO.getConfiguration().size() > 0) {
                for (AlertConfigDTO alertConfigDTO : alertDTO.getConfiguration()) {
                    try {
                        storeAlertConfigurator
                                .addAlertConfiguration(userName, alertDTO.getName(),
                                        AlertsMappingUtil.alertInfoDTOToMap(alertConfigDTO));
                    } catch (APIManagementException e) {
                        failedConfigList.add(alertConfigDTO);
                        log.error("Error while adding alert configuration " + alertConfigDTO.toString());
                    }
                }
            }
        }
        alertsInfoResponseDTO.setFailedConfigurations(failedConfigList);
        try {
            storeAlertConfigurator.subscribe(tenantAwareUserName, emailsList, alertTypesToSubscribe);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while subscribing to alert types", e, log);
        }
        return Response.status(Response.Status.CREATED).entity(alertsInfoResponseDTO).build();
    }

    public Response unsubscribeAllAlerts(MessageContext messageContext) {
        String tenantAwareUserName = SubscriberAlertsAPIUtils.getTenantAwareUserName(
                RestApiCommonUtil.getLoggedInUsername());
        try {
            storeAlertConfigurator = AlertConfigManager.getInstance().getAlertConfigurator(AGENT);
            storeAlertConfigurator.unsubscribe(tenantAwareUserName);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Internal Server Error occurred while un subscribing from alerts", e,
                    log);
        } catch (AlertManagementException e) {
            Response.status(Response.Status.BAD_REQUEST).entity("Analytics not enabled").build();
        }
        return Response.status(Response.Status.OK).build();
    }
}
