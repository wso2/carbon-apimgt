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
import org.wso2.carbon.apimgt.impl.dao.NotificationDAO;
import org.wso2.carbon.apimgt.impl.dto.systemNotifications.NotificationDTO;

public class BaseNotifier implements Notifier {

    private static final Log log = LogFactory.getLog(BaseNotifier.class);

    @Override
    public void sendNotifications(NotificationDTO notificationDTO) {
        try {
            boolean result = NotificationDAO.getInstance().addNotification(notificationDTO);
            if (!result) {
                log.error("Error while adding publisher developer notification.");
            }
        } catch (APIManagementException e) {
            throw new RuntimeException(e);
        }
    }

}
