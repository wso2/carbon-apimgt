/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.impl.TestUtils;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.wso2.carbon.apimgt.impl.token.ClaimsRetriever.DEFAULT_DIALECT_URI;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {LogFactory.class, ServiceReferenceHolder.class,
          MultitenantUtils.class, APIUtil.class})
@PowerMockIgnore("javax.net.ssl.*")
public class APIRealmUtilTest {

    @Test
    public void testLoggedInUserClaims() throws APIManagementException, UserStoreException, XMLStreamException {
        String configuredClaims = "http://wso2.org/claim1,http://wso2.org/claim2,http://wso2.org/claim3";
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.mockStatic(APIUtil.class);
        Mockito.when(MultitenantUtils.getTenantDomain("admin")).thenReturn("carbon.super");
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        PowerMockito.when(tenantManager.getTenantId(Matchers.anyString())).thenReturn(-1234);

        SortedMap<String, String> claimValues = new TreeMap<String, String>();
        claimValues.put("claim1", "http://wso2.org/claim1");
        claimValues.put("claim2", "http://wso2.org/claim2");
        claimValues.put("claim3", "http://wso2.org/claim3");
        PowerMockito.when(APIUtil.getTenantId("carbon.super")).thenReturn(-1234);
        PowerMockito.when(APIUtil.getClaims("admin", -1234, DEFAULT_DIALECT_URI))
                .thenReturn(claimValues);
        Map loggedInUserClaims = APIRealmUtils.getUserClaims("admin");
        assertNotNull(loggedInUserClaims);
        assertEquals(configuredClaims.split(",").length, loggedInUserClaims.size());
    }
}
