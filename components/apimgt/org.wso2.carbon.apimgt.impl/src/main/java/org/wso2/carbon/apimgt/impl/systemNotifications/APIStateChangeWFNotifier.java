/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.systemNotifications;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.systemNotifications.NotificationDTO;
import org.wso2.carbon.apimgt.impl.dto.systemNotifications.NotificationEndUserDTO;

import java.util.ArrayList;
import java.util.List;

public class APIStateChangeWFNotifier extends AbstractWFNotifier {
    private static final Log log = LogFactory.getLog(APIStateChangeWFNotifier.class);

    @Override
    public NotificationDTO prepareNotification(WorkflowDTO workflowDTO) throws APIManagementException {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setNotificationType(NotificationType.API_STATE_CHANGE);
        notificationDTO.setNotificationMetadata(getNotificationMetaData(workflowDTO));
        notificationDTO.setEndUsers(getEndUsers(workflowDTO));
        return notificationDTO;
    }

    @Override
    public NotificationMetaData getNotificationMetaData(WorkflowDTO workflowDTO) {
        NotificationMetaData notificationMetaData = new NotificationMetaData();

        notificationMetaData.setApi(workflowDTO.getProperties(APIConstants.PortalNotifications.API_NAME));
        notificationMetaData.setApiVersion(workflowDTO.getProperties(APIConstants.PortalNotifications.API_VERSION));
        notificationMetaData.setAction(workflowDTO.getProperties(APIConstants.PortalNotifications.ACTION));
        notificationMetaData.setComment(workflowDTO.getComments());
        notificationMetaData.setApiContext(workflowDTO.getMetadata(APIConstants.PortalNotifications.API_CONTEXT_META));

        return notificationMetaData;
    }

    @Override
    public List<NotificationEndUserDTO> getEndUsers(WorkflowDTO workflowDTO) throws APIManagementException {
        List<NotificationEndUserDTO> destinationUserList = new ArrayList<>();
        //add primary user
        String destinationUser = workflowDTO.getMetadata("Invoker");
        destinationUserList.add(getPrimaryUser(destinationUser, workflowDTO.getTenantDomain(),
                APIConstants.PortalNotifications.PUBLISHER_PORTAL));

        // add subscribers
        String apiName = workflowDTO.getProperties(APIConstants.PortalNotifications.API_NAME);
        String apiContext = workflowDTO.getMetadata(APIConstants.PortalNotifications.API_CONTEXT_META);
        String apiVersion = workflowDTO.getProperties(APIConstants.PortalNotifications.API_VERSION);
        String provider = workflowDTO.getMetadata(APIConstants.PortalNotifications.API_PROVIDER);
        String tenantDomain = workflowDTO.getTenantDomain();
        String apiAction = workflowDTO.getMetadata(APIConstants.PortalNotifications.ACTION_META);
        destinationUserList.addAll(
                getSubscriberListOfAPI(apiAction, apiName, apiContext, apiVersion, provider, tenantDomain));

        return destinationUserList;
    }

}
