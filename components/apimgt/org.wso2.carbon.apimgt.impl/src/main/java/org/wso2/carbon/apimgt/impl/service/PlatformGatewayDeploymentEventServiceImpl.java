/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.service;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.PlatformGatewayDeploymentEventService;
import org.wso2.carbon.apimgt.api.model.PlatformGatewayDeploymentEventRecord;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayDeploymentEventDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of platform gateway deployment event service for multi-CP WebSocket sync.
 */
public class PlatformGatewayDeploymentEventServiceImpl implements PlatformGatewayDeploymentEventService {

    private static final PlatformGatewayDeploymentEventServiceImpl INSTANCE =
            new PlatformGatewayDeploymentEventServiceImpl();

    public static PlatformGatewayDeploymentEventServiceImpl getInstance() {
        return INSTANCE;
    }

    private PlatformGatewayDeploymentEventServiceImpl() {
    }

    @Override
    public void persistEvent(String gatewayId, String apiId, String revisionUuid, String eventType, String payload)
            throws APIManagementException {
        PlatformGatewayDeploymentEventDAO.getInstance().insertEvent(
                gatewayId, apiId, revisionUuid, eventType, payload);
    }

    @Override
    public List<PlatformGatewayDeploymentEventRecord> getAndMarkDeliveredPendingEventsForGateway(String gatewayId)
            throws APIManagementException {
        List<PlatformGatewayDeploymentEventDAO.DeploymentEventRecord> daoList =
                PlatformGatewayDeploymentEventDAO.getInstance().getAndMarkDeliveredPendingEventsForGateway(gatewayId);
        List<PlatformGatewayDeploymentEventRecord> result = new ArrayList<>(daoList.size());
        for (PlatformGatewayDeploymentEventDAO.DeploymentEventRecord r : daoList) {
            result.add(new PlatformGatewayDeploymentEventRecord(r.getId(), r.getPayload()));
        }
        return result;
    }
}
