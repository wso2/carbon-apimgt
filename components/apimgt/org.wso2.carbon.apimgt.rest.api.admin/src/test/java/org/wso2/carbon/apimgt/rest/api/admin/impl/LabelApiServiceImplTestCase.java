/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.rest.api.admin.LabelsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.LabelListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.LabelMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestApiUtil.class, LabelMappingUtil.class, ApiMgtDAO.class, APIAdminImpl.class})
public class LabelApiServiceImplTestCase {

    private final String TENANT_DOMAIN = "carbon.super";
    private LabelsApiService labelsApiService;
    private ApiMgtDAO apiMgtDAO;

    @Before
    public void init() throws Exception {
        labelsApiService = new LabelsApiServiceImpl();
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.mockStatic(LabelMappingUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUserTenantDomain()).thenReturn(TENANT_DOMAIN);
    }

    /**
     * This method tests the functionality of LabelsGet, for a successful get labels
     *
     * @throws APIManagementException APIManagementException.
     */
    @Test
    public void testLabelGet() throws APIManagementException {
        LabelListDTO labelListDTO = new LabelListDTO();
        List<Label> labelList = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        urls.add("url1");
        urls.add("url2");

        Label label1 = new Label();
        label1.setLabelId("1111");
        label1.setName("TestLabel");
        label1.setAccessUrls(urls);

        Label label2 = new Label();
        label2.setLabelId("2222");
        label2.setName("TestLabel1");
        label2.setAccessUrls(urls);

        labelList.add(label1);
        labelList.add(label2);
        PowerMockito.mockStatic(ApiMgtDAO.class);
        apiMgtDAO =  PowerMockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        Mockito.when(apiMgtDAO.getAllLabels(TENANT_DOMAIN)).thenReturn(labelList);
        PowerMockito.when(LabelMappingUtil.fromLabelListToLabelListDTO(labelList)).thenReturn(labelListDTO);
        Response response = labelsApiService.labelsGet();
        Assert.assertEquals(response.getStatus(), 200);
    }

    /**
     * This method tests the functionality of labelsPost, for a successful add label
     *
     * @throws APIManagementException APIManagementException.
     */
    @Test
    public void testLabelsPost() throws APIManagementException {
        LabelDTO labelDTO = Mockito.mock(LabelDTO.class);
        List<String> urls = new ArrayList<>();
        urls.add("url1");
        urls.add("url2");

        Label label1 = new Label();
        label1.setLabelId("1111");
        label1.setName("TestLabel");
        label1.setAccessUrls(urls);
        PowerMockito.when(LabelMappingUtil.labelDTOToLabel(labelDTO)).thenReturn(label1);
        PowerMockito.mockStatic(ApiMgtDAO.class);
        apiMgtDAO =  PowerMockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        Mockito.when(apiMgtDAO.addLabel(TENANT_DOMAIN, label1)).thenReturn(label1);

        PowerMockito.when(LabelMappingUtil.fromLabelToLabelDTO(label1)).thenReturn(labelDTO);
        Response response = labelsApiService.labelsPost(labelDTO);
        Assert.assertEquals(response.getStatus(), 201);
    }

    /**
     * This method tests the functionality of labelsLabelIdDelete, for a successful delete of label
     *
     * @throws APIManagementException APIManagementException.
     */
    @Test
    public void testLabelsLabelIdDelete() throws APIManagementException {
        String id = "1111";
        String userName = "admin";
        PowerMockito.mockStatic(ApiMgtDAO.class);
        apiMgtDAO = PowerMockito.mock(ApiMgtDAO.class);
        PowerMockito.mockStatic(APIAdminImpl.class);
        APIAdminImpl apiAdminImpl = PowerMockito.mock(APIAdminImpl.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(userName);
        Mockito.when(apiAdminImpl.isAttachedLabel(userName, id)).thenReturn(false);
        apiMgtDAO.deleteLabel(id);
        Response response = labelsApiService.labelsLabelIdDelete(id, null, null);
        Assert.assertEquals(response.getStatus(), 200);
    }

    /**
     * This method tests the functionality of testLabelsLabelIdPut, for a successful update of label
     *
     * @throws APIManagementException APIManagementException.
     */
    @Test
    public void testLabelsLabelIdPut() throws APIManagementException {
        String id = "1111";
        List<String> urls = new ArrayList<>();
        urls.add("url1");
        urls.add("url2");

        Label label1 = new Label();
        label1.setLabelId("1111");
        label1.setName("TestLabel");
        label1.setAccessUrls(urls);
        LabelDTO labelDTO = Mockito.mock(LabelDTO.class);

        PowerMockito.when(LabelMappingUtil.labelDTOToLabelPut(id, labelDTO)).thenReturn(label1);
        PowerMockito.mockStatic(ApiMgtDAO.class);
        apiMgtDAO =  PowerMockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        Mockito.when(apiMgtDAO.updateLabel(label1)).thenReturn(label1);
        PowerMockito.when(LabelMappingUtil.fromLabelToLabelDTO(label1)).thenReturn(labelDTO);
        Response response = labelsApiService.labelsLabelIdPut(id, labelDTO);
        Assert.assertEquals(response.getStatus(), 200);
    }

}
