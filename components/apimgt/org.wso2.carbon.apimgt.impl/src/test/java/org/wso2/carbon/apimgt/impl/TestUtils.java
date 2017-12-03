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


import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.api.model.policy.Limit;
import org.wso2.carbon.apimgt.api.model.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.api.model.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import javax.xml.ws.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestUtils {

    protected static Application getUniqueApplication() {
        return new Application("TestApplication", getUniqueSubscriber());
    }

    protected static Subscriber getUniqueSubscriber() {
        return new Subscriber(UUID.randomUUID().toString());
    }

    protected static APIIdentifier getUniqueAPIIdentifier() {
        return new APIIdentifier(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID()
                 .toString());
    }

    public static ServiceReferenceHolder mockRegistryAndUserRealm(int tenantId) throws
            UserStoreException, RegistryException {
        return mockRegistryAndUserRealm(tenantId, false);
    }

    public static ServiceReferenceHolder mockRegistryAndUserRealm(int tenantId, boolean isAccessControlEnabled) throws
            UserStoreException, RegistryException {
        ServiceReferenceHolder sh = getServiceReferenceHolder();
        
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tm = Mockito.mock(TenantManager.class);
        
        PowerMockito.when(sh.getRealmService()).thenReturn(realmService);
        PowerMockito.when(realmService.getTenantManager()).thenReturn(tm);
        
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        
        UserRegistry userReg = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceUserRegistry()).thenReturn(userReg);
        
        UserRegistry systemReg = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getConfigSystemRegistry()).thenReturn(systemReg);
        
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        UserRealm bootstrapRealm = Mockito.mock(UserRealm.class);
        
        PowerMockito.when(systemReg.getUserRealm()).thenReturn(userRealm);        
        PowerMockito.doNothing().when(ServiceReferenceHolder.class); 
        ServiceReferenceHolder.setUserRealm(userRealm);
        
        PowerMockito.when(realmService.getBootstrapRealm()).thenReturn(bootstrapRealm);
        ServiceReferenceHolder.setUserRealm(bootstrapRealm);
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);

        PowerMockito.when(sh.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);

        if (isAccessControlEnabled) {
            Mockito.doReturn("true").when(amConfig)
                    .getFirstProperty(APIConstants.API_PUBLISHER_ENABLE_ACCESS_CONTROL_LEVELS);
            PowerMockito.when(tm.getTenantId("carbon.super")).thenReturn(tenantId);
        }
        return sh;
    }
    
    public static ServiceReferenceHolder mockAPIMConfiguration(String propertyName, String value, int tenantId) throws RegistryException,
                                                                                        UserStoreException{
        ServiceReferenceHolder sh = mockRegistryAndUserRealm(tenantId);
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);
        
        PowerMockito.when(sh.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        PowerMockito.when(amConfig.getFirstProperty(propertyName)).thenReturn(value);

        return sh;
    }
    
    public static ServiceReferenceHolder getServiceReferenceHolder() {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        return sh;
    }

    /**
     * Return unique subscription policy with rate limit
     *
     * @return unique subscription policy
     */
    public static SubscriptionPolicy getUniqueSubscriptionPolicyWithRequestCountLimit() {
        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(UUID.randomUUID().toString());
        Limit limit = new RequestCountLimit();
        limit.setTimeUnit("seconds");
        limit.setUnitTime(10);
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setLimit(limit);
        subscriptionPolicy.setDefaultQuotaPolicy(quotaPolicy);
        return subscriptionPolicy;
    }

    /**
     * Return unique subscription policy with Bandwidth limit
     *
     * @return unique subscription policy
     */
    public static SubscriptionPolicy getUniqueSubscriptionPolicyWithBandwidthLimit() {
        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(UUID.randomUUID().toString());
        Limit limit = new BandwidthLimit();
        limit.setTimeUnit("seconds");
        limit.setUnitTime(10);
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setLimit(limit);
        subscriptionPolicy.setDefaultQuotaPolicy(quotaPolicy);
        return subscriptionPolicy;
    }
}
