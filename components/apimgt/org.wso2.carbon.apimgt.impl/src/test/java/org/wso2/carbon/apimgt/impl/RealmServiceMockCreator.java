/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.impl;

import org.mockito.Mockito;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

public class RealmServiceMockCreator {
    private RealmService realmService;
    private TenantManagerMockCreator tenantManagerMockCreator;
    private UserRealmMockCreator userRealmMockCreator;

    public RealmServiceMockCreator(int tenantId) throws UserStoreException {
        userRealmMockCreator = new UserRealmMockCreator();
        tenantManagerMockCreator = new TenantManagerMockCreator(tenantId);
        realmService = Mockito.mock(RealmService.class);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManagerMockCreator.getMock());
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt())).thenReturn(userRealmMockCreator.getMock());
    }

    public TenantManagerMockCreator getTenantManagerMockCreator() {
        return tenantManagerMockCreator;
    }

    public UserRealmMockCreator getUserRealmMockCreator() {
        return userRealmMockCreator;
    }

    RealmService getMock() {
        return realmService;
    }

}
