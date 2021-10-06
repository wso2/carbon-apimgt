/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.persistence.mapper;

import static org.mockito.Matchers.anyString;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPIInfo;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPIInfo;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPIProduct;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.utils.PersistenceHelper;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import junit.framework.Assert;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MultitenantUtils.class, ServiceReferenceHolder.class, GenericArtifact.class,
        PrivilegedCarbonContext.class, GovernanceUtils.class })
public class APIMapperTestCase {

    private final int SUPER_TENANT_ID = -1234;
    private final String SUPER_TENANT_DOMAIN = "carbon.super";
    private Registry registry;
    private API api;
    private APIProduct product;
    
    @Before
    public void setupClass() throws Exception {
        System.setProperty("carbon.home", "");
        ServiceReferenceHolder serviceRefHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceRefHolder);
        RealmService realmService = Mockito.mock(RealmService.class);
        PowerMockito.when(serviceRefHolder.getRealmService()).thenReturn(realmService);

        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        PowerMockito.when(tenantManager.getTenantId(SUPER_TENANT_DOMAIN)).thenReturn(SUPER_TENANT_ID);
        
        registry = Mockito.mock(Registry.class);

        PowerMockito.mockStatic(MultitenantUtils.class);
        Resource resource = new ResourceImpl();
        Mockito.when(registry.get(anyString())).thenReturn(resource);
        Tag[] tags = new Tag[1];
        Tag tag = new Tag();
        tag.setTagName("testTag");
        tags[0] = tag;
        Mockito.when(registry.getTags(anyString())).thenReturn(tags);
        PowerMockito.mockStatic(GovernanceUtils.class);
        
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedContext);
        
        GenericArtifact genericArtifact = PersistenceHelper.getSampleAPIArtifact();
        api = RegistryPersistenceUtil.getAPI(genericArtifact, registry);
        GenericArtifact productGenericArtifact = PersistenceHelper.getSampleAPIProductArtifact();
        product = RegistryPersistenceUtil.getAPIProduct(productGenericArtifact, registry);
    }
    @Test
    public void testAPItoPublisherApiAndBack() throws GovernanceException, APIManagementException {

        PublisherAPI pubAPI = APIMapper.INSTANCE.toPublisherApi(api);
        Assert.assertEquals("API name does not match", api.getId().getName(), pubAPI.getApiName());
        Assert.assertEquals("API uuid does not match", api.getUuid(), pubAPI.getId());
        Assert.assertTrue("Mapped api does not have status", pubAPI.toString().contains(api.getStatus()));
        API mappedAPI = APIMapper.INSTANCE.toApi(pubAPI);
        Assert.assertEquals("Mapped api name does not match", mappedAPI.getId().getName(), api.getId().getName());

    }
    
    @Test
    public void testAPItoDevPortalApiAndBack() throws GovernanceException, APIManagementException {
        DevPortalAPI devAPI = APIMapper.INSTANCE.toDevPortalApi(api);
        Assert.assertEquals("API name does not match", api.getId().getName(), devAPI.getApiName());
        Assert.assertEquals("API uuid does not match", api.getUuid(), devAPI.getId());
        Assert.assertTrue("Mapped api does not have status", devAPI.toString().contains(api.getStatus()));
        API mappedAPI = APIMapper.INSTANCE.toApi(devAPI);
        Assert.assertEquals("Mapped api name does not match", mappedAPI.getId().getName(), api.getId().getName());
    }
    
    @Test
    public void testPublisherAPIInfoToAPI() throws GovernanceException, APIManagementException {
        PublisherAPIInfo infoapi = new PublisherAPIInfo();
        infoapi.setApiName("TestAPI");
        infoapi.setVersion("1.0");
        infoapi.setType("API");
        infoapi.setStatus("PUBLISHED");
        infoapi.setProviderName("admin");
        infoapi.setDescription("Test API description");
        API mappedAPI = APIMapper.INSTANCE.toApi(infoapi);
        
        Assert.assertEquals("Mapped api name does not match", mappedAPI.getId().getName(), infoapi.getApiName());
        Assert.assertEquals("Mapped api version does not match", mappedAPI.getId().getVersion(), infoapi.getVersion());
        Assert.assertEquals("Mapped api state does not match", mappedAPI.getStatus(), infoapi.getStatus());
        Assert.assertEquals("Mapped api description does not match", mappedAPI.getDescription(), infoapi.getDescription());
    }
    
    @Test
    public void testDevPortalAPIInfoToAPI() throws GovernanceException, APIManagementException {
        DevPortalAPIInfo infoapi = new DevPortalAPIInfo();
        infoapi.setApiName("TestAPI");
        infoapi.setVersion("1.0");
        infoapi.setType("API");
        infoapi.setStatus("PUBLISHED");
        infoapi.setProviderName("admin");
        infoapi.setDescription("Test API description");
        API mappedAPI = APIMapper.INSTANCE.toApi(infoapi);
        
        Assert.assertEquals("Mapped api name does not match", mappedAPI.getId().getName(), infoapi.getApiName());
        Assert.assertEquals("Mapped api version does not match", mappedAPI.getId().getVersion(), infoapi.getVersion());
        Assert.assertEquals("Mapped api state does not match", mappedAPI.getStatus(), infoapi.getStatus());
        Assert.assertEquals("Mapped api description does not match", mappedAPI.getDescription(), infoapi.getDescription());

    } 
    
    @Test
    public void testAPIProductToPublisherAPIProductAndBack() throws GovernanceException, APIManagementException {

        PublisherAPIProduct pubAPI = APIProductMapper.INSTANCE.toPublisherApiProduct(product);
        Assert.assertEquals("API product uuid does not match", product.getUuid(), pubAPI.getId());
        Assert.assertEquals("API product type does not match", product.getType(),
                pubAPI.getType());

        APIProduct mappedProduct = APIProductMapper.INSTANCE.toApiProduct(pubAPI);
        Assert.assertEquals("Mapped product uuid does not match", mappedProduct.getUuid(), product.getUuid());
    }
}
