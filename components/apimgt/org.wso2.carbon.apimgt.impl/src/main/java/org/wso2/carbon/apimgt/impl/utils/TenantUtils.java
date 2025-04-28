/*
 *
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.stratos.common.util.ClaimsMgtUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Utility class for Tenant related operations.
 */
public class TenantUtils {

    private TenantUtils() {
    }

    public static Tenant[] getAllTenants(String tenantDomain, String[] filters) throws APIManagementException {
        TenantManager tenantManager = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
        List<Tenant> tenants = new ArrayList<>();
        try {
            Tenant[] allTenants = tenantManager.getAllTenants();
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain) && tenantDomain != null) {
                Set<Tenant> tenantSet = Set.of(allTenants);
                allTenants = tenantSet.stream().filter(tenant -> tenant.getDomain().equals(tenantDomain)).toArray(Tenant[]::new);
            }
            for (Tenant tenant : allTenants) {
                boolean contains = Arrays.asList(filters).contains("*");
                if (contains) {
                    Tenant resolvedTenant = tenantManager.getTenant(tenant.getId());
                    resolvedTenant.setAdminFirstName(ClaimsMgtUtil.getFirstNamefromUserStoreManager(ServiceReferenceHolder.getInstance().getRealmService(), tenant.getId()));
                    resolvedTenant.setAdminLastName(ClaimsMgtUtil.getLastNamefromUserStoreManager(ServiceReferenceHolder.getInstance().getRealmService(), tenant.getId()));
                    tenants.add(tenantManager.getTenant(tenant.getId()));
                } else {
                    for (String filterCriteria : filters) {
                        if (tenant.getDomain().matches(filterCriteria)) {
                            Tenant resolvedTenant = tenantManager.getTenant(tenant.getId());
                            resolvedTenant.setAdminFirstName(ClaimsMgtUtil.getFirstNamefromUserStoreManager(ServiceReferenceHolder.getInstance().getRealmService(), tenant.getId()));
                            resolvedTenant.setAdminLastName(ClaimsMgtUtil.getLastNamefromUserStoreManager(ServiceReferenceHolder.getInstance().getRealmService(), tenant.getId()));
                            tenants.add(tenantManager.getTenant(tenant.getId()));
                        }
                    }
                }
            }
        } catch (UserStoreException e) {
            throw new APIManagementException("Couldn't retrieve all tenants", e);
        }
        return tenants.toArray(new Tenant[0]);
    }
}
