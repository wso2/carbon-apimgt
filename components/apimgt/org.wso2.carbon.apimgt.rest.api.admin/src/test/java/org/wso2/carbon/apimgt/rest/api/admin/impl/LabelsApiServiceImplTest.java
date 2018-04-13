/*
 *
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.impl.APIMgtAdminServiceImpl;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.LabelMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.Request;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestApiUtil.class)
public class LabelsApiServiceImplTest {

    private static final String USER = "admin";

    @Test
    public void testLabelsGetWithoutLabelId() throws Exception {

        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);

        List<Label> labels = new ArrayList<>();
        Label label1 = new Label.Builder().id("1").name("label1").type("GATEWAY").build();
        Label label2 = new Label.Builder().id("2").name("label2").type("STORE").build();
        labels.add(label1);
        labels.add(label2);

        LabelsApiServiceImpl labelService = new LabelsApiServiceImpl();
        Mockito.when(labelService.labelsGet(getRequest())).thenReturn(Response.status(Response.Status.OK).
                entity(LabelMappingUtil.fromLabelArrayToListDTO(labels)).build());

        Response response = labelService.labelsGet(getRequest());
        Assert.assertEquals(response.getEntity(), LabelMappingUtil.fromLabelArrayToListDTO(labels));

    }

    @Test
    public void testLabelsGetWithLabelId() throws Exception {

        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);

        List<Label> labels = new ArrayList<>();
        Label label1 = new Label.Builder().id("1").name("label1").type("GATEWAY").build();
        labels.add(label1);

        LabelsApiServiceImpl labelService = new LabelsApiServiceImpl();
        Mockito.when(labelService.labelsLabelIdGet("1", "", "", getRequest())).thenReturn(Response.status(Response
                .Status.OK).
                entity(LabelMappingUtil.fromLabelArrayToListDTO(labels)).build());

        Response response = labelService.labelsLabelIdGet("1", "", "", getRequest());
        Assert.assertEquals(response.getEntity(), LabelMappingUtil.fromLabelArrayToListDTO(labels));

    }


    private Request getRequest() throws Exception {
        HTTPCarbonMessage carbonMessage = Mockito.mock(HTTPCarbonMessage.class);
        Mockito.when(carbonMessage.getProperty("LOGGED_IN_USER")).thenReturn(USER);
        Request request = new Request(carbonMessage);
        return request;
    }

    @Test
    public void testLabelsLabelIdDelete() throws Exception {
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);

        LabelsApiServiceImpl labelService = new LabelsApiServiceImpl();
        Mockito.doNothing().when(labelService.labelsLabelIdDelete("1", "", "", getRequest()));
        Response response = labelService.labelsLabelIdDelete("1", "", "", getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.OK);
    }

    @Test
    public void testLabelsLabelIdPut() throws Exception {
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);

        List<Label> labels = new ArrayList<>();
        Label label1 = new Label.Builder().id("1").name("label1").type("GATEWAY").build();
        Label label2 = new Label.Builder().id("2").name("label2").type("STORE").build();
        labels.add(label1);
        labels.add(label2);

        LabelsApiServiceImpl labelService = new LabelsApiServiceImpl();
        Mockito.when(labelService.labelsLabelIdPut("1", LabelMappingUtil.fromLabelToDTO(label1), getRequest()))
                .thenReturn(Response.status(Response.Status.OK).
                        entity(LabelMappingUtil.fromLabelArrayToListDTO(labels)).build());

        Response response = labelService.labelsLabelIdPut("1", LabelMappingUtil.fromLabelToDTO(label1), getRequest());
        Assert.assertEquals(response.getEntity(), LabelMappingUtil.fromLabelArrayToListDTO(labels));
    }

    @Test
    public void testLabelsPost() throws Exception {
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);

        Label label1 = new Label.Builder().id("1").name("label1").type("GATEWAY").build();

        LabelsApiServiceImpl labelService = new LabelsApiServiceImpl();
        Mockito.when(labelService.labelsPost(LabelMappingUtil.fromLabelToDTO(label1), getRequest()))
                .thenReturn(Response.status(Response.Status.CREATED).
                        entity(LabelMappingUtil.fromLabelToDTO(label1)).build());

        Response response = labelService.labelsPost(LabelMappingUtil.fromLabelToDTO(label1), getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED);
    }
}