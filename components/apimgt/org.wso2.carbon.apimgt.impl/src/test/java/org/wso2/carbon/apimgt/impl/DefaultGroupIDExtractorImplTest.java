/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.impl;


import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.user.core.tenant.*;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;


@RunWith(PowerMockRunner.class)
@PrepareForTest(ServiceReferenceHolder.class)
public class DefaultGroupIDExtractorImplTest {
    @Test
    public void getGroupingIdentifiersTestCase() throws UserStoreException {
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);

        Mockito.when(tenantManager.getTenantId("carbon.super")).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(userStoreManager.getUserClaimValue(MultitenantUtils.
                getTenantAwareUsername("user"), "http://wso2.org/claims/organization", null)).
                thenReturn("organization");

        DefaultGroupIDExtractorImpl defaultGroupIDExtractor = new DefaultGroupIDExtractorImpl();
        Assert.assertEquals("carbon.super/organization", defaultGroupIDExtractor.
                getGroupingIdentifiers("{\"user\":\"user\", \"isSuperTenant\":true}"));

        Assert.assertEquals("carbon.super/organization", defaultGroupIDExtractor.
                getGroupingIdentifiers("{\"user\":\"user\", \"isSuperTenant\":false}"));
    }
}
