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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.LabelMappingUtil;
import org.wso2.msf4j.Request;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

public class LabelsApiServiceImplTest {

    private static final String USER = "admin";

    @Test
    public void testLabelsGetWithoutLabelId() throws Exception {

        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);

        List<Label> labels = new ArrayList<>();
        Label label1 = new Label.Builder().id("1").name("label1").type("GATEWAY").build();
        Label label2 = new Label.Builder().id("2").name("label2").type("STORE").build();
        labels.add(label1);
        labels.add(label2);

        LabelsApiServiceImpl labelService = new LabelsApiServiceImpl(adminService);
        Mockito.when(adminService.getLabels()).thenReturn(labels);

        Response response = labelService.labelsGet(Mockito.mock(Request.class));
        Assert.assertEquals(response.getEntity(), LabelMappingUtil.fromLabelArrayToListDTO(labels));

    }

    @Test
    public void testLabelsGetWithLabelId() throws Exception {

        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);

        Label label1 = new Label.Builder().id("1").name("label1").type("GATEWAY").build();

        LabelsApiServiceImpl labelService = new LabelsApiServiceImpl(adminService);
        Mockito.when(adminService.getLabelByID("1")).thenReturn(label1);

        Response response = labelService.labelsLabelIdGet("1", "", "", getRequest());
        Assert.assertEquals(response.getEntity(), LabelMappingUtil.fromLabelToDTO(label1));

    }


    private Request getRequest() throws Exception {
        HTTPCarbonMessage carbonMessage = Mockito.mock(HTTPCarbonMessage.class);
        Mockito.when(carbonMessage.getProperty("LOGGED_IN_USER")).thenReturn(USER);
        Request request = new Request(carbonMessage);
        return request;
    }

    @Test
    public void testLabelsLabelIdDelete() throws Exception {
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);

        LabelsApiServiceImpl labelService = new LabelsApiServiceImpl(adminService);
        Response response = labelService.labelsLabelIdDelete("1", "", "", getRequest());

        Mockito.verify(adminService).deleteLabel("1");
        Assert.assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void testLabelsLabelIdPut() throws Exception {
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);

        List<Label> labels = new ArrayList<>();
        Label label1 = new Label.Builder().id("1").name("label1").type("GATEWAY").build();
        Label label2 = new Label.Builder().id("2").name("label2").type("STORE").build();
        labels.add(label1);
        labels.add(label2);

        LabelsApiServiceImpl labelService = new LabelsApiServiceImpl(adminService);
        Mockito.when(adminService.updateLabel(label1)).thenReturn(label1);

        Response response = labelService.labelsLabelIdPut("1", LabelMappingUtil.fromLabelToDTO(label1), getRequest());
        Assert.assertEquals(response.getEntity(), LabelMappingUtil.fromLabelArrayToListDTO(labels));
    }

    @Test
    public void testLabelsPost() throws Exception {
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);

        Label label1 = new Label.Builder().id("1").name("label1").type("GATEWAY").build();

        LabelsApiServiceImpl labelService = new LabelsApiServiceImpl(adminService);
        Mockito.when(labelService.labelsPost(LabelMappingUtil.fromLabelToDTO(label1), getRequest()))
                .thenReturn(Response.status(Response.Status.CREATED).
                        entity(LabelMappingUtil.fromLabelToDTO(label1)).build());

        Response response = labelService.labelsPost(LabelMappingUtil.fromLabelToDTO(label1), getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED);
    }
}