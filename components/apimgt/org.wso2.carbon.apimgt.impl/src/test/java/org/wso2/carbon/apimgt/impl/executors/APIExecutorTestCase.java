/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.impl.executors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.test.TestTenantManager;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.app.RemoteRegistryService;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;


@RunWith(PowerMockRunner.class)
@PrepareForTest({PrivilegedCarbonContext.class, CarbonContext.class, ServiceReferenceHolder.class, APIUtil.class, APIManagerFactory.class})
public class APIExecutorTestCase {

    private RequestContext requestContext = Mockito.mock(RequestContext.class);
    private Resource resource = Mockito.mock(Resource.class);
    private GenericArtifact genericArtifact = Mockito.mock(GenericArtifact.class);
    private GenericArtifactManager genericArtifactManager = Mockito.mock(GenericArtifactManager.class);
    private UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
    private APIProvider apiProvider = Mockito.mock(APIProvider.class);
    private APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);
    private API api = Mockito.mock(API.class);
    private ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
    private RealmService realmService = Mockito.mock(RealmService.class);
    private RemoteRegistryService registryService = Mockito.mock(RemoteRegistryService.class);
    private APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);

    private final String ARTIFACT_ID = "abc123";
    private final int TENANT_ID = -1234;
    private final String TENANT_DOMAIN = "foo.com";
    private final String USER_NAME = "john";
    private final String API_NAME = "pizza-shack";
    private final String API_VERSION = "2.0.0";



    @Before
    public void setup() throws Exception {

        System.setProperty(CARBON_HOME, "");
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.when(privilegedCarbonContext.getUsername()).thenReturn(USER_NAME);

        PowerMockito.mockStatic(CarbonContext.class);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        Mockito.when(carbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

        Mockito.when(resource.getUUID()).thenReturn(ARTIFACT_ID);
        Mockito.when(requestContext.getResource()).thenReturn(resource);
        Mockito.when(genericArtifactManager.getGenericArtifact(ARTIFACT_ID)).thenReturn(genericArtifact);
        Mockito.when(genericArtifact.getLifecycleState()).thenReturn("CREATED");

        Mockito.when(apiProvider.propergateAPIStatusChangeToGateways(apiIdentifier, APIStatus.PUBLISHED))
                .thenReturn(null);
        Mockito.when(apiProvider.updateAPIforStateChange(apiIdentifier, APIStatus.PUBLISHED, null)).thenReturn(true);
        Mockito.when(userRegistry.get("/apimgt/applicationdata/provider/john/pizza-shack/2.0.0/api"))
                .thenReturn(resource);

        Mockito.when(api.getId()).thenReturn(apiIdentifier);
        Mockito.when(apiIdentifier.getProviderName()).thenReturn(USER_NAME);
        Mockito.when(apiIdentifier.getApiName()).thenReturn(API_NAME);
        Mockito.when(apiIdentifier.getVersion()).thenReturn(API_VERSION);
        Mockito.when(api.getEndpointConfig()).thenReturn("http://bar.com");

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        TestTenantManager tenantManager = new TestTenantManager();
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getArtifactManager(requestContext.getSystemRegistry(),APIConstants.API_KEY)).thenReturn(genericArtifactManager);

        PowerMockito.when(APIUtil.replaceEmailDomainBack(USER_NAME+'@'+TENANT_DOMAIN)).thenCallRealMethod();
        PowerMockito.when(APIUtil.replaceEmailDomain(USER_NAME)).thenCallRealMethod();
        PowerMockito.when(APIUtil.getApiStatus("CREATED")).thenCallRealMethod();
        PowerMockito.when(APIUtil.getApiStatus("PUBLISHED")).thenCallRealMethod();
        PowerMockito.when(APIUtil.getAPIPath(apiIdentifier)).thenCallRealMethod();

        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceUserRegistry(USER_NAME,TENANT_ID)).thenReturn(userRegistry);

        PowerMockito.when(APIUtil.getAPI(genericArtifact)).thenReturn(api);

        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);
        Mockito.when(apiManagerFactory.getAPIProvider(USER_NAME+'@'+TENANT_DOMAIN)).thenReturn(apiProvider);

        Tier tier1 = new Tier("GOLD");
        Tier tier2 = new Tier("SILVER");
        Set<Tier> hashSet = new HashSet<Tier>();
        hashSet.add(tier1);
        hashSet.add(tier2);
        Mockito.when(api.getAvailableTiers()).thenReturn(hashSet);
    }

    @Test
    public void testExecute() throws Exception {
        APIExecutor apiExecutor = new APIExecutor();
        apiExecutor.init(new HashMap());
        boolean isExecuted = apiExecutor.execute(requestContext, "CREATED", "PUBLISHED");
        Assert.assertTrue(isExecuted);
    }

    @Test
    public void testExecuteWhenNoTiers() throws Exception {
        Mockito.when(api.getAvailableTiers()).thenReturn(null);
        APIExecutor apiExecutor = new APIExecutor();
        boolean isExecuted = apiExecutor.execute(requestContext, "CREATED", "PUBLISHED");
        Assert.assertFalse(isExecuted);
    }

    @Test
    public void testExecuteWhenNoEndpoint() throws Exception {
        Mockito.when(api.getEndpointConfig()).thenReturn(null);
        APIExecutor apiExecutor = new APIExecutor();
        boolean isExecuted = apiExecutor.execute(requestContext, "CREATED", "PUBLISHED");
        Assert.assertFalse(isExecuted);
    }

    @Test
    public void testExecuteWhenNoArtifactId() throws Exception {
        Mockito.when(resource.getUUID()).thenReturn(null);
        APIExecutor apiExecutor = new APIExecutor();
        boolean isExecuted = apiExecutor.execute(requestContext, "CREATED", "PUBLISHED");
        Assert.assertFalse(isExecuted);
    }

    @Test
    public void testExecuteWhenNewStatusisNull() throws Exception {
        APIExecutor apiExecutor = new APIExecutor();
        boolean isExecuted = apiExecutor.execute(requestContext, "CREATED", null);
        Assert.assertFalse(isExecuted);
    }

    @Test
    public void testExecuteWhenRegistryException() throws Exception {
        APIExecutor apiExecutor = new APIExecutor();
        PowerMockito.doThrow(new RegistryException("")).when(registryService).getGovernanceUserRegistry(USER_NAME, TENANT_ID);
        boolean isExecuted = apiExecutor.execute(requestContext, "CREATED", "PUBLISHED");
        Assert.assertFalse(isExecuted);
    }

    @Test
    public void testExecuteWhenFaultGatewayException() throws Exception {
        APIExecutor apiExecutor = new APIExecutor();
        PowerMockito.doThrow(new FaultGatewaysException(null)).when(apiProvider).updateAPIforStateChange(apiIdentifier, APIStatus.PUBLISHED, null);
        boolean isExecuted = apiExecutor.execute(requestContext, "CREATED", "PUBLISHED");
        Assert.assertFalse(isExecuted);
    }

    @Test
    public void testExecuteWhenUserStoreException() throws Exception {
        APIExecutor apiExecutor = new APIExecutor();
        PowerMockito.doThrow(new FaultGatewaysException(null)).when(apiProvider).updateAPIforStateChange(apiIdentifier, APIStatus.PUBLISHED, null);
        boolean isExecuted = apiExecutor.execute(requestContext, "CREATED", "PUBLISHED");
        Assert.assertFalse(isExecuted);
    }


    @Test
    public void testExecuteWithDeprecated() throws Exception {
        Mockito.when(genericArtifact.isLCItemChecked(0, APIConstants.API_LIFE_CYCLE)).thenReturn(true);
        List<API> apiList = new ArrayList<API>();
        APIIdentifier apiIdTemp = Mockito.mock(APIIdentifier.class);

        Mockito.when(apiIdTemp.getProviderName()).thenReturn(USER_NAME);
        Mockito.when(apiIdTemp.getVersion()).thenReturn("1.0.0");
        Mockito.when(apiIdTemp.getApiName()).thenReturn(API_NAME);
        API apiTemp = new API(apiIdTemp);

        apiTemp.setStatus(APIStatus.PUBLISHED);
        apiList.add(apiTemp);
        Mockito.when(apiProvider.getAPIsByProvider(USER_NAME)).thenReturn(apiList);

        APIExecutor apiExecutor = new APIExecutor();
        boolean isExecuted = apiExecutor.execute(requestContext, "CREATED", "PUBLISHED");
        Assert.assertTrue(isExecuted);
    }

}
