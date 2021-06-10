/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dao.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.LifeCycleEvent;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.CustomComplexityDetails;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.api.model.policy.Condition;
import org.wso2.carbon.apimgt.api.model.policy.DateCondition;
import org.wso2.carbon.apimgt.api.model.policy.DateRangeCondition;
import org.wso2.carbon.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.carbon.apimgt.api.model.policy.HTTPVerbCondition;
import org.wso2.carbon.apimgt.api.model.policy.HeaderCondition;
import org.wso2.carbon.apimgt.api.model.policy.IPCondition;
import org.wso2.carbon.apimgt.api.model.policy.JWTClaimsCondition;
import org.wso2.carbon.apimgt.api.model.policy.Pipeline;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.QueryParameterCondition;
import org.wso2.carbon.apimgt.api.model.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.api.model.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.Notifier;
import org.wso2.carbon.apimgt.impl.notifier.SubscriptionsNotifier;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {KeyManagerHolder.class})
public class APIMgtDAOTest {

    public static ApiMgtDAO apiMgtDAO;
    private KeyManager keyManager;

    @Before
    public void setUp() throws Exception {
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        initializeDatabase(dbConfigPath);
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl
                (config));
        List<Notifier> notifierList = new ArrayList<>();
        Notifier subscriptionsNotifier = Mockito.mock(Notifier.class);
        Mockito.when(subscriptionsNotifier.getType()).thenReturn(APIConstants.NotifierType.SUBSCRIPTIONS.name());
        notifierList.add(subscriptionsNotifier);
        ServiceReferenceHolder.getInstance().getNotifiersMap().put(subscriptionsNotifier.getType(), notifierList);
        PowerMockito.mockStatic(KeyManagerHolder.class);
        keyManager = Mockito.mock(KeyManager.class);
        APIMgtDBUtil.initialize();
        apiMgtDAO = ApiMgtDAO.getInstance();
        IdentityTenantUtil.setRealmService(new TestRealmService());
        String identityConfigPath = System.getProperty("IdentityConfigurationPath");
        IdentityConfigParser.getInstance(identityConfigPath);
        OAuthServerConfiguration oAuthServerConfiguration = OAuthServerConfiguration.getInstance();
        ServiceReferenceHolder.getInstance().setOauthServerConfiguration(oAuthServerConfiguration);

    }

    private static void initializeDatabase(String configFilePath) {

        InputStream in;
        try {
            in = FileUtils.openInputStream(new File(configFilePath));
            StAXOMBuilder builder = new StAXOMBuilder(in);
            String dataSource = builder.getDocumentElement().getFirstChildWithName(new QName("DataSourceName")).
                    getText();
            OMElement databaseElement = builder.getDocumentElement().getFirstChildWithName(new QName("Database"));
            String databaseURL = databaseElement.getFirstChildWithName(new QName("URL")).getText();
            String databaseUser = databaseElement.getFirstChildWithName(new QName("Username")).getText();
            String databasePass = databaseElement.getFirstChildWithName(new QName("Password")).getText();
            String databaseDriver = databaseElement.getFirstChildWithName(new QName("Driver")).getText();

            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName(databaseDriver);
            basicDataSource.setUrl(databaseURL);
            basicDataSource.setUsername(databaseUser);
            basicDataSource.setPassword(databasePass);

            // Create initial context
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES,
                    "org.apache.naming");
            try {
                InitialContext.doLookup("java:/comp/env/jdbc/WSO2AM_DB");
            } catch (NamingException e) {
                InitialContext ic = new InitialContext();
                ic.createSubcontext("java:");
                ic.createSubcontext("java:/comp");
                ic.createSubcontext("java:/comp/env");
                ic.createSubcontext("java:/comp/env/jdbc");

                ic.bind("java:/comp/env/jdbc/WSO2AM_DB", basicDataSource);
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

   /* public void testDataSource(){
        Context ctx = null;
        try {
            ctx = new InitialContext();
            DataSource dataSource = (DataSource) ctx.lookup("java:/comp/env/jdbc/WSO2AM_DB");
            Assert.assertNotNull(dataSource);
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }*/
   @Test
    public void testGetSubscribersOfProvider() throws Exception {
        Set<Subscriber> subscribers = apiMgtDAO.getInstance().getSubscribersOfProvider("SUMEDHA");
        assertNotNull(subscribers);
        assertTrue(subscribers.size() > 0);
    }

    @Test
    public void testGetSubscribedAPIsOfUser() throws Exception {
        APIInfoDTO[] apis = apiMgtDAO.getSubscribedAPIsOfUser("SUMEDHA");
        assertNotNull(apis);
        assertTrue(apis.length > 1);
    }
    @Test
    public void testGetSubscribedUsersForAPI() throws Exception {
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        apiInfoDTO.setApiName("API1");
        apiInfoDTO.setProviderId("SUMEDHA");
        apiInfoDTO.setVersion("V1.0.0");
        APIKeyInfoDTO[] apiKeyInfoDTO = apiMgtDAO.getInstance().getSubscribedUsersForAPI(apiInfoDTO);
        assertNotNull(apiKeyInfoDTO);
        assertTrue(apiKeyInfoDTO.length > 1);
    }
    @Test
    public void testGetSubscriber() throws Exception {
        Subscriber subscriber = apiMgtDAO.getInstance().getSubscriber("SUMEDHA");
        assertNotNull(subscriber);
        assertNotNull(subscriber.getName());
        assertNotNull(subscriber.getId());
    }

    @Test
    public void testIsSubscribed() throws Exception {
        APIIdentifier apiIdentifier = new APIIdentifier("SUMEDHA", "API1", "V1.0.0");
        boolean isSubscribed = apiMgtDAO.isSubscribed(apiIdentifier, "SUMEDHA");
        assertTrue(isSubscribed);

        apiIdentifier = new APIIdentifier("P1", "API2", "V1.0.0");
        isSubscribed = apiMgtDAO.isSubscribed(apiIdentifier, "UDAYANGA");
        assertFalse(isSubscribed);
    }
    @Test
    public void testGetAllAPIUsageByProvider() throws Exception {
        UserApplicationAPIUsage[] userApplicationAPIUsages = apiMgtDAO.getAllAPIUsageByProvider("SUMEDHA");
        assertNotNull(userApplicationAPIUsages);

    }
    @Test
    public void testAddSubscription() throws Exception {
        APIIdentifier apiIdentifier = new APIIdentifier("SUMEDHA", "API1", "V1.0.0");
        apiIdentifier.setApplicationId("APPLICATION99");
        apiIdentifier.setTier("T1");
        API api = new API(apiIdentifier);
        ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(api);
        apiMgtDAO.addSubscription(apiTypeWrapper, 100, "UNBLOCKED", "admin");
    }


    public void checkSubscribersEqual(Subscriber lhs, Subscriber rhs) throws Exception {
        assertEquals(lhs.getId(), rhs.getId());
        assertEquals(lhs.getEmail(), rhs.getEmail());
        assertEquals(lhs.getName(), rhs.getName());
        assertEquals(lhs.getSubscribedDate().getTime(), rhs.getSubscribedDate().getTime());
        assertEquals(lhs.getTenantId(), rhs.getTenantId());
    }

    public void checkApplicationsEqual(Application lhs, Application rhs) throws Exception {
        assertEquals(lhs.getId(), rhs.getId());
        assertEquals(lhs.getName(), rhs.getName());
        assertEquals(lhs.getDescription(), rhs.getDescription());
        assertEquals(lhs.getCallbackUrl(), rhs.getCallbackUrl());

    }
    @Test
    public void testAddGetSubscriber() throws Exception {
        Subscriber subscriber1 = new Subscriber("LA_F");
        subscriber1.setEmail("laf@wso2.com");
        subscriber1.setSubscribedDate(new Date());
        subscriber1.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        apiMgtDAO.addSubscriber(subscriber1, "");
        assertTrue(subscriber1.getId() > 0);
        Subscriber subscriber2 = apiMgtDAO.getSubscriber(subscriber1.getId());
        this.checkSubscribersEqual(subscriber1, subscriber2);
    }
    @Test
    public void testAddGetSubscriberWithGroupId() throws Exception {
        Subscriber subscriber1 = new Subscriber("LA_F_GROUPID");
        subscriber1.setEmail("laf@wso2.com");
        subscriber1.setSubscribedDate(new Date());
        subscriber1.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        apiMgtDAO.addSubscriber(subscriber1, "1");
        assertTrue(subscriber1.getId() > 0);
        Subscriber subscriber2 = apiMgtDAO.getSubscriber(subscriber1.getId());
        this.checkSubscribersEqual(subscriber1, subscriber2);
    }
    @Test
    public void testAddGetSubscriberWithNullGroupId() throws Exception {
        Subscriber subscriber1 = new Subscriber("LA_F2_GROUPID");
        subscriber1.setEmail("laf@wso2.com");
        subscriber1.setSubscribedDate(new Date());
        subscriber1.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        apiMgtDAO.addSubscriber(subscriber1, null);
        assertTrue(subscriber1.getId() > 0);
        Subscriber subscriber2 = apiMgtDAO.getSubscriber(subscriber1.getId());
        this.checkSubscribersEqual(subscriber1, subscriber2);
    }
    @Test
    public void testUpdateGetSubscriber() throws Exception {
        Subscriber subscriber1 = new Subscriber("LA_F2");
        subscriber1.setEmail("laf@wso2.com");
        subscriber1.setSubscribedDate(new Date());
        subscriber1.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        apiMgtDAO.addSubscriber(subscriber1, "2");
        assertTrue(subscriber1.getId() > 0);
        subscriber1.setEmail("laf2@wso2.com");
        subscriber1.setSubscribedDate(new Date());
        subscriber1.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        apiMgtDAO.updateSubscriber(subscriber1);
        Subscriber subscriber2 = apiMgtDAO.getSubscriber(subscriber1.getId());
        this.checkSubscribersEqual(subscriber1, subscriber2);
    }
    @Test
    public void testLifeCycleEvents() throws Exception {
        APIIdentifier apiId = new APIIdentifier("hiranya", "WSO2Earth", "1.0.0");
        API api = new API(apiId);
        api.setContext("/wso2earth");
        api.setContextTemplate("/wso2earth/{version}");

        apiMgtDAO.addAPI(api, -1234);

        List<LifeCycleEvent> events = apiMgtDAO.getLifeCycleEvents(apiId);
        assertEquals(1, events.size());
        LifeCycleEvent event = events.get(0);
        assertEquals(apiId, event.getApi());
        assertNull(event.getOldStatus());
        assertEquals(APIConstants.CREATED, event.getNewStatus());
        assertEquals("hiranya", event.getUserId());

        apiMgtDAO.recordAPILifeCycleEvent(apiId, APIStatus.CREATED, APIStatus.PUBLISHED, "admin", -1234);
        apiMgtDAO.recordAPILifeCycleEvent(apiId, APIStatus.PUBLISHED, APIStatus.DEPRECATED, "admin", -1234);
        events = apiMgtDAO.getLifeCycleEvents(apiId);
        assertEquals(3, events.size());
    }
    @Test
    public void testAddGetApplicationByNameGroupIdNull() throws Exception {
        Subscriber subscriber = new Subscriber("LA_F_GROUP_ID_NULL");
        subscriber.setEmail("laf@wso2.com");
        subscriber.setSubscribedDate(new Date());
        subscriber.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        apiMgtDAO.addSubscriber(subscriber, null);
        Application application = new Application("testApplication", subscriber);
        int applicationId = apiMgtDAO.addApplication(application, subscriber.getName());
        application.setId(applicationId);
        assertTrue(applicationId > 0);
        this.checkApplicationsEqual(application, apiMgtDAO.getApplicationByName("testApplication", subscriber.getName
                (), null));

    }
    @Test
    public void testAddGetApplicationByNameWithGroupId() throws Exception {
        Subscriber subscriber = new Subscriber("LA_F_APP");
        subscriber.setEmail("laf@wso2.com");
        subscriber.setSubscribedDate(new Date());
        subscriber.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        apiMgtDAO.addSubscriber(subscriber, "org1");
        Application application = new Application("testApplication3", subscriber);
        application.setGroupId("org1");
        int applicationId = apiMgtDAO.addApplication(application, subscriber.getName());
        application.setId(applicationId);
        assertTrue(applicationId > 0);
        this.checkApplicationsEqual(application, apiMgtDAO.getApplicationByName("testApplication3", subscriber
                .getName(), "org1"));

    }
    @Test
    public void testAddGetApplicationByNameWithUserNameNull() throws Exception {
        Subscriber subscriber = new Subscriber("LA_F_APP2");
        subscriber.setEmail("laf@wso2.com");
        subscriber.setSubscribedDate(new Date());
        subscriber.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        apiMgtDAO.addSubscriber(subscriber, "org2");
        Application application = new Application("testApplication3", subscriber);
        application.setGroupId("org2");
        int applicationId = apiMgtDAO.addApplication(application, subscriber.getName());
        application.setId(applicationId);
        assertTrue(applicationId > 0);
        this.checkApplicationsEqual(application, apiMgtDAO.getApplicationByName("testApplication3", null, "org2"));

    }

    @Test
    public void testAddGetApplicationByNameWithUserNameNullGroupIdNull() throws Exception {
        Subscriber subscriber = new Subscriber("LA_F_APP_UN_GROUP_ID_NULL");
        subscriber.setEmail("laf@wso2.com");
        subscriber.setSubscribedDate(new Date());
        subscriber.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        apiMgtDAO.addSubscriber(subscriber, null);
        Application application = new Application("testApplication2", subscriber);
        application.setUUID(UUID.randomUUID().toString());
        int applicationId = apiMgtDAO.addApplication(application, subscriber.getName());
        application.setId(applicationId);
        assertTrue(applicationId > 0);
        assertNotNull(apiMgtDAO.getApplicationByUUID(apiMgtDAO.getApplicationById(applicationId).getUUID()));
        assertNull(apiMgtDAO.getApplicationByName("testApplication2", null, null));

    }
    @Test
    public void testKeyForwardCompatibility() throws Exception {
        Set<APIIdentifier> apiSet = apiMgtDAO.getAPIByConsumerKey("SSDCHEJJ-AWUIS-232");
        assertEquals(1, apiSet.size());
        for (APIIdentifier apiId : apiSet) {
            assertEquals("SUMEDHA", apiId.getProviderName());
            assertEquals("API1", apiId.getApiName());
            assertEquals("V1.0.0", apiId.getVersion());
        }

        API api = new API(new APIIdentifier("SUMEDHA", "API1", "V2.0.0"));
        api.setContext("/context1");
        api.setContextTemplate("/context1/{version}");

        apiMgtDAO.addAPI(api, -1234);
        ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(api);
        apiMgtDAO.makeKeysForwardCompatible(apiTypeWrapper, "V1.0.0");
        apiSet = apiMgtDAO.getAPIByConsumerKey("SSDCHEJJ-AWUIS-232");
        assertEquals(2, apiSet.size());
        for (APIIdentifier apiId : apiSet) {
            assertEquals("SUMEDHA", apiId.getProviderName());
            assertEquals("API1", apiId.getApiName());
            assertTrue("V1.0.0".equals(apiId.getVersion()) || "V2.0.0".equals(apiId.getVersion()));
        }

        apiSet = apiMgtDAO.getAPIByConsumerKey("p1q2r3s4");
        assertEquals(2, apiSet.size());
        for (APIIdentifier apiId : apiSet) {
            assertEquals("SUMEDHA", apiId.getProviderName());
            assertEquals("API1", apiId.getApiName());
            assertTrue("V1.0.0".equals(apiId.getVersion()) || "V2.0.0".equals(apiId.getVersion()));
        }

        apiSet = apiMgtDAO.getAPIByConsumerKey("a1b2c3d4");
        assertEquals(1, apiSet.size());
        for (APIIdentifier apiId : apiSet) {
            assertEquals("PRABATH", apiId.getProviderName());
            assertEquals("API2", apiId.getApiName());
            assertEquals("V1.0.0", apiId.getVersion());
        }
    }
    @Test
    public void testInsertApplicationPolicy() throws APIManagementException {
        String policyName = "TestInsertAppPolicy";
        apiMgtDAO.addApplicationPolicy((ApplicationPolicy) getApplicationPolicy(policyName));
    }
    @Test
    public void testInsertSubscriptionPolicy() throws APIManagementException {
        String policyName = "TestInsertSubPolicy";
        apiMgtDAO.addSubscriptionPolicy((SubscriptionPolicy) getSubscriptionPolicy(policyName));
    }
    @Test
    public void testInsertAPIPolicy() throws APIManagementException {
        String policyName = "TestInsertAPIPolicy";
        apiMgtDAO.addAPIPolicy((APIPolicy) getPolicyAPILevelPerUser(policyName));
    }
    @Test
    public void testUpdateApplicationPolicy() throws APIManagementException {
        String policyName = "TestUpdateAppPolicy";
        ApplicationPolicy policy = (ApplicationPolicy) getApplicationPolicy(policyName);
        apiMgtDAO.addApplicationPolicy(policy);
        policy = apiMgtDAO.getApplicationPolicy(policyName, 4);
        policy.setDescription("Updated application description");
        apiMgtDAO.updateApplicationPolicy(policy);
    }
    @Test
    public void testUpdateSubscriptionPolicy() throws APIManagementException {
        String policyName = "TestUpdateSubPolicy";
        SubscriptionPolicy policy = (SubscriptionPolicy) getSubscriptionPolicy(policyName);
        policy.setMonetizationPlan(APIConstants.Monetization.FIXED_RATE);
        policy.setMonetizationPlanProperties(new HashMap<String, String>());
        apiMgtDAO.addSubscriptionPolicy(policy);
        policy = (SubscriptionPolicy) getSubscriptionPolicy(policyName);
        policy.setDescription("Updated subscription description");
        policy.setGraphQLMaxComplexity(200);
        policy.setGraphQLMaxDepth(7);
        apiMgtDAO.updateSubscriptionPolicy(policy);
    }
    @Test
    public void testUpdateAPIPolicy() throws APIManagementException {
        String policyName = "TestUpdateApiPolicy";
        APIPolicy policy = (APIPolicy) getPolicyAPILevelPerUser(policyName);
        apiMgtDAO.addAPIPolicy(policy);
        policy = apiMgtDAO.getAPIPolicy(policyName,-1234);
        policy.setDescription("New Description");

        ArrayList<Pipeline> pipelines = new ArrayList<Pipeline>();

        Pipeline p = new Pipeline();

        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("requestCount");
        RequestCountLimit requestCountLimit = new RequestCountLimit();
        requestCountLimit.setTimeUnit("min");
        requestCountLimit.setUnitTime(50);
        requestCountLimit.setRequestCount(1000);
        quotaPolicy.setLimit(requestCountLimit);

        ArrayList<Condition> conditions = new ArrayList<Condition>();

        DateCondition dateCondition = new DateCondition();
        dateCondition.setSpecificDate("2016-03-03");
        conditions.add(dateCondition);

        HeaderCondition headerCondition1 = new HeaderCondition();
        headerCondition1.setHeader("User-Agent");
        headerCondition1.setValue("Chrome");
        conditions.add(headerCondition1);

        HeaderCondition headerCondition2 = new HeaderCondition();
        headerCondition2.setHeader("Accept-Ranges");
        headerCondition2.setValue("bytes");
        conditions.add(headerCondition2);

        QueryParameterCondition queryParameterCondition1 = new QueryParameterCondition();
        queryParameterCondition1.setParameter("test1");
        queryParameterCondition1.setValue("testValue1");
        conditions.add(queryParameterCondition1);

        QueryParameterCondition queryParameterCondition2 = new QueryParameterCondition();
        queryParameterCondition2.setParameter("x");
        queryParameterCondition2.setValue("abc");
        conditions.add(queryParameterCondition2);

        JWTClaimsCondition jwtClaimsCondition1 = new JWTClaimsCondition();
        jwtClaimsCondition1.setClaimUrl("test_url");
        jwtClaimsCondition1.setAttribute("test_attribute");
        conditions.add(jwtClaimsCondition1);

        p.setQuotaPolicy(quotaPolicy);
        p.setConditions(conditions);
        pipelines.add(p);

        policy.setPipelines(pipelines);
        apiMgtDAO.updateAPIPolicy(policy);
        APIPolicy apiPolicy = apiMgtDAO.getAPIPolicy(policyName,-1234);
        assertNotNull(apiPolicy);
        List<Pipeline> pipelineList = apiPolicy.getPipelines();
        assertNotNull(pipelineList);
        assertEquals(pipelineList.size(),pipelines.size());
    }

    @Test
    public void testDeletePolicy() throws APIManagementException {
        apiMgtDAO.removeThrottlePolicy("app", "Bronze", -1234);
    }

    @Test
    public void testGetApplicationPolicies() throws APIManagementException {
        apiMgtDAO.getApplicationPolicies(-1234);
    }

    @Test
    public void testGetSubscriptionPolicies() throws APIManagementException {
        apiMgtDAO.getSubscriptionPolicies(-1234);
    }

    @Test
    public void testGetApiPolicies() throws APIManagementException {
        apiMgtDAO.getAPIPolicies(-1234);
    }

    @Test
    public void testGetApplicationPolicy() throws APIManagementException {
        String policyName = "TestGetAppPolicy";
        apiMgtDAO.addApplicationPolicy((ApplicationPolicy) getApplicationPolicy(policyName));
        apiMgtDAO.getApplicationPolicy(policyName, 4);
    }

    @Test
    public void testGetSubscriptionPolicy() throws APIManagementException {
        String policyName = "TestGetSubPolicy";
        apiMgtDAO.addSubscriptionPolicy((SubscriptionPolicy) getSubscriptionPolicy(policyName));
        apiMgtDAO.getSubscriptionPolicy(policyName, 6);
    }

    @Test
    public void testGetApiPolicy() throws APIManagementException {
        String policyName = "TestGetAPIPolicy";
        apiMgtDAO.addAPIPolicy((APIPolicy) getPolicyAPILevelPerUser(policyName));
        APIPolicy apiPolicy = apiMgtDAO.getAPIPolicy(policyName, -1234);
        assertNotNull(apiPolicy);
        assertEquals(apiPolicy.getPolicyName(), policyName);
        APIPolicy apiPolicyFromUUId = apiMgtDAO.getAPIPolicyByUUID(apiPolicy.getUUID());
        assertNotNull(apiPolicyFromUUId);
        assertEquals(apiPolicyFromUUId.getPolicyName(), policyName);
        apiMgtDAO.setPolicyDeploymentStatus(PolicyConstants.POLICY_LEVEL_API, apiPolicy.getPolicyName(), -1234, true);
        assertTrue(apiMgtDAO.isPolicyDeployed(PolicyConstants.POLICY_LEVEL_API, -1234, apiPolicy.getPolicyName()));
        assertTrue(apiMgtDAO.getPolicyNames(PolicyConstants.POLICY_LEVEL_API, "admin").length > 0);
        assertTrue(apiMgtDAO.isPolicyExist(PolicyConstants.POLICY_LEVEL_API, -1234, apiPolicy.getPolicyName()));
    }



    private Policy getPolicyAPILevelPerUser(String policyName) {
        APIPolicy policy = new APIPolicy(policyName);

        policy.setUserLevel(PolicyConstants.PER_USER);
        policy.setDescription("Description");
        policy.setTenantId(-1234);

        BandwidthLimit defaultLimit = new BandwidthLimit();
        defaultLimit.setTimeUnit("min");
        defaultLimit.setUnitTime(5);
        defaultLimit.setDataAmount(400);
        defaultLimit.setDataUnit("MB");

        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setLimit(defaultLimit);
        defaultQuotaPolicy.setType(PolicyConstants.BANDWIDTH_TYPE);

        policy.setDefaultQuotaPolicy(defaultQuotaPolicy);

        List<Pipeline> pipelines;
        QuotaPolicy quotaPolicy;
        List<Condition> condition;
        BandwidthLimit bandwidthLimit;
        RequestCountLimit requestCountLimit;
        pipelines = new ArrayList<Pipeline>();


        ///////////pipeline item 1 start//////
        Pipeline p1 = new Pipeline();

        quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType(PolicyConstants.BANDWIDTH_TYPE);
        bandwidthLimit = new BandwidthLimit();
        bandwidthLimit.setTimeUnit("min");
        bandwidthLimit.setUnitTime(5);
        bandwidthLimit.setDataAmount(100);
        bandwidthLimit.setDataUnit("GB");
        quotaPolicy.setLimit(bandwidthLimit);

        condition = new ArrayList<Condition>();
        HTTPVerbCondition verbCond = new HTTPVerbCondition();
        verbCond.setHttpVerb("POST");
        condition.add(verbCond);

        IPCondition ipCondition = new IPCondition(PolicyConstants.IP_SPECIFIC_TYPE);
        ipCondition.setSpecificIP("127.0.0.1");
        condition.add(ipCondition);


        DateRangeCondition dateRangeCondition = new DateRangeCondition();
        dateRangeCondition.setStartingDate("2016-01-03");
        dateRangeCondition.setEndingDate("2016-01-31");
        condition.add(dateRangeCondition);

        p1.setQuotaPolicy(quotaPolicy);
        p1.setConditions(condition);
        pipelines.add(p1);
        ///////////pipeline item 1 end//////

        ///////////pipeline item 2 start//////
        Pipeline p2 = new Pipeline();

        quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("requestCount");
        requestCountLimit = new RequestCountLimit();
        requestCountLimit.setTimeUnit("min");
        requestCountLimit.setUnitTime(50);
        requestCountLimit.setRequestCount(1000);
        quotaPolicy.setLimit(requestCountLimit);

        List<Condition> condition2 = new ArrayList<Condition>();

        DateCondition dateCondition = new DateCondition();
        dateCondition.setSpecificDate("2016-01-02");
        condition2.add(dateCondition);

        HeaderCondition headerCondition1 = new HeaderCondition();
        headerCondition1.setHeader("User-Agent");
        headerCondition1.setValue("Firefox");
        condition2.add(headerCondition1);

        HeaderCondition headerCondition2 = new HeaderCondition();
        headerCondition2.setHeader("Accept-Ranges");
        headerCondition2.setValue("bytes");
        condition2.add(headerCondition2);

        QueryParameterCondition queryParameterCondition1 = new QueryParameterCondition();
        queryParameterCondition1.setParameter("test1");
        queryParameterCondition1.setValue("testValue1");
        condition2.add(queryParameterCondition1);

        QueryParameterCondition queryParameterCondition2 = new QueryParameterCondition();
        queryParameterCondition2.setParameter("test2");
        queryParameterCondition2.setValue("testValue2");
        condition2.add(queryParameterCondition2);

        JWTClaimsCondition jwtClaimsCondition1 = new JWTClaimsCondition();
        jwtClaimsCondition1.setClaimUrl("test_url");
        jwtClaimsCondition1.setAttribute("test_attribute");
        condition2.add(jwtClaimsCondition1);

        JWTClaimsCondition jwtClaimsCondition2 = new JWTClaimsCondition();
        jwtClaimsCondition2.setClaimUrl("test_url");
        jwtClaimsCondition2.setAttribute("test_attribute");
        condition2.add(jwtClaimsCondition2);

        IPCondition ipRangeCondition = new IPCondition(PolicyConstants.IP_RANGE_TYPE);
        ipCondition.setStartingIP("127.0.0.1");
        ipCondition.setEndingIP("127.0.0.12");
        condition2.add(ipRangeCondition);
        p2.setQuotaPolicy(quotaPolicy);
        p2.setConditions(condition2);
        pipelines.add(p2);
        ///////////pipeline item 2 end//////

        policy.setPipelines(pipelines);
        return policy;
    }

    private Policy getApplicationPolicy(String policyName) {
        ApplicationPolicy policy = new ApplicationPolicy(policyName);
        policy.setDescription(policyName);
        policy.setDescription("Application policy Description");
        policy.setTenantId(4);

        BandwidthLimit defaultLimit = new BandwidthLimit();
        defaultLimit.setTimeUnit("min");
        defaultLimit.setUnitTime(5);
        defaultLimit.setDataAmount(600);
        defaultLimit.setDataUnit("KB");

        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setLimit(defaultLimit);
        defaultQuotaPolicy.setType(PolicyConstants.BANDWIDTH_TYPE);

        policy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return policy;
    }

    private Policy getSubscriptionPolicy(String policyName) {
        SubscriptionPolicy policy = new SubscriptionPolicy(policyName);
        policy.setDisplayName(policyName);
        policy.setDescription("Subscription policy Description");
        policy.setTenantId(6);
        policy.setBillingPlan("FREE");
        policy.setGraphQLMaxDepth(5);
        policy.setGraphQLMaxComplexity(100);
        policy.setMonetizationPlan(APIConstants.Monetization.FIXED_RATE);
        policy.setMonetizationPlanProperties(new HashMap<String, String>());
        RequestCountLimit defaultLimit = new RequestCountLimit();
        defaultLimit.setTimeUnit("min");
        defaultLimit.setUnitTime(50);
        defaultLimit.setRequestCount(800);
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setLimit(defaultLimit);
        defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
        policy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return policy;
    }

    @Test
    public void testGetAPIVersionsMatchingApiName() throws Exception {
        APIIdentifier apiId = new APIIdentifier("getAPIVersionsMatchingApiName", "getAPIVersionsMatchingApiName",
                "1.0.0");
        API api = new API(apiId);
        api.setContext("/getAPIVersionsMatchingApiName");
        api.setContextTemplate("/getAPIVersionsMatchingApiName/{version}");
        apiMgtDAO.addAPI(api, -1234);
        APIIdentifier apiId2 = new APIIdentifier("getAPIVersionsMatchingApiName", "getAPIVersionsMatchingApiName",
                "2.0.0");
        API api2 = new API(apiId2);
        api2.setContext("/getAPIVersionsMatchingApiName");
        api2.setContextTemplate("/getAPIVersionsMatchingApiName/{version}");
        apiMgtDAO.addAPI(api2, -1234);
        List<String> versionList = apiMgtDAO.getAPIVersionsMatchingApiName("getAPIVersionsMatchingApiName",
                "getAPIVersionsMatchingApiName");
        assertNotNull(versionList);
        assertTrue(versionList.contains("1.0.0"));
        assertTrue(versionList.contains("2.0.0"));
        apiMgtDAO.deleteAPI(apiId);
        apiMgtDAO.deleteAPI(apiId2);
    }

    @Test
    public void testCreateApplicationRegistrationEntry() throws Exception {
        Subscriber subscriber = new Subscriber("testCreateApplicationRegistrationEntry");
        subscriber.setTenantId(-1234);
        subscriber.setEmail("abc@wso2.com");
        subscriber.setSubscribedDate(new Date(System.currentTimeMillis()));
        apiMgtDAO.addSubscriber(subscriber, null);
        Policy applicationPolicy = getApplicationPolicy("testCreateApplicationRegistrationEntry");
        applicationPolicy.setTenantId(-1234);
        apiMgtDAO.addApplicationPolicy((ApplicationPolicy) applicationPolicy);
        Application application = new Application("testCreateApplicationRegistrationEntry", subscriber);
        application.setTier("testCreateApplicationRegistrationEntry");
        application.setId(apiMgtDAO.addApplication(application, "testCreateApplicationRegistrationEntry"));

        ApplicationRegistrationWorkflowDTO applicationRegistrationWorkflowDTO = new
                ApplicationRegistrationWorkflowDTO();
        applicationRegistrationWorkflowDTO.setApplication(application);
        applicationRegistrationWorkflowDTO.setKeyType("PRODUCTION");
        applicationRegistrationWorkflowDTO.setDomainList("*");
        applicationRegistrationWorkflowDTO.setWorkflowReference(UUID.randomUUID().toString());
        applicationRegistrationWorkflowDTO.setValidityTime(100L);
        applicationRegistrationWorkflowDTO.setExternalWorkflowReference(UUID.randomUUID().toString());
        applicationRegistrationWorkflowDTO.setStatus(WorkflowStatus.CREATED);
        applicationRegistrationWorkflowDTO.setKeyManager("Default");
        apiMgtDAO.addWorkflowEntry(applicationRegistrationWorkflowDTO);
        OAuthAppRequest oAuthAppRequest = new OAuthAppRequest();
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setJsonString("");
        oAuthApplicationInfo.addParameter("tokenScope", "deafault");
        oAuthAppRequest.setOAuthApplicationInfo(oAuthApplicationInfo);
        applicationRegistrationWorkflowDTO.setAppInfoDTO(oAuthAppRequest);
        APIIdentifier apiId = new APIIdentifier("testCreateApplicationRegistrationEntry",
                "testCreateApplicationRegistrationEntry", "1.0.0");
        API api = new API(apiId);
        api.setContext("/testCreateApplicationRegistrationEntry");
        api.setContextTemplate("/testCreateApplicationRegistrationEntry/{version}");
        apiMgtDAO.addAPI(api, -1234);
        APIIdentifier apiId1 = new APIIdentifier("testCreateApplicationRegistrationEntry1",
                "testCreateApplicationRegistrationEntry1", "1.0.0");
        API api1 = new API(apiId1);
        api1.setContext("/testCreateApplicationRegistrationEntry1");
        api1.setContextTemplate("/testCreateApplicationRegistrationEntry1/{version}");
        apiMgtDAO.addAPI(api1, -1234);
        apiMgtDAO.createApplicationRegistrationEntry(applicationRegistrationWorkflowDTO, false);
        ApplicationRegistrationWorkflowDTO retrievedApplicationRegistrationWorkflowDTO = new
                ApplicationRegistrationWorkflowDTO();
        retrievedApplicationRegistrationWorkflowDTO.setExternalWorkflowReference(applicationRegistrationWorkflowDTO
                .getExternalWorkflowReference());
        apiMgtDAO.populateAppRegistrationWorkflowDTO(retrievedApplicationRegistrationWorkflowDTO);
        ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(api);
        ApiTypeWrapper apiTypeWrapper1 = new ApiTypeWrapper(api1);
        apiMgtDAO.addSubscription(apiTypeWrapper, application.getId(), APIConstants.SubscriptionStatus.ON_HOLD,subscriber.getName());
        int subsId = apiMgtDAO.addSubscription(apiTypeWrapper1, application.getId(),
                APIConstants.SubscriptionStatus.ON_HOLD, subscriber.getName());
        assertTrue(apiMgtDAO.isContextExist(api.getContext()));
        assertTrue(api.getContext().equals(apiMgtDAO.getAPIContext(apiId)));
        apiMgtDAO.removeSubscription(apiId, application.getId());
        apiMgtDAO.removeSubscriptionById(subsId);
        apiMgtDAO.deleteAPI(apiId);
        apiMgtDAO.deleteAPI(apiId1);
        assertNotNull(apiMgtDAO.getWorkflowReference(application.getName(), subscriber.getName()));
        applicationRegistrationWorkflowDTO.setStatus(WorkflowStatus.APPROVED);
        apiMgtDAO.updateWorkflowStatus(applicationRegistrationWorkflowDTO);
        assertNotNull(apiMgtDAO.retrieveWorkflow(applicationRegistrationWorkflowDTO.getExternalWorkflowReference
                ()));
        assertNotNull(apiMgtDAO.retrieveWorkflowFromInternalReference(applicationRegistrationWorkflowDTO
                .getWorkflowReference(), applicationRegistrationWorkflowDTO.getWorkflowType()));
        apiMgtDAO.removeWorkflowEntry(applicationRegistrationWorkflowDTO.getExternalWorkflowReference(),
                applicationRegistrationWorkflowDTO.getWorkflowType());
        apiMgtDAO.deleteApplicationKeyMappingByApplicationIdAndType(application.getId(),"PRODUCTION");
        apiMgtDAO.deleteApplicationRegistration(application.getId(), "PRODUCTION",
                APIConstants.KeyManager.DEFAULT_KEY_MANAGER);
        apiMgtDAO.deleteApplication(application);
        apiMgtDAO.removeThrottlePolicy(PolicyConstants.POLICY_LEVEL_APP, "testCreateApplicationRegistrationEntry",
                -1234);
        deleteSubscriber(subscriber.getId());
    }



    @Test
    public void testDeleteSubscriptionsForapiId() throws Exception {

        Subscriber subscriber = new Subscriber("testCreateApplicationRegistrationEntry");
        subscriber.setTenantId(-1234);
        subscriber.setEmail("abc@wso2.com");
        subscriber.setSubscribedDate(new Date(System.currentTimeMillis()));
        apiMgtDAO.addSubscriber(subscriber, null);
        Policy applicationPolicy = getApplicationPolicy("testCreateApplicationRegistrationEntry");
        SubscriptionPolicy subscriptionPolicy = (SubscriptionPolicy) getSubscriptionPolicy
                ("testCreateApplicationRegistrationEntry");
        subscriptionPolicy.setMonetizationPlan(APIConstants.Monetization.FIXED_RATE);
        apiMgtDAO.addSubscriptionPolicy((SubscriptionPolicy) subscriptionPolicy);
        applicationPolicy.setTenantId(-1234);
        apiMgtDAO.addApplicationPolicy((ApplicationPolicy) applicationPolicy);
        Application application = new Application("testCreateApplicationRegistrationEntry", subscriber);
        application.setTier("testCreateApplicationRegistrationEntry");
        application.setId(apiMgtDAO.addApplication(application, "testCreateApplicationRegistrationEntry"));
        application.setDescription("updated description");
        apiMgtDAO.updateApplication(application);
        assertEquals(apiMgtDAO.getApplicationById(application.getId()).getDescription(), "updated description");
        APIIdentifier apiId = new APIIdentifier("testCreateApplicationRegistrationEntry",
                "testCreateApplicationRegistrationEntry", "1.0.0");
        API api = new API(apiId);
        api.setContext("/testCreateApplicationRegistrationEntry");
        api.setContextTemplate("/testCreateApplicationRegistrationEntry/{version}");
        APIPolicy apiPolicy = (APIPolicy) getPolicyAPILevelPerUser("testCreateApplicationRegistrationEntry");
        api.setApiLevelPolicy(apiPolicy.getPolicyName());
        apiMgtDAO.addAPI(api, -1234);
        apiId.setTier(subscriptionPolicy.getPolicyName());
        ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(api);
        int subsId = apiMgtDAO.addSubscription(apiTypeWrapper, application.getId(),
                APIConstants.SubscriptionStatus.ON_HOLD, subscriber.getName());
        assertTrue(apiMgtDAO.getApplicationsByTier(subscriptionPolicy.getPolicyName()).length > 0);
        String subStatus = apiMgtDAO.getSubscriptionStatusById(subsId);
        assertEquals(subStatus, APIConstants.SubscriptionStatus.ON_HOLD);
        SubscribedAPI subscribedAPI = apiMgtDAO.getSubscriptionById(subsId);
        String clientIdProduction = UUID.randomUUID().toString();
        String clientIdSandbox = UUID.randomUUID().toString();
        apiMgtDAO.createApplicationKeyTypeMappingForManualClients(APIConstants.API_KEY_TYPE_PRODUCTION, application
                .getId(), subscriber.getName(), clientIdProduction, "Default", UUID.randomUUID().toString());
        apiMgtDAO.createApplicationKeyTypeMappingForManualClients(APIConstants.API_KEY_TYPE_SANDBOX, application
                .getId(), subscriber.getName(), clientIdSandbox, "Default", UUID.randomUUID().toString());
        int appIdProduction = insertConsumerApp(clientIdProduction, application.getName(), subscriber.getName());
        int appIdSandBox = insertConsumerApp(clientIdSandbox, application.getName(), subscriber.getName());
        String tokenProduction = UUID.randomUUID().toString();
        String tokenSandBox = UUID.randomUUID().toString();
        String tokenIdProduction = insertAccessTokenForApp(appIdProduction, subscriber.getName(), tokenProduction);
        String tokenIdSandbox = insertAccessTokenForApp(appIdSandBox, subscriber.getName(), tokenSandBox);
        insertTokenScope(tokenIdProduction, "default");
        insertTokenScope(tokenIdSandbox, "default");
        assertTrue(apiMgtDAO.getSubscriptionCount(subscriber, application.getName(), null) > 0);
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        Mockito.when(keyManager.retrieveApplication(clientIdProduction)).thenReturn(oAuthApplicationInfo);
        Mockito.when(keyManager.retrieveApplication(clientIdSandbox)).thenReturn(oAuthApplicationInfo);
        assertTrue(apiMgtDAO.getSubscribedAPIs(subscriber, null).size() > 0);
        assertEquals(subscribedAPI.getSubCreatedStatus(), APIConstants.SubscriptionCreatedStatus.SUBSCRIBE);
        assertEquals(subscribedAPI.getApiId(), apiId);
        assertEquals(subscribedAPI.getApplication().getId(), application.getId());
        SubscribedAPI subscribedAPIFromUuid = apiMgtDAO.getSubscriptionByUUID(subscribedAPI.getUUID());
        assertEquals(subscribedAPIFromUuid.getSubCreatedStatus(), APIConstants.SubscriptionCreatedStatus.SUBSCRIBE);
        assertEquals(subscribedAPIFromUuid.getApiId(), apiId);
        assertEquals(subscribedAPIFromUuid.getApplication().getId(), application.getId());
        List<AccessTokenInfo> accessTokenInfoList = apiMgtDAO.getAccessTokenListForUser(subscriber.getName(),
                application.getName(), subscriber.getName());
        assertTrue(accessTokenInfoList.size()==2);
        apiMgtDAO.updateApplicationStatus(application.getId(), APIConstants.ApplicationStatus.APPLICATION_APPROVED);
        String status = apiMgtDAO.getApplicationStatus("testCreateApplicationRegistrationEntry",
                "testCreateApplicationRegistrationEntry");
        assertEquals(status, APIConstants.ApplicationStatus.APPLICATION_APPROVED);
        boolean applicationExist = apiMgtDAO.isApplicationExist(application.getName(), subscriber.getName(), null);
        assertTrue(applicationExist);
        assertNotNull(apiMgtDAO.getPaginatedSubscribedAPIs(subscriber, application.getName(), 0, 10, null));
        Set<SubscribedAPI> subscribedAPIS = apiMgtDAO.getSubscribedAPIs(subscriber, application.getName(), null);
        assertEquals(subscribedAPIS.size(), 1);
        apiMgtDAO.updateSubscription(apiId, APIConstants.SubscriptionStatus.BLOCKED, application.getId());
        subscribedAPI.setSubStatus(APIConstants.SubscriptionStatus.REJECTED);
        apiMgtDAO.updateSubscription(subscribedAPI);
        assertTrue(apiMgtDAO.hasSubscription(subscriptionPolicy.getPolicyName(), subscriber.getName(),
                PolicyConstants.POLICY_LEVEL_SUB));
        assertTrue(apiMgtDAO.hasSubscription(applicationPolicy.getPolicyName(), subscriber.getName(),
                PolicyConstants.POLICY_LEVEL_APP));
        assertTrue(apiMgtDAO.hasSubscription(apiPolicy.getPolicyName(), subscriber.getName(),
                PolicyConstants.POLICY_LEVEL_API));
        assertTrue(apiPolicy.getPolicyName().equals(apiMgtDAO.getAPILevelTier(apiMgtDAO.getAPIID(apiId, null))));
        apiMgtDAO.recordAPILifeCycleEvent(apiId, "CREATED", "PUBLISHED", "testCreateApplicationRegistrationEntry",
                -1234);
        apiMgtDAO.updateDefaultAPIPublishedVersion(apiId, APIConstants.PUBLISHED, APIConstants.CREATED);
        assertTrue(apiMgtDAO.getConsumerKeys(apiId).length > 0);
        apiMgtDAO.removeAllSubscriptions(apiId);
        assertTrue(apiMgtDAO.getAPINamesMatchingContext(api.getContext()).size() > 0);
        apiMgtDAO.deleteAPI(apiId);
        apiMgtDAO.deleteApplication(application);
        apiMgtDAO.removeThrottlePolicy(PolicyConstants.POLICY_LEVEL_APP, "testCreateApplicationRegistrationEntry",
                -1234);
        apiMgtDAO.deleteApplicationKeyMappingByConsumerKey(clientIdProduction);
        apiMgtDAO.deleteApplicationMappingByConsumerKey(clientIdSandbox);
        deleteAccessTokenForApp(appIdProduction);
        deleteAccessTokenForApp(appIdSandBox);
        deleteConsumerApp(clientIdProduction);
        deleteConsumerApp(clientIdSandbox);
        deleteSubscriber(subscriber.getId());
    }


    @Test
    public void testAddAndGetSubscriptionPolicy() throws Exception {
        SubscriptionPolicy subscriptionPolicy = (SubscriptionPolicy) getSubscriptionPolicy
                ("testAddAndGetSubscriptionPolicy");
        String customAttributes = "{api:abc}";
        subscriptionPolicy.setTenantId(-1234);
        subscriptionPolicy.setCustomAttributes(customAttributes.getBytes());
        subscriptionPolicy.setMonetizationPlan(APIConstants.Monetization.FIXED_RATE);
        subscriptionPolicy.setMonetizationPlanProperties(new HashMap<String, String>());
        apiMgtDAO.addSubscriptionPolicy(subscriptionPolicy);
        SubscriptionPolicy retrievedPolicy = apiMgtDAO.getSubscriptionPolicy(subscriptionPolicy.getPolicyName(), -1234);
        SubscriptionPolicy retrievedPolicyFromUUID = apiMgtDAO.getSubscriptionPolicyByUUID(retrievedPolicy.getUUID());
        assertEquals(retrievedPolicy.getDescription(), retrievedPolicyFromUUID.getDescription());
        assertEquals(retrievedPolicy.getDisplayName(), retrievedPolicyFromUUID.getDisplayName());
        assertEquals(retrievedPolicy.getRateLimitCount(), retrievedPolicyFromUUID.getRateLimitCount());
        assertEquals(retrievedPolicy.getRateLimitTimeUnit(), retrievedPolicyFromUUID.getRateLimitTimeUnit());
        retrievedPolicyFromUUID.setCustomAttributes(customAttributes.getBytes());
        apiMgtDAO.updateSubscriptionPolicy(retrievedPolicyFromUUID);
        retrievedPolicyFromUUID.setPolicyName(null);
        apiMgtDAO.updateSubscriptionPolicy(retrievedPolicyFromUUID);
        SubscriptionPolicy[] subscriptionPolicies = apiMgtDAO.getSubscriptionPolicies(-1234);
        apiMgtDAO.updateThrottleTierPermissions(subscriptionPolicy.getPolicyName(), "allow", "internal/everyone",
                -1234);
        Set<TierPermissionDTO> throttleTierPermissions = apiMgtDAO.getThrottleTierPermissions(-1234);
        for (TierPermissionDTO tierPermissionDTO : throttleTierPermissions) {
            if (subscriptionPolicy.getPolicyName().equals(tierPermissionDTO.getTierName())) {
                assertTrue(true);
                break;
            }
        }
        apiMgtDAO.updateThrottleTierPermissions(subscriptionPolicy.getPolicyName(), "deny", "internal/everyone", -1234);
        assertNotNull(apiMgtDAO.getThrottleTierPermission(subscriptionPolicy.getPolicyName(), -1234));
        assertTrue(subscriptionPolicies.length > 0);
        apiMgtDAO.setPolicyDeploymentStatus(PolicyConstants.POLICY_LEVEL_SUB, subscriptionPolicy.getPolicyName(), -1234,
                true);
        assertTrue(apiMgtDAO.getPolicyNames(PolicyConstants.POLICY_LEVEL_SUB, "admin").length > 0);
        assertTrue(apiMgtDAO.isPolicyDeployed(PolicyConstants.POLICY_LEVEL_SUB, -1234, subscriptionPolicy
                .getPolicyName()));
        assertTrue(apiMgtDAO.isPolicyExist(PolicyConstants.POLICY_LEVEL_SUB, -1234, subscriptionPolicy.getPolicyName()));
        apiMgtDAO.removeThrottlePolicy(PolicyConstants.POLICY_LEVEL_SUB, "testAddAndGetSubscriptionPolicy", -1234);
    }


    @Test
    public void testAddAndGetApplicationPolicy() throws Exception {
        ApplicationPolicy applicationPolicy = (ApplicationPolicy) getApplicationPolicy
                ("testAddAndGetSubscriptionPolicy");
        String customAttributes = "{api:abc}";
        applicationPolicy.setTenantId(-1234);
        applicationPolicy.setCustomAttributes(customAttributes.getBytes());
        apiMgtDAO.addApplicationPolicy(applicationPolicy);
        ApplicationPolicy retrievedPolicy = apiMgtDAO.getApplicationPolicy(applicationPolicy.getPolicyName(), -1234);
        ApplicationPolicy retrievedPolicyFromUUID = apiMgtDAO.getApplicationPolicyByUUID(retrievedPolicy.getUUID());
        assertEquals(retrievedPolicy.getDescription(), retrievedPolicyFromUUID.getDescription());
        assertEquals(retrievedPolicy.getDisplayName(), retrievedPolicyFromUUID.getDisplayName());
        retrievedPolicyFromUUID.setCustomAttributes(customAttributes.getBytes());
        apiMgtDAO.updateApplicationPolicy(retrievedPolicyFromUUID);
        ApplicationPolicy[] applicationPolicies = apiMgtDAO.getApplicationPolicies(-1234);
        assertTrue(applicationPolicies.length > 0);
        apiMgtDAO.setPolicyDeploymentStatus(PolicyConstants.POLICY_LEVEL_APP, applicationPolicy.getPolicyName(), -1234,
                true);
        assertTrue(apiMgtDAO.getPolicyNames(PolicyConstants.POLICY_LEVEL_APP, "admin").length > 0);
        assertTrue(apiMgtDAO.isPolicyDeployed(PolicyConstants.POLICY_LEVEL_APP, -1234, applicationPolicy
                .getPolicyName()));
        assertTrue(apiMgtDAO.isPolicyExist(PolicyConstants.POLICY_LEVEL_APP, -1234, applicationPolicy.getPolicyName()));
        apiMgtDAO.removeThrottlePolicy(PolicyConstants.POLICY_LEVEL_APP, "testAddAndGetSubscriptionPolicy", -1234);
    }

    @Test
    public void testAddAndGetGlobalPolicy() throws Exception {
        GlobalPolicy globalPolicy = new GlobalPolicy("testAddAndGetGlobalPolicy");
        globalPolicy.setTenantId(-1234);
        globalPolicy.setKeyTemplate("$user");
        globalPolicy.setSiddhiQuery("Select From 1");
        apiMgtDAO.addGlobalPolicy(globalPolicy);
        GlobalPolicy retrievedGlobalPolicyFromName = apiMgtDAO.getGlobalPolicy("testAddAndGetGlobalPolicy");
        assertTrue(apiMgtDAO.isKeyTemplatesExist(globalPolicy));
        GlobalPolicy retrievedFromUUID = apiMgtDAO.getGlobalPolicyByUUID(retrievedGlobalPolicyFromName.getUUID());
        assertEquals(retrievedGlobalPolicyFromName.getKeyTemplate(), retrievedFromUUID.getKeyTemplate());
        assertTrue(apiMgtDAO.getGlobalPolicies(-1234).length > 0);
        assertTrue(apiMgtDAO.getGlobalPolicyKeyTemplates(-1234).contains("$user"));
        apiMgtDAO.updateGlobalPolicy(globalPolicy);
        retrievedFromUUID.setPolicyName(null);
        apiMgtDAO.updateGlobalPolicy(retrievedFromUUID);
        apiMgtDAO.setPolicyDeploymentStatus(PolicyConstants.POLICY_LEVEL_GLOBAL, globalPolicy.getPolicyName(), -1234,
                true);
        assertTrue(apiMgtDAO.isPolicyDeployed(PolicyConstants.POLICY_LEVEL_GLOBAL, -1234, globalPolicy.getPolicyName
                ()));
        assertTrue(apiMgtDAO.getPolicyNames(PolicyConstants.POLICY_LEVEL_GLOBAL, "admin").length > 0);
        assertTrue(apiMgtDAO.isPolicyExist(PolicyConstants.POLICY_LEVEL_GLOBAL, -1234, globalPolicy.getPolicyName()));
        apiMgtDAO.removeThrottlePolicy(PolicyConstants.POLICY_LEVEL_GLOBAL, "testAddAndGetGlobalPolicy", -1234);
    }

    @Test
    public void testAddUpdateDeleteBlockCondition() throws Exception {
        Subscriber subscriber = new Subscriber("blockuser1");
        subscriber.setTenantId(-1234);
        subscriber.setEmail("abc@wso2.com");
        subscriber.setSubscribedDate(new Date(System.currentTimeMillis()));
        apiMgtDAO.addSubscriber(subscriber, null);
        Policy applicationPolicy = getApplicationPolicy("testAddUpdateDeleteBlockCondition");
        applicationPolicy.setTenantId(-1234);
        apiMgtDAO.addApplicationPolicy((ApplicationPolicy) applicationPolicy);
        Application application = new Application("testAddUpdateDeleteBlockCondition", subscriber);
        application.setTier("testAddUpdateDeleteBlockCondition");
        application.setId(apiMgtDAO.addApplication(application, "blockuser1"));
        APIIdentifier apiId = new APIIdentifier("testAddUpdateDeleteBlockCondition",
                "testAddUpdateDeleteBlockCondition", "1.0.0");
        API api = new API(apiId);
        api.setContext("/testAddUpdateDeleteBlockCondition");
        api.setContextTemplate("/testAddUpdateDeleteBlockCondition/{version}");
        apiMgtDAO.addAPI(api, -1234);
        BlockConditionsDTO apiBlockConditionDto = new BlockConditionsDTO();
        apiBlockConditionDto.setConditionValue("/testAddUpdateDeleteBlockCondition");
        apiBlockConditionDto.setConditionType(APIConstants.BLOCKING_CONDITIONS_API);
        apiBlockConditionDto.setUUID(UUID.randomUUID().toString());
        apiBlockConditionDto.setTenantDomain("carbon.super");
        BlockConditionsDTO apiUUID = apiMgtDAO.addBlockConditions(apiBlockConditionDto);
        BlockConditionsDTO applicationBlockCondition = new BlockConditionsDTO();
        applicationBlockCondition.setConditionValue("blockuser1:testAddUpdateDeleteBlockCondition");
        applicationBlockCondition.setConditionType(APIConstants.BLOCKING_CONDITIONS_APPLICATION);
        applicationBlockCondition.setUUID(UUID.randomUUID().toString());
        applicationBlockCondition.setTenantDomain("carbon.super");
        BlockConditionsDTO applicationUUID = apiMgtDAO.addBlockConditions(applicationBlockCondition);
        assertNotNull(applicationUUID);
        BlockConditionsDTO ipBlockcondition = new BlockConditionsDTO();
        ipBlockcondition.setConditionValue("127.0.0.1");
        ipBlockcondition.setConditionType(APIConstants.BLOCKING_CONDITIONS_IP);
        ipBlockcondition.setUUID(UUID.randomUUID().toString());
        ipBlockcondition.setTenantDomain("carbon.super");
        BlockConditionsDTO ipUUID = apiMgtDAO.addBlockConditions(ipBlockcondition);
        assertNotNull(ipUUID);
        BlockConditionsDTO userBlockcondition = new BlockConditionsDTO();
        userBlockcondition.setConditionValue("admin");
        userBlockcondition.setConditionType(APIConstants.BLOCKING_CONDITIONS_USER);
        userBlockcondition.setUUID(UUID.randomUUID().toString());
        userBlockcondition.setTenantDomain("carbon.super");
        BlockConditionsDTO userUUID = apiMgtDAO.addBlockConditions(userBlockcondition);
        assertNotNull(apiMgtDAO.getBlockConditionByUUID(apiUUID.getUUID()));
        assertNotNull(userUUID);
        assertNotNull(apiMgtDAO
                .updateBlockConditionState(apiMgtDAO.getBlockConditionByUUID(userUUID.getUUID()).getConditionId(),
                        "FALSE"));
        assertNotNull(apiMgtDAO.updateBlockConditionStateByUUID(userUUID.getUUID(), "FALSE"));
        apiMgtDAO.deleteBlockCondition(apiMgtDAO.getBlockConditionByUUID(userUUID.getUUID()).getConditionId());
        apiMgtDAO.getBlockCondition(apiMgtDAO.getBlockConditionByUUID(ipUUID.getUUID()).getConditionId());

        List<BlockConditionsDTO> blockConditions = apiMgtDAO.getBlockConditions("carbon.super");
        for (BlockConditionsDTO blockConditionsDTO : blockConditions) {
            apiMgtDAO.deleteBlockConditionByUUID(blockConditionsDTO.getUUID());
        }
        apiMgtDAO.deleteApplication(application);
        apiMgtDAO.removeThrottlePolicy(PolicyConstants.POLICY_LEVEL_APP, applicationPolicy.getPolicyName(), -1234);
        apiMgtDAO.deleteAPI(apiId);
        deleteSubscriber(subscriber.getId());
    }

    @Test
    public void testAddUpdateDeleteAlert() throws Exception {
        apiMgtDAO.addAlertTypesConfigInfo("admin","admin@abc.com,admin@cde.com","1,2,3","admin-dashboard");
        apiMgtDAO.addAlertTypesConfigInfo("admin","admin@abc.com,admin@cde.com","1,2,3","publisher");
        List<String> retrievedEmailList = apiMgtDAO.retrieveSavedEmailList("admin", "admin-dashboard");
        assertTrue(retrievedEmailList.contains("admin@abc.com"));
        assertTrue(retrievedEmailList.contains("admin@cde.com"));
        assertTrue(apiMgtDAO.getAllAlertTypesByStakeHolder("admin-dashboard").size() > 0);
        assertTrue(apiMgtDAO.getAllAlertTypesByStakeHolder("publisher").size() > 0);
        assertTrue(apiMgtDAO.getSavedAlertTypesIdsByUserNameAndStakeHolder("admin","admin-dashboard").contains(1));
        apiMgtDAO.unSubscribeAlerts("admin","admin-dashboard");
        apiMgtDAO.unSubscribeAlerts("admin","publisher");
    }

    @Test
    public void testAddAndGetApi() throws Exception{
        APIIdentifier apiIdentifier = new APIIdentifier("testAddAndGetApi",
                "testAddAndGetApi", "1.0.0");
        API api = new API(apiIdentifier);
        api.setContext("/testAddAndGetApi");
        api.setContextTemplate("/testAddAndGetApi/{version}");
        api.setUriTemplates(getUriTemplateSet());
        api.setScopes(getScopes());
        api.setStatus(APIConstants.PUBLISHED);
        api.setAsDefaultVersion(true);
        int apiID = apiMgtDAO.addAPI(api, -1234);
        apiMgtDAO.addURITemplates(apiID, api, -1234);
        apiMgtDAO.updateAPI(api);
        apiMgtDAO.updateURITemplates(api, -1234);
        Set<APIStore> apiStoreSet = new HashSet<APIStore>();
        APIStore apiStore = new APIStore();
        apiStore.setDisplayName("wso2");
        apiStore.setEndpoint("http://localhost:9433/store");
        apiStore.setName("wso2");
        apiStore.setType("wso2");
        apiStoreSet.add(apiStore);
        apiMgtDAO.addExternalAPIStoresDetails(apiIdentifier,apiStoreSet);
        assertTrue(apiMgtDAO.getExternalAPIStoresDetails(apiIdentifier).size()>0);
        apiMgtDAO.deleteExternalAPIStoresDetails(apiIdentifier, apiStoreSet);
        apiMgtDAO.updateExternalAPIStoresDetails(apiIdentifier, Collections.<APIStore>emptySet());
        assertTrue(apiMgtDAO.getExternalAPIStoresDetails(apiIdentifier).size()==0);
        apiMgtDAO.deleteAPI(apiIdentifier);
    }

    @Test
    public void testAddAndConvertNullThrottlingTiers() throws APIManagementException {

        //Adding an API with a null THROTTLING_TIER should automatically convert it to Unlimited
        APIIdentifier apiIdentifier = new APIIdentifier("testAddAndGetApi", "testAddAndGetApi", "1.0.0");
        API api = new API(apiIdentifier);
        api.setContext("/testAddAndGetApi");
        api.setContextTemplate("/testAddAndGetApi/{version}");
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
        uriTemplates.add(getUriTemplate("/abc", "GET", "Any", "read", null));
        api.setUriTemplates(uriTemplates);
        api.setScopes(getScopes());
        api.setStatus(APIConstants.PUBLISHED);
        api.setAsDefaultVersion(true);
        int apiId = apiMgtDAO.addAPI(api, -1234);
        apiMgtDAO.addURITemplates(apiId, api, -1234);
        HashMap<String, String> result1 = apiMgtDAO.getURITemplatesPerAPIAsString(apiIdentifier);
        Assert.assertTrue(result1.containsKey("/abc::GET::Any::Unlimited::abcd defgh fff"));

        //Change the inserted throttling tier back to Null and test the convertNullThrottlingTier method
        updateThrottlingTierToNull();
        apiMgtDAO.convertNullThrottlingTiers();
        HashMap<String, String> result2 = apiMgtDAO.getURITemplatesPerAPIAsString(apiIdentifier);
        Assert.assertTrue(result2.containsKey("/abc::GET::Any::Unlimited::abcd defgh fff"));
   }

    @Test
    public void testGetProviderByNameVersionTenant() throws APIManagementException, SQLException {
        final String apiProviderSuperTenant = "testUser1";
        final String apiProviderWSO2Tenant = "testUser1@wso2.test";

        final String apiName = "testAPI1";
        final String apiVersion = "1.0.0";
        try {
            apiMgtDAO.getAPIProviderByNameAndVersion(apiName, apiVersion, "");
            assertFalse("Should throw an exception when tenant value is blank string", true);
        } catch (APIManagementException ex){
            assertTrue(ex.getMessage().contains("cannot be null when fetching provider"));
        }

        try {
            apiMgtDAO.getAPIProviderByNameAndVersion(apiName, apiVersion, null);
            assertFalse("Should throw an exception when tenant value is null", true);
        } catch (APIManagementException ex){
            assertTrue(ex.getMessage().contains("cannot be null when fetching provider"));
        }

        String apiProviderSuperTenantResult = apiMgtDAO.getAPIProviderByNameAndVersion(apiName, apiVersion, APIConstants.SUPER_TENANT_DOMAIN);
        Assert.assertEquals(apiProviderSuperTenant, apiProviderSuperTenantResult);

        String apiProviderWSO2TenantResult = apiMgtDAO.getAPIProviderByNameAndVersion(apiName, apiVersion, "wso2.test");
        Assert.assertEquals(apiProviderWSO2Tenant, apiProviderWSO2TenantResult);

    }

    private void deleteSubscriber(int subscriberId) throws APIManagementException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String query = "DELETE FROM AM_SUBSCRIBER WHERE SUBSCRIBER_ID = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, subscriberId);
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
    }

    private void updateThrottlingTierToNull() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String query = "UPDATE AM_API_URL_MAPPING SET THROTTLING_TIER=null where THROTTLING_TIER='Unlimited'";
            ps = conn.prepareStatement(query);
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
    }

    private int insertConsumerApp(String clientId, String appName, String username) throws SQLException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        int appId = 0;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String query = "INSERT INTO IDN_OAUTH_CONSUMER_APPS ( APP_NAME , CALLBACK_URL , CONSUMER_KEY , " +
                    "CONSUMER_SECRET ,OAUTH_VERSION , TENANT_ID , USERNAME ) VALUES (?,?,?,?,?,?,?)";
            ps = conn.prepareStatement(query);
            ps.setString(1, username + "_" + appName + "_" + APIConstants.API_KEY_TYPE_PRODUCTION);
            ps.setString(2, null);
            ps.setString(3, clientId);
            ps.setString(4, UUID.randomUUID().toString());
            ps.setString(5, "2.0");
            ps.setInt(6, -1234);
            ps.setString(7, username);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            while (rs.next()) {
                appId = Integer.parseInt(rs.getString(1));
            }
            conn.commit();
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return appId;
    }

    private void deleteConsumerApp(String clientId) throws SQLException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);

            String query = "DELETE FROM IDN_OAUTH_CONSUMER_APPS WHERE CONSUMER_KEY = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, clientId);
            ps.executeUpdate();
            conn.commit();
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
    }

    private String insertAccessTokenForApp(int clientId, String user, String token) throws SQLException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String tokenId = UUID.randomUUID().toString();
            String query = "INSERT INTO IDN_OAUTH2_ACCESS_TOKEN (TOKEN_ID, ACCESS_TOKEN, REFRESH_TOKEN, " +
                    "CONSUMER_KEY_ID, AUTHZ_USER, TENANT_ID, USER_TYPE, GRANT_TYPE, VALIDITY_PERIOD, " +
                    "REFRESH_TOKEN_VALIDITY_PERIOD, TOKEN_STATE,TIME_CREATED,REFRESH_TOKEN_TIME_CREATED) VALUES ('" +
                    tokenId + "'," + " '" + token + "'," + " 'aa', ?,?, " +
                    "'-1234','" + APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION + "', 'client_credentials', '3600'," +
                    " '3600', 'ACTIVE','2017-10-17','2017-10-17')";
            ps = conn.prepareStatement(query);
            ps.setInt(1, clientId);
            ps.setString(2, user);
            ps.executeUpdate();
            conn.commit();
            return tokenId;
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
    }

    private void deleteAccessTokenForApp(int clientId) throws SQLException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String query = "DELETE FROM IDN_OAUTH2_ACCESS_TOKEN WHERE CONSUMER_KEY_ID = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, clientId);
            ps.executeUpdate();
            conn.commit();
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
    }

    private void insertTokenScope(String tokenId, String scope) throws SQLException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            String query = "INSERT INTO IDN_OAUTH2_ACCESS_TOKEN_SCOPE (TOKEN_ID,TOKEN_SCOPE,TENANT_ID) " +
                    "VALUES(?,?,-1234)";
            ps = conn.prepareStatement(query);
            ps.setString(1, tokenId);
            ps.setString(2, scope);
            ps.executeUpdate();
            conn.commit();
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
    }

    private Set<URITemplate> getUriTemplateSet() {
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
        uriTemplates.add(getUriTemplate("/abc", "GET", "Any", "read",
                "Unlimited"));
        uriTemplates.add(getUriTemplate("/abc", "GET", "Any", null,
                "Unlimited"));
        return uriTemplates;
    }
    private URITemplate getUriTemplate(String resourceString,String httpVerb,String authType,String scope, String throtlingTier){
        URITemplate uriTemplate = new URITemplate();
        uriTemplate.setUriTemplate(resourceString);
        uriTemplate.setHTTPVerb(httpVerb);
        uriTemplate.setThrottlingTier(throtlingTier);
        uriTemplate.setAuthType(authType);
        uriTemplate.setMediationScript("abcd defgh fff");
        if (scope!= null){
            Scope scope1 = new Scope();
            scope1.setId("0");
            scope1.setDescription("");
            scope1.setKey(scope);
            scope1.setName(scope);
            scope1.setRoles("admin");
            uriTemplate.setScope(scope1);
            uriTemplate.setScopes(scope1);
        }
        return uriTemplate;
    }
    private Set<Scope> getScopes(){
        Scope scope1 = new Scope();
        scope1.setId("1");
        scope1.setDescription("");
        scope1.setKey("read");
        scope1.setName("read");
        scope1.setRoles("admin");
        Set<Scope> scopeSet = new HashSet<Scope>();
        scopeSet.add(scope1);
        return scopeSet;
    }

    @Test
    public void testGetGraphQLComplexityDetails() throws APIManagementException {
        APIIdentifier apiIdentifier = new APIIdentifier("RASIKA", "API1", "1.0.0");
        apiMgtDAO.addOrUpdateComplexityDetails(apiIdentifier, getGraphqlComplexityInfoDetails());
        GraphqlComplexityInfo graphqlComplexityInfo = apiMgtDAO.getComplexityDetails(apiIdentifier);
        assertEquals(2,graphqlComplexityInfo.getList().size());
    }

    private GraphqlComplexityInfo getGraphqlComplexityInfoDetails() {
        GraphqlComplexityInfo graphqlComplexityInfo = new GraphqlComplexityInfo();

        CustomComplexityDetails customComplexity1 = new CustomComplexityDetails();
        customComplexity1.setType("type1");
        customComplexity1.setField("field1");
        customComplexity1.setComplexityValue(5);

        CustomComplexityDetails customComplexity2 = new CustomComplexityDetails();
        customComplexity2.setType("type1");
        customComplexity2.setField("field2");
        customComplexity2.setComplexityValue(4);

        List<CustomComplexityDetails> list = new ArrayList<>();
        list.add(customComplexity1);
        list.add(customComplexity2);
        graphqlComplexityInfo.setList(list);

        return graphqlComplexityInfo;
    }
}
