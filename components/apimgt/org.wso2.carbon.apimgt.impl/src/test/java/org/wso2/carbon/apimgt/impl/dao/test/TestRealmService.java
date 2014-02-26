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

import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.TenantMgtConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.config.multitenancy.MultiTenantRealmConfigBuilder;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

public class TestRealmService implements RealmService {

    public UserRealm getUserRealm(RealmConfiguration realmConfiguration) throws UserStoreException {
        return null;
    }

    public RealmConfiguration getBootstrapRealmConfiguration() {
        return null;
    }

    public UserRealm getBootstrapRealm() throws UserStoreException {
        return null;
    }

    public void setTenantManager(TenantManager tenantManager) throws UserStoreException {

    }

    public TenantManager getTenantManager() {
        return new TestTenantManager();
    }

    public MultiTenantRealmConfigBuilder getMultiTenantRealmConfigBuilder() throws UserStoreException {
        return null;
    }

    public UserRealm getCachedUserRealm(int i) throws UserStoreException {
        return null;
    }

    public void setTenantManager(org.wso2.carbon.user.api.TenantManager tenantManager) throws org.wso2.carbon.user.api.UserStoreException {

    }

    public org.wso2.carbon.user.api.UserRealm getTenantUserRealm(int i) throws org.wso2.carbon.user.api.UserStoreException {
        return null;
    }

    public TenantMgtConfiguration getTenantMgtConfiguration() {
        return null;
    }

    public void setBootstrapRealmConfiguration(RealmConfiguration realmConfiguration) {
        //TODO implement method
    }

	public void clearCachedUserRealm(int arg0) throws UserStoreException {
	
	}
}
