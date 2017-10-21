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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.PolicyDeploymentFailureException;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStateChangeResponse;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.Documentation.DocumentSourceType;
import org.wso2.carbon.apimgt.api.model.Documentation.DocumentVisibility;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.DuplicateAPIException;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Condition;
import org.wso2.carbon.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.carbon.apimgt.api.model.policy.HTTPVerbCondition;
import org.wso2.carbon.apimgt.api.model.policy.Pipeline;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.api.model.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.definitions.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.APIStateChangeSimpleWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.beans.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.CheckListItem;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.LifecycleBeanPopulator;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.Property;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.cache.Caching;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("org.wso2.carbon.context.PrivilegedCarbonContext")
@PrepareForTest({ServiceReferenceHolder.class, ApiMgtDAO.class, APIUtil.class, APIGatewayManager.class, 
    GovernanceUtils.class, PrivilegedCarbonContext.class, WorkflowExecutorFactory.class, RegistryUtils.class,
    ThrottlePolicyDeploymentManager.class, LifecycleBeanPopulator.class, Caching.class, PaginationContext.class,
    APIProviderImpl.class})
public class APIProviderImplTest {
    
    private static String EP_CONFIG_WSDL = "{\"production_endpoints\":{\"url\":\"http://ws.cdyne.com/phoneverify/phoneverify.asmx?wsdl\""
            + ",\"config\":null,\"template_not_supported\":false},\"endpoint_type\":\"wsdl\"}";
    private static String WSDL_URL = "http://ws.cdyne.com/phoneverify/phoneverify.asmx?wsdl";
    
    private ApiMgtDAO apimgtDAO;
    private GenericArtifactManager artifactManager;
    private APIGatewayManager gatewayManager;
    private GenericArtifact artifact;
    
    @Before
    public void init() throws Exception {
        System.setProperty("carbon.home", "");
        
        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(RegistryUtils.class);
        PowerMockito.mockStatic(GovernanceUtils.class);
        PowerMockito.mockStatic(WorkflowExecutorFactory.class);
        PowerMockito.mockStatic(ThrottlePolicyDeploymentManager.class);
        PowerMockito.mockStatic(LifecycleBeanPopulator.class);
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.mockStatic(PaginationContext.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(APIGatewayManager.class);
        
        apimgtDAO = Mockito.mock(ApiMgtDAO.class);
        
        PowerMockito.when(APIUtil.isAPIManagementEnabled()).thenReturn(false);
        PowerMockito.when(APIUtil.replaceEmailDomainBack(Matchers.anyString())).thenReturn("admin");
        Mockito.when(APIUtil.replaceEmailDomain(Matchers.anyString())).thenReturn("admin");
        
        PrivilegedCarbonContext prcontext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(prcontext);

        PowerMockito.doNothing().when(prcontext).setUsername(Matchers.anyString());
        PowerMockito.doNothing().when(prcontext).setTenantDomain(Matchers.anyString(), Matchers.anyBoolean());
        
        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);
        
        artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(APIUtil.getArtifactManager(Matchers.any(Registry.class), Matchers.anyString()))
                                                                                          .thenReturn(artifactManager);
        
        artifact = Mockito.mock(GenericArtifact.class);
        
        gatewayManager = Mockito.mock(APIGatewayManager.class);
        Mockito.when(APIGatewayManager.getInstance()).thenReturn(gatewayManager);
        
        TestUtils.mockRegistryAndUserRealm(-1234);  
        TestUtils.mockAPICacheClearence();
        TestUtils.mockAPIMConfiguration();
        mockDocumentationCreation();
    }
    
    @Test
    public void testAddAPI() throws APIManagementException, GovernanceException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIStatus.CREATED);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        
        Mockito.doThrow(APIManagementException.class).when(apimgtDAO).addAPI(api, -1234);
        
        try {
            apiProvider.addAPI(api);
        } catch(APIManagementException e) {
            Assert.assertEquals("Error in adding API :" + api.getId().getApiName(), e.getMessage());
        }
    }
    
    @Test
    public void testUpdateAPIStatus() throws APIManagementException, FaultGatewaysException, UserStoreException, 
                                                                                RegistryException, WorkflowException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.1");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIStatus.CREATED);
        
        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenReturn(APIStatus.PUBLISHED);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        apiProvider.addAPI(api);
        
        //Existing APIs of the provider
        API api1 = new API(new APIIdentifier("admin", "API1", "1.0.0"));
        api1.setStatus(APIStatus.PUBLISHED);
        API api2 = new API(new APIIdentifier("admin", "API2", "1.0.0"));
        
        //Mock Updating API
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);
        
        Mockito.when(artifact.getId()).thenReturn("12640983654");
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                                                                                        thenReturn(apiSourcePath); 
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);
        
        prepareForGetAPIsByProvider(artifactManager, apiProvider, "admin", api1, api2);
        prepareForChangeLifeCycleStatus(apiProvider, apimgtDAO, apiId, artifact);
        
        boolean status = apiProvider.updateAPIStatus(api.getId(), "PUBLISHED", true, true, true);
        
        Assert.assertTrue(status);
        
        //Test for non existing API Id
        APIIdentifier apiId1 = new APIIdentifier("admin", "API2", "1.0.0");
        try {            
            apiProvider.updateAPIStatus(apiId1, "PUBLISHED", true, true, true);
        } catch(APIManagementException e) {
            Assert.assertEquals("Couldn't find an API with the name-" + apiId.getApiName() + "version-" 
                    + apiId1.getVersion(), e.getMessage());
        }
    }
    
    @Test
    public void testUpdateAPIStatus_InvalidAPIId() throws APIManagementException, FaultGatewaysException, UserStoreException, 
                                                                                RegistryException, WorkflowException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");        
        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenReturn(APIStatus.PUBLISHED);        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);

        try {            
            apiProvider.updateAPIStatus(apiId, "PUBLISHED", true, true, true);
        } catch(APIManagementException e) {
            Assert.assertEquals("Couldn't find an API with the name-" + apiId.getApiName() + "version-" 
                    + apiId.getVersion(), e.getMessage());
        }
    }
    
    @Test(expected = APIManagementException.class)
    public void testUpdateAPIStatus_WithFaultyGateways() throws APIManagementException, FaultGatewaysException, 
                                                                            RegistryException, UserStoreException {
        API api = new API(new APIIdentifier("admin", "API1", "1.0.0"));
        api.setContext("/test");
        api.setStatus(APIStatus.CREATED);
        
        Set<String> environments = new HashSet<String>();
        environments.add("Production");
        environments.add("Sandbox");
        
        api.setEnvironments(environments);
        
        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenReturn(APIStatus.PUBLISHED);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        apiProvider.addAPI(api);
        
        //Mock Updating API
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);

        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);
        
        Mockito.when(artifact.getId()).thenReturn("12640983654");
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                                                                                        thenReturn(apiSourcePath); 
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);
        
        Map<String, String> failedGWEnv = new HashMap<String, String>();
        failedGWEnv.put("Production", "Failed to publish");
        
        Mockito.when(gatewayManager.publishToGateway(Matchers.any(API.class), Matchers.any(APITemplateBuilder.class), 
                Matchers.anyString())).thenReturn(failedGWEnv);
        
        String newStatusValue = "PUBLISHED";
        
        apiProvider.updateAPIStatus(api.getId(), newStatusValue, true, false, true);
    }
    
    @Test(expected = APIManagementException.class)
    public void testUpdateAPIStatus_ToRetiredWithFaultyGateways() throws APIManagementException, FaultGatewaysException, 
                                                                            RegistryException, UserStoreException {
        API api = new API(new APIIdentifier("admin", "API1", "1.0.0"));
        api.setContext("/test");
        api.setStatus(APIStatus.CREATED);
        
        Set<String> environments = new HashSet<String>();
        environments.add("Production");
        environments.add("Sandbox");
        
        api.setEnvironments(environments);
        
        PowerMockito.when(APIUtil.getApiStatus("RETIRED")).thenReturn(APIStatus.RETIRED);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        apiProvider.addAPI(api);
        
        //Mock Updating API
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        
        GenericArtifact artifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);
        
        PowerMockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        Mockito.when(artifact.getId()).thenReturn("12640983654");
        
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                                                                                        thenReturn(apiSourcePath); 
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);
        
        Map<String, String> failedGWEnv = new HashMap<String, String>();
        failedGWEnv.put("Production", "Failed to publish");
        
        Mockito.when(gatewayManager.publishToGateway(Matchers.any(API.class), Matchers.any(APITemplateBuilder.class), 
                Matchers.anyString())).thenReturn(failedGWEnv);
        
        Mockito.when(gatewayManager.removeFromGateway(Matchers.any(API.class), 
                Matchers.anyString())).thenReturn(failedGWEnv);
        
        String newStatusValue = "RETIRED";
        
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
        
        Mockito.when(apimgtDAO.getAllAPIUsageByProvider(apiId.getProviderName())).thenReturn(apiResults);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
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
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";

        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        
        
        //API Status is CREATED and user has permission
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
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("PROTOTYPED");
        //Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(true);
        
        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertTrue(status);
        
        //API Status is PROTOTYPED and user doesn't have permission
        api.setStatus(APIStatus.PROTOTYPED);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("PROTOTYPED");
        //Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(false);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(false);
        
        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertFalse(status);
        
        //API Status is DEPRECATED and has publish permission
        api.setStatus(APIStatus.DEPRECATED);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("DEPRECATED");
        //Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertTrue(status);
        
        //API Status is DEPRECATED and doesn't have publish permission
        api.setStatus(APIStatus.DEPRECATED);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("DEPRECATED");
        //Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(false);
        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertFalse(status);
        
        //API Status is RETIRED and has publish permission
        api.setStatus(APIStatus.RETIRED);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("RETIRED");
        //Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        status = apiProvider.isAPIUpdateValid(api);
        Assert.assertTrue(status);
        
        //API Status is RETIRED and doesn't have publish permission
        api.setStatus(APIStatus.RETIRED);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("RETIRED");
        //Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
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
        
        CORSConfiguration corsConfig = getCORSConfiguration();
        api.setCorsConfiguration(corsConfig);       
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        apiProvider.addAPI(api);
        
        //No state changes
        Map<String, String> failedGatewaysReturned = apiProvider.propergateAPIStatusChangeToGateways(apiId, 
                APIStatus.CREATED);        
        Assert.assertEquals(0, failedGatewaysReturned.size());
        Assert.assertEquals(APIStatus.CREATED, api.getStatus());
        
        PowerMockito.when(apimgtDAO.getPublishedDefaultVersion(api.getId())).thenReturn("1.0.0");
        
        //Change to PUBLISHED state
        failedGatewaysReturned = apiProvider.propergateAPIStatusChangeToGateways(apiId, APIStatus.PUBLISHED);
        Assert.assertEquals(0, failedGatewaysReturned.size());
        Assert.assertEquals(APIStatus.PUBLISHED, api.getStatus());
        
        //Change to PUBLISHED state and error thrown while publishing
        api.setStatus(APIStatus.CREATED);
        Map<String, String> failedGWEnv = new HashMap<String, String>();
        failedGWEnv.put("Production", "Failed to publish");
        
        Mockito.when(gatewayManager.publishToGateway(Matchers.any(API.class), Matchers.any(APITemplateBuilder.class), 
                Matchers.anyString())).thenReturn(failedGWEnv);
       
        failedGatewaysReturned = apiProvider.propergateAPIStatusChangeToGateways(apiId, APIStatus.PUBLISHED);
        Assert.assertEquals(1, failedGatewaysReturned.size());
        Assert.assertEquals(APIStatus.PUBLISHED, api.getStatus());
        
        //Change to RETIRED state
        api.setStatus(APIStatus.CREATED);
        
        failedGatewaysReturned = apiProvider.propergateAPIStatusChangeToGateways(apiId, APIStatus.RETIRED);
        Assert.assertEquals(0, failedGatewaysReturned.size());
        Assert.assertEquals(APIStatus.RETIRED, api.getStatus());
        
        //Change to RETIRED state and error thrown while un-publishing
        api.setStatus(APIStatus.CREATED);
        
        Mockito.when(gatewayManager.removeFromGateway(Matchers.any(API.class), 
                Matchers.anyString())).thenReturn(failedGWEnv);
        
        failedGatewaysReturned = apiProvider.propergateAPIStatusChangeToGateways(apiId, APIStatus.RETIRED);
        Assert.assertEquals(1, failedGatewaysReturned.size());
        Assert.assertEquals(APIStatus.RETIRED, api.getStatus());
    }
    
    @Test
    public void testPropergateAPIStatusChangeToGateways_InvalidAPIID() throws RegistryException, UserStoreException,
            APIManagementException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIStatus.CREATED);
        
        CORSConfiguration corsConfig = getCORSConfiguration();
        api.setCorsConfiguration(corsConfig);       
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        try {
            apiProvider.propergateAPIStatusChangeToGateways(apiId, 
                    APIStatus.CREATED);
        } catch(APIManagementException e) {
            Assert.assertEquals("Couldn't find an API with the name-" + apiId.getApiName() + "version-" 
                    + apiId.getVersion(), e.getMessage());
        }
    }
    
    @Test
    public void testCreateNewAPIVersion() throws Exception {
        //Create Original API
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId);
        api.setContext("/test");
        api.setVisibility("Public");
        api.setStatus(APIStatus.CREATED);
        
        String newVersion = "1.0.1";
        //Create new API object
        APIIdentifier newApiId = new APIIdentifier("admin", "API1", "1.0.1");
        final API newApi = new API(newApiId);
        newApi.setStatus(APIStatus.CREATED);
        newApi.setContext("/test");
        
        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenReturn(APIStatus.PUBLISHED);
        
        //Create Documentation List
        List<Documentation> documentationList = getDocumentationList();
        
        final APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, documentationList);
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        
        GenericArtifact artifactNew = Mockito.mock(GenericArtifact.class);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, newApi)).thenReturn(artifactNew);
        apiProvider.addAPI(api);
        
        String targetPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                api.getId().getProviderName() +
                RegistryConstants.PATH_SEPARATOR + api.getId().getApiName() +
                RegistryConstants.PATH_SEPARATOR + newVersion +
                APIConstants.API_RESOURCE_NAME;
        
        String apiSourcePath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiId.getProviderName() +
                RegistryConstants.PATH_SEPARATOR + apiId.getApiName() +
                RegistryConstants.PATH_SEPARATOR + apiId.getVersion() +
                APIConstants.API_RESOURCE_NAME;
        PowerMockito.when(APIUtil.getAPIPath(apiId)).thenReturn(apiSourcePath);
        String apiSourceUUID = "87ty543-899hyt";
        
        Mockito.when(apiProvider.registry.resourceExists(targetPath)).thenReturn(false);
        Mockito.doNothing().when(apiProvider.registry).beginTransaction();
        Mockito.doNothing().when(apiProvider.registry).commitTransaction();
        
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        
        
        //Mocking Old API retrieval
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn(apiSourceUUID);
        
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("PUBLISHED");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT)).thenReturn("test");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE)).thenReturn("test/{version}");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_WEBSOCKET)).thenReturn("false");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES)).thenReturn("admin, subscriber");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceUUID)).thenReturn(artifact);
        
        //Mocking thumbnail
        String thumbUrl = APIConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR +
                api.getId().getProviderName() + RegistryConstants.PATH_SEPARATOR +
                api.getId().getApiName() + RegistryConstants.PATH_SEPARATOR +
                api.getId().getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
        Resource image = Mockito.mock(Resource.class);
        Mockito.when(apiProvider.registry.get(thumbUrl)).thenReturn(image);
        Mockito.when(apiProvider.registry.resourceExists(thumbUrl)).thenReturn(true);
        
        //Mocking In sequence retrieval 
        String inSeqFilePath = "API1/1.0.0/in";
        PowerMockito.when(APIUtil.getSequencePath(api.getId(), "in")).thenReturn(inSeqFilePath);
        Mockito.when(apiProvider.registry.resourceExists(inSeqFilePath)).thenReturn(true);        
        Collection inSeqCollection = Mockito.mock(Collection.class);
        Mockito.when(apiProvider.registry.get(inSeqFilePath)).thenReturn(inSeqCollection);
        String[] inSeqChildPaths = {"path1"};
        Mockito.when(inSeqCollection.getChildren()).thenReturn(inSeqChildPaths);
        
        Mockito.when(apiProvider.registry.get(inSeqChildPaths[0])).thenReturn(apiSourceArtifact);
        InputStream responseStream = IOUtils.toInputStream("<sequence name=\"in-seq\"></sequence>", "UTF-8");
        OMElement seqElment = buildOMElement(responseStream);
        PowerMockito.when(APIUtil.buildOMElement(responseStream)).thenReturn(seqElment);
        Mockito.when(apiSourceArtifact.getContentStream()).thenReturn(responseStream);
        
        //Mocking Out sequence retrieval 
        Resource apiSourceArtifact1 = Mockito.mock(Resource.class);
        String outSeqFilePath = "API1/1.0.0/out";
        PowerMockito.when(APIUtil.getSequencePath(api.getId(), "out")).thenReturn(outSeqFilePath);
        Mockito.when(apiProvider.registry.resourceExists(outSeqFilePath)).thenReturn(true); 
        Collection outSeqCollection = Mockito.mock(Collection.class);
        Mockito.when(apiProvider.registry.get(outSeqFilePath)).thenReturn(outSeqCollection);
        String[] outSeqChildPaths = {"path2"};
        Mockito.when(outSeqCollection.getChildren()).thenReturn(outSeqChildPaths);
        
        Mockito.when(apiProvider.registry.get(outSeqChildPaths[0])).thenReturn(apiSourceArtifact1);
        InputStream responseStream2 = IOUtils.toInputStream("<sequence name=\"in-seq\"></sequence>", "UTF-8");
        OMElement seqElment2 = buildOMElement(responseStream2);
        PowerMockito.when(APIUtil.buildOMElement(responseStream2)).thenReturn(seqElment2);
        Mockito.when(apiSourceArtifact1.getContentStream()).thenReturn(responseStream2);
        
        //Mock Adding new API artifact with new version
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                apiProvider.createAPI(newApi);
                return null;
            }
        }).when(artifactManager).addGenericArtifact(artifact);
        Mockito.doNothing().when(artifact).attachLifecycle(APIConstants.API_LIFE_CYCLE);
        PowerMockito.when(APIUtil.getAPIProviderPath(api.getId())).thenReturn("/dummy/provider/path");
        Mockito.doNothing().when(apiProvider.registry).addAssociation("/dummy/provider/path", 
                                                             targetPath, APIConstants.PROVIDER_ASSOCIATION);
        
        String artifactPath = "artifact/path";
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, artifact.getId())).
                                    thenReturn(artifactPath);
        PowerMockito.doNothing().when(APIUtil.class);
        String[] roles = {"admin", "subscriber"};
        APIUtil.setResourcePermissions("admin", "Public", roles, artifactPath);
        
        //Mock no tags case
        Mockito.when(apiProvider.registry.getTags(apiSourcePath)).thenReturn(null);
        
        
        //Mock new API retrieval
        String newApiPath = "API1/1.0.1/";
        PowerMockito.when(APIUtil.getAPIPath(newApi.getId())).thenReturn(newApiPath);
        String newApiUUID = "87ty543-899hy23";
        GenericArtifact newArtifact = Mockito.mock(GenericArtifact.class);
        Resource newApiResource = Mockito.mock(Resource.class);
        Mockito.when(newApiResource.getUUID()).thenReturn(newApiUUID);
        Mockito.when(apiProvider.registry.get(newApiPath)).thenReturn(newApiResource);
        Mockito.when(artifactManager.getGenericArtifact(newApiUUID)).thenReturn(newArtifact);
        PowerMockito.when(APIUtil.getAPI(newArtifact, apiProvider.registry, api.getId(), "test")).thenReturn(newApi);
        
        //Swagger resource
        String resourcePath = APIUtil.getSwagger20DefinitionFilePath(api.getId().getApiName(),
                                                                     api.getId().getVersion(),
                                                                     api.getId().getProviderName());        
        Mockito.when(apiProvider.registry.resourceExists(resourcePath + APIConstants.API_DOC_2_0_RESOURCE_NAME)).
                                                                                                    thenReturn(true);
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = Mockito.mock(APIDefinitionFromSwagger20.class);
        setFinalStatic(AbstractAPIManager.class.getDeclaredField("definitionFromSwagger20"),
                apiDefinitionFromSwagger20);
        Mockito.when(apiDefinitionFromSwagger20.getAPIDefinition(apiId, apiProvider.registry)).thenReturn(
                "{\"info\": {\"swagger\":\"data\"}}");
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);
        
        //Mock Config system registry
        ServiceReferenceHolder sh = TestUtils.getServiceReferenceHolder();
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry systemReg = Mockito.mock(UserRegistry.class);
        
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tm = Mockito.mock(TenantManager.class);
        
        PowerMockito.when(sh.getRealmService()).thenReturn(realmService);
        PowerMockito.when(realmService.getTenantManager()).thenReturn(tm);
        PowerMockito.when(tm.getTenantId(Matchers.anyString())).thenReturn(-1234);
        
        AuthorizationManager authManager = Mockito.mock(AuthorizationManager.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        PowerMockito.when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        PowerMockito.when(userRealm.getAuthorizationManager()).thenReturn(authManager);        
        
        PowerMockito.when(registryService.getConfigSystemRegistry(-1234)).thenReturn(systemReg);
        Mockito.when(systemReg.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(true);
        Resource tenantConfResource = Mockito.mock(Resource.class);
        Mockito.when(systemReg.get(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(tenantConfResource);
        Mockito.when(tenantConfResource.getContent()).thenReturn(getTenantConfigContent());
        
        apiProvider.createNewAPIVersion(api, newVersion);
        
        Assert.assertEquals(newVersion, apiProvider.getAPI(newApi.getId()).getId().getVersion());
        
    }
    
    @Test
    public void testCreateNewAPIVersion_ForDefaultVersion() throws Exception {
        //Create Original API
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId);
        api.setContext("/test");
        api.setVisibility("Public");
        api.setStatus(APIStatus.CREATED);
        api.setAsDefaultVersion(true);
        
        String newVersion = "1.0.1";
        //Create new API object
        APIIdentifier newApiId = new APIIdentifier("admin", "API1", "1.0.1");
        final API newApi = new API(newApiId);
        newApi.setStatus(APIStatus.CREATED);
        newApi.setContext("/test");

        //Mock API as a default version
        Mockito.when(apimgtDAO.getDefaultVersion(apiId)).thenReturn("1.0.0");

        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenReturn(APIStatus.PUBLISHED);
                
        final APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        
        GenericArtifact artifactNew = Mockito.mock(GenericArtifact.class);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, newApi)).thenReturn(artifactNew);
        apiProvider.addAPI(api);
        
        String targetPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                api.getId().getProviderName() +
                RegistryConstants.PATH_SEPARATOR + api.getId().getApiName() +
                RegistryConstants.PATH_SEPARATOR + newVersion +
                APIConstants.API_RESOURCE_NAME;
        
        String apiSourcePath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiId.getProviderName() +
                RegistryConstants.PATH_SEPARATOR + apiId.getApiName() +
                RegistryConstants.PATH_SEPARATOR + apiId.getVersion() +
                APIConstants.API_RESOURCE_NAME;
        PowerMockito.when(APIUtil.getAPIPath(apiId)).thenReturn(apiSourcePath);
        String apiSourceUUID = "87ty543-899hyt";
        
        Mockito.when(apiProvider.registry.resourceExists(targetPath)).thenReturn(false);
        
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        
        //Mock API as a default version
        Mockito.when(apimgtDAO.getDefaultVersion(apiId)).thenReturn("1.0.0");
        
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn(apiSourceUUID);
        
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);
        
        //Mocking Old API retrieval       
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("PUBLISHED");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT)).thenReturn("test");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE)).thenReturn("test/{version}");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_WEBSOCKET)).thenReturn("false");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES)).thenReturn("admin, subscriber");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceUUID)).thenReturn(artifact);
        
        //Mocking no thumbnail case
        String thumbUrl = APIConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR +
                api.getId().getProviderName() + RegistryConstants.PATH_SEPARATOR +
                api.getId().getApiName() + RegistryConstants.PATH_SEPARATOR +
                api.getId().getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
        Mockito.when(apiProvider.registry.resourceExists(thumbUrl)).thenReturn(false);
        
        //Mocking In sequence retrieval 
        String inSeqFilePath = "API1/1.0.0/in";
        PowerMockito.when(APIUtil.getSequencePath(api.getId(), "in")).thenReturn(inSeqFilePath);
        Mockito.when(apiProvider.registry.resourceExists(inSeqFilePath)).thenReturn(false);        
        
        
        //Mocking Out sequence retrieval 
        String outSeqFilePath = "API1/1.0.0/out";
        PowerMockito.when(APIUtil.getSequencePath(api.getId(), "out")).thenReturn(outSeqFilePath);
        Mockito.when(apiProvider.registry.resourceExists(outSeqFilePath)).thenReturn(false); 
        
        //Mock Adding new API artifact with new version
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                apiProvider.createAPI(newApi);
                return null;
            }
        }).when(artifactManager).addGenericArtifact(artifact);
        Mockito.doNothing().when(artifact).attachLifecycle(APIConstants.API_LIFE_CYCLE);
        PowerMockito.when(APIUtil.getAPIProviderPath(api.getId())).thenReturn("/dummy/provider/path");
        Mockito.doNothing().when(apiProvider.registry).addAssociation("/dummy/provider/path", 
                                                             targetPath, APIConstants.PROVIDER_ASSOCIATION);
        
        String artifactPath = "artifact/path";
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, artifact.getId())).
                                    thenReturn(artifactPath);
        PowerMockito.doNothing().when(APIUtil.class);
        String[] roles = {"admin", "subscriber"};
        APIUtil.setResourcePermissions("admin", "Public", roles, artifactPath);
        
        //Mock no tags case
        Mockito.when(apiProvider.registry.getTags(apiSourcePath)).thenReturn(null);
        
        //Mock new API retrieval
        String newApiPath = "API1/1.0.1/";
        PowerMockito.when(APIUtil.getAPIPath(newApi.getId())).thenReturn(newApiPath);
        String newApiUUID = "87ty543-899hy23";
        GenericArtifact newArtifact = Mockito.mock(GenericArtifact.class);
        Resource newApiResource = Mockito.mock(Resource.class);
        Mockito.when(newApiResource.getUUID()).thenReturn(newApiUUID);
        Mockito.when(apiProvider.registry.get(newApiPath)).thenReturn(newApiResource);
        Mockito.when(artifactManager.getGenericArtifact(newApiUUID)).thenReturn(newArtifact);
        PowerMockito.when(APIUtil.getAPI(newArtifact, apiProvider.registry, api.getId(), "test")).thenReturn(newApi);
        
        String resourcePath = APIUtil.getSwagger20DefinitionFilePath(api.getId().getApiName(),
                                                                     api.getId().getVersion(),
                                                                     api.getId().getProviderName());        
        
        Mockito.when(apiProvider.registry.resourceExists(resourcePath + APIConstants.API_DOC_2_0_RESOURCE_NAME)).
                                                                                                    thenReturn(false);
        
        //Mock Config system registry
        ServiceReferenceHolder sh = TestUtils.getServiceReferenceHolder();
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry systemReg = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getConfigSystemRegistry(-1234)).thenReturn(systemReg);
        Mockito.when(systemReg.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(false);
        
        apiProvider.createNewAPIVersion(api, newVersion);
        
        Assert.assertEquals(newVersion, apiProvider.getAPI(newApi.getId()).getId().getVersion());
        
    }
    
    @Test (expected = DuplicateAPIException.class)
    public void testCreateNewAPIVersion_DuplicateAPI() throws RegistryException, UserStoreException, 
                APIManagementException, IOException, DuplicateAPIException {
        //Create Original API
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId);
        api.setContext("/test");
        api.setVisibility("Public");
        api.setStatus(APIStatus.CREATED);
        
        String newVersion = "1.0.0";
        //Create new API object
        APIIdentifier newApiId = new APIIdentifier("admin", "API1", "1.0.1");
        final API newApi = new API(newApiId);
        newApi.setStatus(APIStatus.CREATED);
        newApi.setContext("/test");

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        
        GenericArtifact artifactNew = Mockito.mock(GenericArtifact.class);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, newApi)).thenReturn(artifactNew);
        apiProvider.addAPI(api);
        
        String targetPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                api.getId().getProviderName() +
                RegistryConstants.PATH_SEPARATOR + api.getId().getApiName() +
                RegistryConstants.PATH_SEPARATOR + newVersion +
                APIConstants.API_RESOURCE_NAME;
        
        String apiSourcePath = "API1/1.0.0/";
        PowerMockito.when(APIUtil.getAPIPath(apiId)).thenReturn(apiSourcePath);
        
        Mockito.when(apiProvider.registry.resourceExists(targetPath)).thenReturn(true);
        
        apiProvider.createNewAPIVersion(api, newVersion);
    }
    
    @Test (expected = APIManagementException.class)
    public void testCreateNewAPIVersion_Exception() throws RegistryException, UserStoreException, 
                APIManagementException, IOException, DuplicateAPIException {
        //Create Original API
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId);
        api.setContext("/test");
        api.setVisibility("Public");
        api.setStatus(APIStatus.CREATED);
        
        String newVersion = "1.0.0";
        //Create new API object
        APIIdentifier newApiId = new APIIdentifier("admin", "API1", "1.0.1");
        final API newApi = new API(newApiId);
        newApi.setStatus(APIStatus.CREATED);
        newApi.setContext("/test");

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        
        GenericArtifact artifactNew = Mockito.mock(GenericArtifact.class);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, newApi)).thenReturn(artifactNew);
        apiProvider.addAPI(api);
        
        String targetPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                api.getId().getProviderName() +
                RegistryConstants.PATH_SEPARATOR + api.getId().getApiName() +
                RegistryConstants.PATH_SEPARATOR + newVersion +
                APIConstants.API_RESOURCE_NAME;
        
        String apiSourcePath = "API1/1.0.0/";
        PowerMockito.when(APIUtil.getAPIPath(apiId)).thenReturn(apiSourcePath);
        
        Mockito.when(apiProvider.registry.resourceExists(targetPath)).thenThrow(RegistryException.class);
        
        apiProvider.createNewAPIVersion(api, newVersion);
    }
    
    @Test
    public void testGetAPIsByProvider() throws RegistryException, UserStoreException, APIManagementException {
        
        String providerId = "admin"; 
        API api1 = new API(new APIIdentifier("admin", "API1", "1.0.0"));
        API api2 = new API(new APIIdentifier("admin", "API2", "1.0.0"));
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        prepareForGetAPIsByProvider(artifactManager, apiProvider, providerId, api1, api2);
        
        List<API> apiResponse = apiProvider.getAPIsByProvider(providerId);
        
        Assert.assertEquals(2, apiResponse.size());
        Assert.assertEquals("API1", apiResponse.get(0).getId().getApiName());
        
        Mockito.when(apiProvider.registry.getAspectActions(Matchers.anyString(), 
                Matchers.anyString())).thenThrow(RegistryException.class);
        try {
            apiProvider.getAPIsByProvider(providerId);
        } catch(APIManagementException e) {
            Assert.assertEquals("Failed to get APIs for provider : " + providerId, e.getMessage());
        }
        
    }
    
    @Test
    public void testChangeLifeCycleStatus_WFAlreadyStarted() throws RegistryException, UserStoreException, 
                APIManagementException, FaultGatewaysException, WorkflowException {
        
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");     
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        GenericArtifact apiArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(APIUtil.getAPIArtifact(apiId, apiProvider.registry)).thenReturn(apiArtifact);
        Mockito.when(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER)).thenReturn("admin");
        Mockito.when(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME)).thenReturn("API1");
        Mockito.when(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION)).thenReturn("1.0.0");
        Mockito.when(apiArtifact.getLifecycleState()).thenReturn("CREATED");
        
        Mockito.when(apimgtDAO.getAPIID(apiId, null)).thenReturn(1);
        
        //Workflow has started already
        WorkflowDTO wfDTO = Mockito.mock(WorkflowDTO.class);
        Mockito.when(wfDTO.getStatus()).thenReturn(WorkflowStatus.CREATED);
        Mockito.when(apimgtDAO.retrieveWorkflowFromInternalReference("1",
                        WorkflowConstants.WF_TYPE_AM_API_STATE)).thenReturn(wfDTO);
        
        APIStateChangeResponse response = apiProvider.
                changeLifeCycleStatus(apiId, APIConstants.API_LC_ACTION_DEPRECATE);
        
        Assert.assertNotNull(response);      
    }
    
    @Test
    public void testChangeLifeCycleStatus() throws RegistryException, UserStoreException, APIManagementException, 
                                            FaultGatewaysException, WorkflowException {
        
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        prepareForChangeLifeCycleStatus(apiProvider, apimgtDAO, apiId, artifact);        
        
        APIStateChangeResponse response1 = apiProvider.
                changeLifeCycleStatus(apiId, APIConstants.API_LC_ACTION_DEPRECATE);
        Assert.assertEquals("APPROVED", response1.getStateChangeStatus());        
    }
    
    @Test(expected = APIManagementException.class)
    public void testChangeLifeCycleStatusAPIMgtException() throws RegistryException, UserStoreException, 
                APIManagementException, FaultGatewaysException, WorkflowException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        prepareForChangeLifeCycleStatus(apiProvider, apimgtDAO, apiId, artifact);
        
        GovernanceException exception = new GovernanceException(new APIManagementException("APIManagementException:"
                + "Error while retrieving Life cycle state"));
        
        Mockito.when(artifact.getLifecycleState()).thenThrow(exception);
        
        apiProvider.changeLifeCycleStatus(apiId, APIConstants.API_LC_ACTION_DEPRECATE);        
    }
    
    @Test(expected = FaultGatewaysException.class)
    public void testChangeLifeCycleStatus_FaultyGWException() throws RegistryException, UserStoreException, 
                APIManagementException, FaultGatewaysException, WorkflowException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        prepareForChangeLifeCycleStatus(apiProvider, apimgtDAO, apiId, artifact);
        
        GovernanceException exception = new GovernanceException(new APIManagementException("FaultGatewaysException:"
                + "{\"PUBLISHED\":{\"PROD\":\"Error\"}}"));
        
        Mockito.when(artifact.getLifecycleState()).thenThrow(exception);
        
        apiProvider.changeLifeCycleStatus(apiId, APIConstants.API_LC_ACTION_DEPRECATE);        
    }
    
    @Test(expected = APIManagementException.class)
    public void testUpdateAPI_WithStatusChange() throws RegistryException, UserStoreException, APIManagementException, 
                                                                FaultGatewaysException {
        APIIdentifier identifier = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(identifier);
        api.setStatus(APIStatus.PUBLISHED);
        api.setVisibility("public");
        
        //API status change is not allowed in UpdateAPI(). Should throw an exception.
        API oldApi = new API(identifier);
        oldApi.setStatus(APIStatus.CREATED);
        oldApi.setVisibility("public");

        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenReturn(APIStatus.PUBLISHED);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, oldApi)).thenReturn(artifact);
        apiProvider.addAPI(oldApi);
        
        //mock has permission
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        
        //API Status is CREATED and user has permission
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(true);
        
        apiProvider.updateAPI(api); 
        
    }
    
    @Test
    public void testUpdateAPI_InCreatedState() throws RegistryException, UserStoreException, APIManagementException, 
                                                                FaultGatewaysException {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
        Set<URITemplate> newUriTemplates = new HashSet<URITemplate>();

        URITemplate uriTemplate1 = new URITemplate();
        uriTemplate1.setHTTPVerb("POST");
        uriTemplate1.setAuthType("Application");
        uriTemplate1.setUriTemplate("/add");
        uriTemplate1.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate1);
        
        URITemplate uriTemplate2 = new URITemplate();
        uriTemplate2.setHTTPVerb("PUT");
        uriTemplate2.setAuthType("Application");
        uriTemplate2.setUriTemplate("/update");
        uriTemplate2.setThrottlingTier("Gold");
        newUriTemplates.add(uriTemplate1);
        newUriTemplates.add(uriTemplate2);        
        
        final API api = new API(identifier);
        api.setStatus(APIStatus.CREATED);
        api.setVisibility("public");
        api.setTransports("http,https");
        api.setContext("/test");
        api.setEnvironments(environments);
        api.setUriTemplates(newUriTemplates);
        
        API oldApi = new API(identifier);
        oldApi.setStatus(APIStatus.CREATED);
        oldApi.setVisibility("public");
        oldApi.setContext("/test");        
        oldApi.setEnvironments(environments);
        api.setUriTemplates(uriTemplates);
        
        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenReturn(APIStatus.PUBLISHED);
        
        List<Documentation> documentationList = getDocumentationList();
        
        final APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, documentationList);
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, oldApi)).thenReturn(artifact);
        apiProvider.addAPI(oldApi);
        
        //mock has permission
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(APIUtil.getAPIPath(oldApi.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        
        //API Status is CREATED and user has permission
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(true);
        
        Mockito.when(apimgtDAO.getDefaultVersion(identifier)).thenReturn("1.0.0");
        Mockito.when(apimgtDAO.getPublishedDefaultVersion(identifier)).thenReturn("1.0.0");
        
        //updateDefaultAPIInRegistry
        String defaultAPIPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getApiName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getVersion() +
                APIConstants.API_RESOURCE_NAME;
        Resource defaultAPISourceArtifact = Mockito.mock(Resource.class);
        String defaultAPIUUID = "12640983600";
        Mockito.when(defaultAPISourceArtifact.getUUID()).thenReturn(defaultAPIUUID);
        Mockito.when(apiProvider.registry.get(defaultAPIPath)).thenReturn(defaultAPISourceArtifact);
        GenericArtifact defaultAPIArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact(defaultAPIUUID)).thenReturn(defaultAPIArtifact);
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(defaultAPIArtifact);
        TestUtils.mockAPIMConfiguration(APIConstants.API_GATEWAY_TYPE, APIConstants.API_GATEWAY_TYPE_SYNAPSE);
        
        //updateApiArtifact
        PowerMockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        Mockito.when(artifact.getId()).thenReturn("12640983654");
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                                                                                        thenReturn(apiSourcePath); 
        //Mock Updating API
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                apiProvider.createAPI(api);
                return null;
            }
        }).when(artifactManager).updateGenericArtifact(artifact);
        
        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);
        
        apiProvider.updateAPI(api);
        Assert.assertEquals(0, api.getEnvironments().size());
    }
    
    
    @Test
    public void testUpdateAPI_InPublishedState() throws Exception {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        environments.add("PRODUCTION");
        
        Set<String> newEnvironments = new HashSet<String>();
        newEnvironments.add("SANDBOX");
        
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
        Set<URITemplate> newUriTemplates = new HashSet<URITemplate>();

        URITemplate uriTemplate1 = new URITemplate();
        uriTemplate1.setHTTPVerb("POST");
        uriTemplate1.setAuthType("Application");
        uriTemplate1.setUriTemplate("/add");
        uriTemplate1.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate1);
        
        URITemplate uriTemplate2 = new URITemplate();
        uriTemplate2.setHTTPVerb("PUT");
        uriTemplate2.setAuthType("Application");
        uriTemplate2.setUriTemplate("/update");
        uriTemplate2.setThrottlingTier("Gold");
        newUriTemplates.add(uriTemplate1);
        newUriTemplates.add(uriTemplate2);        
        
        final API api = new API(identifier);
        api.setStatus(APIStatus.PUBLISHED);
        api.setVisibility("private");
        api.setVisibleRoles("admin");
        api.setTransports("http,https");
        api.setContext("/test");
        api.setEnvironments(newEnvironments);
        api.setUriTemplates(newUriTemplates);
        
        API oldApi = new API(identifier);
        oldApi.setStatus(APIStatus.PUBLISHED);
        oldApi.setVisibility("public");
        oldApi.setContext("/test");        
        oldApi.setEnvironments(environments);
        api.setUriTemplates(uriTemplates);
        
        
        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenReturn(APIStatus.PUBLISHED);
        
        List<Documentation> documentationList = getDocumentationList();
        
        final APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, documentationList);
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, oldApi)).thenReturn(artifact);
        apiProvider.addAPI(oldApi);
        
        RegistryAuthorizationManager registryAuthorizationManager = Mockito.mock(RegistryAuthorizationManager.class);
        PowerMockito.whenNew(RegistryAuthorizationManager.class).withAnyArguments()
                .thenReturn(registryAuthorizationManager);
        
        //mock has permission
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(APIUtil.getAPIPath(oldApi.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        
        //API Status is CREATED and user has permission
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("PUBLISHED");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_USERNAME)).thenReturn("user1");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(true);
        
        Mockito.when(apimgtDAO.getDefaultVersion(identifier)).thenReturn("1.0.0");
        Mockito.when(apimgtDAO.getPublishedDefaultVersion(identifier)).thenReturn("1.0.0");
        
        //updateDefaultAPIInRegistry
        String defaultAPIPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getApiName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getVersion() +
                APIConstants.API_RESOURCE_NAME;
        Resource defaultAPISourceArtifact = Mockito.mock(Resource.class);
        String defaultAPIUUID = "12640983600";
        Mockito.when(defaultAPISourceArtifact.getUUID()).thenReturn(defaultAPIUUID);
        Mockito.when(apiProvider.registry.get(defaultAPIPath)).thenReturn(defaultAPISourceArtifact);
        GenericArtifact defaultAPIArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact(defaultAPIUUID)).thenReturn(defaultAPIArtifact);
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(defaultAPIArtifact);
        TestUtils.mockAPIMConfiguration(APIConstants.API_GATEWAY_TYPE, APIConstants.API_GATEWAY_TYPE_SYNAPSE);
        
        //updateApiArtifact
        PowerMockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        Mockito.when(artifact.getId()).thenReturn("12640983654");
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                                                                                        thenReturn(apiSourcePath); 
        //Mock Updating API
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                apiProvider.createAPI(api);
                return null;
            }
        }).when(artifactManager).updateGenericArtifact(artifact);
        
        //Mocking API already not published and published
        Mockito.when(gatewayManager.isAPIPublished(Matchers.any(API.class), Matchers.anyString())).thenReturn(true);
        Mockito.when(gatewayManager.getAPIEndpointSecurityType(Matchers.any(API.class), Matchers.anyString()))
                                .thenReturn(APIConstants.APIEndpointSecurityConstants.BASIC_AUTH, 
                                        APIConstants.APIEndpointSecurityConstants.DIGEST_AUTH);        
        apiProvider.updateAPI(api);
        Assert.assertEquals(1, api.getEnvironments().size());
        Assert.assertEquals(true, api.getEnvironments().contains("SANDBOX"));
        
        //Previous updateAPI() call enabled API security. Therefore need to set it as false for the second test
        api.setEndpointSecured(false);
        apiProvider.updateAPI(api);  
        Assert.assertEquals(1, api.getEnvironments().size());
        Assert.assertEquals(true, api.getEnvironments().contains("SANDBOX"));
        
        //Test WSDL endpoint API
        api.setEndpointConfig(EP_CONFIG_WSDL);        
        PowerMockito.when(APIUtil.isValidWSDLURL(WSDL_URL, true)).thenReturn(true);
        PowerMockito.when(APIUtil.createWSDL(apiProvider.registry, api)).thenReturn("wsdl_path");
        
        apiProvider.updateAPI(api);
        Assert.assertEquals(1, api.getEnvironments().size());
        Assert.assertEquals(true, api.getEnvironments().contains("SANDBOX"));
        
    }
    
    @Test(expected = APIManagementException.class)
    public void testUpdateAPI_WithExceptionInUpdateArtifact() throws RegistryException, UserStoreException, 
            APIManagementException, FaultGatewaysException {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
        Set<URITemplate> newUriTemplates = new HashSet<URITemplate>();

        URITemplate uriTemplate1 = new URITemplate();
        uriTemplate1.setHTTPVerb("POST");
        uriTemplate1.setAuthType("Application");
        uriTemplate1.setUriTemplate("/add");
        uriTemplate1.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate1);
        
        URITemplate uriTemplate2 = new URITemplate();
        uriTemplate2.setHTTPVerb("PUT");
        uriTemplate2.setAuthType("Application");
        uriTemplate2.setUriTemplate("/update");
        uriTemplate2.setThrottlingTier("Gold");
        newUriTemplates.add(uriTemplate1);
        newUriTemplates.add(uriTemplate2);        
        
        final API api = new API(identifier);
        api.setStatus(APIStatus.CREATED);
        api.setVisibility("public");
        api.setTransports("http,https");
        api.setContext("/test");
        api.setEnvironments(environments);
        api.setUriTemplates(newUriTemplates);
        
        API oldApi = new API(identifier);
        oldApi.setStatus(APIStatus.CREATED);
        oldApi.setVisibility("public");
        oldApi.setContext("/test");        
        oldApi.setEnvironments(environments);
        api.setUriTemplates(uriTemplates);
        
        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenReturn(APIStatus.PUBLISHED);
        
        List<Documentation> documentationList = getDocumentationList();
        
        final APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, documentationList);
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, oldApi)).thenReturn(artifact);
        apiProvider.addAPI(oldApi);
        
        //mock has permission
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(APIUtil.getAPIPath(oldApi.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        
        //API Status is CREATED and user has permission
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(true);
        
        Mockito.when(apimgtDAO.getDefaultVersion(identifier)).thenReturn("1.0.0");
        Mockito.when(apimgtDAO.getPublishedDefaultVersion(identifier)).thenReturn("1.0.0");
        
        //updateDefaultAPIInRegistry
        String defaultAPIPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getApiName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getVersion() +
                APIConstants.API_RESOURCE_NAME;
        Resource defaultAPISourceArtifact = Mockito.mock(Resource.class);
        String defaultAPIUUID = "12640983600";
        Mockito.when(defaultAPISourceArtifact.getUUID()).thenReturn(defaultAPIUUID);
        Mockito.when(apiProvider.registry.get(defaultAPIPath)).thenReturn(defaultAPISourceArtifact);
        GenericArtifact defaultAPIArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact(defaultAPIUUID)).thenReturn(defaultAPIArtifact);
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(defaultAPIArtifact);
        TestUtils.mockAPIMConfiguration(APIConstants.API_GATEWAY_TYPE, APIConstants.API_GATEWAY_TYPE_SYNAPSE);
        
        //updateApiArtifact
        PowerMockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenThrow(APIManagementException.class);        
        apiProvider.updateAPI(api);
    }
    
    @Test(expected = FaultGatewaysException.class)
    public void testUpdateAPI_WithFailedGWs() throws RegistryException, UserStoreException, APIManagementException, 
                                                                FaultGatewaysException {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
        Set<URITemplate> newUriTemplates = new HashSet<URITemplate>();

        URITemplate uriTemplate1 = new URITemplate();
        uriTemplate1.setHTTPVerb("POST");
        uriTemplate1.setAuthType("Application");
        uriTemplate1.setUriTemplate("/add");
        uriTemplate1.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate1);
        
        URITemplate uriTemplate2 = new URITemplate();
        uriTemplate2.setHTTPVerb("PUT");
        uriTemplate2.setAuthType("Application");
        uriTemplate2.setUriTemplate("/update");
        uriTemplate2.setThrottlingTier("Gold");
        newUriTemplates.add(uriTemplate1);
        newUriTemplates.add(uriTemplate2);        
        
        final API api = new API(identifier);
        api.setStatus(APIStatus.PUBLISHED);
        api.setVisibility("public");
        api.setTransports("http,https");
        api.setContext("/test");
        api.setEnvironments(environments);
        api.setUriTemplates(newUriTemplates);
        
        API oldApi = new API(identifier);
        oldApi.setStatus(APIStatus.PUBLISHED);
        oldApi.setVisibility("public");
        oldApi.setContext("/test");        
        oldApi.setEnvironments(environments);
        api.setUriTemplates(uriTemplates);

        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenReturn(APIStatus.PUBLISHED);
        
        final APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, oldApi)).thenReturn(artifact);
        
        GenericArtifact artifactNew = Mockito.mock(GenericArtifact.class);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifactNew);
        apiProvider.addAPI(oldApi);
        
        //mock API artifact retrieval for has permission check
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(APIUtil.getAPIPath(oldApi.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);

        //API Status is CREATED and user has permission
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_PUBLISH)).thenReturn(true);
        PowerMockito.when(APIUtil.hasPermission(null, APIConstants.Permissions.API_CREATE)).thenReturn(true);
        
        Mockito.when(apimgtDAO.getDefaultVersion(identifier)).thenReturn("1.0.0");
        Mockito.when(apimgtDAO.getPublishedDefaultVersion(identifier)).thenReturn("1.0.0");
        
        //updateDefaultAPIInRegistry
        String defaultAPIPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getApiName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getVersion() +
                APIConstants.API_RESOURCE_NAME;
        Resource defaultAPISourceArtifact = Mockito.mock(Resource.class);
        String defaultAPIUUID = "12640983600";
        Mockito.when(defaultAPISourceArtifact.getUUID()).thenReturn(defaultAPIUUID);
        Mockito.when(apiProvider.registry.get(defaultAPIPath)).thenReturn(defaultAPISourceArtifact);
        GenericArtifact defaultAPIArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact(defaultAPIUUID)).thenReturn(defaultAPIArtifact);
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(defaultAPIArtifact);
        TestUtils.mockAPIMConfiguration(APIConstants.API_GATEWAY_TYPE, APIConstants.API_GATEWAY_TYPE_SYNAPSE);
        
        //updateApiArtifact
        Mockito.when(artifact.getId()).thenReturn("12640983654");
        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                                                                                        thenReturn(apiSourcePath); 
        //Mock Updating API
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                apiProvider.createAPI(api);
                return null;
            }
        }).when(artifactManager).updateGenericArtifact(artifact);
        
        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);
        
        
        //Mock faulty GWs
        Map<String, String> failedToPubGWEnv = new HashMap<String, String>();
        failedToPubGWEnv.put("Production", "Failed to publish");
        Map<String, String> failedToUnPubGWEnv = new HashMap<String, String>();
        failedToUnPubGWEnv.put("Production", "Failed to unpublish");
        
        Mockito.when(gatewayManager.removeFromGateway(Matchers.any(API.class), 
                Matchers.anyString())).thenReturn(failedToUnPubGWEnv);
        Mockito.when(gatewayManager.publishToGateway(Matchers.any(API.class), Matchers.any(APITemplateBuilder.class), 
                Matchers.anyString())).thenReturn(failedToPubGWEnv);
        
        apiProvider.updateAPI(api);        
    }
    
    @Test
    public void testDeleteAPI() throws RegistryException, UserStoreException, APIManagementException, 
                                                                                        WorkflowException {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName()+RegistryConstants.PATH_SEPARATOR+identifier.getVersion();
        
        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenReturn(APIStatus.PUBLISHED);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        PowerMockito.when(APIUtil.getAPIPath(identifier)).thenReturn(path);
        PowerMockito.when(apiProvider.registry.get(path)).thenReturn(apiSourceArtifact);

        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        GovernanceArtifact[] dependenciesArray = {};
        Mockito.when(artifact.getDependencies()).thenReturn(dependenciesArray);
        Mockito.doNothing().when(artifactManager).removeGenericArtifact(artifact);
        Mockito.doNothing().when(artifactManager).removeGenericArtifact("12640983654");
        
        Mockito.when(APIUtil.getIconPath(identifier)).thenReturn("icon/path");
        Mockito.when(apiProvider.registry.resourceExists("icon/path")).thenReturn(false);
        
        String apiDefinitionFilePath = APIConstants.API_DOC_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + '-'  + identifier.getVersion() + '-' + identifier.getProviderName();
        
        Mockito.when(APIUtil.getIconPath(identifier)).thenReturn(apiDefinitionFilePath);
        Mockito.when(apiProvider.registry.resourceExists(apiDefinitionFilePath)).thenReturn(true);
        
        String apiCollectionPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR + identifier.getApiName();
        CollectionImpl collection = Mockito.mock(CollectionImpl.class);
        Mockito.when(apiProvider.registry.get(apiCollectionPath)).thenReturn(collection);
        Mockito.when(collection.getChildCount()).thenReturn(0);
        Mockito.when(apiProvider.registry.resourceExists(apiCollectionPath)).thenReturn(true);
        
        String apiProviderPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName();
        CollectionImpl providerCollection = Mockito.mock(CollectionImpl.class);
        Mockito.when(apiProvider.registry.get(apiProviderPath)).thenReturn(providerCollection);
        Mockito.when(providerCollection.getChildCount()).thenReturn(0);
        Mockito.when(apiProvider.registry.resourceExists(apiProviderPath)).thenReturn(true);
        
        Mockito.when(APIUtil.getIconPath(identifier)).thenReturn(apiDefinitionFilePath);
        Mockito.when(apiProvider.registry.resourceExists(apiDefinitionFilePath)).thenReturn(true);
        
        WorkflowExecutorFactory wfe = PowerMockito.mock(WorkflowExecutorFactory.class);
        Mockito.when(WorkflowExecutorFactory.getInstance()).thenReturn(wfe);
        APIStateChangeSimpleWorkflowExecutor apiStateWFExecutor = 
                    Mockito.mock(APIStateChangeSimpleWorkflowExecutor.class);
        Mockito.when(wfe.getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_STATE)).thenReturn(apiStateWFExecutor);
        
        WorkflowDTO wfDTO = Mockito.mock(WorkflowDTO.class);
        Mockito.when(wfDTO.getStatus()).thenReturn(WorkflowStatus.APPROVED);
        Mockito.when(wfDTO.getExternalWorkflowReference()).thenReturn("REF");
        Mockito.when(apimgtDAO.retrieveWorkflowFromInternalReference("1",
                        WorkflowConstants.WF_TYPE_AM_API_STATE)).thenReturn(wfDTO);
        Mockito.doNothing().when(apiStateWFExecutor).cleanUpPendingTask("REF");        
        
        apiProvider.deleteAPI(identifier);
    }
    
    @Test(expected = APIManagementException.class)
    public void testDeleteAPI_RegistryException() throws RegistryException, UserStoreException, APIManagementException, 
                                                                                        WorkflowException {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName()+RegistryConstants.PATH_SEPARATOR+identifier.getVersion();
     
        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenReturn(APIStatus.PUBLISHED);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        PowerMockito.when(APIUtil.getAPIPath(identifier)).thenReturn(path);
        PowerMockito.when(apiProvider.registry.get(path)).thenThrow(RegistryException.class);   
        
        apiProvider.deleteAPI(identifier);
    }
    
    @Test(expected = APIManagementException.class)
    public void testDeleteAPI_WFException() throws RegistryException, UserStoreException, APIManagementException, 
                                                                                        WorkflowException {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName()+RegistryConstants.PATH_SEPARATOR+identifier.getVersion();
        
        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenReturn(APIStatus.PUBLISHED);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        PowerMockito.when(APIUtil.getAPIPath(identifier)).thenReturn(path);
        PowerMockito.when(apiProvider.registry.get(path)).thenReturn(apiSourceArtifact);
        
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        GovernanceArtifact[] dependenciesArray = {};
        Mockito.when(artifact.getDependencies()).thenReturn(dependenciesArray);
        Mockito.doNothing().when(artifactManager).removeGenericArtifact(artifact);
        Mockito.doNothing().when(artifactManager).removeGenericArtifact("12640983654");
        
        Mockito.when(APIUtil.getIconPath(identifier)).thenReturn("icon/path");
        Mockito.when(apiProvider.registry.resourceExists("icon/path")).thenReturn(false);
        
        String apiDefinitionFilePath = APIConstants.API_DOC_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + '-'  + identifier.getVersion() + '-' + identifier.getProviderName();
        
        Mockito.when(APIUtil.getIconPath(identifier)).thenReturn(apiDefinitionFilePath);
        Mockito.when(apiProvider.registry.resourceExists(apiDefinitionFilePath)).thenReturn(true);
        
        String apiCollectionPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR + identifier.getApiName();
        CollectionImpl collection = Mockito.mock(CollectionImpl.class);
        Mockito.when(apiProvider.registry.get(apiCollectionPath)).thenReturn(collection);
        Mockito.when(collection.getChildCount()).thenReturn(0);
        Mockito.when(apiProvider.registry.resourceExists(apiCollectionPath)).thenReturn(true);
        
        String apiProviderPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName();
        CollectionImpl providerCollection = Mockito.mock(CollectionImpl.class);
        Mockito.when(apiProvider.registry.get(apiProviderPath)).thenReturn(providerCollection);
        Mockito.when(providerCollection.getChildCount()).thenReturn(0);
        Mockito.when(apiProvider.registry.resourceExists(apiProviderPath)).thenReturn(true);
        
        Mockito.when(APIUtil.getIconPath(identifier)).thenReturn(apiDefinitionFilePath);
        Mockito.when(apiProvider.registry.resourceExists(apiDefinitionFilePath)).thenReturn(true);
        
        WorkflowExecutorFactory wfe = PowerMockito.mock(WorkflowExecutorFactory.class);
        Mockito.when(WorkflowExecutorFactory.getInstance()).thenReturn(wfe);
        APIStateChangeSimpleWorkflowExecutor apiStateWFExecutor = 
                    Mockito.mock(APIStateChangeSimpleWorkflowExecutor.class);
        Mockito.when(wfe.getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_STATE)).
                                                                            thenThrow(WorkflowException.class);       
        
        apiProvider.deleteAPI(identifier);
    }
    
    @Test
    public void testDeleteAPI_WithActiveSubscriptions() throws RegistryException, UserStoreException, 
                                APIManagementException, WorkflowException {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        
        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenReturn(APIStatus.PUBLISHED);        
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.getAPISubscriptionCountByAPI(identifier)).thenReturn(1L);        
        
        try {
            apiProvider.deleteAPI(identifier);
        } catch(APIManagementException e) {
            Assert.assertEquals("Cannot remove the API as active subscriptions exist.", e.getMessage());
        }       
    }
    
    @Test
    public void testAddPolicy_APIType() throws RegistryException, UserStoreException, APIManagementException { 
        
        APIPolicy policy = getPolicyAPILevelPerUser();
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.addAPIPolicy(policy)).thenReturn(policy);
        
        apiProvider.addPolicy(policy);        
    }
    
    @Test
    public void testAddPolicy_APPType() throws RegistryException, UserStoreException, APIManagementException {               
        
        ApplicationPolicy policy = getPolicyAppLevel();
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        apiProvider.addPolicy(policy);        
    }
    
    @Test
    public void testAddPolicy_SubsType() throws RegistryException, UserStoreException, APIManagementException { 
        
        SubscriptionPolicy policy = getPolicySubscriptionLevelperUser();
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        apiProvider.addPolicy(policy);        
    }
    
    @Test
    public void testAddPolicy_GlobalType() throws RegistryException, UserStoreException, APIManagementException, 
                                                                            APITemplateException { 
        GlobalPolicy policy = getPolicyGlobalLevel();
        
        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        String policyString = apiProvider.getThrottlePolicyTemplateBuilder().getThrottlePolicyForGlobalLevel(policy);
        Mockito.when(manager.validateExecutionPlan(policyString)).thenReturn(true);
        
        apiProvider.addPolicy(policy);        
    }
    
    @Test
    public void testAddPolicy_WrongType() throws RegistryException, UserStoreException, APIManagementException, 
                                                                            APITemplateException {
        Policy policy = new Policy("Test");
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        try {
            apiProvider.addPolicy(policy); 
        } catch(Exception e) {
            Assert.assertEquals("Policy type " + policy.getClass().getName() + " is not supported", e.getMessage());
        }               
    }
    
    @Test
    public void testUpdatePolicy_APIType() throws RegistryException, UserStoreException, APIManagementException {
        
        APIPolicy policy = getPolicyAPILevelPerUser();
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.getAPIPolicy(policy.getPolicyName(), policy.getTenantId())).thenReturn(policy);
        Mockito.when(apimgtDAO.updateAPIPolicy(policy)).thenReturn(policy);
        
        apiProvider.updatePolicy(policy);
    }
    
    @Test
    public void testUpdatePolicy_AppType() throws RegistryException, UserStoreException, APIManagementException {
        
        SubscriptionPolicy policy = getPolicySubscriptionLevelperUser();
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.getSubscriptionPolicy(policy.getPolicyName(), policy.getTenantId())).thenReturn(policy);
        Mockito.doNothing().when(apimgtDAO).updateSubscriptionPolicy(policy);
        
        apiProvider.updatePolicy(policy);
    }
    
    @Test
    public void testUpdatePolicy_GlobalType() throws RegistryException, UserStoreException, APIManagementException, 
                    APITemplateException {
        
        GlobalPolicy policy = getPolicyGlobalLevel();
        
        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.getGlobalPolicy(policy.getPolicyName())).thenReturn(policy);
        Mockito.doNothing().when(apimgtDAO).updateGlobalPolicy(policy);
        
        String policyString = apiProvider.getThrottlePolicyTemplateBuilder().getThrottlePolicyForGlobalLevel(policy);
        Mockito.when(manager.validateExecutionPlan(policyString)).thenReturn(true);
        
        apiProvider.updatePolicy(policy);
    }
    
    @Test
    public void testUpdatePolicy_GlobalTypeInvalidPlan() throws RegistryException, UserStoreException, 
            APIManagementException, APITemplateException {
        
        GlobalPolicy policy = getPolicyGlobalLevel();
        
        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.getGlobalPolicy(policy.getPolicyName())).thenReturn(policy);
        Mockito.doNothing().when(apimgtDAO).updateGlobalPolicy(policy);
        
        String policyString = apiProvider.getThrottlePolicyTemplateBuilder().getThrottlePolicyForGlobalLevel(policy);
        Mockito.when(manager.validateExecutionPlan(policyString)).thenReturn(false);
        
        try {
            apiProvider.updatePolicy(policy);
        } catch(Exception e) {
            Assert.assertEquals("Invalid Execution Plan", e.getMessage());
        }
    }
    
    @Test
    public void testUpdatePolicy_GlobalTypeAlreadyExist() throws RegistryException, UserStoreException, 
            APIManagementException, APITemplateException {
        
        GlobalPolicy policy = getPolicyGlobalLevel();
        
        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.getGlobalPolicy(policy.getPolicyName())).thenReturn(policy);
        Mockito.doNothing().when(apimgtDAO).updateGlobalPolicy(policy);
        
        String policyString = apiProvider.getThrottlePolicyTemplateBuilder().getThrottlePolicyForGlobalLevel(policy);
        Mockito.when(manager.validateExecutionPlan(policyString)).thenReturn(true);
        
        Mockito.when(apimgtDAO.isKeyTemplatesExist(policy)).thenReturn(true);
        
        try {
            apiProvider.updatePolicy(policy);
        } catch(Exception e) {
            Assert.assertEquals("Key Template Already Exist", e.getMessage());
        }
    }
    
    @Test
    public void testUpdatePolicy_SubsType() throws RegistryException, UserStoreException, APIManagementException {
        
        ApplicationPolicy policy = getPolicyAppLevel();
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.getApplicationPolicy(policy.getPolicyName(), policy.getTenantId())).thenReturn(policy);
        Mockito.doNothing().when(apimgtDAO).updateApplicationPolicy(policy);
        
        apiProvider.updatePolicy(policy);
    }
    
    @Test
    public void testUpdatePolicy_WrongType() throws RegistryException, UserStoreException, APIManagementException {
        
        Policy policy = new Policy("Test");
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        try {
            apiProvider.updatePolicy(policy);
        } catch(Exception e) {
            Assert.assertEquals("Policy type " + policy.getClass().getName() + " is not supported", e.getMessage());
        }    
    }
    
    @Test
    public void testUpdatePolicy_APITypeErrorWhileDeploying() throws RegistryException, UserStoreException, 
                                                APIManagementException {
        
        APIPolicy policy = getPolicyAPILevelPerUser();
        
        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.getAPIPolicy(policy.getPolicyName(), policy.getTenantId())).thenReturn(policy);
        Mockito.when(apimgtDAO.updateAPIPolicy(policy)).thenReturn(policy);
        
        Mockito.doThrow(APIManagementException.class).when(manager).undeployPolicyFromGlobalCEP(Matchers.anyString());
        
        try {
            apiProvider.updatePolicy(policy);
        } catch(Exception e) {
            Assert.assertEquals("Error while deploying policy to gateway", e.getMessage());
            Assert.assertEquals(PolicyDeploymentFailureException.class, e.getClass());
        }  
    }
    
    @Test
    public void testDeletePolicy_APIType() throws RegistryException, UserStoreException, APIManagementException {
        
        APIPolicy policy = getPolicyAPILevelPerUser();
        policy.setDeployed(true);
        String policyName = "custom1";
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.getAPIPolicy(policyName, APIUtil.getTenantId("admin"))).thenReturn(policy);
        
        apiProvider.deletePolicy("admin", PolicyConstants.POLICY_LEVEL_API, policyName);
    }
    
    @Test
    public void testDeletePolicy_APItypeErrorWhileDeploying() throws RegistryException, UserStoreException, 
                APIManagementException {
        
        APIPolicy policy = getPolicyAPILevelPerUser();
        policy.setDeployed(true);
        String policyName = "custom1";
        
        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.getAPIPolicy(policyName, APIUtil.getTenantId("admin"))).thenReturn(policy);
        
        List<String> policyFileNames = new ArrayList<String>();
        String policyFile = policy.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_RESOURCE + "_" + policyName;
        policyFileNames.add(policyFile + "_default");
        for (Pipeline pipeline : policy.getPipelines()) {
            policyFileNames.add(policyFile + "_condition_" + pipeline.getId());
        }
        Mockito.doThrow(APIManagementException.class).when(manager).undeployPolicyFromGatewayManager(
                policyFileNames.toArray(new String[policyFileNames.size()]));
        
        try {
            apiProvider.deletePolicy("admin", PolicyConstants.POLICY_LEVEL_API, policyName);
        } catch(Exception e) {
            Assert.assertEquals("Error while undeploying policy: ", e.getMessage());
            Assert.assertEquals(APIManagementException.class, e.getClass());
        }        
    }
    
    @Test
    public void testDeletePolicy_AppType() throws RegistryException, UserStoreException, APIManagementException {
        
        ApplicationPolicy policy = getPolicyAppLevel();
        policy.setDeployed(true);
        String policyName = "gold";
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.getApplicationPolicy(policyName, APIUtil.getTenantId("admin"))).thenReturn(policy);
        
        apiProvider.deletePolicy("admin", PolicyConstants.POLICY_LEVEL_APP, policyName);
    }
    
    @Test
    public void testDeletePolicy_AppTypeErrorWhileDeploying() throws RegistryException, UserStoreException, 
                    APIManagementException {
        
        ApplicationPolicy policy = getPolicyAppLevel();
        policy.setDeployed(true);
        String policyName = "gold";
        
        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.getApplicationPolicy(policyName, APIUtil.getTenantId("admin"))).thenReturn(policy);
        
        List<String> policyFileNames = new ArrayList<String>();
        String policyFile = policy.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_APP + "_" + policyName;
        policyFileNames.add(policyFile);
        Mockito.doThrow(APIManagementException.class).when(manager).undeployPolicyFromGatewayManager(
                policyFileNames.toArray(new String[policyFileNames.size()]));
        
        try {
            apiProvider.deletePolicy("admin", PolicyConstants.POLICY_LEVEL_APP, policyName);
        } catch(Exception e) {
            Assert.assertEquals("Error while undeploying policy: ", e.getMessage());
            Assert.assertEquals(APIManagementException.class, e.getClass());
        }     
    }
    
    @Test
    public void testDeletePolicy_SubType() throws RegistryException, UserStoreException, APIManagementException {
        
        SubscriptionPolicy policy = getPolicySubscriptionLevelperUser();
        policy.setDeployed(true);
        String policyName = "gold";
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.getSubscriptionPolicy(policyName, APIUtil.getTenantId("admin"))).thenReturn(policy);
        
        apiProvider.deletePolicy("admin", PolicyConstants.POLICY_LEVEL_SUB, policyName);
    }
    
    @Test
    public void testDeletePolicy_SubTypeErrorWhileDeploying() throws RegistryException, UserStoreException, 
                    APIManagementException {
        
        SubscriptionPolicy policy = getPolicySubscriptionLevelperUser();
        policy.setDeployed(true);
        String policyName = "gold";
        
        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.getSubscriptionPolicy(policyName, APIUtil.getTenantId("admin"))).thenReturn(policy);
        
        List<String> policyFileNames = new ArrayList<String>();
        String policyFile = policy.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_SUB + "_" + policyName;
        policyFileNames.add(policyFile);
        Mockito.doThrow(APIManagementException.class).when(manager).undeployPolicyFromGatewayManager(
                policyFileNames.toArray(new String[policyFileNames.size()]));
        
        try {
            apiProvider.deletePolicy("admin", PolicyConstants.POLICY_LEVEL_SUB, policyName);
        } catch(Exception e) {
            Assert.assertEquals("Error while undeploying policy: ", e.getMessage());
            Assert.assertEquals(APIManagementException.class, e.getClass());
        }        
    }
    
    @Test
    public void testDeletePolicy_GlobalType() throws RegistryException, UserStoreException, APIManagementException {
        
        GlobalPolicy policy = getPolicyGlobalLevel();
        policy.setDeployed(true);
        String policyName = "gold";
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.getGlobalPolicy(policyName)).thenReturn(policy);
        
        apiProvider.deletePolicy("admin", PolicyConstants.POLICY_LEVEL_GLOBAL, policyName);
    }
    
    @Test
    public void testDeletePolicy_GlobalTypeErrorWhileDeploying() throws RegistryException, UserStoreException, 
                    APIManagementException {
        
        GlobalPolicy policy = getPolicyGlobalLevel();
        policy.setDeployed(true);
        String policyName = "gold";
        
        ThrottlePolicyDeploymentManager manager = Mockito.mock(ThrottlePolicyDeploymentManager.class);
        PowerMockito.when(ThrottlePolicyDeploymentManager.getInstance()).thenReturn(manager);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(apimgtDAO.getGlobalPolicy(policyName)).thenReturn(policy);
        
        List<String> policyFileNames = new ArrayList<String>();
        String policyFile = PolicyConstants.POLICY_LEVEL_GLOBAL + "_" + policyName;
        policyFileNames.add(policyFile);
        Mockito.doThrow(APIManagementException.class).when(manager).undeployPolicyFromGatewayManager(
                policyFileNames.toArray(new String[policyFileNames.size()]));
        
        try {
            apiProvider.deletePolicy("admin", PolicyConstants.POLICY_LEVEL_GLOBAL, policyName);
        } catch(Exception e) {
            Assert.assertEquals("Error while undeploying policy: ", e.getMessage());
            Assert.assertEquals(APIManagementException.class, e.getClass());
        }        
    }
    
    @Test
    public void testGetAPILifeCycleData() throws Exception {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName()+RegistryConstants.PATH_SEPARATOR+identifier.getVersion();
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        PowerMockito.when(APIUtil.getAPIPath(identifier)).thenReturn(path);
        PowerMockito.when(apiProvider.registry.get(path)).thenReturn(apiSourceArtifact);
        
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        Mockito.when(artifact.getLifecycleState()).thenReturn("Created");
        
        LifecycleBean bean = getLCBean();
        
        Mockito.when(LifecycleBeanPopulator.getLifecycleBean(path, (UserRegistry) apiProvider.registry, 
                apiProvider.configRegistry)).thenReturn(bean);
        
        Map<String, Object> lcData = apiProvider.getAPILifeCycleData(identifier);
        List checkListItems = (List) lcData.get(APIConstants.LC_CHECK_ITEMS);
        
        Assert.assertEquals(2, checkListItems.size());

        if (checkListItems.get(0) instanceof CheckListItem) {
            CheckListItem checkListItem = (CheckListItem) checkListItems.get(0);
            Assert.assertTrue(("Requires re-subscription when publish the API").equals(checkListItem.getName()) || 
                    ("Deprecate old versions after publish the API").equals(checkListItem.getName()));
            
            if (("Requires re-subscription when publish the API").equals(checkListItem.getName())) {
                Assert.assertEquals("1", checkListItem.getOrder());
            } else {
                Assert.assertEquals("0", checkListItem.getOrder());
            }
            
            Assert.assertEquals("Created", checkListItem.getLifeCycleStatus());
        }
        
        if (checkListItems.get(1) instanceof CheckListItem) {
            CheckListItem checkListItem = (CheckListItem) checkListItems.get(1);
            Assert.assertTrue(("Requires re-subscription when publish the API").equals(checkListItem.getName()) || 
                    ("Deprecate old versions after publish the API").equals(checkListItem.getName()));
            if (("Requires re-subscription when publish the API").equals(checkListItem.getName())) {
                Assert.assertEquals("1", checkListItem.getOrder());
            } else {
                Assert.assertEquals("0", checkListItem.getOrder());
            }
            Assert.assertEquals("Created", checkListItem.getLifeCycleStatus());
        }
    }
    
    @Test
    public void testGetAPILifeCycleData_WithException() throws Exception {
        APIIdentifier identifier = new APIIdentifier("admin-AT-carbon.super", "API1", "1.0.0");
        
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName()+RegistryConstants.PATH_SEPARATOR+identifier.getVersion();
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        PowerMockito.when(APIUtil.getAPIPath(identifier)).thenReturn(path);
        PowerMockito.when(apiProvider.registry.get(path)).thenReturn(apiSourceArtifact);
        
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        Mockito.when(artifact.getLifecycleState()).thenReturn("Created");
        
        Mockito.when(LifecycleBeanPopulator.getLifecycleBean(path, 
                (UserRegistry) apiProvider.registry, apiProvider.configRegistry)).
                thenThrow(new Exception("Failed to get LC data"));
        
        try {
            apiProvider.getAPILifeCycleData(identifier);
        } catch(APIManagementException e) {
            Assert.assertEquals("Failed to get LC data", e.getMessage());
        }
    }
    
    @Test
    public void testUpdateAPIforStateChange_ToPublished() throws RegistryException, UserStoreException, 
            APIManagementException, FaultGatewaysException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        environments.add("Production");
        environments.add("Sandbox");
        
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIStatus.CREATED);
        api.setAsDefaultVersion(true);
        api.setEnvironments(environments);
        
        Mockito.when(apimgtDAO.getPublishedDefaultVersion(apiId)).thenReturn("1.0.0");
        
        Map<String, String> failedGWEnv = new HashMap<String, String>();    
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        apiProvider.addAPI(api);
        
        //Mock Updating API
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);
        
        PowerMockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        Mockito.when(artifact.getId()).thenReturn("12640983654");

        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                                                                                        thenReturn(apiSourcePath); 
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);
        
        apiProvider.updateAPIforStateChange(apiId, APIStatus.PUBLISHED, failedGWEnv);
        
        //From the 2 environments, only 1 was successful
        Assert.assertEquals(2, api.getEnvironments().size());
    }
    
    @Test
    public void testUpdateAPIforStateChange_ToRetired() throws RegistryException, UserStoreException, 
            APIManagementException, FaultGatewaysException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        environments.add("Production");
        environments.add("Sandbox");
        
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIStatus.CREATED);
        api.setAsDefaultVersion(true);
        api.setEnvironments(environments);
        
        Mockito.when(apimgtDAO.getPublishedDefaultVersion(apiId)).thenReturn("1.0.0");
        
        Map<String, String> failedGWEnv = new HashMap<String, String>();    
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null); 
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        apiProvider.addAPI(api);
        
        //Mock Updating API
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);
        
        PowerMockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        Mockito.when(artifact.getId()).thenReturn("12640983654");

        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                                                                                        thenReturn(apiSourcePath); 
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);
        
        boolean status = apiProvider.updateAPIforStateChange(apiId, APIStatus.RETIRED, failedGWEnv);
        
        Assert.assertEquals(2, api.getEnvironments().size());
        Assert.assertEquals(true, status);
    }
    
    @Test
    public void testUpdateAPIforStateChange_ToPublishedWithFaultyGWs() throws RegistryException, UserStoreException, 
            APIManagementException, FaultGatewaysException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        environments.add("Production");
        environments.add("Sandbox");
        
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIStatus.CREATED);
        api.setAsDefaultVersion(true);
        api.setEnvironments(environments);
        
        Mockito.when(apimgtDAO.getPublishedDefaultVersion(apiId)).thenReturn("1.0.0");
        
        Map<String, String> failedGWEnv = new HashMap<String, String>();
        failedGWEnv.put("Production", "Failed to publish");        
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        apiProvider.addAPI(api);
        
        //Mock Updating API
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);
        
        PowerMockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        Mockito.when(artifact.getId()).thenReturn("12640983654");

        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                                                                                        thenReturn(apiSourcePath); 
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);
        
        try {
            apiProvider.updateAPIforStateChange(apiId, APIStatus.PUBLISHED, failedGWEnv);
        } catch(FaultGatewaysException e) {
            Assert.assertTrue(e.getFaultMap().contains("Failed to publish"));
        }
        
        //From the 2 environments, only 1 was successful
        Assert.assertEquals(1, api.getEnvironments().size());
    }
    
    @Test
    public void testUpdateAPIforStateChange_ToRetiredWithFaultyGWs() throws RegistryException, UserStoreException, 
            APIManagementException, FaultGatewaysException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        environments.add("Production");
        
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIStatus.CREATED);
        api.setAsDefaultVersion(true);
        api.setEnvironments(environments);
        
        Mockito.when(apimgtDAO.getPublishedDefaultVersion(apiId)).thenReturn("1.0.0");
        
        Map<String, String> failedGWEnv = new HashMap<String, String>();
        failedGWEnv.put("Sandbox", "Failed to un-publish");        
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);   
        
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
        Mockito.when(APIUtil.createAPIArtifactContent(artifact, api)).thenReturn(artifact);
        apiProvider.addAPI(api);
        
        //Mock Updating API
        Resource apiSourceArtifact = Mockito.mock(Resource.class);
        Mockito.when(apiSourceArtifact.getUUID()).thenReturn("12640983654");
        String apiSourcePath = "path";
        PowerMockito.when(APIUtil.getAPIPath(api.getId())).thenReturn(apiSourcePath);
        PowerMockito.when(apiProvider.registry.get(apiSourcePath)).thenReturn(apiSourceArtifact);
        
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("CREATED");
        Mockito.when(artifactManager.getGenericArtifact(apiSourceArtifact.getUUID())).thenReturn(artifact);
        
        Mockito.when(gatewayManager.isAPIPublished(api, "carbon.super")).thenReturn(false);
        
        Mockito.when(artifact.getId()).thenReturn("12640983654");

        PowerMockito.when(GovernanceUtils.getArtifactPath(apiProvider.registry, "12640983654")).
                                                                                        thenReturn(apiSourcePath); 
        Mockito.doNothing().when(artifactManager).updateGenericArtifact(artifact);
        
        try {
            apiProvider.updateAPIforStateChange(apiId, APIStatus.RETIRED, failedGWEnv);
        } catch(FaultGatewaysException e) {
            Assert.assertTrue(e.getFaultMap().contains("Failed to un-publish"));
        }
        
        //API was going to be removed from 1 gateway, but removal has failed. Therefore API is available 
        // in both environments
        Assert.assertEquals(2, api.getEnvironments().size());
    }
    
    @Test
    public void testUpdateAPIforStateChange_InvalidAPI() throws RegistryException, UserStoreException, 
            APIManagementException, FaultGatewaysException {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        Set<String> environments = new HashSet<String>();
        environments.add("Production");
        environments.add("Sandbox");
        
        API api = new API(apiId);
        api.setContext("/test");
        api.setStatus(APIStatus.CREATED);
        api.setAsDefaultVersion(true);
        api.setEnvironments(environments);
        
        Mockito.when(apimgtDAO.getPublishedDefaultVersion(apiId)).thenReturn("1.0.0");
        
        Map<String, String> failedGWEnv = new HashMap<String, String>();   
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        try {
            apiProvider.updateAPIforStateChange(apiId, APIStatus.PUBLISHED, failedGWEnv);
        } catch(APIManagementException e) {
            Assert.assertEquals("Couldn't find an API with the name-" + apiId.getApiName() + "version-" 
                    + apiId.getVersion(), e.getMessage());
        }
    }
    
    @Test
    public void testGetAllPaginatedAPIs() throws RegistryException, UserStoreException, APIManagementException {
        APIIdentifier apiId1 = new APIIdentifier("admin", "API1", "1.0.0");
        API api1 = new API(apiId1);
        api1.setContext("/test");
        
        APIIdentifier apiId2 = new APIIdentifier("admin", "API2", "1.0.0");
        API api2 = new API(apiId2);
        api2.setContext("/test1");
        
        PaginationContext paginationCtx = Mockito.mock(PaginationContext.class);
        PowerMockito.when(PaginationContext.getInstance()).thenReturn(paginationCtx);
        Mockito.when(paginationCtx.getLength()).thenReturn(2);
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        ServiceReferenceHolder sh = TestUtils.getServiceReferenceHolder();
        RegistryService registryService = Mockito.mock(RegistryService.class);
        PowerMockito.when(sh.getRegistryService()).thenReturn(registryService);
        UserRegistry userReg = Mockito.mock(UserRegistry.class);
        PowerMockito.when(registryService.getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME,
                -1234)).thenReturn(userReg);
        
        PowerMockito.when(APIUtil.getArtifactManager(userReg, APIConstants.API_KEY)).thenReturn(artifactManager);
        
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);
        
        PowerMockito.when(sh.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        PowerMockito.when(amConfig.getFirstProperty(APIConstants.API_PUBLISHER_APIS_PER_PAGE)).thenReturn("2");
        
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tm = Mockito.mock(TenantManager.class);
        
        PowerMockito.when(sh.getRealmService()).thenReturn(realmService);
        PowerMockito.when(realmService.getTenantManager()).thenReturn(tm);
        PowerMockito.when(tm.getTenantId("carbon.super")).thenReturn(-1234);
        
        GenericArtifact genericArtifact1 = Mockito.mock(GenericArtifact.class);
        GenericArtifact genericArtifact2 = Mockito.mock(GenericArtifact.class);
        
        Mockito.when(APIUtil.getAPI(genericArtifact1)).thenReturn(api1);
        Mockito.when(APIUtil.getAPI(genericArtifact2)).thenReturn(api2);
        
        GenericArtifact[] genericArtifacts = {genericArtifact1, genericArtifact2};
        GenericArtifact[] genericArtifacts1 = {};
        
        Mockito.when(artifactManager.findGenericArtifacts(Matchers.anyMap())).thenReturn(genericArtifacts, 
                genericArtifacts1);
        
        Map<String, Object> result = apiProvider.getAllPaginatedAPIs("carbon.super", 0, 10);
        
        List<API> apiList = (List<API>) result.get("apis");
        Assert.assertEquals(2, apiList.size());
        Assert.assertEquals("API1", apiList.get(0).getId().getApiName());
        Assert.assertEquals("API2", apiList.get(1).getId().getApiName());
        Assert.assertEquals(2, result.get("totalLength"));
        
        //No APIs available        
        Map<String, Object> result1 = apiProvider.getAllPaginatedAPIs("carbon.super", 0, 10);
        
        List<API> apiList1 = (List<API>) result1.get("apis");
        Assert.assertEquals(0, apiList1.size());
        
        //Registry Exception while retrieving artifacts
        Mockito.when(artifactManager.findGenericArtifacts(Matchers.anyMap())).thenThrow(RegistryException.class);
        try {
            apiProvider.getAllPaginatedAPIs("carbon.super", 0, 10);
        } catch(APIManagementException e) {
            Assert.assertEquals("Failed to get all APIs", e.getMessage());
        }
    }
    
    @Test
    public void testAddDocumentationContent() throws Exception {
        APIIdentifier apiId = new APIIdentifier("admin", "API1", "1.0.0");
        API api = new API(apiId);
        
        String docName = "HowTo";
        Documentation doc = new Documentation(DocumentationType.HOWTO, docName);
        doc.setVisibility(DocumentVisibility.API_LEVEL);
        
        String docPath = "/apimgt/applicationdata/provider/admin/API1/1.0.0/documentation/contents";
        
        String documentationPath = docPath + docName;
        String contentPath = docPath + APIConstants.INLINE_DOCUMENT_CONTENT_DIR +
                RegistryConstants.PATH_SEPARATOR + docName;
        
        Mockito.when(APIUtil.getAPIDocPath(apiId)).thenReturn(docPath);
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        Resource docResource = Mockito.mock(Resource.class);
        Mockito.when(docResource.getUUID()).thenReturn("678ghk");
        Mockito.when(apiProvider.registry.get(documentationPath)).thenReturn(docResource);
        
        GenericArtifact docArtifact = Mockito.mock(GenericArtifact.class);

        PowerMockito.whenNew(GenericArtifactManager.class).withAnyArguments().thenReturn(artifactManager);
        Mockito.when(artifactManager.getGenericArtifact("678ghk")).thenReturn(docArtifact);
        Mockito.when(APIUtil.getDocumentation(docArtifact)).thenReturn(doc);
                
        Resource docContent = Mockito.mock(Resource.class);
        Mockito.when(apiProvider.registry.resourceExists(contentPath)).thenReturn(true, false);
        Mockito.when(apiProvider.registry.get(contentPath)).thenReturn(docContent);
        Mockito.when(apiProvider.registry.newResource()).thenReturn(docContent);
        
        apiProvider.addDocumentationContent(api, docName, "content");
        
        RegistryAuthorizationManager registryAuthorizationManager = Mockito.mock(RegistryAuthorizationManager.class);
        PowerMockito.whenNew(RegistryAuthorizationManager.class).withAnyArguments()
                .thenReturn(registryAuthorizationManager);
        apiProvider.tenantDomain = "carbon.super";
        
        doc.setVisibility(DocumentVisibility.OWNER_ONLY);
        apiProvider.addDocumentationContent(api, docName, "content");
        
        doc.setVisibility(DocumentVisibility.PRIVATE);
        apiProvider.addDocumentationContent(api, docName, "content");
        
        Mockito.doThrow(RegistryException.class).when(apiProvider.registry).put(Matchers.anyString(), 
                Matchers.any(Resource.class));
        try {
            apiProvider.addDocumentationContent(api, docName, "content");
        } catch(APIManagementException e) {
            String msg = "Failed to add the documentation content of : "
                    + docName + " of API :" + apiId.getApiName();
            Assert.assertEquals(msg, e.getMessage());            
        }
    }
    
    @Test
    public void testSearchAPIs() throws APIManagementException, RegistryException {
        //APIs of the provider
        API api1 = new API(new APIIdentifier("admin", "API1", "1.0.1"));
        api1.setContext("api1context");
        api1.setStatus(APIStatus.PUBLISHED);
        api1.setDescription("API 1 Desciption");
        
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
       
        URITemplate uriTemplate1 = new URITemplate();
        uriTemplate1.setHTTPVerb("POST");
        uriTemplate1.setAuthType("Application");
        uriTemplate1.setUriTemplate("/add");
        uriTemplate1.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate1);
        
        api1.setUriTemplates(uriTemplates);
        
        API api2 = new API(new APIIdentifier("admin", "API2", "1.0.0"));
        api2.setContext("api2context");
        api2.setStatus(APIStatus.CREATED);
        api2.setDescription("API 2 Desciption");
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        prepareForGetAPIsByProvider(artifactManager, apiProvider, "admin", api1, api2);
        
        //Search by Name matching both APIs
        List<API> foundApiList0 = apiProvider.searchAPIs("API", "Name", "admin"); 
        Assert.assertNotNull(foundApiList0);
        Assert.assertEquals(2, foundApiList0.size());
        
        //Search by exact name
        List<API> foundApiList1 = apiProvider.searchAPIs("API1", "Name", "admin");
        Assert.assertNotNull(foundApiList1);
        Assert.assertEquals(1, foundApiList1.size());
        
        //Search by exact provider
        List<API> foundApiList2 = apiProvider.searchAPIs("admin", "Provider", "admin");
        Assert.assertNotNull(foundApiList2);
        Assert.assertEquals(2, foundApiList2.size());
        
        //Search by exact version
        List<API> foundApiList3 = apiProvider.searchAPIs("1.0.0", "Version", "admin");
        Assert.assertNotNull(foundApiList3);
        Assert.assertEquals(1, foundApiList3.size());
        
        //Search by exact context
        List<API> foundApiList4 = apiProvider.searchAPIs("api1context", "Context", "admin");
        Assert.assertNotNull(foundApiList4);
        Assert.assertEquals(1, foundApiList4.size());
        
        //Search by exact context
        List<API> foundApiList5 = apiProvider.searchAPIs("api2context", "Context", "admin");
        Assert.assertNotNull(foundApiList5);
        Assert.assertEquals(1, foundApiList5.size());
        
        //Search by wrong context
        List<API> foundApiList6 = apiProvider.searchAPIs("test", "Context", "admin");
        Assert.assertNotNull(foundApiList6);
        Assert.assertEquals(0, foundApiList6.size());
        
        //Search by status
        List<API> foundApiList7 = apiProvider.searchAPIs("Published", "Status", "admin");
        Assert.assertNotNull(foundApiList7);
        Assert.assertEquals(1, foundApiList7.size());
        
        //Search by Description
        List<API> foundApiList8 = apiProvider.searchAPIs("API 1 Desciption", "Description", "admin");
        Assert.assertNotNull(foundApiList8);
        Assert.assertEquals(1, foundApiList8.size());
        
        //Search by Description
        List<API> foundApiList9 = apiProvider.searchAPIs("API", "Description", "admin");
        Assert.assertNotNull(foundApiList9);
        Assert.assertEquals(2, foundApiList9.size());
        
        //Search by Subcontext
        List<API> foundApiList10 = apiProvider.searchAPIs("add", "Subcontext", "admin");
        Assert.assertNotNull(foundApiList10);
        Assert.assertEquals(1, foundApiList10.size());
    }
    
    @Test
    public void testSearchAPIs_NoProviderId() throws APIManagementException, RegistryException {
        API api1 = new API(new APIIdentifier("admin", "API1", "1.0.1"));
        api1.setContext("api1context");
        api1.setStatus(APIStatus.PUBLISHED);
        api1.setDescription("API 1 Desciption");
        
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
       
        URITemplate uriTemplate1 = new URITemplate();
        uriTemplate1.setHTTPVerb("POST");
        uriTemplate1.setAuthType("Application");
        uriTemplate1.setUriTemplate("/add");
        uriTemplate1.setThrottlingTier("Gold");
        uriTemplates.add(uriTemplate1);
        
        api1.setUriTemplates(uriTemplates);
        
        API api2 = new API(new APIIdentifier("admin", "API2", "1.0.0"));
        api2.setContext("api2context");
        api2.setStatus(APIStatus.CREATED);
        api2.setDescription("API 2 Desciption");
        
        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        GenericArtifact genericArtifact1 = Mockito.mock(GenericArtifact.class);
        GenericArtifact genericArtifact2 = Mockito.mock(GenericArtifact.class);
        
        Mockito.when(genericArtifact1.getAttribute(APIConstants.API_OVERVIEW_NAME)).thenReturn("API1");
        Mockito.when(genericArtifact1.getAttribute(APIConstants.API_OVERVIEW_VERSION)).thenReturn("1.0.1");
        Mockito.when(genericArtifact1.getAttribute(APIConstants.API_OVERVIEW_CONTEXT)).thenReturn("api1context");
        Mockito.when(genericArtifact1.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION)).thenReturn(
                "API 1 Desciption");
        Mockito.when(genericArtifact1.getAttribute(APIConstants.API_OVERVIEW_PROVIDER)).thenReturn("admin");
        Mockito.when(genericArtifact1.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("Published");
        
        Mockito.when(genericArtifact2.getAttribute(APIConstants.API_OVERVIEW_NAME)).thenReturn("API2");
        Mockito.when(genericArtifact2.getAttribute(APIConstants.API_OVERVIEW_VERSION)).thenReturn("1.0.0");
        Mockito.when(genericArtifact2.getAttribute(APIConstants.API_OVERVIEW_CONTEXT)).thenReturn("api2context");
        Mockito.when(genericArtifact2.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION)).thenReturn(
                "API 2 Desciption");
        Mockito.when(genericArtifact2.getAttribute(APIConstants.API_OVERVIEW_PROVIDER)).thenReturn("admin");
        Mockito.when(genericArtifact2.getAttribute(APIConstants.API_OVERVIEW_STATUS)).thenReturn("Created");
        
        Mockito.when(APIUtil.getAPI(genericArtifact1, apiProvider.registry)).thenReturn(api1);
        Mockito.when(APIUtil.getAPI(genericArtifact2, apiProvider.registry)).thenReturn(api2);
        Mockito.when(APIUtil.getAPI(genericArtifact1)).thenReturn(api1);
        Mockito.when(APIUtil.getAPI(genericArtifact2)).thenReturn(api2);
        
        GenericArtifact[] genericArtifacts = {genericArtifact1, genericArtifact2};
        
        Mockito.when(artifactManager.getAllGenericArtifacts()).thenReturn(genericArtifacts);
        
        //Search by Name matching both APIs
        List<API> foundApiList0 = apiProvider.searchAPIs("API", "Name", null); 
        Assert.assertNotNull(foundApiList0);
        Assert.assertEquals(2, foundApiList0.size());
        
        //Search by exact name
        List<API> foundApiList1 = apiProvider.searchAPIs("API1", "Name", null);
        Assert.assertNotNull(foundApiList1);
        Assert.assertEquals(1, foundApiList1.size());
        
        //Search by exact provider
        List<API> foundApiList2 = apiProvider.searchAPIs("admin", "Provider", null);
        Assert.assertNotNull(foundApiList2);
        Assert.assertEquals(2, foundApiList2.size());
        
        //Search by exact version
        List<API> foundApiList3 = apiProvider.searchAPIs("1.0.0", "Version", null);
        Assert.assertNotNull(foundApiList3);
        Assert.assertEquals(1, foundApiList3.size());
        
        //Search by exact context
        List<API> foundApiList4 = apiProvider.searchAPIs("api1context", "Context", null);
        Assert.assertNotNull(foundApiList4);
        Assert.assertEquals(1, foundApiList4.size());
        
        //Search by exact context
        List<API> foundApiList5 = apiProvider.searchAPIs("api2context", "Context", null);
        Assert.assertNotNull(foundApiList5);
        Assert.assertEquals(1, foundApiList5.size());
        
        //Search by wrong context
        List<API> foundApiList6 = apiProvider.searchAPIs("test", "Context", null);
        Assert.assertNotNull(foundApiList6);
        Assert.assertEquals(0, foundApiList6.size());
        
        //Search by status
        List<API> foundApiList7 = apiProvider.searchAPIs("Published", "Status", null);
        Assert.assertNotNull(foundApiList7);
        Assert.assertEquals(1, foundApiList7.size());
        
        //Search by Description
        List<API> foundApiList8 = apiProvider.searchAPIs("API 1 Desciption", "Description", null);
        Assert.assertNotNull(foundApiList8);
        Assert.assertEquals(1, foundApiList8.size());
        
        //Search by Description
        List<API> foundApiList9 = apiProvider.searchAPIs("API", "Description", null);
        Assert.assertNotNull(foundApiList9);
        Assert.assertEquals(2, foundApiList9.size());
        
        //Search by Subcontext
        List<API> foundApiList10 = apiProvider.searchAPIs("add", "Subcontext", null);
        Assert.assertNotNull(foundApiList10);
        Assert.assertEquals(1, foundApiList10.size());
    }
    
    @Test
    public void testSearchAPIs_WhenNoAPIs() throws APIManagementException, RegistryException {
        String providerId = "admin";
        String providerPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + providerId;

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        //Mock to return 0 APIs by provider
        Association[] associactions = {};
        Mockito.when(apiProvider.registry.getAssociations(providerPath, APIConstants.PROVIDER_ASSOCIATION)).
                                thenReturn(associactions);
        
        List<API> foundApiList0 = apiProvider.searchAPIs("API", "Name", "admin"); 
        Assert.assertNotNull(foundApiList0);
        Assert.assertEquals(0, foundApiList0.size());
    }
    
    @Test
    public void testSearchAPIs_ForException() throws APIManagementException, RegistryException {
        String providerId = "admin";
        String providerPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + providerId;

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        //Mock to throw exception when retrieving APIs by provider
        Mockito.when(apiProvider.registry.getAssociations(providerPath, APIConstants.PROVIDER_ASSOCIATION)).
                                thenThrow(RegistryException.class);
        
        try {
            apiProvider.searchAPIs("API", "Name", "admin");
        } catch(APIManagementException e) {
            Assert.assertEquals("Failed to search APIs with type", e.getMessage());
        }
    }
    
    @Test
    public void testSearchAPIs_NoProviderId_WhenNoAPIs() throws APIManagementException, RegistryException {
        String providerId = "admin";

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        //Mock to return 0 APIs 
        GenericArtifact[] genericArtifacts = {};        
        Mockito.when(artifactManager.getAllGenericArtifacts()).thenReturn(genericArtifacts);
        
        List<API> foundApiList0 = apiProvider.searchAPIs("API", "Name", null); 
        Assert.assertNotNull(foundApiList0);
        Assert.assertEquals(0, foundApiList0.size());
    }
    
    @Test
    public void testSearchAPIs_NoProviderId_ForException() throws APIManagementException, RegistryException {
        String providerId = "admin";

        APIProviderImplWrapper apiProvider = new APIProviderImplWrapper(apimgtDAO, null);
        
        //Mock to throw exception when retrieving APIs
        Mockito.when(artifactManager.getAllGenericArtifacts()).thenThrow(RegistryException.class);
        
        try {
            apiProvider.searchAPIs("API", "Name", null);
        } catch(APIManagementException e) {
            Assert.assertEquals("Failed to search APIs with type", e.getMessage());
        }
    }
    
    /**
     * This method can be used when invoking getAPIsByProvider()
     * 
     */
    private void prepareForGetAPIsByProvider(GenericArtifactManager artifactManager, APIProviderImplWrapper apiProvider,
            String providerId, API api1, API api2) throws APIManagementException, RegistryException {
        
        
        String providerPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + providerId;

        //Mocking API1 association
        Association association1 = Mockito.mock(Association.class);
        String apiPath1 = "/API1/1.0.0";
        Resource resource1 = Mockito.mock(Resource.class);
        String apiArtifactId1 = "76897689";
        Mockito.when(association1.getDestinationPath()).thenReturn(apiPath1);
        Mockito.when(apiProvider.registry.get(apiPath1)).thenReturn(resource1);
        Mockito.when(resource1.getUUID()).thenReturn(apiArtifactId1);
        GenericArtifact apiArtifact1 = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact(apiArtifactId1)).thenReturn(apiArtifact1);
        Mockito.when(APIUtil.getAPI(apiArtifact1, apiProvider.registry)).thenReturn(api1);
        
        //Mocking API2 association
        Association association2 = Mockito.mock(Association.class);
        String apiPath2 = "/API2/1.0.0";
        Resource resource2 = Mockito.mock(Resource.class);
        String apiArtifactId2 = "76897622";
        Mockito.when(association2.getDestinationPath()).thenReturn(apiPath2);
        Mockito.when(apiProvider.registry.get(apiPath2)).thenReturn(resource2);
        Mockito.when(resource2.getUUID()).thenReturn(apiArtifactId2);
        GenericArtifact apiArtifact2 = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact(apiArtifactId2)).thenReturn(apiArtifact2);
        Mockito.when(APIUtil.getAPI(apiArtifact2, apiProvider.registry)).thenReturn(api2);
        
        Association[] associactions = {association1, association2};
        Mockito.when(apiProvider.registry.getAssociations(providerPath, APIConstants.PROVIDER_ASSOCIATION)).
                                thenReturn(associactions);
    }
    
    /**
     * This method can be used when invoking changeLifeCycleStatus()
     * 
     */
    private void prepareForChangeLifeCycleStatus(APIProviderImplWrapper apiProvider, ApiMgtDAO apimgtDAO, 
            APIIdentifier apiId, GenericArtifact apiArtifact) throws GovernanceException, APIManagementException, 
            FaultGatewaysException, WorkflowException {
        
        Mockito.when(APIUtil.getAPIArtifact(apiId, apiProvider.registry)).thenReturn(apiArtifact);
        Mockito.when(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER)).thenReturn("admin");
        Mockito.when(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME)).thenReturn("API1");
        Mockito.when(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION)).thenReturn("1.0.0");
        Mockito.when(apiArtifact.getLifecycleState()).thenReturn("CREATED");
        
        Mockito.when(apimgtDAO.getAPIID(apiId, null)).thenReturn(1);
        
        //Workflow has not started, this will trigger the executor
        WorkflowDTO wfDTO1 = Mockito.mock(WorkflowDTO.class);
        Mockito.when(wfDTO1.getStatus()).thenReturn(null);
        
        WorkflowDTO wfDTO2 = Mockito.mock(WorkflowDTO.class);
        Mockito.when(wfDTO2.getStatus()).thenReturn(WorkflowStatus.APPROVED);
        Mockito.when(apimgtDAO.retrieveWorkflowFromInternalReference("1",
                        WorkflowConstants.WF_TYPE_AM_API_STATE)).thenReturn(wfDTO1, wfDTO2);
        ServiceReferenceHolder sh = TestUtils.getServiceReferenceHolder();
        APIManagerConfigurationService amConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration amConfig = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(sh.getAPIManagerConfigurationService()).thenReturn(amConfigService);
        PowerMockito.when(amConfigService.getAPIManagerConfiguration()).thenReturn(amConfig);
        WorkflowProperties workflowProperties = Mockito.mock(WorkflowProperties.class);
        Mockito.when(workflowProperties.getWorkflowCallbackAPI()).thenReturn("");
        Mockito.when(amConfig.getWorkflowProperties()).thenReturn(workflowProperties);
        
        WorkflowExecutorFactory wfe = PowerMockito.mock(WorkflowExecutorFactory.class);
        Mockito.when(WorkflowExecutorFactory.getInstance()).thenReturn(wfe);

        WorkflowExecutor apiStateWFExecutor = new APIStateChangeSimpleWorkflowExecutor();
        Mockito.when(wfe.getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_STATE)).thenReturn(apiStateWFExecutor);
        Mockito.when(APIUtil.isAnalyticsEnabled()).thenReturn(false);
    }
    
    private GlobalPolicy getPolicyGlobalLevel(){
        GlobalPolicy policy = new GlobalPolicy("1");        

        policy.setDescription("Description");    
        String siddhiQuery = 
                "FROM RequestStream\n"
               + "SELECT 'global_1' AS rule, messageID, true AS isEligible, (cast(map:get(propertiesMap,’ip’),’string’)"
                + " == 3232235778) as isLocallyThrottled,"
                + " 'global_1_key' AS throttle_key\n"
                + "INSERT INTO EligibilityStream;";
        policy.setSiddhiQuery(siddhiQuery); 
      
        return policy;
    }
    
    private static OMElement buildOMElement(InputStream inputStream) throws APIManagementException {
        XMLStreamReader parser;
        StAXOMBuilder builder;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            parser = factory.createXMLStreamReader(inputStream);
            builder = new StAXOMBuilder(parser);
        } catch (XMLStreamException e) {
            String msg = "Error in initializing the parser.";
            throw new APIManagementException(msg, e);
        }

        return builder.getDocumentElement();
    }
    
    private APIPolicy getPolicyAPILevelPerUser(){
        APIPolicy policy = new APIPolicy("custom1");
        
        policy.setUserLevel(PolicyConstants.PER_USER);
        policy.setDescription("Description");    
        //policy.setPolicyLevel("api");
        policy.setTenantDomain("carbon.super");

        RequestCountLimit defaultLimit = new RequestCountLimit();
        defaultLimit.setTimeUnit("min");
        defaultLimit.setUnitTime(5);
        defaultLimit.setRequestCount(400);
      
        
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setLimit(defaultLimit);
        defaultQuotaPolicy.setType("RequestCount");
        
        policy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        
        
        List<Pipeline> pipelines;
        Pipeline p;
        QuotaPolicy quotaPolicy;
        List<Condition> condition;
        RequestCountLimit countlimit;     
        Condition cond;
        pipelines = new ArrayList<Pipeline>();
        
       
        ///////////pipeline item start//////
        p = new Pipeline();
        
        quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("RequestCount");     
        countlimit = new RequestCountLimit();
        countlimit.setTimeUnit("min");
        countlimit.setUnitTime(5);
        countlimit.setRequestCount(100);
        quotaPolicy.setLimit(countlimit);   

        condition =  new ArrayList<Condition>();
        HTTPVerbCondition verbCond = new HTTPVerbCondition();
        verbCond.setHttpVerb("POST");   
        condition.add(verbCond);
            
        
        p.setQuotaPolicy(quotaPolicy);
        p.setConditions(condition);
        pipelines.add(p);
        ///////////pipeline item end//////    
      
        
        
        policy.setPipelines(pipelines);
        
        return policy;
    }
    
    private ApplicationPolicy getPolicyAppLevel(){
        ApplicationPolicy policy = new ApplicationPolicy("gold");
         
        policy.setDescription("Description");    
        policy.setTenantDomain("carbon.super");
        RequestCountLimit defaultLimit = new RequestCountLimit();
        defaultLimit.setTimeUnit("min");
        defaultLimit.setUnitTime(5);
        defaultLimit.setRequestCount(1000);
      
        
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setLimit(defaultLimit);
        defaultQuotaPolicy.setType("RequestCount");
        
        policy.setDefaultQuotaPolicy(defaultQuotaPolicy);    
        
        return policy;
    }
    
    private SubscriptionPolicy getPolicySubscriptionLevelperUser(){
        SubscriptionPolicy policy = new SubscriptionPolicy("gold");
        
      
        policy.setDescription("Description");    
       
        RequestCountLimit defaultLimit = new RequestCountLimit();
        defaultLimit.setTimeUnit("min");
        defaultLimit.setUnitTime(5);
        defaultLimit.setRequestCount(200);
      
        
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setLimit(defaultLimit);
        defaultQuotaPolicy.setType("RequestCount");
        
        policy.setDefaultQuotaPolicy(defaultQuotaPolicy);    
      
        return policy;
    }
    
    private LifecycleBean getLCBean() {
        LifecycleBean bean = new LifecycleBean();
        List<Property> lifecycleProps = new ArrayList<Property>();
        
        Property property1 = new Property();
        property1.setKey("registry.custom_lifecycle.checklist.option.APILifeCycle.1.item");
        String[] values1 = {"status:Created", "name:Requires re-subscription when publish the API", "value:false", 
                "order:1"};
        property1.setValues(values1);
        
        Property property2 = new Property();
        property2.setKey("registry.lifecycle.APILifeCycle.state");
        String[] values2 = {"Created"};
        property2.setValues(values2);
        
        Property property3 = new Property();
        property3.setKey("registry.custom_lifecycle.checklist.option.APILifeCycle.0.item.permission");
        String[] values3 = {"registry.custom_lifecycle.checklist.option.APILifeCycle.0.item.permission"};
        property3.setValues(values3);
        
        Property property4 = new Property();
        property4.setKey("registry.lifecycle.APILifeCycle.lastStateUpdatedTime");
        String[] values4 = {"2017-08-31 13:36:54.501"};
        property4.setValues(values4);
        
        Property property5 = new Property();
        property5.setKey("registry.custom_lifecycle.checklist.option.APILifeCycle.1.item.permission");
        String[] values5 = {"registry.custom_lifecycle.checklist.option.APILifeCycle.1.item.permission"};
        property5.setValues(values5);
        
        Property property6 = new Property();
        property6.setKey("registry.custom_lifecycle.checklist.option.APILifeCycle.0.item");
        String[] values6 = {"status:Created", "name:Deprecate old versions after publish the API", "value:false", 
                "order:0"};
        property6.setValues(values6);
        
        Property property7 = new Property();
        property7.setKey("registry.LC.name");
        String[] values7 = {"APILifeCycle"};
        property7.setValues(values7);
        
        lifecycleProps.add(property1);
        lifecycleProps.add(property2);
        lifecycleProps.add(property3);
        lifecycleProps.add(property4);
        lifecycleProps.add(property5);
        lifecycleProps.add(property6);
        
        Property[] propertyArr = new Property[lifecycleProps.size()];
        bean.setLifecycleProperties(lifecycleProps.toArray(propertyArr));
        
        String[] userRoles = {"publisher"};
        bean.setRolesOfUser(userRoles);
        
        return bean;
    }
    
    private byte[] getTenantConfigContent() {
       String tenantConf = "{\"EnableMonetization\":false,\"IsUnlimitedTierPaid\":false,\"ExtensionHandlerPosition\":\"bottom\","
                + "\"RESTAPIScopes\":{\"Scope\":[{\"Name\":\"apim:api_publish\",\"Roles\":\"admin,Internal/publisher\"},"
                + "{\"Name\":\"apim:api_create\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:api_view\","
                + "\"Roles\":\"admin,Internal/publisher,Internal/creator\"},{\"Name\":\"apim:subscribe\",\"Roles\":"
                + "\"admin,Internal/subscriber\"},{\"Name\":\"apim:tier_view\",\"Roles\":\"admin,Internal/publisher,"
                + "Internal/creator\"},{\"Name\":\"apim:tier_manage\",\"Roles\":\"admin\"},{\"Name\":\"apim:bl_view\","
                + "\"Roles\":\"admin\"},{\"Name\":\"apim:bl_manage\",\"Roles\":\"admin\"},{\"Name\":"
                + "\"apim:subscription_view\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":"
                + "\"apim:subscription_block\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":"
                + "\"apim:mediation_policy_view\",\"Roles\":\"admin\"},{\"Name\":\"apim:mediation_policy_create\","
                + "\"Roles\":\"admin\"},{\"Name\":\"apim:api_workflow\",\"Roles\":\"admin\"}]},\"NotificationsEnabled\":"
                + "\"true\",\"Notifications\":[{\"Type\":\"new_api_version\",\"Notifiers\":[{\"Class\":"
                + "\"org.wso2.carbon.apimgt.impl.notification.NewAPIVersionEmailNotifier\",\"ClaimsRetrieverImplClass\":"
                + "\"org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever\",\"Title\":\"Version $2 of $1 Released\","
                + "\"Template\":\" <html> <body> <h3 style=\\\"color:Black;\\\">We’re happy to announce the arrival of"
                + " the next major version $2 of $1 API which is now available in Our API Store.</h3><a href=\\\"https:"
                + "//localhost:9443/store\\\">Click here to Visit WSO2 API Store</a></body></html>\"}]}],"
                + "\"DefaultRoles\":{\"PublisherRole\":{\"CreateOnTenantLoad\":true,\"RoleName\":"
                + "\"Internal/publisher\"},\"CreatorRole\":{\"CreateOnTenantLoad\":true,\"RoleName\":"
                + "\"Internal/creator\"},\"SubscriberRole\":{\"CreateOnTenantLoad\":true}}}";
       
       return tenantConf.getBytes();
    }
    
    private CORSConfiguration getCORSConfiguration() {
        List<String> acceessControlAllowHeaders = new ArrayList<String>();
        acceessControlAllowHeaders.add("Authorization");
        acceessControlAllowHeaders.add("Content-Type");
        
        List<String> acceessControlAllowOrigins = new ArrayList<String>();
        acceessControlAllowOrigins.add("*");
        
        List<String> acceessControlAllowMethods = new ArrayList<String>();
        acceessControlAllowMethods.add("GET");
        acceessControlAllowMethods.add("POST");
        
        return new CORSConfiguration(true, acceessControlAllowOrigins, true, 
                acceessControlAllowHeaders, acceessControlAllowMethods);
    }
    
    private List<Documentation> getDocumentationList() {
        Documentation doc1 = new Documentation(DocumentationType.HOWTO, "How To");
        doc1.setVisibility(DocumentVisibility.API_LEVEL);
        doc1.setSourceType(DocumentSourceType.INLINE);
        
        Documentation doc2 = new Documentation(DocumentationType.SUPPORT_FORUM, "Support Docs");
        doc2.setVisibility(DocumentVisibility.API_LEVEL);
        doc2.setSourceType(DocumentSourceType.FILE);
        doc2.setFilePath("/registry/resource/_system/governance/apimgt/applicationdata/provider/"
                + "files/provider/fileName");
        
        List<Documentation> docList = new ArrayList<Documentation>();
        docList.add(doc1);
        docList.add(doc2);
        
        return docList;
    }
    
    private void mockDocumentationCreation() throws Exception {
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.whenNew(GenericArtifactManager.class).withAnyArguments().thenReturn(artifactManager);
        GenericArtifact artifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.newGovernanceArtifact(Matchers.any(QName.class))).thenReturn(artifact);
    }
    
    private static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }

}
