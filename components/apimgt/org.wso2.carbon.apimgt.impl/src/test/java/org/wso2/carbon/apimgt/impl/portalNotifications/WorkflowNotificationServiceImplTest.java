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

package org.wso2.carbon.apimgt.impl.portalNotifications;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.PortalNotificationDAO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.portalNotifications.PortalNotificationDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ APIUtil.class, PortalNotificationDAO.class })
public class WorkflowNotificationServiceImplTest {

    private WorkflowNotificationServiceImpl notificationService;
    private PortalNotificationDAO portalNotificationDAO;

    private WorkflowDTO workflowDTO;

    @Before
    public void init() {
        notificationService = new WorkflowNotificationServiceImpl();
        portalNotificationDAO = Mockito.mock(PortalNotificationDAO.class);
        workflowDTO = createMockWorkflowDTO();
    }

    @Test
    public void testSendPortalNotificationsSuccess() throws APIManagementException {

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(PortalNotificationDAO.class);
        String tenantDomainOfUser = "carbonSuper";

        PowerMockito.when(APIUtil.isMultiGroupAppSharingEnabled()).thenReturn(false);
        PowerMockito.when(PortalNotificationDAO.getInstance()).thenReturn(portalNotificationDAO);
        Mockito.when(portalNotificationDAO.addNotification(Mockito.any(PortalNotificationDTO.class))).thenReturn(true);
        notificationService.sendPortalNotifications(workflowDTO, tenantDomainOfUser);
        Mockito.verify(portalNotificationDAO, Mockito.times(1)).addNotification(Mockito.any(PortalNotificationDTO.class));
    }

    private WorkflowDTO createMockWorkflowDTO() {
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setProperties("apiName", "Test API");
        workflowDTO.setProperties("apiVersion", "1.0");
        workflowDTO.setMetadata("Action", "Publish");
        workflowDTO.setMetadata("Provider", "Ali");
        workflowDTO.setProperties("revisionId", "1");
        workflowDTO.setComments("Test Comment");
        workflowDTO.setWorkflowType(WorkflowConstants.WF_TYPE_AM_API_STATE);
        workflowDTO.setMetadata("ApiContext", "/test/1.0");
        workflowDTO.setMetadata("Invoker", "Kevin");
        return workflowDTO;
    }

}

