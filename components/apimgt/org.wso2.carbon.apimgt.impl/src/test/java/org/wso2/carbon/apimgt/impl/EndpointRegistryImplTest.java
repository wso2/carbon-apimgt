/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.model.EndpointRegistryEntry;
import org.wso2.carbon.apimgt.api.model.EndpointRegistryInfo;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, ApiMgtDAO.class, MultitenantUtils.class})
public class EndpointRegistryImplTest {
    private final String ADMIN_USERNAME = "admin";
    private final String TENANT_DOMAIN = "carbon.super";
    private final int TENANT_ID = -1234;

    private ApiMgtDAO apiMgtDAO;
    private EndpointRegistryImpl endpointRegistry;

    @Before
    public void init() throws Exception {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        RealmService realmService = Mockito.mock(RealmService.class);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.doReturn(TENANT_DOMAIN).when(MultitenantUtils.class, "getTenantDomain", ADMIN_USERNAME);

        PowerMockito.mockStatic(ApiMgtDAO.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.doReturn(apiMgtDAO).when(ApiMgtDAO.class, "getInstance");

        endpointRegistry = new EndpointRegistryImpl(ADMIN_USERNAME);
    }

    @Test
    public void addEndpointRegistry() throws APIManagementException {
        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setMode("ReadOnly");
        endpointRegistryInfo.setType("wso2");
        endpointRegistryInfo.setOwner(ADMIN_USERNAME);
        endpointRegistryInfo.setUuid("abc1");

        Mockito.when(apiMgtDAO.isEndpointRegistryNameExists(endpointRegistryInfo.getName(), TENANT_ID))
                .thenReturn(false);
        Mockito.when(apiMgtDAO.addEndpointRegistry(endpointRegistryInfo, TENANT_ID))
                .thenReturn(endpointRegistryInfo.getUuid());

        String registryUUID = endpointRegistry.addEndpointRegistry(endpointRegistryInfo);

        Assert.assertEquals(endpointRegistryInfo.getUuid(), registryUUID);
    }

    @Test(expected = APIMgtResourceAlreadyExistsException.class)
    public void addEndpointRegistry_existingEntryName() throws APIManagementException {
        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setMode("ReadOnly");
        endpointRegistryInfo.setType("wso2");
        endpointRegistryInfo.setOwner(ADMIN_USERNAME);
        endpointRegistryInfo.setUuid("abc1");

        Mockito.when(apiMgtDAO.isEndpointRegistryNameExists(endpointRegistryInfo.getName(), TENANT_ID))
                .thenReturn(true);

        endpointRegistry.addEndpointRegistry(endpointRegistryInfo);
    }

    @Test
    public void updateEndpointRegistry() throws APIManagementException {
        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setMode("ReadOnly");
        endpointRegistryInfo.setType("wso2");
        endpointRegistryInfo.setOwner(ADMIN_USERNAME);
        endpointRegistryInfo.setUuid("abc1");

        endpointRegistry.updateEndpointRegistry(endpointRegistryInfo.getUuid(), endpointRegistryInfo.getName(),
                endpointRegistryInfo);
        Mockito.verify(apiMgtDAO).updateEndpointRegistry(endpointRegistryInfo.getUuid(), endpointRegistryInfo);
    }

    @Test(expected = APIMgtResourceAlreadyExistsException.class)
    public void updateEndpointRegistry_existingEntryName() throws APIManagementException {
        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 2");
        endpointRegistryInfo.setMode("ReadOnly");
        endpointRegistryInfo.setType("wso2");
        endpointRegistryInfo.setOwner(ADMIN_USERNAME);
        endpointRegistryInfo.setUuid("abc1");

        Mockito.when(apiMgtDAO.isEndpointRegistryNameExists(endpointRegistryInfo.getName(), TENANT_ID))
                .thenReturn(true);

        endpointRegistry.updateEndpointRegistry(endpointRegistryInfo.getUuid(), "Endpoint Registry 1",
                endpointRegistryInfo);
    }

    @Test
    public void deleteEndpointRegistry() throws APIManagementException {
        final String REGISTRY_UUID = "abc1";
        endpointRegistry.deleteEndpointRegistry(REGISTRY_UUID);
        Mockito.verify(apiMgtDAO).deleteEndpointRegistry(REGISTRY_UUID);
    }

    @Test
    public void getEndpointRegistryByUUID() throws APIManagementException {
        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setMode("ReadOnly");
        endpointRegistryInfo.setType("wso2");
        endpointRegistryInfo.setOwner(ADMIN_USERNAME);
        endpointRegistryInfo.setUuid("abc1");

        Mockito.when(apiMgtDAO.getEndpointRegistryByUUID(endpointRegistryInfo.getUuid(), TENANT_ID))
                .thenReturn(endpointRegistryInfo);
        EndpointRegistryInfo endpointRegistryInfoResponse
                = endpointRegistry.getEndpointRegistryByUUID(endpointRegistryInfo.getUuid(), TENANT_DOMAIN);

        compareRegistryInfo(endpointRegistryInfo, endpointRegistryInfoResponse);
    }

    @Test
    public void getEndpointRegistries() throws APIManagementException {
        List<EndpointRegistryInfo> endpointRegistryInfoList = new ArrayList<>();

        EndpointRegistryInfo endpointRegistryInfo1 = new EndpointRegistryInfo();
        endpointRegistryInfo1.setName("Endpoint Registry 1");
        endpointRegistryInfo1.setMode("ReadOnly");
        endpointRegistryInfo1.setOwner("admin");
        endpointRegistryInfo1.setRegistryId(1);
        endpointRegistryInfo1.setType("wso2");
        endpointRegistryInfo1.setUuid("abc1");
        endpointRegistryInfoList.add(endpointRegistryInfo1);

        EndpointRegistryInfo endpointRegistryInfo2 = new EndpointRegistryInfo();
        endpointRegistryInfo2.setName("Endpoint Registry 2");
        endpointRegistryInfo2.setMode("ReadWrite");
        endpointRegistryInfo2.setOwner("admin");
        endpointRegistryInfo2.setRegistryId(1);
        endpointRegistryInfo2.setType("etcd");
        endpointRegistryInfo2.setUuid("abc2");
        endpointRegistryInfoList.add(endpointRegistryInfo2);

        Mockito.when(apiMgtDAO.getEndpointRegistries(EndpointRegistryConstants.COLUMN_REG_NAME, "ASC",
                25, 0, TENANT_ID))
                .thenReturn(endpointRegistryInfoList);

        List<EndpointRegistryInfo> endpointRegistryInfoListResponse
                = endpointRegistry.getEndpointRegistries(EndpointRegistryConstants.COLUMN_REG_NAME, "ASC",
                25, 0, TENANT_DOMAIN);

        for (int i = 0; i < endpointRegistryInfoListResponse.size(); i++) {
            compareRegistryInfo(endpointRegistryInfoList.get(i), endpointRegistryInfoListResponse.get(i));
        }
    }

    @Test
    public void getEndpointRegistryEntryByUUID() throws APIManagementException {
        String registryUUID = "reg1";

        EndpointRegistryEntry endpointRegistryEntry = new EndpointRegistryEntry();
        endpointRegistryEntry.setEntryId("abc1");
        endpointRegistryEntry.setName("Entry 1");
        endpointRegistryEntry.setMetaData("{mutualTLS:true}");
        endpointRegistryEntry.setRegistryId(1);
        endpointRegistryEntry.setServiceURL("https://xyz.com");
        endpointRegistryEntry.setServiceType("REST");
        endpointRegistryEntry.setServiceCategory("UTILITY");
        endpointRegistryEntry.setDefinitionURL("https://petstore.swagger.io/v2/swagger.json");
        endpointRegistryEntry.setDefinitionType("OAS");
        endpointRegistryEntry.setEndpointDefinition(new ByteArrayInputStream(new byte[]{}));

        Mockito.when(apiMgtDAO.getEndpointRegistryEntryByUUID(endpointRegistryEntry.getEntryId()))
                .thenReturn(endpointRegistryEntry);

        EndpointRegistryEntry endpointRegistryEntryResponse =
                endpointRegistry.getEndpointRegistryEntryByUUID(registryUUID, endpointRegistryEntry.getEntryId());

        compareRegistryEntryInfo(endpointRegistryEntry, endpointRegistryEntryResponse);
    }

    @Test
    public void getEndpointRegistryEntries() throws APIManagementException {
        List<EndpointRegistryEntry> endpointRegistryEntryList = new ArrayList<>();
        String registryUUID = "reg1";

        EndpointRegistryEntry endpointRegistryEntry1 = new EndpointRegistryEntry();
        endpointRegistryEntry1.setEntryId("abc1");
        endpointRegistryEntry1.setName("Entry 1");
        endpointRegistryEntry1.setMetaData("{mutualTLS:true}");
        endpointRegistryEntry1.setRegistryId(1);
        endpointRegistryEntry1.setServiceURL("https://xyz.com");
        endpointRegistryEntry1.setServiceType("REST");
        endpointRegistryEntry1.setServiceCategory("UTILITY");
        endpointRegistryEntry1.setDefinitionURL("https://petstore.swagger.io/v2/swagger.json");
        endpointRegistryEntry1.setDefinitionType("OAS");
        endpointRegistryEntry1.setEndpointDefinition(new ByteArrayInputStream(new byte[]{}));
        endpointRegistryEntryList.add(endpointRegistryEntry1);

        EndpointRegistryEntry endpointRegistryEntry2 = new EndpointRegistryEntry();
        endpointRegistryEntry2.setEntryId("abc2");
        endpointRegistryEntry2.setName("Entry 2");
        endpointRegistryEntry2.setMetaData("{mutualTLS:true}");
        endpointRegistryEntry2.setRegistryId(1);
        endpointRegistryEntry2.setServiceURL("https://xyz2.com");
        endpointRegistryEntry2.setServiceType("REST");
        endpointRegistryEntry2.setServiceCategory("UTILITY");
        endpointRegistryEntry2.setDefinitionURL("https://petstore.swagger.io/v2/swagger2.json");
        endpointRegistryEntry2.setDefinitionType("OAS");
        endpointRegistryEntry2.setEndpointDefinition(new ByteArrayInputStream(new byte[]{}));
        endpointRegistryEntryList.add(endpointRegistryEntry2);

        Mockito.when(apiMgtDAO.getEndpointRegistryEntries(EndpointRegistryConstants.COLUMN_ENTRY_NAME,
                "ASC", 25, 0, registryUUID)).thenReturn(endpointRegistryEntryList);

        List<EndpointRegistryEntry> endpointRegistryEntryListResponse =
                endpointRegistry.getEndpointRegistryEntries(EndpointRegistryConstants.COLUMN_ENTRY_NAME,
                        "ASC", 25, 0, registryUUID);

        for (int i = 0; i < endpointRegistryEntryListResponse.size(); i++) {
            compareRegistryEntryInfo(endpointRegistryEntryList.get(i), endpointRegistryEntryListResponse.get(i));
        }
    }

    @Test
    public void addEndpointRegistryEntry() throws APIManagementException {
        EndpointRegistryEntry endpointRegistryEntry = new EndpointRegistryEntry();
        endpointRegistryEntry.setEntryId("abc1");
        endpointRegistryEntry.setName("Entry 1");
        endpointRegistryEntry.setMetaData("{mutualTLS:true}");
        endpointRegistryEntry.setRegistryId(1);
        endpointRegistryEntry.setServiceURL("https://xyz.com");
        endpointRegistryEntry.setServiceType("REST");
        endpointRegistryEntry.setServiceCategory("UTILITY");
        endpointRegistryEntry.setDefinitionURL("https://petstore.swagger.io/v2/swagger.json");
        endpointRegistryEntry.setDefinitionType("OAS");
        endpointRegistryEntry.setEndpointDefinition(new ByteArrayInputStream(new byte[]{}));

        Mockito.when(apiMgtDAO.addEndpointRegistryEntry(endpointRegistryEntry))
                .thenReturn(endpointRegistryEntry.getEntryId());

        String entryUUID = endpointRegistry.addEndpointRegistryEntry(endpointRegistryEntry);

        Assert.assertEquals(endpointRegistryEntry.getEntryId(), entryUUID);
    }

    @Test
    public void updateEndpointRegistryEntry() throws APIManagementException {
        EndpointRegistryEntry endpointRegistryEntryOld = new EndpointRegistryEntry();
        endpointRegistryEntryOld.setEntryId("abc1");
        endpointRegistryEntryOld.setName("Entry 1");
        endpointRegistryEntryOld.setMetaData("{mutualTLS:true}");
        endpointRegistryEntryOld.setRegistryId(1);
        endpointRegistryEntryOld.setServiceURL("https://xyz.com");
        endpointRegistryEntryOld.setServiceType("REST");
        endpointRegistryEntryOld.setServiceCategory("UTILITY");
        endpointRegistryEntryOld.setDefinitionURL("https://petstore.swagger.io/v2/swagger.json");
        endpointRegistryEntryOld.setDefinitionType("OAS");
        endpointRegistryEntryOld.setEndpointDefinition(new ByteArrayInputStream(new byte[]{}));

        EndpointRegistryEntry endpointRegistryEntryNew = new EndpointRegistryEntry();
        endpointRegistryEntryNew.setEntryId("abc1");
        endpointRegistryEntryNew.setName("Entry 2");
        endpointRegistryEntryNew.setMetaData("{mutualTLS:flase}");
        endpointRegistryEntryNew.setRegistryId(1);
        endpointRegistryEntryNew.setServiceURL("https://xyz2.com");
        endpointRegistryEntryNew.setServiceType("REST");
        endpointRegistryEntryNew.setServiceCategory("UTILITY");
        endpointRegistryEntryNew.setDefinitionURL("https://petstore.swagger.io/v2/swagger2.json");
        endpointRegistryEntryNew.setDefinitionType("WSDL1");
        endpointRegistryEntryNew.setEndpointDefinition(new ByteArrayInputStream(new byte[]{}));

        Mockito.when(apiMgtDAO.getEndpointRegistryEntryByUUID(endpointRegistryEntryOld.getEntryId()))
                .thenReturn(endpointRegistryEntryOld);
        Mockito.when(apiMgtDAO.isRegistryEntryNameExists(endpointRegistryEntryNew))
                .thenReturn(false);

        endpointRegistry.updateEndpointRegistryEntry(endpointRegistryEntryNew);
        Mockito.verify(apiMgtDAO).updateEndpointRegistryEntry(endpointRegistryEntryNew);
    }

    @Test(expected = APIMgtResourceAlreadyExistsException.class)
    public void updateEndpointRegistryEntry_existingEntryName() throws APIManagementException {
        EndpointRegistryEntry endpointRegistryEntryOld = new EndpointRegistryEntry();
        endpointRegistryEntryOld.setEntryId("abc1");
        endpointRegistryEntryOld.setName("Entry 1");
        endpointRegistryEntryOld.setMetaData("{mutualTLS:true}");
        endpointRegistryEntryOld.setRegistryId(1);
        endpointRegistryEntryOld.setServiceURL("https://xyz.com");
        endpointRegistryEntryOld.setServiceType("REST");
        endpointRegistryEntryOld.setServiceCategory("UTILITY");
        endpointRegistryEntryOld.setDefinitionURL("https://petstore.swagger.io/v2/swagger.json");
        endpointRegistryEntryOld.setDefinitionType("OAS");
        endpointRegistryEntryOld.setEndpointDefinition(new ByteArrayInputStream(new byte[]{}));

        EndpointRegistryEntry endpointRegistryEntryNew = new EndpointRegistryEntry();
        endpointRegistryEntryNew.setEntryId("abc1");
        endpointRegistryEntryNew.setName("Entry 2");
        endpointRegistryEntryNew.setMetaData("{mutualTLS:flase}");
        endpointRegistryEntryNew.setRegistryId(1);
        endpointRegistryEntryNew.setServiceURL("https://xyz2.com");
        endpointRegistryEntryNew.setServiceType("REST");
        endpointRegistryEntryNew.setServiceCategory("UTILITY");
        endpointRegistryEntryNew.setDefinitionURL("https://petstore.swagger.io/v2/swagger2.json");
        endpointRegistryEntryNew.setDefinitionType("WSDL1");
        endpointRegistryEntryNew.setEndpointDefinition(new ByteArrayInputStream(new byte[]{}));

        Mockito.when(apiMgtDAO.getEndpointRegistryEntryByUUID(endpointRegistryEntryOld.getEntryId()))
                .thenReturn(endpointRegistryEntryOld);
        Mockito.when(apiMgtDAO.isRegistryEntryNameExists(endpointRegistryEntryNew))
                .thenReturn(true);

        endpointRegistry.updateEndpointRegistryEntry(endpointRegistryEntryNew);
    }

    @Test
    public void deleteEndpointRegistryEntry() throws APIManagementException {
        final String ENTRY_UUID = "entry1";
        endpointRegistry.deleteEndpointRegistryEntry(ENTRY_UUID);
        Mockito.verify(apiMgtDAO).deleteEndpointRegistryEntry(ENTRY_UUID);
    }

    private void compareRegistryInfo(EndpointRegistryInfo expected, EndpointRegistryInfo actual) {
        Assert.assertEquals(expected.getUuid(), actual.getUuid());
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getMode(), actual.getMode());
        Assert.assertEquals(expected.getType(), actual.getType());
        Assert.assertEquals(expected.getOwner(), actual.getOwner());
        Assert.assertEquals(expected.getRegistryId(), actual.getRegistryId());
    }

    private void compareRegistryEntryInfo(EndpointRegistryEntry expected, EndpointRegistryEntry actual) {
        Assert.assertEquals(expected.getEntryId(), actual.getEntryId());
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getMetaData(), actual.getMetaData());
        Assert.assertEquals(expected.getRegistryId(), actual.getRegistryId());
        Assert.assertEquals(expected.getServiceURL(), actual.getServiceURL());
        Assert.assertEquals(expected.getServiceType(), actual.getServiceType());
        Assert.assertEquals(expected.getServiceCategory(), actual.getServiceCategory());
        Assert.assertEquals(expected.getDefinitionURL(), actual.getDefinitionURL());
        Assert.assertEquals(expected.getDefinitionType(), actual.getDefinitionType());
        Assert.assertEquals(expected.getEndpointDefinition(), actual.getEndpointDefinition());
    }
}