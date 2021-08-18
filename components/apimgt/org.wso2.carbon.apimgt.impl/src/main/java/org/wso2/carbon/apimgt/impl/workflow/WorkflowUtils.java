/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.workflow;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationRegistrationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.UUID;

public class WorkflowUtils {

    public static void sendNotificationAfterWFComplete(WorkflowDTO workflowDTO, String wfType)
            throws APIManagementException {

        if (WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION.equalsIgnoreCase(wfType)) {
            String applicationId = workflowDTO.getWorkflowReference();
            int appId = Integer.parseInt(applicationId);
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            Application application = apiMgtDAO.getApplicationById(appId);
            ApplicationWorkflowDTO appWFDto = (ApplicationWorkflowDTO) workflowDTO;
            appWFDto.setApplication(application);
            ApplicationEvent applicationEvent = new ApplicationEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.APPLICATION_CREATE.name(),
                    appWFDto.getTenantId(), appWFDto.getTenantDomain(), appWFDto.getApplication().getId(),
                    appWFDto.getApplication().getUUID(),
                    appWFDto.getApplication().getName(), appWFDto.getApplication().getTokenType(),
                    appWFDto.getApplication().getTier(), appWFDto.getApplication().getGroupId(),
                    appWFDto.getApplication().getApplicationAttributes(), application.getSubscriber().getName());
            APIUtil.sendNotification(applicationEvent, APIConstants.NotifierType.APPLICATION.name());
        } else if (WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION.equalsIgnoreCase(wfType)) {
            ApplicationWorkflowDTO appWFDto = (ApplicationWorkflowDTO) workflowDTO;
            ApplicationEvent applicationEvent = new ApplicationEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.APPLICATION_DELETE.name(),
                    appWFDto.getTenantId(), appWFDto.getTenantDomain(), appWFDto.getApplication().getId(),
                    appWFDto.getApplication().getUUID(),
                    appWFDto.getApplication().getName(), appWFDto.getApplication().getTokenType(),
                    appWFDto.getApplication().getTier(), appWFDto.getApplication().getGroupId(),
                    appWFDto.getApplication().getApplicationAttributes(), "");
            APIUtil.sendNotification(applicationEvent, APIConstants.NotifierType.APPLICATION.name());
        } else if (WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION.equalsIgnoreCase(wfType)) {
            SubscriptionWorkflowDTO subWFDto = (SubscriptionWorkflowDTO) workflowDTO; 
            SubscribedAPI sub = ApiMgtDAO.getInstance()
                    .getSubscriptionById(Integer.valueOf(subWFDto.getWorkflowReference()));
            SubscriptionEvent subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_CREATE.name(),
                    subWFDto.getTenantId(), subWFDto.getTenantDomain(),
                    Integer.valueOf(subWFDto.getWorkflowReference()), sub.getIdentifier().getId(),
                    sub.getApplication().getId(), sub.getTier().getName(), sub.getSubCreatedStatus());
            APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
        } else if (WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE.equalsIgnoreCase(wfType)) {
            SubscriptionWorkflowDTO subWFDto = (SubscriptionWorkflowDTO) workflowDTO;
            SubscribedAPI sub = ApiMgtDAO.getInstance()
                    .getSubscriptionById(Integer.valueOf(subWFDto.getWorkflowReference()));
            SubscriptionEvent subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_UPDATE.name(),
                    subWFDto.getTenantId(), subWFDto.getTenantDomain(),
                    Integer.valueOf(subWFDto.getWorkflowReference()), sub.getIdentifier().getId(),
                    sub.getApplication().getId(), sub.getTier().getName(), sub.getSubCreatedStatus());
            APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
        } else if (WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION.equalsIgnoreCase(wfType)) {
            SubscriptionWorkflowDTO subWFDto = (SubscriptionWorkflowDTO) workflowDTO;
            SubscribedAPI sub = ApiMgtDAO.getInstance()
                    .getSubscriptionById(Integer.valueOf(subWFDto.getWorkflowReference()));
            SubscriptionEvent subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_DELETE.name(),
                    subWFDto.getTenantId(), subWFDto.getTenantDomain(),
                    Integer.valueOf(subWFDto.getWorkflowReference()), sub.getIdentifier().getId(),
                    sub.getApplication().getId(), sub.getTier().getName(), sub.getSubCreatedStatus());
            APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
        } else if (WorkflowConstants.WF_TYPE_AM_API_STATE.equalsIgnoreCase(wfType)) {
            APIStateWorkflowDTO apiStateWFDto = (APIStateWorkflowDTO) workflowDTO;

            APIEvent apiEvent = new APIEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                    APIConstants.EventType.API_LIFECYCLE_CHANGE.name(), apiStateWFDto.getTenantId(),
                    apiStateWFDto.getTenantDomain(), apiStateWFDto.getApiName(),
                    Integer.valueOf(apiStateWFDto.getWorkflowReference()),
                    APIUtil.getUUIDOfAPI(apiStateWFDto.getApiProvider(),apiStateWFDto.getApiName(),
                            apiStateWFDto.getApiVersion(), apiStateWFDto.getTenantDomain()),
                    apiStateWFDto.getApiVersion(), apiStateWFDto.getApiType(), apiStateWFDto.getApiContext(),
                    apiStateWFDto.getApiProvider(), apiStateWFDto.getApiLCAction());
            APIUtil.sendNotification(apiEvent, APIConstants.NotifierType.API.name());
        } else if (WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION.equalsIgnoreCase(wfType)) {
            ApplicationRegistrationWorkflowDTO appRegWFDto = (ApplicationRegistrationWorkflowDTO) workflowDTO;
            ApplicationRegistrationEvent applicationRegistrationEvent = new ApplicationRegistrationEvent(
                    UUID.randomUUID().toString(), System.currentTimeMillis(),
                    APIConstants.EventType.APPLICATION_REGISTRATION_CREATE.name(), appRegWFDto.getTenantId(),
                    appRegWFDto.getTenantDomain(), appRegWFDto.getApplication().getId(),
                    appRegWFDto.getApplicationInfo().getClientId(), appRegWFDto.getApplication().getTokenType(),
                    appRegWFDto.getKeyManager());
            APIUtil.sendNotification(applicationRegistrationEvent,
                    APIConstants.NotifierType.APPLICATION_REGISTRATION.name());
        } else if (WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX.equalsIgnoreCase(wfType)) {
            ApplicationRegistrationWorkflowDTO appRegWFDto = (ApplicationRegistrationWorkflowDTO) workflowDTO;
            ApplicationRegistrationEvent applicationRegistrationEvent = new ApplicationRegistrationEvent(
                    UUID.randomUUID().toString(), System.currentTimeMillis(),
                    APIConstants.EventType.APPLICATION_REGISTRATION_CREATE.name(), appRegWFDto.getTenantId(),
                    appRegWFDto.getTenantDomain(), appRegWFDto.getApplication().getId(),
                    appRegWFDto.getApplicationInfo().getClientId(), appRegWFDto.getApplication().getTokenType(),
                    appRegWFDto.getKeyManager());
            APIUtil.sendNotification(applicationRegistrationEvent,
                    APIConstants.NotifierType.APPLICATION_REGISTRATION.name());
        }
    }
}
