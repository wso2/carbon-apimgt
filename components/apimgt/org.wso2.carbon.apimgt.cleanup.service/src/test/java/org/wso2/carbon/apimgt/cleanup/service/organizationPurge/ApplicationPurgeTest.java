/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.cleanup.service.organizationPurge;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.mockito.Mockito;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Caching.class, Cache.class, CacheProvider.class, Cache.class })
public class ApplicationPurgeTest {

    private OrganizationPurgeDAO organizationPurgeDAO;
    private ApiMgtDAO apiMgtDAO;

    @Before
    public void init() {
        organizationPurgeDAO = Mockito.mock(OrganizationPurgeDAO.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
    }

    @Test
    public void testOrganizationRemoval() throws APIManagementException {

        Mockito.doNothing().when(organizationPurgeDAO).removePendingSubscriptions(Mockito.anyString());
        Mockito.doNothing().when(organizationPurgeDAO).removeApplicationCreationWorkflows(Mockito.anyString());
        Mockito.doNothing().when(organizationPurgeDAO).deletePendingApplicationRegistrations(Mockito.anyString());
        Mockito.doNothing().when(organizationPurgeDAO).deleteApplicationList(Mockito.anyString());

        List<Integer> subscriberIdList = new ArrayList<>();
        subscriberIdList.add(1);

        Mockito.doReturn(subscriberIdList).when(organizationPurgeDAO).getSubscribersForOrganization(Mockito.any());

        List<String> mappedOrganizations = new ArrayList<>();
        mappedOrganizations.add("testOrg");

        Mockito.doReturn(mappedOrganizations).when(organizationPurgeDAO).getOrganizationsOfSubscriber(Mockito.anyInt());
        Mockito.doNothing().when(organizationPurgeDAO)
                .removeOrganizationFromSubscriber(Mockito.anyInt(), Mockito.anyString());

        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.doReturn(subscriber).when(apiMgtDAO).getSubscriber(Mockito.anyInt());
        Mockito.doNothing().when(organizationPurgeDAO).removeSubscriber(Mockito.anyInt());

        Cache cache = Mockito.mock(Cache.class);
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        PowerMockito.mockStatic(Caching.class);

        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);

        Subscriber cachedSubscriber = new Subscriber("application-purge-sub");

        PowerMockito.doReturn(cachedSubscriber).when(cache).get(Mockito.any());
        PowerMockito.doReturn(true).when(cache).remove(Mockito.any());
        Mockito.doNothing().when(organizationPurgeDAO).removeSubscriber(Mockito.anyInt());

        ApplicationPurge applicationPurge = new ApplicationPurgeWrapper(apiMgtDAO, organizationPurgeDAO);
        applicationPurge.organizationPurgeDAO = organizationPurgeDAO;
        applicationPurge.apiMgtDAO = apiMgtDAO;
        applicationPurge.purge("testOrg");

        Mockito.verify(organizationPurgeDAO, Mockito.times(1)).removePendingSubscriptions(Mockito.anyString());
        Mockito.verify(organizationPurgeDAO, Mockito.times(1)).removeApplicationCreationWorkflows(Mockito.anyString());
        Mockito.verify(organizationPurgeDAO, Mockito.times(1))
                .deletePendingApplicationRegistrations(Mockito.anyString());
        Mockito.verify(organizationPurgeDAO, Mockito.times(1)).deleteApplicationList(Mockito.anyString());
        Mockito.verify(organizationPurgeDAO, Mockito.times(1)).getSubscribersForOrganization(Mockito.anyString());
        Mockito.verify(organizationPurgeDAO, Mockito.times(1)).getOrganizationsOfSubscriber(Mockito.anyInt());
        Mockito.verify(organizationPurgeDAO, Mockito.times(1))
                .removeOrganizationFromSubscriber(Mockito.anyInt(), Mockito.anyString());
        Mockito.verify(apiMgtDAO, Mockito.times(1)).getSubscriber(Mockito.anyInt());
        Mockito.verify(organizationPurgeDAO, Mockito.times(1)).removeSubscriber(Mockito.anyInt());

    }
}
