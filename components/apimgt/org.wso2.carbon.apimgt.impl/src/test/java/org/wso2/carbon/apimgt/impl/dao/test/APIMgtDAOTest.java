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
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyInfoDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class APIMgtDAOTest extends TestCase {

	ApiMgtDAO apiMgtDAO;

	@Override
	protected void setUp() throws Exception {
		String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
		APIManagerConfiguration config = new APIManagerConfiguration();
		config.load(dbConfigPath);
		ServiceReferenceHolder.getInstance()
		                      .setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl(
		                                                                                                config));
		APIMgtDBUtil.initialize();
		apiMgtDAO = new ApiMgtDAO();
		IdentityTenantUtil.setRealmService(new TestRealmService());
		String identityConfigPath = System.getProperty("IdentityConfigurationPath");
		IdentityConfigParser.getInstance(identityConfigPath);
	}

	public void testGetSubscribersOfProvider() throws Exception {
		Set<Subscriber> subscribers = apiMgtDAO.getSubscribersOfProvider("SUMEDHA");
		assertNotNull(subscribers);
		assertTrue(subscribers.size() > 0);
	}

	public void testAccessKeyForAPI() throws Exception {
		APIInfoDTO apiInfoDTO = new APIInfoDTO();
		apiInfoDTO.setApiName("API1");
		apiInfoDTO.setProviderId("SUMEDHA");
		apiInfoDTO.setVersion("V1.0.0");
		String accessKey =
		                   apiMgtDAO.getAccessKeyForAPI("SUMEDHA", "APPLICATION1", apiInfoDTO,
		                                                "PRODUCTION");
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
		APIKeyInfoDTO[] apiKeyInfoDTO = apiMgtDAO.getSubscribedUsersForAPI(apiInfoDTO);
		assertNotNull(apiKeyInfoDTO);
		assertTrue(apiKeyInfoDTO.length > 1);
	}

	public void testGetSubscriber() throws Exception {
		Subscriber subscriber = ApiMgtDAO.getSubscriber("SUMEDHA");
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
		UserApplicationAPIUsage[] userApplicationAPIUsages =
		                                                     apiMgtDAO.getAllAPIUsageByProvider("SUMEDHA");
		assertNotNull(userApplicationAPIUsages);

	}

	public void testAddSubscription() throws Exception {
		APIIdentifier apiIdentifier = new APIIdentifier("SUMEDHA", "API1", "V1.0.0");
		apiIdentifier.setApplicationId("APPLICATION99");
		apiIdentifier.setTier("T1");
		API api = new API(apiIdentifier);
		apiMgtDAO.addSubscription(apiIdentifier, api.getContext(), 100, "UNBLOCKED");
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

		apiMgtDAO.addAPI(api,-1234);

		List<LifeCycleEvent> events = apiMgtDAO.getLifeCycleEvents(apiId);
		assertEquals(1, events.size());
		LifeCycleEvent event = events.get(0);
		assertEquals(apiId, event.getApi());
		assertNull(event.getOldStatus());
		assertEquals(APIStatus.CREATED.toString(), event.getNewStatus());
		assertEquals("hiranya", event.getUserId());

		apiMgtDAO.recordAPILifeCycleEvent(apiId, APIStatus.CREATED, APIStatus.PUBLISHED, "admin");
		apiMgtDAO.recordAPILifeCycleEvent(apiId, APIStatus.PUBLISHED, APIStatus.DEPRECATED, "admin");
		events = apiMgtDAO.getLifeCycleEvents(apiId);
		assertEquals(3, events.size());
	}
	
	   public void testAddGetApplicationByNameGroupIdNull() throws Exception{
	        Subscriber subscriber = new Subscriber("LA_F_GROUP_ID_NULL");
	        subscriber.setEmail("laf@wso2.com");
	        subscriber.setSubscribedDate(new Date());
	        subscriber.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
	        apiMgtDAO.addSubscriber(subscriber, null);
	        Application application = new Application("testApplication", subscriber);
	        int applicationId = apiMgtDAO.addApplication(application, subscriber.getName());
	        application.setId(applicationId);
	        assertTrue(applicationId > 0);
	        this.checkApplicationsEqual(application, apiMgtDAO.getApplicationByName("testApplication", subscriber.getName(), null));
	        
	    }
	   
	   public void testAddGetApplicationByNameWithGroupId() throws Exception{
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
           this.checkApplicationsEqual(application, apiMgtDAO.getApplicationByName("testApplication3", subscriber.getName(), "org1"));
           
       }
	   
       
       public void testAddGetApplicationByNameWithUserNameNull() throws Exception{
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
       
       public void testAddGetApplicationByNameWithUserNameNullGroupIdNull() throws Exception{
           Subscriber subscriber = new Subscriber("LA_F_APP_UN_GROUP_ID_NULL");
           subscriber.setEmail("laf@wso2.com");
           subscriber.setSubscribedDate(new Date());
           subscriber.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
           apiMgtDAO.addSubscriber(subscriber, null);
           Application application = new Application("testApplication2", subscriber);
           int applicationId = apiMgtDAO.addApplication(application, subscriber.getName());
           application.setId(applicationId);
           assertTrue(applicationId  > 0);
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

		apiMgtDAO.addAPI(api,-1234);
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

}
