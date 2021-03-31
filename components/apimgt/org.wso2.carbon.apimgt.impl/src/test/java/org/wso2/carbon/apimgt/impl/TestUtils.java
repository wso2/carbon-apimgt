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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.mockito.Matchers;
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
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.xml.stream.XMLStreamException;

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
    
    public static ServiceReferenceHolder mockRegistryAndUserRealm(int tenantId) throws UserStoreException,
            RegistryException, XMLStreamException {
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
        org.wso2.carbon.user.api.UserRealm userR = Mockito.mock(org.wso2.carbon.user.api.UserRealm.class);
        PowerMockito.when(realmService.getTenantUserRealm(-1234)).thenReturn(userR);
        AuthorizationManager authManager = Mockito.mock(AuthorizationManager.class);
        PowerMockito.when(userR.getAuthorizationManager()).thenReturn(authManager);
        PowerMockito.when(realmService.getBootstrapRealm()).thenReturn(bootstrapRealm);
        ServiceReferenceHolder.setUserRealm(bootstrapRealm);

        PowerMockito.when(tm.getTenantId(Matchers.anyString())).thenReturn(tenantId);

        return sh;
    }

    public static ServiceReferenceHolder mockAPIMConfiguration(String propertyName, String value, int tenantId)
            throws RegistryException,
            UserStoreException, XMLStreamException {
        ServiceReferenceHolder sh = mockRegistryAndUserRealm(tenantId);
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);
        
        PowerMockito.when(sh.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        PowerMockito.when(amConfig.getFirstProperty(propertyName)).thenReturn(value);

        Map<String, Environment> apiGatewayEnvironments = new HashMap<String, Environment>();
        Environment env1 = new Environment();
        apiGatewayEnvironments.put("PROD", env1);
        //Mocking some commonly used configs
        PowerMockito.when(amConfig.getApiGatewayEnvironments()).thenReturn(apiGatewayEnvironments);
        PowerMockito.when(amConfig.getFirstProperty(APIConstants.API_GATEWAY_TYPE)).
                thenReturn(APIConstants.API_GATEWAY_TYPE_SYNAPSE);
        PowerMockito.when(amConfig.getFirstProperty(APIConstants.API_PUBLISHER_ENABLE_API_DOC_VISIBILITY_LEVELS)).
                thenReturn("true", "false");
        return sh;
    }

    public static void mockAPIMConfiguration() throws RegistryException,
            UserStoreException, XMLStreamException {
        ServiceReferenceHolder sh = mockRegistryAndUserRealm(-1234);
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);

        PowerMockito.when(sh.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);

        Map<String, Environment> apiGatewayEnvironments = new HashMap<String, Environment>();
        Environment env1 = new Environment();
        env1.setApiGatewayEndpoint("https://abc.com, http://abc.com");
        apiGatewayEnvironments.put("PROD", env1);
        // Mocking some commonly used configs
        PowerMockito.when(amConfig.getApiGatewayEnvironments()).thenReturn(apiGatewayEnvironments);
        PowerMockito.when(amConfig.getFirstProperty(APIConstants.API_GATEWAY_TYPE)).thenReturn(
                APIConstants.API_GATEWAY_TYPE_SYNAPSE);
        PowerMockito.when(amConfig.getFirstProperty(APIConstants.API_PUBLISHER_ENABLE_API_DOC_VISIBILITY_LEVELS)).
                thenReturn("true", "false");

        ThrottleProperties throttleProperties = new ThrottleProperties();
        PowerMockito.when(amConfig.getThrottleProperties()).thenReturn(throttleProperties);

    }
    
    public static ServiceReferenceHolder getServiceReferenceHolder() throws XMLStreamException {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder sh = PowerMockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(sh);
        ConfigurationContextService configurationContextService = initConfigurationContextService(false);
        PowerMockito.when(ServiceReferenceHolder.getContextService()).thenReturn(configurationContextService);
        return sh;
    }

    public static void mockAPICacheClearence() {
        PowerMockito.mockStatic(Caching.class);
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Cache<Object, Object> cache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(APIConstants.RECENTLY_ADDED_API_CACHE_NAME)).thenReturn(cache);
        Mockito.doNothing().when(cache).removeAll();
    }

    public static ApiMgtDAO getApiMgtDAO(){
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        return apiMgtDAO;
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

    public static TenantManager getTenantManager() throws XMLStreamException {
        ServiceReferenceHolder serviceReferenceHolder = getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        PowerMockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        return tenantManager;
    }

    /**
     * To initialize the (@link {@link ConfigurationContextService}} for testing.
     *
     * @return initialized configuration context service.
     */
    public static ConfigurationContextService initConfigurationContextService(boolean initAPIMConfigurationService)
            throws XMLStreamException {
        ConfigurationContextService configurationContextService = Mockito.mock(ConfigurationContextService.class);
        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        AxisConfiguration axisConfiguration = Mockito.mock(AxisConfiguration.class);
        Mockito.doReturn(axisConfiguration).when(configurationContext).getAxisConfiguration();
        TransportInDescription transportInDescription = Mockito.mock(TransportInDescription.class);
        Mockito.doReturn(transportInDescription).when(axisConfiguration).getTransportIn(Mockito.anyString());
        Parameter dynamicSSLProfilesConfigParameter = Mockito.mock(Parameter.class);
        Mockito.when(dynamicSSLProfilesConfigParameter.getParameterElement()).thenReturn(getDynamicSSLElement());
        Mockito.when(transportInDescription.getParameter("dynamicSSLProfilesConfig"))
                .thenReturn(dynamicSSLProfilesConfigParameter);
        TransportOutDescription transportOutDescription = Mockito.mock(TransportOutDescription.class);
        Mockito.doReturn(transportOutDescription).when(axisConfiguration).getTransportOut(Mockito.anyString());
        Mockito.when(transportOutDescription.getParameter("dynamicSSLProfilesConfig"))
                .thenReturn(dynamicSSLProfilesConfigParameter);
        Mockito.doReturn(configurationContext).when(configurationContextService).getServerConfigContext();

        if (initAPIMConfigurationService) {
            APIManagerConfigurationService apiManagerConfigurationService = Mockito
                    .mock(APIManagerConfigurationService.class);
            APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
            Mockito.doReturn("true").when(apiManagerConfiguration).getFirstProperty(APIConstants.ENABLE_MTLS_FOR_APIS);
            Mockito.doReturn(apiManagerConfiguration).when(apiManagerConfigurationService).getAPIManagerConfiguration();
            ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(apiManagerConfigurationService);
        }
        ServiceReferenceHolder.setContextService(configurationContextService);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStore", ".");
        return configurationContextService;
    }
    private static OMElement getDynamicSSLElement() throws XMLStreamException {
        return AXIOMUtil.stringToOM("<parameter name=\"dynamicSSLProfilesConfig\">\n" +
                "           <filePath>/target/test-classes//security/sslprofiles.xml</filePath>\n" +
                "           <fileReadInterval>600000</fileReadInterval>\n" +
                "       </parameter>");
    }
}
