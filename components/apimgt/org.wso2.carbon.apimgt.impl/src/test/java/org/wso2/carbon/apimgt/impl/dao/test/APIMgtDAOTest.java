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

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.LifeCycleEvent;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.api.model.policy.Condition;
import org.wso2.carbon.apimgt.api.model.policy.DateCondition;
import org.wso2.carbon.apimgt.api.model.policy.DateRangeCondition;
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
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class APIMgtDAOTest extends TestCase {

    public static ApiMgtDAO apiMgtDAO;

    @Override
    protected void setUp() throws Exception {
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        initializeDatabase  (dbConfigPath);
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl(config));
        APIMgtDBUtil.initialize();
        apiMgtDAO = ApiMgtDAO.getInstance();
        IdentityTenantUtil.setRealmService(new TestRealmService());
        String identityConfigPath = System.getProperty("IdentityConfigurationPath");
        IdentityConfigParser.getInstance(identityConfigPath);
    }

    private void initializeDatabase(String configFilePath) {

        InputStream in = null;
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

    public void testGetSubscribersOfProvider() throws Exception {
        Set<Subscriber> subscribers = apiMgtDAO.getInstance().getSubscribersOfProvider("SUMEDHA");
        assertNotNull(subscribers);
        assertTrue(subscribers.size() > 0);
    }

    public void testAccessKeyForAPI() throws Exception {
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        apiInfoDTO.setApiName("API1");
        apiInfoDTO.setProviderId("SUMEDHA");
        apiInfoDTO.setVersion("V1.0.0");
        String accessKey = apiMgtDAO.getAccessKeyForAPI("SUMEDHA", "APPLICATION1", apiInfoDTO, "PRODUCTION");
        assertNotNull(accessKey);
        assertTrue(accessKey.length() > 0);
    }

    public void testGetSubscribedAPIsOfUser() throws Exception {
        APIInfoDTO[] apis = apiMgtDAO.getSubscribedAPIsOfUser("SUMEDHA");
        assertNotNull(apis);
        assertTrue(apis.length > 1);
    }

    //Commented out due to identity version update and cannot use apiMgtDAO.validateKey to validate anymore
	/*public void testValidateApplicationKey() throws Exception {
		APIKeyValidationInfoDTO apiKeyValidationInfoDTO =
		                                                  apiMgtDAO.validateKey("/context1",
		                                                                        "V1.0.0", "test1",
		                                                                        "DEVELOPER");
		assertNotNull(apiKeyValidationInfoDTO);
		assertTrue(apiKeyValidationInfoDTO.isAuthorized());
		assertEquals("SUMEDHA", apiKeyValidationInfoDTO.getSubscriber());
		assertEquals("PRODUCTION", apiKeyValidationInfoDTO.getType());
		assertEquals("T1", apiKeyValidationInfoDTO.getTier());

		apiKeyValidationInfoDTO =
		                          apiMgtDAO.validateKey("/context1", "V1.0.0", "test2", "DEVELOPER");
		assertNotNull(apiKeyValidationInfoDTO);
		assertTrue(apiKeyValidationInfoDTO.isAuthorized());
		assertEquals("SUMEDHA", apiKeyValidationInfoDTO.getSubscriber());
		assertEquals("SANDBOX", apiKeyValidationInfoDTO.getType());
		assertEquals("T1", apiKeyValidationInfoDTO.getTier());

		apiKeyValidationInfoDTO = apiMgtDAO.validateKey("/deli2", "V1.0.0", "test3", "DEVELOPER");
		assertNotNull(apiKeyValidationInfoDTO);
		assertFalse(apiKeyValidationInfoDTO.isAuthorized());
	}*/


    public void testGetSubscribedUsersForAPI() throws Exception {
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        apiInfoDTO.setApiName("API1");
        apiInfoDTO.setProviderId("SUMEDHA");
        apiInfoDTO.setVersion("V1.0.0");
        APIKeyInfoDTO[] apiKeyInfoDTO = apiMgtDAO.getInstance().getSubscribedUsersForAPI(apiInfoDTO);
        assertNotNull(apiKeyInfoDTO);
        assertTrue(apiKeyInfoDTO.length > 1);
    }

    public void testGetSubscriber() throws Exception {
        Subscriber subscriber = apiMgtDAO.getInstance().getSubscriber("SUMEDHA");
        assertNotNull(subscriber);
        assertNotNull(subscriber.getName());
        assertNotNull(subscriber.getId());
    }

    public void testIsSubscribed() throws Exception {
        APIIdentifier apiIdentifier = new APIIdentifier("SUMEDHA", "API1", "V1.0.0");
        boolean isSubscribed = apiMgtDAO.isSubscribed(apiIdentifier, "SUMEDHA");
        assertTrue(isSubscribed);

        apiIdentifier = new APIIdentifier("P1", "API2", "V1.0.0");
        isSubscribed = apiMgtDAO.isSubscribed(apiIdentifier, "UDAYANGA");
        assertFalse(isSubscribed);
    }

    public void testGetAllAPIUsageByProvider() throws Exception {
        UserApplicationAPIUsage[] userApplicationAPIUsages = apiMgtDAO.getAllAPIUsageByProvider("SUMEDHA");
        assertNotNull(userApplicationAPIUsages);

    }

    public void testAddSubscription() throws Exception {
        APIIdentifier apiIdentifier = new APIIdentifier("SUMEDHA", "API1", "V1.0.0");
        apiIdentifier.setApplicationId("APPLICATION99");
        apiIdentifier.setTier("T1");
        API api = new API(apiIdentifier);
        apiMgtDAO.addSubscription(apiIdentifier, api.getContext(), 100, "UNBLOCKED", "admin");
    }

	/*
	public void testRegisterAccessToken() throws Exception {
		APIInfoDTO apiInfoDTO = new APIInfoDTO();
		apiInfoDTO.setApiName("API2");
		apiInfoDTO.setProviderId("PRABATH");
		apiInfoDTO.setVersion("V1.0.0");
		apiInfoDTO.setContext("/api2context");
//   IDENT UNUSED
//		apiMgtDAO.registerAccessToken("CON1", "APPLICATION3", "PRABATH",
//		                              MultitenantConstants.SUPER_TENANT_ID, apiInfoDTO, "SANDBOX");
//		String key1 =
//		              apiMgtDAO.getAccessKeyForAPI("PRABATH", "APPLICATION3", apiInfoDTO, "SANDBOX");
//		assertNotNull(key1);
//
//		apiMgtDAO.registerAccessToken("CON1", "APPLICATION3", "PRABATH",
//		                              MultitenantConstants.SUPER_TENANT_ID, apiInfoDTO,
//		                              "PRODUCTION");
//		String key2 =
//		              apiMgtDAO.getAccessKeyForAPI("PRABATH", "APPLICATION3", apiInfoDTO,
//		                                           "PRODUCTION");
//		assertNotNull(key2);
//
//		assertTrue(!key1.equals(key2));
	}
	*/

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
        assertEquals(APIStatus.CREATED.toString(), event.getNewStatus());
        assertEquals("hiranya", event.getUserId());

        apiMgtDAO.recordAPILifeCycleEvent(apiId, APIStatus.CREATED, APIStatus.PUBLISHED, "admin", -1234);
        apiMgtDAO.recordAPILifeCycleEvent(apiId, APIStatus.PUBLISHED, APIStatus.DEPRECATED, "admin", -1234);
        events = apiMgtDAO.getLifeCycleEvents(apiId);
        assertEquals(3, events.size());
    }

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

    public void testAddGetApplicationByNameWithUserNameNullGroupIdNull() throws Exception {
        Subscriber subscriber = new Subscriber("LA_F_APP_UN_GROUP_ID_NULL");
        subscriber.setEmail("laf@wso2.com");
        subscriber.setSubscribedDate(new Date());
        subscriber.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        apiMgtDAO.addSubscriber(subscriber, null);
        Application application = new Application("testApplication2", subscriber);
        int applicationId = apiMgtDAO.addApplication(application, subscriber.getName());
        application.setId(applicationId);
        assertTrue(applicationId > 0);
        assertNull(apiMgtDAO.getApplicationByName("testApplication2", null, null));

    }

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
        apiMgtDAO.makeKeysForwardCompatible("SUMEDHA", "API1", "V1.0.0", "V2.0.0", "/context1");
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
    public void testInsertApplicationPolicy() throws APIManagementException {
        String policyName = "TestInsertAppPolicy";
        apiMgtDAO.addApplicationPolicy((ApplicationPolicy) getApplicationPolicy(policyName));
    }

    public void testInsertSubscriptionPolicy() throws APIManagementException {
        String policyName = "TestInsertSubPolicy";
        apiMgtDAO.addSubscriptionPolicy((SubscriptionPolicy) getSubscriptionPolicy(policyName));
    }

    public void testInsertAPIPolicy() throws APIManagementException {
        String policyName = "TestInsertAPIPolicy";
        apiMgtDAO.addAPIPolicy((APIPolicy) getPolicyAPILevelPerUser(policyName));
    }

    public void testUpdateApplicationPolicy() throws APIManagementException {
        String policyName = "TestUpdateAppPolicy";
        ApplicationPolicy policy = (ApplicationPolicy) getApplicationPolicy(policyName);
        apiMgtDAO.addApplicationPolicy(policy);
        policy = (ApplicationPolicy) getApplicationPolicy(policyName);
        policy.setDescription("Updated application description");
        apiMgtDAO.updateApplicationPolicy(policy);
    }

    public void testUpdateSubscriptionPolicy() throws APIManagementException {
        String policyName = "TestUpdateSubPolicy";
        SubscriptionPolicy policy = (SubscriptionPolicy) getSubscriptionPolicy(policyName);
        apiMgtDAO.addSubscriptionPolicy(policy);
        policy = (SubscriptionPolicy) getSubscriptionPolicy(policyName);
        policy.setDescription("Updated subscription description");
        apiMgtDAO.updateSubscriptionPolicy(policy);
    }

    public void testUpdateAPIPolicy() throws APIManagementException {
        String policyName = "TestUpdateApiPolicy";
        APIPolicy policy = (APIPolicy) getPolicyAPILevelPerUser(policyName);
        apiMgtDAO.addAPIPolicy(policy);
        policy = (APIPolicy) getPolicyAPILevelPerUser(policyName);
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

        ArrayList<Condition> conditions =  new ArrayList<Condition>();

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

        JWTClaimsCondition jwtClaimsCondition1= new JWTClaimsCondition();
        jwtClaimsCondition1.setClaimUrl("test_url");
        jwtClaimsCondition1.setAttribute("test_attribute");
        conditions.add(jwtClaimsCondition1);

        p.setQuotaPolicy(quotaPolicy);
        p.setConditions(conditions);
        pipelines.add(p);

        policy.setPipelines(pipelines);
        apiMgtDAO.updateAPIPolicy(policy);
    }

    public void testDeletePolicy() throws APIManagementException {
        apiMgtDAO.removeThrottlePolicy("app", "Bronze", -1234);
    }

    public void testGetApplicationPolicies() throws APIManagementException {
        apiMgtDAO.getApplicationPolicies(-1234);
    }

    public void testGetSubscriptionPolicies() throws APIManagementException {
        apiMgtDAO.getSubscriptionPolicies(-1234);
    }

    public void testGetApiPolicies() throws APIManagementException {
        apiMgtDAO.getAPIPolicies(-1234);
    }

    public void testGetApplicationPolicy() throws APIManagementException {
        String policyName = "TestGetAppPolicy";
        apiMgtDAO.addApplicationPolicy((ApplicationPolicy) getApplicationPolicy(policyName));
        apiMgtDAO.getApplicationPolicy(policyName, 4);
    }

    public void testGetSubscriptionPolicy() throws APIManagementException {
        String policyName = "TestGetSubPolicy";
        apiMgtDAO.addSubscriptionPolicy((SubscriptionPolicy) getSubscriptionPolicy(policyName));
        apiMgtDAO.getSubscriptionPolicy(policyName, 6);
    }

    public void testGetApiPolicy() throws APIManagementException {
        String policyName = "TestGetAPIPolicy";
        apiMgtDAO.addAPIPolicy((APIPolicy) getPolicyAPILevelPerUser(policyName));
        apiMgtDAO.getAPIPolicy(policyName, -1234);
    }

    public void testValidateSubscriptionDetails() throws APIManagementException {

        Subscriber subscriber = new Subscriber("sub_user1");
        subscriber.setEmail("user1@wso2.com");
        subscriber.setSubscribedDate(new Date());
        subscriber.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        apiMgtDAO.addSubscriber(subscriber, null);

        Application application = new Application("APP-10", subscriber);
        int applicationId = apiMgtDAO.addApplication(application, subscriber.getName());

        APIIdentifier apiId = new APIIdentifier("provider1", "WSO2-Utils", "V1.0.0");
        apiId.setTier("T10");
        API api = new API(apiId);
        api.setContext("/wso2utils");
        api.setContextTemplate("/wso2utils/{version}");
        apiMgtDAO.addAPI(api, MultitenantConstants.SUPER_TENANT_ID);
        apiMgtDAO.addSubscription(apiId, api.getContext(), applicationId, "UNBLOCKED", "sub_user1");

        String policyName = "T10";
        SubscriptionPolicy policy = (SubscriptionPolicy) getSubscriptionPolicy(policyName);
        policy.setRateLimitCount(20);
        policy.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        apiMgtDAO.addSubscriptionPolicy(policy);

        APIKeyValidationInfoDTO infoDTO = new APIKeyValidationInfoDTO();
        apiMgtDAO.createApplicationKeyTypeMappingForManualClients("password","APP-10","sub_user1","clientId1");

        boolean validation = apiMgtDAO.validateSubscriptionDetails("/wso2utils","V1.0.0","clientId1", infoDTO);

        if(validation) {
            assertEquals(20, infoDTO.getSpikeArrestLimit());
        }else{
            assertTrue("Expected validation for subscription details - true, but found - "+ validation,validation);
        }

    }

    private Policy getPolicyAPILevelPerUser(String policyName){
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
        Pipeline p;
        QuotaPolicy quotaPolicy;
        List<Condition> condition;
        BandwidthLimit bandwidthLimit;
        RequestCountLimit requestCountLimit;
        pipelines = new ArrayList<Pipeline>();


        ///////////pipeline item 1 start//////
        p = new Pipeline();

        quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType(PolicyConstants.BANDWIDTH_TYPE);
        bandwidthLimit = new BandwidthLimit();
        bandwidthLimit.setTimeUnit("min");
        bandwidthLimit.setUnitTime(5);
        bandwidthLimit.setDataAmount(100);
        bandwidthLimit.setDataUnit("GB");
        quotaPolicy.setLimit(bandwidthLimit);

        condition =  new ArrayList<Condition>();
        HTTPVerbCondition verbCond = new HTTPVerbCondition();
        verbCond.setHttpVerb("POST");
        condition.add(verbCond);

        IPCondition ipCondition = new IPCondition(PolicyConstants.IP_SPECIFIC_TYPE);
        condition.add(ipCondition);


        DateRangeCondition dateRangeCondition = new DateRangeCondition();
        dateRangeCondition.setStartingDate("2016-01-03");
        dateRangeCondition.setEndingDate("2016-01-31");
        condition.add(dateRangeCondition);

        p.setQuotaPolicy(quotaPolicy);
        p.setConditions(condition);
        pipelines.add(p);
        ///////////pipeline item 1 end//////

        ///////////pipeline item 2 start//////
        p = new Pipeline();

        quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("requestCount");
        requestCountLimit = new RequestCountLimit();
        requestCountLimit.setTimeUnit("min");
        requestCountLimit.setUnitTime(50);
        requestCountLimit.setRequestCount(1000);
        quotaPolicy.setLimit(requestCountLimit);

        condition =  new ArrayList<Condition>();

        DateCondition dateCondition = new DateCondition();
        dateCondition.setSpecificDate("2016-01-02");
        condition.add(dateCondition);

        HeaderCondition headerCondition1 = new HeaderCondition();
        headerCondition1.setHeader("User-Agent");
        headerCondition1.setValue("Firefox");
        condition.add(headerCondition1);

        HeaderCondition headerCondition2 = new HeaderCondition();
        headerCondition2.setHeader("Accept-Ranges");
        headerCondition2.setValue("bytes");
        condition.add(headerCondition2);

        QueryParameterCondition queryParameterCondition1 = new QueryParameterCondition();
        queryParameterCondition1.setParameter("test1");
        queryParameterCondition1.setValue("testValue1");
        condition.add(queryParameterCondition1);

        QueryParameterCondition queryParameterCondition2 = new QueryParameterCondition();
        queryParameterCondition2.setParameter("test2");
        queryParameterCondition2.setValue("testValue2");
        condition.add(queryParameterCondition2);

        JWTClaimsCondition jwtClaimsCondition1= new JWTClaimsCondition();
        jwtClaimsCondition1.setClaimUrl("test_url");
        jwtClaimsCondition1.setAttribute("test_attribute");
        condition.add(jwtClaimsCondition1);

        JWTClaimsCondition jwtClaimsCondition2= new JWTClaimsCondition();
        jwtClaimsCondition2.setClaimUrl("test_url");
        jwtClaimsCondition2.setAttribute("test_attribute");
        condition.add(jwtClaimsCondition2);

        p.setQuotaPolicy(quotaPolicy);
        p.setConditions(condition);
        pipelines.add(p);
        ///////////pipeline item 2 end//////

        policy.setPipelines(pipelines);
        return policy;
    }

    private Policy getApplicationPolicy(String policyName){
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

    private Policy getSubscriptionPolicy(String policyName){
        SubscriptionPolicy policy = new SubscriptionPolicy(policyName);
        policy.setDisplayName(policyName);
        policy.setDescription("Subscription policy Description");
        policy.setTenantId(6);
        policy.setBillingPlan("FREE");
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
}
