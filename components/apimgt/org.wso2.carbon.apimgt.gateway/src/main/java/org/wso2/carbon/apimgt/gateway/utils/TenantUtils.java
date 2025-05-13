package org.wso2.carbon.apimgt.gateway.utils;

import org.jetbrains.annotations.NotNull;
import org.wso2.carbon.apimgt.gateway.dto.TenantInfo;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.dto.LoadingTenants;
import org.wso2.carbon.apimgt.impl.notifier.events.TenantEvent;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.stratos.common.exception.TenantMgtException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.UUID;

public class TenantUtils {
    public static void addTenant(TenantInfo tenantInfo) throws TenantMgtException {
        Tenant tenant = fromTenantInfoToTenant(tenantInfo);
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        try {
            ServiceReferenceHolder.getInstance().getTenantMgtService().addTenant(tenant);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @NotNull
    private static Tenant fromTenantInfoToTenant(TenantInfo tenantInfo) {
        Tenant tenant = new Tenant();
        tenant.setActive(tenantInfo.isActive());
        tenant.setDomain(tenantInfo.getDomain());
        tenant.setEmail(tenantInfo.getEmail());
        tenant.setAdminFirstName(tenantInfo.getAdminFirstName());
        tenant.setAdminLastName(tenantInfo.getAdminLastName());
        tenant.setAdminFullName(tenantInfo.getAdminFullName());
        tenant.setAdminName(tenantInfo.getAdmin());
        tenant.setAdminPassword(UUID.randomUUID().toString());
        return tenant;
    }

    public static void addTenant(TenantEvent tenantEvent) throws TenantMgtException {
        addTenant(fromTenantEventToTenantInfo(tenantEvent));

    }

    private static TenantInfo fromTenantEventToTenantInfo(TenantEvent tenantEvent) {
        TenantInfo tenantInfo = new TenantInfo();
        tenantInfo.setDomain(tenantEvent.getTenantDomain());
        tenantInfo.setActive(tenantEvent.isActive());
        tenantInfo.setEmail(tenantEvent.getEmail());
        tenantInfo.setAdminFirstName(tenantEvent.getFirstname());
        tenantInfo.setAdminLastName(tenantEvent.getLastName());
        tenantInfo.setAdmin(tenantEvent.getAdmin());
        tenantInfo.setTenantId(tenantEvent.getTenantId());
        return tenantInfo;
    }

    public static void updateTenant(TenantEvent tenantEvent) throws UserStoreException {
        updateTenant(fromTenantEventToTenantInfo(tenantEvent));
    }

    public static void updateTenant(TenantInfo tenantInfo) throws UserStoreException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            TenantManager tenantManager = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
            int tenantId = tenantManager.getTenantId(tenantInfo.getDomain());
            if (tenantInfo.isActive()) {
                tenantManager.activateTenant(tenantId);
            } else {
                tenantManager.deactivateTenant(tenantId);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }

    /**
     * Verify Tenant Available to deploy and invoke apis
     *
     * @param tenantDomain requested Tenant
     * @return true if available
     */
    public static boolean isTenantAvailable(String tenantDomain) {
        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties =
                apiManagerConfiguration.getGatewayArtifactSynchronizerProperties();
        if (gatewayArtifactSynchronizerProperties.isTenantLoading()) {
            LoadingTenants loadingTenants = gatewayArtifactSynchronizerProperties.getLoadingTenants();
            if (loadingTenants != null) {
                return (loadingTenants.isIncludeAllTenants() ||
                        loadingTenants.getIncludingTenants().contains(tenantDomain)) &&
                        !loadingTenants.getExcludingTenants().contains(tenantDomain);
            }
        }
        return true;
    }
}
