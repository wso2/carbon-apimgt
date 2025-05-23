/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.carbon.apimgt.impl.listeners;

import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.TenantEvent;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.UUID;

/**
 * Tenant management listener to handle tenant related events.
 */
public class APIMTenantMgtListener implements TenantMgtListener {
    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) {
        TenantEvent tenantCreationEvent = new TenantEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                APIConstants.EventType.TENANT_CREATE.name(), tenantInfoBean.getTenantId(),
                tenantInfoBean.getTenantDomain(), tenantInfoBean.getAdmin(), tenantInfoBean.isActive(),
                tenantInfoBean.getFirstname(), tenantInfoBean.getLastname(), tenantInfoBean.getEmail());
        APIUtil.sendNotification(tenantCreationEvent, APIConstants.NotifierType.TENANT.name());
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) {
        TenantEvent tenantCreationEvent = new TenantEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                APIConstants.EventType.TENANT_UPDATE.name(), tenantInfoBean.getTenantId(),
                tenantInfoBean.getTenantDomain(), tenantInfoBean.getAdmin(), tenantInfoBean.isActive(),
                tenantInfoBean.getFirstname(), tenantInfoBean.getLastname(), tenantInfoBean.getEmail());
        APIUtil.sendNotification(tenantCreationEvent, APIConstants.NotifierType.TENANT.name());
    }

    @Override
    public void onTenantDelete(int i) {
    }

    @Override
    public void onTenantRename(int i, String s, String s1) {
    }

    @Override
    public void onTenantInitialActivation(int i) throws StratosException {
        sendTenantEvent(APIConstants.EventType.TENANT_ACTIVATION.name(), i);
    }

    @Override
    public void onTenantActivation(int i) throws StratosException {
        sendTenantEvent(APIConstants.EventType.TENANT_ACTIVATION.name(), i);
    }

    @Override
    public void onTenantDeactivation(int i) throws StratosException {
        sendTenantEvent(APIConstants.EventType.TENANT_DEACTIVATION.name(), i);

    }

    private void sendTenantEvent(String eventType, int tenantId) throws StratosException {
        TenantManager tenantManager = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
        try {
            Tenant tenant = tenantManager.getTenant(tenantId);
            if (tenant != null) {
                TenantEvent tenantCreationEvent =
                        new TenantEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                                eventType, tenantId, tenant.getDomain(),
                                tenant.getAdminName(), tenant.isActive(), tenant.getAdminFirstName(),
                                tenant.getAdminLastName(), tenant.getEmail());
                APIUtil.sendNotification(tenantCreationEvent, APIConstants.NotifierType.TENANT.name());
            }
        } catch (UserStoreException e) {
            throw new StratosException(e);
        }
    }

    @Override
    public void onSubscriptionPlanChange(int i, String s, String s1) {
    }

    @Override
    public int getListenerOrder() {
        return 0;
    }

    @Override
    public void onPreDelete(int i) {
    }
}
