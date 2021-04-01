/*
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
 */
package org.wso2.carbon.apimgt.gateway.utils;

import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.Member;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.codec.binary.Base64;
import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.registry.RegistryServiceHolder;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * Test class for GatewayUtils.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({GatewayUtilsTestCase.class, PrivilegedCarbonContext.class, ServiceReferenceHolder.class,
        RegistryServiceHolder.class, Base64.class, SubscriptionDataHolder.class})
public class GatewayUtilsTestCase {
    private String apiProviderName = "admin";
    private String apiName = "PhoneVerify";
    private String version = "1.0";
    private String propertyName = "abc";
    private String propertyValue = "ishara";
    private String path = "/_abc/xyz";
    private String tenantDomain = "foo.com";
    private int tenantID = 1;
    private Resource resource;
    private ServiceReferenceHolder serviceReferenceHolder;
    private UserRegistry userRegistry;

    @Before
    public void setup() {
        System.setProperty("carbon.home", "jhkjn");
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(RegistryServiceHolder.class);
        PowerMockito.mockStatic(SubscriptionDataHolder.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryServiceHolder registryServiceHolder = Mockito.mock(RegistryServiceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        userRegistry = Mockito.mock(UserRegistry.class);
        resource = Mockito.mock(Resource.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.when(RegistryServiceHolder.getInstance()).thenReturn(registryServiceHolder);
        Mockito.when(registryServiceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(privilegedCarbonContext.getTenantId()).thenReturn(tenantID);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("carbon.super");
        try {
            Mockito.when(registryService.getConfigSystemRegistry(tenantID)).thenReturn(userRegistry);
        } catch (RegistryException e) {
            fail("Error while mocking registryService.getConfigSystemRegistry");
        }
        try {
            Mockito.when(userRegistry.get(path)).thenReturn(resource);
        } catch (RegistryException e) {
            fail("Error while mocking userRegistry.get(path)");
        }

    }

    @Test
    public void testSetRegistryProperty() throws RegistryException {
        try {
            GatewayUtils.setRegistryProperty(propertyName, propertyValue, path, tenantDomain);
        } catch (APIManagementException e) {
            fail("APIManagementException occurred. " + e.getStackTrace());
        }

        try {
            Mockito.when(userRegistry.get(path)).thenThrow(new RegistryException(""));
            GatewayUtils.setRegistryProperty(propertyName, propertyValue, path, tenantDomain);
            fail("Expected Registry Exception is not thrown");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error while reading registry resource"));
        }

        //test update property when tenant domain is null

        try {
            Mockito.when(resource.getProperty(propertyName)).thenReturn("oldValue");
            GatewayUtils.setRegistryProperty(propertyName, propertyValue, path, "");
            fail("expected APIManagementException is not thrown when accessing registry");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error while reading registry resource"));
        }

    }

    @Test
    public void testGenerateMap() {
        List list = new ArrayList<Object>();
        list.add(new Object());
        GatewayUtils.generateMap(list);
        Assert.assertTrue(true);
    }

    @Test
    public void testGetAPIEndpointSecretAlias() {
        Assert.assertEquals("admin--PhoneVerify1.0", GatewayUtils.getAPIEndpointSecretAlias(apiProviderName,
                apiName, version));

    }

    @Test
    public void testIsClusteringEnabled() {
        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        AxisConfiguration axisConfiguration = Mockito.mock(AxisConfiguration.class);
        ClusteringAgent clusteringAgent = Mockito.mock(ClusteringAgent.class);
        Member member = Mockito.mock(Member.class);
        List<Member> memberList = new ArrayList<>(1);
        memberList.add(member);
        clusteringAgent.setMembers(new ArrayList<Member>());
        Mockito.when(serviceReferenceHolder.getServerConfigurationContext()).thenReturn(configurationContext);
        Mockito.when(configurationContext.getAxisConfiguration()).thenReturn(axisConfiguration);

        //test when clusteringAgent is null
        Mockito.when(axisConfiguration.getClusteringAgent()).thenReturn(null);
        GatewayUtils.isClusteringEnabled();

        // test when clusteringAgent is set
        Mockito.when(axisConfiguration.getClusteringAgent()).thenReturn(clusteringAgent);
        Mockito.when(clusteringAgent.getMembers()).thenReturn(memberList);
        GatewayUtils.isClusteringEnabled();
        Assert.assertEquals(1, clusteringAgent.getMembers().size());
    }

    @Test
    public void testGetAPI() {
        API api = new API();
        api.setApiName("api1");
        api.setApiVersion("1.0.0");
        api.setApiProvider("admin");
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        SubscriptionDataHolder subscriptionDataHolder = Mockito.mock(SubscriptionDataHolder.class);
        Mockito.when(SubscriptionDataHolder.getInstance()).thenReturn(subscriptionDataHolder);
        SubscriptionDataStore subscriptionDataStore = Mockito.mock(SubscriptionDataStore.class);
        Mockito.when(subscriptionDataHolder.getTenantSubscriptionStore("carbon.super")).thenReturn(subscriptionDataStore);
        Mockito.when(subscriptionDataStore.getApiByContextAndVersion("/abc", "1.0.0")).thenReturn(api);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/abc");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        Assert.assertEquals(GatewayUtils.getAPI(messageContext), api);
        Assert.assertEquals(GatewayUtils.getAPINameFromContextAndVersion(messageContext),"api1");
        Assert.assertEquals(GatewayUtils.getApiProviderFromContextAndVersion(messageContext),"admin");
        Mockito.verify(messageContext, Mockito.times(6)).getProperty(APIMgtGatewayConstants.API_OBJECT);
        Mockito.verify(messageContext, Mockito.times(3)).getProperty(RESTConstants.REST_API_CONTEXT);
        Mockito.verify(messageContext, Mockito.times(3)).getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        Mockito.verify(subscriptionDataStore, Mockito.times(3)).getApiByContextAndVersion("/abc", "1.0.0");
    }

    @Test
    public void testGetAPIFromProperty() {
        API api = Mockito.mock(API.class);
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        SubscriptionDataHolder subscriptionDataHolder = Mockito.mock(SubscriptionDataHolder.class);
        Mockito.when(SubscriptionDataHolder.getInstance()).thenReturn(subscriptionDataHolder);
        SubscriptionDataStore subscriptionDataStore = Mockito.mock(SubscriptionDataStore.class);
        Mockito.when(subscriptionDataHolder.getTenantSubscriptionStore("carbon.super")).thenReturn(subscriptionDataStore);
        Mockito.when(subscriptionDataStore.getApiByContextAndVersion("/abc", "1.0.0")).thenReturn(api);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/abc");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.API_OBJECT)).thenReturn(api);
        Assert.assertEquals(GatewayUtils.getAPI(messageContext), api);
        Mockito.verify(messageContext, Mockito.times(1)).getProperty(APIMgtGatewayConstants.API_OBJECT);
        Mockito.verify(messageContext, Mockito.times(0)).getProperty(RESTConstants.REST_API_CONTEXT);
        Mockito.verify(messageContext, Mockito.times(0)).getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        Mockito.verify(subscriptionDataStore, Mockito.times(0)).getApiByContextAndVersion("/abc", "1.0.0");
    }

    @Test
    public void testGetAPIasNull() {
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        SubscriptionDataHolder subscriptionDataHolder = Mockito.mock(SubscriptionDataHolder.class);
        Mockito.when(SubscriptionDataHolder.getInstance()).thenReturn(subscriptionDataHolder);
        SubscriptionDataStore subscriptionDataStore = Mockito.mock(SubscriptionDataStore.class);
        Mockito.when(subscriptionDataHolder.getTenantSubscriptionStore("carbon.super")).thenReturn(subscriptionDataStore);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/abc1");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        Assert.assertNull(GatewayUtils.getAPI(messageContext));
        Assert.assertNull(GatewayUtils.getAPINameFromContextAndVersion(messageContext));
        Assert.assertNull(GatewayUtils.getApiProviderFromContextAndVersion(messageContext));
        Mockito.verify(messageContext, Mockito.times(0)).setProperty(Mockito.anyString(), Mockito.any());
        Mockito.verify(messageContext, Mockito.times(6)).getProperty(APIMgtGatewayConstants.API_OBJECT);
        Mockito.verify(messageContext, Mockito.times(3)).getProperty(RESTConstants.REST_API_CONTEXT);
        Mockito.verify(messageContext, Mockito.times(3)).getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        Mockito.verify(subscriptionDataStore, Mockito.times(3)).getApiByContextAndVersion("/abc1", "1.0.0");
    }

    @Test
    public void testGetAPIasNullWhenTenantSubscriptionStoreNull() {
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        SubscriptionDataHolder subscriptionDataHolder = Mockito.mock(SubscriptionDataHolder.class);
        Mockito.when(SubscriptionDataHolder.getInstance()).thenReturn(subscriptionDataHolder);
        SubscriptionDataStore subscriptionDataStore = Mockito.mock(SubscriptionDataStore.class);
        Mockito.when(subscriptionDataHolder.getTenantSubscriptionStore("carbon.super")).thenReturn(null);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/abc1");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        Assert.assertNull(GatewayUtils.getAPI(messageContext));
        Assert.assertNull(GatewayUtils.getStatus(messageContext));
        Mockito.verify(messageContext, Mockito.times(0)).setProperty(Mockito.anyString(), Mockito.any());
        Mockito.verify(messageContext, Mockito.times(4)).getProperty(APIMgtGatewayConstants.API_OBJECT);
        Mockito.verify(messageContext, Mockito.times(2)).getProperty(RESTConstants.REST_API_CONTEXT);
        Mockito.verify(messageContext, Mockito.times(2)).getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        Mockito.verify(subscriptionDataStore, Mockito.times(0)).getApiByContextAndVersion("/abc1", "1.0.0");
    }
    @Test
    public void testGetAPIStatusFromProperty() {
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.API_STATUS)).thenReturn(APIConstants.CREATED);
        Assert.assertEquals(GatewayUtils.getStatus(messageContext),APIConstants.CREATED);
        Assert.assertFalse(GatewayUtils.isAPIStatusPrototype(messageContext));
        Mockito.verify(messageContext, Mockito.times(2)).getProperty(APIMgtGatewayConstants.API_STATUS);
        Mockito.verify(messageContext, Mockito.times(0)).getProperty(APIMgtGatewayConstants.API_OBJECT);
        Mockito.verify(messageContext, Mockito.times(0)).setProperty(APIMgtGatewayConstants.API_STATUS,
                APIConstants.CREATED);
    }

    @Test
    public void testGetAPIStatusFromAPIObject() {
        API api = new API();
        api.setStatus(APIConstants.PROTOTYPED);
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.API_OBJECT)).thenReturn(api);
        Assert.assertEquals(GatewayUtils.getStatus(messageContext), APIConstants.PROTOTYPED);
        Assert.assertTrue(GatewayUtils.isAPIStatusPrototype(messageContext));
        Mockito.verify(messageContext, Mockito.times(2)).getProperty(APIMgtGatewayConstants.API_STATUS);
        Mockito.verify(messageContext, Mockito.times(2)).getProperty(APIMgtGatewayConstants.API_OBJECT);
        Mockito.verify(messageContext, Mockito.times(2)).setProperty(APIMgtGatewayConstants.API_STATUS,
                APIConstants.PROTOTYPED);
    }

    @Test
    public void testapiKey(){
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().claim(APIConstants.JwtTokenConstants.TOKEN_TYPE,
                APIConstants.JwtTokenConstants.API_KEY_TOKEN_TYPE).build();
        Assert.assertTrue(GatewayUtils.isAPIKey(jwtClaimsSet));
    }
    @Test
    public void testapiKeyWhenApplicationClaimAvailable(){
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().claim(APIConstants.JwtTokenConstants.APPLICATION,
                APIConstants.JwtTokenConstants.API_KEY_TOKEN_TYPE).build();
        Assert.assertTrue(GatewayUtils.isAPIKey(jwtClaimsSet));
    }
    @Test
    public void testNotAPIKey(){
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().build();
        Assert.assertFalse(GatewayUtils.isAPIKey(jwtClaimsSet));
    }
    @Test
    public void testIsInternalKey(){
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().claim(APIConstants.JwtTokenConstants.TOKEN_TYPE,
                APIConstants.JwtTokenConstants.INTERNAL_KEY_TOKEN_TYPE).build();
        Assert.assertTrue(GatewayUtils.isInternalKey(jwtClaimsSet));
    }
    @Test
    public void testIsNotInternalKey(){
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().build();
        Assert.assertFalse(GatewayUtils.isInternalKey(jwtClaimsSet));
    }
    @Test
    public void testIsNotInternalKeyWhenTokenTypeNotSame(){
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().claim(APIConstants.JwtTokenConstants.TOKEN_TYPE,
                APIConstants.JwtTokenConstants.API_KEY_TOKEN_TYPE).build();
        Assert.assertFalse(GatewayUtils.isInternalKey(jwtClaimsSet));
    }

}
