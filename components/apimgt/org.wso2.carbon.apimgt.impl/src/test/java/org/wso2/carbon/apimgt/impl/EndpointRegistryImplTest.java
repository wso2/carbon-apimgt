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
import org.wso2.carbon.apimgt.api.endpoint.registry.api.EndpointRegistryException;
import org.wso2.carbon.apimgt.api.endpoint.registry.api.EndpointRegistryResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.endpoint.registry.model.EndpointRegistryEntryFilterParams;
import org.wso2.carbon.apimgt.impl.endpoint.registry.constants.EndpointRegistryConstants;
import org.wso2.carbon.apimgt.impl.endpoint.registry.dao.EndpointRegistryDAO;
import org.wso2.carbon.apimgt.impl.endpoint.registry.impl.EndpointRegistryImpl;
import org.wso2.carbon.apimgt.api.endpoint.registry.model.EndpointRegistryEntry;
import org.wso2.carbon.apimgt.api.endpoint.registry.model.EndpointRegistryInfo;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, EndpointRegistryDAO.class, MultitenantUtils.class})
public class EndpointRegistryImplTest {
    private final String ADMIN_USERNAME = "admin";
    private final String TENANT_DOMAIN = "carbon.super";
    private final int TENANT_ID = -1234;

    private EndpointRegistryDAO endpointRegistryDAO;
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

        PowerMockito.mockStatic(EndpointRegistryDAO.class);
        endpointRegistryDAO = Mockito.mock(EndpointRegistryDAO.class);
        PowerMockito.doReturn(endpointRegistryDAO).when(EndpointRegistryDAO.class, "getInstance");

        endpointRegistry = new EndpointRegistryImpl(ADMIN_USERNAME);
    }

    @Test
    public void addEndpointRegistry() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();

        Mockito.when(endpointRegistryDAO.isEndpointRegistryNameExists(endpointRegistryInfo.getName(),
                false, TENANT_ID)).thenReturn(false);
        Mockito.when(endpointRegistryDAO.isEndpointRegistryNameExists(endpointRegistryInfo.getDisplayName(),
                true, TENANT_ID)).thenReturn(false);
        Mockito.when(endpointRegistryDAO.addEndpointRegistry(endpointRegistryInfo, TENANT_ID))
                .thenReturn(endpointRegistryInfo.getUuid());

        String registryUUID = endpointRegistry.addEndpointRegistry(endpointRegistryInfo);

        Assert.assertEquals(endpointRegistryInfo.getUuid(), registryUUID);
    }

    @Test(expected = EndpointRegistryResourceAlreadyExistsException.class)
    public void addEndpointRegistry_existingEntryName() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();

        Mockito.when(endpointRegistryDAO.isEndpointRegistryNameExists(
                endpointRegistryInfo.getName(), false, TENANT_ID)).thenReturn(true);

        endpointRegistry.addEndpointRegistry(endpointRegistryInfo);
    }

    @Test
    public void updateEndpointRegistry() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();

        endpointRegistry.updateEndpointRegistry(endpointRegistryInfo.getUuid(), endpointRegistryInfo.getName(),
                endpointRegistryInfo.getType(), endpointRegistryInfo);
        Mockito.verify(endpointRegistryDAO).updateEndpointRegistry(endpointRegistryInfo.getUuid(), endpointRegistryInfo,
                ADMIN_USERNAME);
    }

    @Test(expected = EndpointRegistryResourceAlreadyExistsException.class)
    public void updateEndpointRegistry_existingEntryName() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();

        Mockito.when(endpointRegistryDAO.isEndpointRegistryNameExists(
                endpointRegistryInfo.getDisplayName(), true, TENANT_ID)).thenReturn(true);

        endpointRegistry.updateEndpointRegistry(endpointRegistryInfo.getUuid(), "Endpoint Registry 2",
                "wso2", endpointRegistryInfo);
    }

    @Test
    public void deleteEndpointRegistry() throws EndpointRegistryException {
        final String REGISTRY_UUID = "abc1";
        endpointRegistry.deleteEndpointRegistry(REGISTRY_UUID);
        Mockito.verify(endpointRegistryDAO).deleteEndpointRegistry(REGISTRY_UUID);
    }

    @Test
    public void getEndpointRegistryByUUID() throws EndpointRegistryException {
        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();

        Mockito.when(endpointRegistryDAO.getEndpointRegistryByUUID(endpointRegistryInfo.getUuid(), TENANT_ID))
                .thenReturn(endpointRegistryInfo);
        EndpointRegistryInfo endpointRegistryInfoResponse
                = endpointRegistry.getEndpointRegistryByUUID(endpointRegistryInfo.getUuid(), TENANT_DOMAIN);

        compareRegistryInfo(endpointRegistryInfo, endpointRegistryInfoResponse);
    }

    @Test
    public void getEndpointRegistry() throws EndpointRegistryException {

        EndpointRegistryInfo endpointRegistryInfo = createRegistryWithDefaultParams();

        Mockito.when(endpointRegistryDAO.getEndpointRegistry(TENANT_ID))
                .thenReturn(endpointRegistryInfo);
        Mockito.when(endpointRegistryDAO.isEndpointRegistryTypeExists(
                EndpointRegistryConstants.REGISTRY_TYPE_WSO2, TENANT_ID))
                .thenReturn(true);

        List<EndpointRegistryInfo> endpointRegistryInfoListResponse
                = endpointRegistry.getEndpointRegistries(TENANT_DOMAIN);

        compareRegistryInfo(endpointRegistryInfo, endpointRegistryInfoListResponse.get(0));
    }

    @Test
    public void getEndpointRegistryEntryByUUID() throws EndpointRegistryException {
        String registryUUID = "reg1";

        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();

        Mockito.when(endpointRegistryDAO.getEndpointRegistryEntryByUUID(endpointRegistryEntry.getEntryId()))
                .thenReturn(endpointRegistryEntry);

        EndpointRegistryEntry endpointRegistryEntryResponse =
                endpointRegistry.getEndpointRegistryEntryByUUID(registryUUID, endpointRegistryEntry.getEntryId());

        compareRegistryEntryInfo(endpointRegistryEntry, endpointRegistryEntryResponse);
    }

    @Test
    public void getEndpointRegistryEntries() throws EndpointRegistryException {
        List<EndpointRegistryEntry> endpointRegistryEntryList = new ArrayList<>();
        String registryUUID = "reg1";

        EndpointRegistryEntry endpointRegistryEntry1 = createRegistryEntry("abc1", "Entry 1",
                "Entry 1", "v1",
                "A Registry Entry that exposes a REST endpoint", "https://xyz.com",
                "REST", "UTILITY",
                "https://petstore.swagger.io/v2/swagger.json", "OAS", null);
        endpointRegistryEntryList.add(endpointRegistryEntry1);

        EndpointRegistryEntry endpointRegistryEntry2 = createRegistryEntry("abc2", "Entry 2",
                "Entry 2", "v1",
                "A Registry Entry that exposes a REST endpoint", "https://xyz2.com",
                "REST", "DOMAIN",
                "https://petstore.swagger.io/v2/swagger.json", "WSDL1", null);
        endpointRegistryEntryList.add(endpointRegistryEntry2);

        Mockito.when(endpointRegistryDAO.getEndpointRegistryEntries(createDefaultEntryFilterParams(), registryUUID))
                .thenReturn(endpointRegistryEntryList);

        List<EndpointRegistryEntry> endpointRegistryEntryListResponse =
                endpointRegistry.getEndpointRegistryEntries(createDefaultEntryFilterParams(), registryUUID);

        for (int i = 0; i < endpointRegistryEntryListResponse.size(); i++) {
            compareRegistryEntryInfo(endpointRegistryEntryList.get(i), endpointRegistryEntryListResponse.get(i));
        }
    }

    @Test
    public void addEndpointRegistryEntry() throws EndpointRegistryException {
        EndpointRegistryEntry endpointRegistryEntry = createRegistryEntryWithDefaultParams();

        Mockito.when(endpointRegistryDAO.addEndpointRegistryEntry(endpointRegistryEntry, ADMIN_USERNAME))
                .thenReturn(endpointRegistryEntry.getEntryId());

        String entryUUID = endpointRegistry.addEndpointRegistryEntry(endpointRegistryEntry);

        Assert.assertEquals(endpointRegistryEntry.getEntryId(), entryUUID);
    }

    @Test
    public void updateEndpointRegistryEntry() throws EndpointRegistryException {
        EndpointRegistryEntry endpointRegistryEntryOld = createRegistryEntryWithDefaultParams();

        EndpointRegistryEntry endpointRegistryEntryNew = createRegistryEntry(endpointRegistryEntryOld.getEntryId(),
                "Entry 1", "Entry 2", "v1",
                "A Registry Entry that exposes a REST endpoint", "https://xyz2.com",
                "REST",
                "DOMAIN", "https://petstore.swagger.io/v2/swagger.json",
                "WSDL1", null);

        Mockito.when(endpointRegistryDAO.getEndpointRegistryEntryByUUID(endpointRegistryEntryOld.getEntryId()))
                .thenReturn(endpointRegistryEntryOld);
        Mockito.when(endpointRegistryDAO.isRegistryEntryNameExists(endpointRegistryEntryNew, false))
                .thenReturn(false);

        endpointRegistry.updateEndpointRegistryEntry(endpointRegistryEntryOld.getEntryName(), endpointRegistryEntryNew);
        Mockito.verify(endpointRegistryDAO).updateEndpointRegistryEntry(endpointRegistryEntryNew, ADMIN_USERNAME);
    }

    @Test(expected = EndpointRegistryResourceAlreadyExistsException.class)
    public void updateEndpointRegistryEntry_existingEntryName() throws EndpointRegistryException {
        EndpointRegistryEntry endpointRegistryEntryOld = createRegistryEntryWithDefaultParams();

        EndpointRegistryEntry endpointRegistryEntryNew = createRegistryEntry(endpointRegistryEntryOld.getEntryId(),
                "Entry 1", "Entry 2", "v1",
                "A Registry Entry that exposes a REST endpoint", "https://xyz2.com",
                "REST",
                "DOMAIN", "https://petstore.swagger.io/v2/swagger.json",
                "WSDL1", null);

        Mockito.when(endpointRegistryDAO.getEndpointRegistryEntryByUUID(endpointRegistryEntryOld.getEntryId()))
                .thenReturn(endpointRegistryEntryOld);
        Mockito.when(endpointRegistryDAO.isRegistryEntryNameExists(endpointRegistryEntryNew, true))
                .thenReturn(true);

        endpointRegistry.updateEndpointRegistryEntry(endpointRegistryEntryOld.getEntryName(), endpointRegistryEntryNew);
    }

    @Test
    public void deleteEndpointRegistryEntry() throws EndpointRegistryException {
        final String ENTRY_UUID = "entry1";
        endpointRegistry.deleteEndpointRegistryEntry(ENTRY_UUID);
        Mockito.verify(endpointRegistryDAO).deleteEndpointRegistryEntry(ENTRY_UUID);
    }

    @Test
    public void createNewEntryVersion() throws EndpointRegistryException {
        final String NEW_VERSION = "v2";
        final String NEW_ENTRY_ID = "abc1";
        EndpointRegistryEntry endpointRegistryEntryOld = createRegistryEntryWithDefaultParams();
        endpointRegistryEntryOld.setVersion(NEW_VERSION);

        Mockito.when(endpointRegistryDAO.addEndpointRegistryEntry(endpointRegistryEntryOld, ADMIN_USERNAME))
                .thenReturn(NEW_ENTRY_ID);
        Mockito.when(endpointRegistryDAO.isRegistryEntryNameAndVersionExists(endpointRegistryEntryOld))
                .thenReturn(false);

        String newEntryId = endpointRegistry.createNewEntryVersion(endpointRegistryEntryOld.getEntryId(),
                endpointRegistryEntryOld);

        Assert.assertEquals(NEW_ENTRY_ID, newEntryId);
    }

    @Test(expected = EndpointRegistryResourceAlreadyExistsException.class)
    public void createNewEntryVersion_withExistingVersion() throws EndpointRegistryException {
        final String NEW_VERSION = "v2";
        EndpointRegistryEntry endpointRegistryEntryOld = createRegistryEntryWithDefaultParams();
        endpointRegistryEntryOld.setVersion(NEW_VERSION);

        Mockito.when(endpointRegistryDAO.isRegistryEntryNameAndVersionExists(endpointRegistryEntryOld))
                .thenReturn(true);

        endpointRegistry.createNewEntryVersion(endpointRegistryEntryOld.getEntryId(),
                endpointRegistryEntryOld);
    }

    private void compareRegistryInfo(EndpointRegistryInfo expected, EndpointRegistryInfo actual) {
        Assert.assertEquals(expected.getUuid(), actual.getUuid());
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getType(), actual.getType());
        Assert.assertEquals(expected.getOwner(), actual.getOwner());
        Assert.assertEquals(expected.getRegistryId(), actual.getRegistryId());
    }

    private void compareRegistryEntryInfo(EndpointRegistryEntry expected, EndpointRegistryEntry actual) {
        Assert.assertEquals(expected.getEntryId(), actual.getEntryId());
        Assert.assertEquals(expected.getEntryName(), actual.getEntryName());
        Assert.assertEquals(expected.getVersion(), actual.getVersion());
        Assert.assertEquals(expected.getDescription(), actual.getDescription());
        Assert.assertEquals(expected.getRegistryId(), actual.getRegistryId());
        Assert.assertEquals(expected.getServiceURL(), actual.getServiceURL());
        Assert.assertEquals(expected.getServiceType(), actual.getServiceType());
        Assert.assertEquals(expected.getServiceCategory(), actual.getServiceCategory());
        Assert.assertEquals(expected.getDefinitionURL(), actual.getDefinitionURL());
        Assert.assertEquals(expected.getDefinitionType(), actual.getDefinitionType());
        Assert.assertEquals(expected.getEndpointDefinition(), actual.getEndpointDefinition());
    }

    private EndpointRegistryInfo createRegistryWithDefaultParams() {

        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setUuid("abc1");
        endpointRegistryInfo.setRegistryId(1);
        endpointRegistryInfo.setName("Endpoint Registry 1");
        endpointRegistryInfo.setType("wso2");
        endpointRegistryInfo.setOwner(ADMIN_USERNAME);

        return endpointRegistryInfo;
    }

    private EndpointRegistryInfo createRegistry(String uuid, int id, String name, String displayName, String type,
                                                String owner) {
        EndpointRegistryInfo endpointRegistryInfo = new EndpointRegistryInfo();
        endpointRegistryInfo.setUuid(uuid);
        endpointRegistryInfo.setRegistryId(id);
        endpointRegistryInfo.setName(name);
        endpointRegistryInfo.setDisplayName(displayName);
        endpointRegistryInfo.setType(type);
        endpointRegistryInfo.setOwner(owner);

        return endpointRegistryInfo;
    }

    private EndpointRegistryEntry createRegistryEntryWithDefaultParams() {
        EndpointRegistryEntry endpointRegistryEntry = new EndpointRegistryEntry();
        endpointRegistryEntry.setEntryId("entry1");
        endpointRegistryEntry.setEntryName("Entry Name 1");
        endpointRegistryEntry.setDisplayName("Entry Name 1");
        endpointRegistryEntry.setVersion("v1");
        endpointRegistryEntry.setDescription("A Registry Entry that exposes a REST endpoint");
        endpointRegistryEntry.setServiceURL("https://xyz.com");
        endpointRegistryEntry.setServiceType("REST");
        endpointRegistryEntry.setServiceCategory("UTILITY");
        endpointRegistryEntry.setDefinitionURL("https://petstore.swagger.io/v2/swagger.json");
        endpointRegistryEntry.setDefinitionType("OAS");

        return endpointRegistryEntry;
    }

    private EndpointRegistryEntry createRegistryEntry(String id, String name, String displayName,
                                                      String version, String description,
                                                      String serviceUrl, String serviceType, String serviceCategory,
                                                      String definitionUrl, String definitionType,
                                                      InputStream definitionFile) {
        EndpointRegistryEntry endpointRegistryEntry = new EndpointRegistryEntry();
        endpointRegistryEntry.setEntryId(id);
        endpointRegistryEntry.setEntryName(name);
        endpointRegistryEntry.setDisplayName(displayName);
        endpointRegistryEntry.setVersion(version);
        endpointRegistryEntry.setDescription(description);
        endpointRegistryEntry.setServiceURL(serviceUrl);
        endpointRegistryEntry.setServiceType(serviceType);
        endpointRegistryEntry.setServiceCategory(serviceCategory);
        endpointRegistryEntry.setDefinitionURL(definitionUrl);
        endpointRegistryEntry.setDefinitionType(definitionType);
        endpointRegistryEntry.setEndpointDefinition(definitionFile);

        return endpointRegistryEntry;
    }

    private EndpointRegistryEntryFilterParams createDefaultEntryFilterParams() {
        EndpointRegistryEntryFilterParams filterParams = new EndpointRegistryEntryFilterParams();
        filterParams.setEntryName("Entry 1");
        filterParams.setDisplayName("Entry 1");
        filterParams.setVersion("v1");
        filterParams.setServiceType("REST");
        filterParams.setServiceCategory("UTILITY");
        filterParams.setDefinitionType("OAS");
        filterParams.setSortBy(EndpointRegistryConstants.COLUMN_ENTRY_NAME);
        filterParams.setSortOrder("ASC");
        filterParams.setLimit(25);
        filterParams.setOffset(0);

        return filterParams;
    }
}
