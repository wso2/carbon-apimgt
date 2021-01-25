/*
 *   Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.service.catalog.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.impl.ServiceCatalogImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceInfoDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.ServiceEntryMappingUtil;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.FileBasedServicesImportExportManager;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.Md5HashGenerator;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import static org.mockito.Matchers.eq;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestApiUtil.class, APIUtil.class, RestApiCommonUtil.class, ServiceEntryMappingUtil.class,
        FileBasedServicesImportExportManager.class, Md5HashGenerator.class, ServiceEntriesApiServiceImpl.class})
public class ServiceEntriesApiServiceImplTest {
    private final String USER = "admin";
    private ServiceEntriesApiServiceImpl serviceEntriesApiService;
    private FileBasedServicesImportExportManager importExportManager;

    @Before
    public void init() {
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(ServiceEntryMappingUtil.class);
        PowerMockito.mockStatic(FileBasedServicesImportExportManager.class);
        PowerMockito.mockStatic(Md5HashGenerator.class);
        serviceEntriesApiService = new ServiceEntriesApiServiceImpl();
    }

    @Test
    public void testImportServiceFirstRegistrationWithOverwriteTrue() throws Exception {
        PowerMockito.when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(USER);
        PowerMockito.when(APIUtil.getTenantId(USER)).thenReturn(-1234);
        String tempDirPath = System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator + UUID.randomUUID().toString();
        File file = new File(tempDirPath);
        file.mkdir();
        PowerMockito.when(FileBasedServicesImportExportManager.directoryCreator(RestApiConstants.JAVA_IO_TMPDIR)).thenReturn(tempDirPath);
        importExportManager = Mockito.mock(FileBasedServicesImportExportManager.class);
        ClassLoader classLoader = getClass().getClassLoader();
        File zipFile = new File(classLoader.getResource("payload.zip").getFile());
        FileInputStream fis;
        fis = new FileInputStream(zipFile);
        HashMap<String, String> newResourcesHash = new HashMap<>();
        newResourcesHash.put("swaggerPetstore1-1.0.0", "e2a249160f86a87decf1525cf9c67996d754f66047ebd8c2eb6863a0d6e83fed");
        newResourcesHash.put("swaggerPetstore2-1.0.0", "4837288958e1ca6a26aba57f39c666506172857ef6feb314539429afc39b7434");
        PowerMockito.when(Md5HashGenerator.generateHash(Mockito.anyString())).thenReturn(newResourcesHash);
        HashMap<String, ServiceEntry> catalogEntries = new HashMap<>();
        ServiceEntry entry1 = Mockito.mock(ServiceEntry.class), entry2 = Mockito.mock(ServiceEntry.class);
        catalogEntries.put("swaggerPetstore1-1.0.0", entry1);
        catalogEntries.put("swaggerPetstore2-1.0.0", entry2);
        PowerMockito.when(ServiceEntryMappingUtil.fromDirToServiceCatalogInfoMap(Mockito.anyString())).thenReturn(catalogEntries);
        PowerMockito.when(entry1.getKey()).thenReturn("swaggerPetstore1-1.0.0");
        PowerMockito.when(entry2.getKey()).thenReturn("swaggerPetstore2-1.0.0");

        ServiceCatalogImpl serviceCatalog = Mockito.mock(ServiceCatalogImpl.class);
        PowerMockito.whenNew(ServiceCatalogImpl.class).withNoArguments().thenReturn(serviceCatalog);
        PowerMockito.when(serviceCatalog.addService(Mockito.anyObject(), Mockito.anyInt())).thenReturn("2632c379-b57a-4ccc-ab60-683d8450ccae");
        PowerMockito.when(serviceCatalog.updateService(Mockito.anyObject(), Mockito.anyInt())).thenReturn("swaggerPetstoreN-1.0.0");

        ServiceInfoDTO serviceInfoDTO1 = Mockito.mock(ServiceInfoDTO.class), serviceInfoDTO2 = Mockito.mock(ServiceInfoDTO.class);
        List<ServiceInfoDTO> serviceInfoDTOList = new ArrayList<>();
        serviceInfoDTOList.add(serviceInfoDTO1);
        serviceInfoDTOList.add(serviceInfoDTO2);
        PowerMockito.when(ServiceEntryMappingUtil.fromServiceCatalogInfoToDTOList(Mockito.anyObject())).thenReturn(serviceInfoDTOList);
        ServiceInfoListDTO serviceInfoListDTO = Mockito.mock(ServiceInfoListDTO.class);
        PowerMockito.when(ServiceEntryMappingUtil.fromServiceInfoDTOToServiceInfoListDTO(Mockito.anyObject())).thenReturn(serviceInfoListDTO);

        Response response = serviceEntriesApiService.importService(fis, null, true, null, null);
        Assert.assertEquals(response.getStatus(), 200);
    }
}
