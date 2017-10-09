/*
 *  Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, ApiMgtDAO.class, APIUtil.class, APIGatewayManager.class})
public class APIProviderImplTest {    
    
    @Test
    public void testUpdateAPIStatus() throws APIManagementException, FaultGatewaysException, UserStoreException, 
                                                                                            RegistryException {
        API api = new API(new APIIdentifier("admin", "API1", "1.0.0"));
        api.setContext("/test");
        api.setStatus(APIStatus.CREATED);
        
        TestUtils.mockRegistryAndUserRealm(-1234);
        
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apimgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apimgtDAO);
        Mockito.doNothing().when(apimgtDAO).addAPI(api, -1234);
        
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.isAPIManagementEnabled()).thenReturn(false);
        PowerMockito.when(APIUtil.replaceEmailDomainBack(api.getId().getProviderName())).thenReturn("admin");
        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenReturn(APIStatus.PUBLISHED);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        apiProvider.addAPI(api);
        
        boolean status = apiProvider.updateAPIStatus(api.getId(), "PUBLISHED", true, false, true);
        
        Assert.assertTrue(status);
    }
    
    @Test(expected = APIManagementException.class)
    public void testUpdateAPIStatusWithFaultyGateways() throws APIManagementException, FaultGatewaysException, 
                                                                            RegistryException, UserStoreException {
        API api = new API(new APIIdentifier("admin", "API1", "1.0.0"));
        api.setContext("/test");
        api.setStatus(APIStatus.CREATED);
        
        TestUtils.mockRegistryAndUserRealm(-1234);
        
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apimgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apimgtDAO);
        Mockito.doNothing().when(apimgtDAO).addAPI(api, -1234);
        
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.isAPIManagementEnabled()).thenReturn(false);
        PowerMockito.when(APIUtil.replaceEmailDomainBack(api.getId().getProviderName())).thenReturn("admin");
        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenReturn(APIStatus.PUBLISHED);
        
        Map<String, Map<String,String>> failedGateways = new ConcurrentHashMap<String, Map<String, String>>();
        failedGateways.put("PUBLISHED",new HashMap<String, String>());
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, failedGateways);
        apiProvider.addAPI(api);
               
        
        String newStatusValue = "PUBLISHED";
        
        apiProvider.updateAPIStatus(api.getId(), newStatusValue, true, false, true);
    }
    
    @Test
    public void testGetAPIUsageByAPIId() throws APIManagementException, RegistryException, UserStoreException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        SubscribedAPI subscribedAPI1 = new SubscribedAPI(new Subscriber("user1"), 
                new APIIdentifier("admin", "API1", "1.0.0"));
        SubscribedAPI subscribedAPI2 = new SubscribedAPI(new Subscriber("user1"), 
                new APIIdentifier("admin", "API2", "1.0.0"));
        
        UserApplicationAPIUsage apiResult1 = new UserApplicationAPIUsage();
        apiResult1.addApiSubscriptions(subscribedAPI1);
        apiResult1.addApiSubscriptions(subscribedAPI2);
        
        SubscribedAPI subscribedAPI3 = new SubscribedAPI(new Subscriber("user2"), 
                new APIIdentifier("admin", "API1", "1.0.0"));
        SubscribedAPI subscribedAPI4 = new SubscribedAPI(new Subscriber("user2"), 
                new APIIdentifier("admin", "API2", "1.0.0"));
        
        UserApplicationAPIUsage apiResult2 = new UserApplicationAPIUsage();
        apiResult2.addApiSubscriptions(subscribedAPI3);
        apiResult2.addApiSubscriptions(subscribedAPI4);
        
        UserApplicationAPIUsage[] apiResults = {apiResult1, apiResult2};
        
        TestUtils.mockRegistryAndUserRealm(-1234);
        
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apimgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apimgtDAO);
        Mockito.when(apimgtDAO.getAllAPIUsageByProvider(apiId.getProviderName())).thenReturn(apiResults);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        
        List<SubscribedAPI> subscribedAPIs = apiProvider.getAPIUsageByAPIId(apiId);
        
        Assert.assertEquals(2, subscribedAPIs.size());
        Assert.assertEquals("user1", subscribedAPIs.get(0).getSubscriber().getName());
        Assert.assertEquals("user2", subscribedAPIs.get(1).getSubscriber().getName());
    }
    
    @Test
    public void testIsAPIUpdateValid() throws RegistryException, UserStoreException, APIManagementException {
        API api = new API(new APIIdentifier("admin", "API1", "1.0.0"));
        api.setContext("/test");
        api.setStatus(APIStatus.CREATED);
        
        TestUtils.mockRegistryAndUserRealm(-1234);
        
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apimgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apimgtDAO);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, null);
        
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(APIUtil.getArtifactManager(apiProvider.registry, APIConstants.API_KEY))
                                                                                          .thenReturn(artifactManager);
        
        //API Status is CREATED and user has permission
        GenericArtifact artifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(true);
        
        boolean status = apiProvider.isAPIUpdateValid(api);        
        Assert.assertTrue(status);
        
        //API Status is CREATED and user doesn't have permission
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(false);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(false);
        
        status = apiProvider.isAPIUpdateValid(api);        
        Assert.assertFalse(status);
        
        //API Status is PROTOTYPED and user has permission
        api.setStatus(APIStatus.PROTOTYPED);
        GenericArtifact artifact1 = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifact1.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("PROTOTYPED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact1);
        
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(true);
        
        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertTrue(status);
        
        //API Status is PROTOTYPED and user doesn't have permission
        api.setStatus(APIStatus.PROTOTYPED);
        Mockito.when(artifact1.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("PROTOTYPED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact1);
        
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(false);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(false);
        
        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertFalse(status);
        
        //API Status is DEPRECATED and has publish permission
        api.setStatus(APIStatus.DEPRECATED);
        Mockito.when(artifact1.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("DEPRECATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact1);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertTrue(status);
        
        //API Status is DEPRECATED and doesn't have publish permission
        api.setStatus(APIStatus.DEPRECATED);
        Mockito.when(artifact1.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("DEPRECATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact1);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(false);
        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertFalse(status);
        
        //API Status is RETIRED and has publish permission
        api.setStatus(APIStatus.RETIRED);
        Mockito.when(artifact1.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("RETIRED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact1);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertTrue(status);
        
        //API Status is RETIRED and doesn't have publish permission
        api.setStatus(APIStatus.RETIRED);
        Mockito.when(artifact1.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("RETIRED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact1);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(false);
        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertFalse(status);
    }
    
    @Test    
    public void testPropergateAPIStatusChangeToGateways() throws RegistryException, UserStoreException,
            APIManagementException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIStatus.CREATED);
        
        TestUtils.mockRegistryAndUserRealm(-1234);
        
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apimgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apimgtDAO);
        Mockito.doNothing().when(apimgtDAO).addAPI(api, -1234);
        
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.replaceEmailDomain(apiId.getProviderName())).thenReturn("admin");
        PowerMockito.when(APIUtil.replaceEmailDomainBack(api.getId().getProviderName())).thenReturn("admin");
        
        Map<String, Map<String,String>> failedGateways = new ConcurrentHashMap<String, Map<String, String>>();
        
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null, failedGateways);        
        apiProvider.addAPI(api);
        
        //No state changes
        Map<String, String> failedGatewaysReturned = apiProvider.propergateAPIStatusChangeToGateways(apiId, 
                APIStatus.CREATED);        
        Assert.assertEquals(0, failedGatewaysReturned.size());
        Assert.assertEquals(APIStatus.CREATED, api.getStatus());
        
        TestUtils.mockAPIMConfiguration(APIConstants.API_GATEWAY_TYPE, APIConstants.API_GATEWAY_TYPE_SYNAPSE);
        PowerMockito.when(apimgtDAO.getPublishedDefaultVersion(api.getId())).thenReturn("1.0.0");
        
        //Change to PUBLISHED state
        failedGatewaysReturned = apiProvider.propergateAPIStatusChangeToGateways(apiId, APIStatus.PUBLISHED);
        Assert.assertEquals(0, failedGatewaysReturned.size());
        Assert.assertEquals(APIStatus.PUBLISHED, api.getStatus());
        
        //Change to PUBLISHED state and error thrown while publishing
        api.setStatus(APIStatus.CREATED);
        Map<String, String> failedGWEnv = new HashMap<String, String>();
        failedGWEnv.put("Production", "Failed to publish");
        failedGateways.put("PUBLISHED",failedGWEnv);
       
        failedGatewaysReturned = apiProvider.propergateAPIStatusChangeToGateways(apiId, APIStatus.PUBLISHED);
        Assert.assertEquals(1, failedGatewaysReturned.size());
        Assert.assertEquals(APIStatus.PUBLISHED, api.getStatus());
        
        //Change to RETIRED state
        api.setStatus(APIStatus.CREATED);
        failedGateways.remove("PUBLISHED");
        
        failedGatewaysReturned = apiProvider.propergateAPIStatusChangeToGateways(apiId, APIStatus.RETIRED);
        Assert.assertEquals(0, failedGatewaysReturned.size());
        Assert.assertEquals(APIStatus.RETIRED, api.getStatus());
        
        //Change to RETIRED state and error thrown while un-publishing
        api.setStatus(APIStatus.CREATED);
        failedGateways.put("UNPUBLISHED",failedGWEnv);        
        
        failedGatewaysReturned = apiProvider.propergateAPIStatusChangeToGateways(apiId, APIStatus.RETIRED);
        Assert.assertEquals(1, failedGatewaysReturned.size());
        Assert.assertEquals(APIStatus.RETIRED, api.getStatus());
    }

}
