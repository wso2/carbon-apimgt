/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.dao.test;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;

public class TestTenantManager implements TenantManager {

    public String getSuperTenantDomain() throws org.wso2.carbon.user.core.UserStoreException {
        return null;
    }

    public void setBundleContext(BundleContext bundleContext) {

    }

    public void initializeExistingPartitions() {

    }

    public int addTenant(Tenant tenant) throws UserStoreException {
        return 0;
    }

    public void updateTenant(Tenant tenant) throws UserStoreException {

    }

    public Tenant getTenant(int i) throws UserStoreException {
        return null;
    }

    public Tenant[] getAllTenants() throws UserStoreException {
        return new Tenant[MultitenantConstants.SUPER_TENANT_ID];
    }

    public String getDomain(int i) throws UserStoreException {
        return null;
    }

    public int getTenantId(String s) throws UserStoreException {
        return MultitenantConstants.SUPER_TENANT_ID;
    }

    public void activateTenant(int i) throws UserStoreException {

    }

    public void deactivateTenant(int i) throws UserStoreException {

    }

    public boolean isTenantActive(int i) throws UserStoreException {
        return false;
    }

    public void deleteTenant(int i) throws UserStoreException {

    }
    public Tenant[] getAllTenantsForTenantDomainStr(String domain) throws UserStoreException {
        return null;
    }

	@Override
	public String[] getAllTenantDomainStrOfUser(String arg0)
			throws UserStoreException {
		return null;
	}
}
