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
package org.wso2.carbon.apimgt.persistence.utils;

import static org.mockito.Matchers.anyString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import junit.framework.Assert;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MultitenantUtils.class, ServiceReferenceHolder.class, GenericArtifact.class,
        PrivilegedCarbonContext.class, GovernanceUtils.class, ServerConfiguration.class })
public class RegistryPersistenceUtilTestCase {
    
    private final int SUPER_TENANT_ID = -1234;
    private final String SUPER_TENANT_DOMAIN = "carbon.super";
    private final int TENANT_ID = 1;
    private final String TENANT_DOMAIN = "wso2.com";
    private Registry registry;
    
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
    }

    @Test
    public void testAPIGet() throws APIManagementException, RegistryException, UserStoreException {
       
        GenericArtifact artifact = PersistenceHelper.getSampleAPIArtifact();
        API api = RegistryPersistenceUtil.getAPI(artifact, registry);
        Assert.assertEquals("Attibute overview_type does not match", artifact.getAttribute("overview_type"),
                api.getType());
        Assert.assertEquals("API id does not match", artifact.getId(),
                api.getUuid());
        Assert.assertEquals("API tag does not match", "testTag",
                api.getTags().iterator().next());
    }
    @Test
    public void testcreateAPIArtifactContent() throws APIPersistenceException, APIManagementException, RegistryException {
        API api = new API(new APIIdentifier("pubuser", "TestAPI", "1.0"));
        Set<Tier> availableTiers = new HashSet<Tier>();
        availableTiers.add(new Tier("Unlimited"));
        availableTiers.add(new Tier("Gold"));
        api.setAvailableTiers(availableTiers);
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
        URITemplate template = new URITemplate();
        template.setHTTPVerb("GET");
        template.setUriTemplate("/test");
        template.setAuthType("None");
        uriTemplates.add(template);
        api.setUriTemplates(uriTemplates);
        List<APICategory> categories = new ArrayList<APICategory>();
        APICategory category = new APICategory();
        category.setName("testcategory");
        categories.add(category);
        api.setApiCategories(categories);
        List<Label> gatewayLabels = new ArrayList<Label>();
        Label label = new Label();
        label.setName("TestLabel");
        gatewayLabels.add(label);
        api.setGatewayLabels(gatewayLabels);
        
        GenericArtifact genericArtifact = new GenericArtifactImpl(new QName("", "TestAPI", ""),
                "application/vnd.wso2-api+xml");
        genericArtifact.setAttribute("URITemplate", "/test");

        GenericArtifact retArtifact = RegistryPersistenceUtil.createAPIArtifactContent(genericArtifact, api);
        
        Assert.assertEquals("API name does not match", api.getId().getApiName(),
                retArtifact.getAttribute("overview_name"));
        Assert.assertEquals("API version does not match", api.getId().getVersion(),
                retArtifact.getAttribute("overview_version"));
        Assert.assertEquals("API provider does not match", api.getId().getProviderName(),
                retArtifact.getAttribute("overview_provider"));
    }
    @Test
    public void testAPIProductGet() throws GovernanceException, APIManagementException {

        GenericArtifact artifact = PersistenceHelper.getSampleAPIProductArtifact();
        APIProduct apiProduct = RegistryPersistenceUtil.getAPIProduct(artifact, registry);
        Assert.assertEquals("Attibute overview_type does not match", artifact.getAttribute("overview_type"),
                apiProduct.getType());
        Assert.assertEquals("API product id does not match", artifact.getId(),
                apiProduct.getUuid());
    }
    
    @Test
    public void testcreateAPIProductArtifactContent()
            throws APIPersistenceException, APIManagementException, RegistryException {
        APIProduct product = new APIProduct(new APIProductIdentifier("pubuser", "TestAPIProd", "1.0.0"));
        GenericArtifact genericArtifact = new GenericArtifactImpl(new QName("", "TestAPIProd", ""),
                "application/vnd.wso2-api+xml");
        List<APICategory> categories = new ArrayList<APICategory>();
        APICategory category = new APICategory();
        category.setName("testcategory");
        categories.add(category);
        product.setApiCategories(categories);
        Set<Tier> availableTiers = new HashSet<Tier>();
        availableTiers.add(new Tier("Unlimited"));
        availableTiers.add(new Tier("Gold"));
        product.setAvailableTiers(availableTiers);
        GenericArtifact retArtifact = RegistryPersistenceUtil.createAPIProductArtifactContent(genericArtifact, product);
        
        Assert.assertEquals("API name does not match", product.getId().getName(),
                retArtifact.getAttribute("overview_name"));
        Assert.assertEquals("API version does not match", product.getId().getVersion(),
                retArtifact.getAttribute("overview_version"));
        Assert.assertEquals("API provider does not match", product.getId().getProviderName(),
                retArtifact.getAttribute("overview_provider"));
    }
    
    @Test
    public void testGetAPIForSearch() throws APIPersistenceException, GovernanceException {
        GenericArtifact genericArtifact = PersistenceHelper.getSampleAPIArtifact();
        PublisherAPI api = RegistryPersistenceUtil.getAPIForSearch(genericArtifact);
        Assert.assertEquals("API name does not match", genericArtifact.getAttribute("overview_name"),
                api.getApiName());
        Assert.assertEquals("API version does not match", genericArtifact.getAttribute("overview_version"),
                api.getVersion());
        Assert.assertEquals("API provider does not match", genericArtifact.getAttribute("overview_provider"),
                api.getProviderName());
    }
    
    @Test
    public void testGetDevPortalAPIForSearch() throws APIPersistenceException, GovernanceException {
        GenericArtifact genericArtifact = PersistenceHelper.getSampleAPIArtifact();
        DevPortalAPI api = RegistryPersistenceUtil.getDevPortalAPIForSearch(genericArtifact);
        Assert.assertEquals("API name does not match", genericArtifact.getAttribute("overview_name"),
                api.getApiName());
        Assert.assertEquals("API version does not match", genericArtifact.getAttribute("overview_version"),
                api.getVersion());
        Assert.assertEquals("API provider does not match", genericArtifact.getAttribute("overview_provider"),
                api.getProviderName());
    }
    
    @Test
    public void testTenantDomain() {
        PowerMockito.mockStatic(ServerConfiguration.class);
        ServerConfiguration config = Mockito.mock(ServerConfiguration.class);
        PowerMockito.when(ServerConfiguration.getInstance()).thenReturn(config);
        PowerMockito.when(config.getFirstProperty(anyString())).thenReturn(null);
      
        String domain = RegistryPersistenceUtil
                .getTenantDomain(new APIIdentifier("test@" + TENANT_DOMAIN, "test", "1.0"));
        //Assert.assertEquals("Tenant domain does not match", TENANT_DOMAIN, domain);
        
        domain = RegistryPersistenceUtil
                .getTenantDomain(new APIIdentifier("test", "test", "1.0"));
        //Assert.assertEquals("Super tenant domain does not match", SUPER_TENANT_DOMAIN, domain);
        
    }
    
    @Test
    public void testGetArtifactManager() throws RegistryException, APIPersistenceException {
        Registry registry = Mockito.mock(UserRegistry.class);
        GovernanceArtifactConfiguration conf = Mockito.mock(GovernanceArtifactConfiguration.class);
        PowerMockito.when(GovernanceUtils.findGovernanceArtifactConfiguration(APIConstants.API_KEY, registry))
                .thenReturn(conf);
        Association[] assosiations = new Association[0];
        Mockito.when(conf.getRelationshipDefinitions()).thenReturn(assosiations );
        GenericArtifactManager manager = RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY);
        Assert.assertNotNull("Manager is null", manager);
    }
}
