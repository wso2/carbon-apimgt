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

public class APIMTenantMgtListener implements TenantMgtListener {
    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) {
        TenantEvent tenantCreationEvent = new TenantEvent(UUID.randomUUID().toString(), System.currentTimeMillis(), APIConstants.EventType.TENANT_CREATE.name(), tenantInfoBean.getTenantId(), tenantInfoBean.getTenantDomain(), tenantInfoBean.getAdmin(), tenantInfoBean.isActive(), tenantInfoBean.getFirstname(), tenantInfoBean.getLastname(), tenantInfoBean.getEmail());
        APIUtil.sendNotification(tenantCreationEvent, APIConstants.NotifierType.TENANT.name());
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) {
        TenantEvent tenantCreationEvent = new TenantEvent(UUID.randomUUID().toString(), System.currentTimeMillis(), APIConstants.EventType.TENANT_UPDATE.name(), tenantInfoBean.getTenantId(), tenantInfoBean.getTenantDomain(), tenantInfoBean.getAdmin(), tenantInfoBean.isActive(), tenantInfoBean.getFirstname(), tenantInfoBean.getLastname(), tenantInfoBean.getEmail());
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
        TenantManager tenantManager = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
        try {
            Tenant tenant = tenantManager.getTenant(i);
            if (tenant != null) {
                TenantEvent tenantCreationEvent = new TenantEvent(UUID.randomUUID().toString(), System.currentTimeMillis(), APIConstants.EventType.TENANT_ACTIVATION.name(), i, tenant.getDomain(), tenant.getAdminName(), tenant.isActive(), tenant.getAdminFirstName(), tenant.getAdminLastName(), tenant.getEmail());
                APIUtil.sendNotification(tenantCreationEvent, APIConstants.NotifierType.TENANT.name());
            }
        } catch (UserStoreException e) {
            throw new StratosException(e);
        }
    }

    @Override
    public void onTenantActivation(int i) throws StratosException {
        TenantManager tenantManager = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
        try {
            Tenant tenant = tenantManager.getTenant(i);
            if (tenant != null) {
                TenantEvent tenantCreationEvent = new TenantEvent(UUID.randomUUID().toString(), System.currentTimeMillis(), APIConstants.EventType.TENANT_ACTIVATION.name(), i, tenant.getDomain(), tenant.getAdminName(), tenant.isActive(), tenant.getAdminFirstName(), tenant.getAdminLastName(), tenant.getEmail());
                APIUtil.sendNotification(tenantCreationEvent, APIConstants.NotifierType.TENANT.name());
            }
        } catch (UserStoreException e) {
            throw new StratosException(e);
        }
    }

    @Override
    public void onTenantDeactivation(int i) throws StratosException {
        TenantManager tenantManager = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
        try {
            Tenant tenant = tenantManager.getTenant(i);
            if (tenant != null) {
                TenantEvent tenantCreationEvent = new TenantEvent(UUID.randomUUID().toString(), System.currentTimeMillis(), APIConstants.EventType.TENANT_DEACTIVATION.name(), i, tenant.getDomain(), tenant.getAdminName(), tenant.isActive(), tenant.getAdminFirstName(), tenant.getAdminLastName(), tenant.getEmail());
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
