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
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.systemNotifications.NotificationDTO;
import org.wso2.carbon.apimgt.impl.dto.systemNotifications.NotificationEndUserDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.List;

public class ApplicationCreationWFNotifier extends AbstractWFNotifier {
    private static final Log log = LogFactory.getLog(ApplicationCreationWFNotifier.class);

    @Override
    public NotificationDTO prepareNotification(WorkflowDTO workflowDTO) throws APIManagementException {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setNotificationType(NotificationType.APPLICATION_CREATION);
        notificationDTO.setNotificationMetadata(getNotificationMetaData(workflowDTO));
        notificationDTO.setEndUsers(getEndUsers(workflowDTO));
        return notificationDTO;
    }

    @Override
    public NotificationMetaData getNotificationMetaData(WorkflowDTO workflowDTO) {
        NotificationMetaData notificationMetaData = new NotificationMetaData();
        notificationMetaData.setApplicationName(
                workflowDTO.getProperties(APIConstants.PortalNotifications.APPLICATION_NAME));
        notificationMetaData.setComment(workflowDTO.getComments());
        return notificationMetaData;
    }

    @Override
    public List<NotificationEndUserDTO> getEndUsers(WorkflowDTO workflowDTO) throws APIManagementException {
        List<NotificationEndUserDTO> destinationUserList = new ArrayList<>();
        String destinationUser = workflowDTO.getProperties("userName");
        destinationUserList.add(getPrimaryUser(destinationUser, workflowDTO.getTenantDomain(),
                APIConstants.PortalNotifications.DEV_PORTAL));
        //add users of the same group
        try {
            if (APIUtil.isMultiGroupAppSharingEnabled()) {
                destinationUserList.addAll(getAllUsersBelongToGroupList(destinationUser,
                        getAllUsersBelongToGroup(workflowDTO, groupID(workflowDTO))));
            }
        } catch (UserStoreException e) {
            APIUtil.handleException("Error while getting user list belong to a group ", e);
        }
        return destinationUserList;
    }

    public String groupID(WorkflowDTO workflowDTO) throws APIManagementException {
        String applicationId = workflowDTO.getWorkflowReference();
        int appId = Integer.parseInt(applicationId);
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        Application application = apiMgtDAO.getApplicationById(appId);
        return application.getGroupId();
    }

}