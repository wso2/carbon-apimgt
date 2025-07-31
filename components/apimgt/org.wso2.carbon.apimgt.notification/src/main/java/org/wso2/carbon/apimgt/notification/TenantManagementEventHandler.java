/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.notification;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.clients.TenantManagementClient;
import org.wso2.carbon.apimgt.impl.handlers.EventHandler;
import org.wso2.carbon.apimgt.notification.event.TenantManagementEvent;

import java.util.List;
import java.util.Map;

/**
 * Event handler to handle tenant management related events
 */
public class TenantManagementEventHandler implements EventHandler {

    private static final Log log = LogFactory.getLog(TenantManagementEventHandler.class);
    private TenantManagementClient client;

    @Override
    public boolean handleEvent(String event, Map<String, List<String>> headers) throws APIManagementException {
        try {
            if (client == null) {
                client = new TenantManagementClient();
            }
            TenantManagementEvent tenantMgtEvent = new Gson().fromJson(event, TenantManagementEvent.class);
            if (tenantMgtEvent == null || tenantMgtEvent.getTenantDomain() == null) {
                throw new APIManagementException("Invalid tenant management event data");
            }

            if (log.isDebugEnabled()) {
                log.debug("Processing tenant management event of type: " + tenantMgtEvent.getType());
            }

            if (tenantMgtEvent.getType() == null) {
                throw new APIManagementException("Event type cannot be null");
            }

            if (APIConstants.TenantManagementEvent.TYPE_ADD_TENANT.equals(tenantMgtEvent.getType())) {
                addTenant(tenantMgtEvent);
            } else if (APIConstants.TenantManagementEvent.TYPE_UPDATE_TENANT.equals(tenantMgtEvent.getType())) {
                updateTenant(tenantMgtEvent);
            } else if (APIConstants.TenantManagementEvent.TYPE_ACTIVATE_TENANT.equals(tenantMgtEvent.getType())) {
                activateTenant(tenantMgtEvent);
            } else if (APIConstants.TenantManagementEvent.TYPE_DEACTIVATE_TENANT.equals(tenantMgtEvent.getType())) {
                deactivateTenant(tenantMgtEvent);
            } else {
                throw new APIManagementException("Invalid event type " + tenantMgtEvent.getType());
            }
        } catch (APIManagementException e) {
            log.error("Error processing tenant management event", e);
            throw new APIManagementException("Error while creating tenant management client", e);
        }
        return true;
    }

    private void deactivateTenant(TenantManagementEvent tenantMgtEvent) throws APIManagementException {

        client.deactivateTenant(tenantMgtEvent.getTenantDomain());

    }

    private void activateTenant(TenantManagementEvent tenantMgtEvent) throws APIManagementException {

        client.activateTenant(tenantMgtEvent.getTenantDomain());

    }

    private void updateTenant(TenantManagementEvent updateTenantEvent) throws APIManagementException {

        client.updateTenant(updateTenantEvent.getFirstName(), updateTenantEvent.getLastName(),
                updateTenantEvent.getAdminUserName(), updateTenantEvent.getAdminPassword(),
                updateTenantEvent.getEmail(), updateTenantEvent.getTenantDomain(), updateTenantEvent.isActive());
    }

    private void addTenant(TenantManagementEvent addTenantEvent) throws APIManagementException {

        client.addTenant(addTenantEvent.getFirstName(), addTenantEvent.getLastName(), addTenantEvent.getAdminUserName(),
                addTenantEvent.getAdminPassword(), addTenantEvent.getEmail(), addTenantEvent.getTenantDomain(),
                addTenantEvent.isActive());

    }

    @Override
    public String getType() {
        return APIConstants.TenantManagementEvent.TENANT_MANAGEMENT_TYPE;
    }

}
