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
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestApiUtil.class)
public class LabelsApiServiceImplTest {

    @Test
    public void testLabelsGetWithoutLabelId() throws Exception {

        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);

        List<Label> labels = new ArrayList<>();
        Label label1 =  new Label.Builder().id("1").name("label1").type("GATEWAY").build();
        Label label2 =  new Label.Builder().id("2").name("label2").type("STORE").build();
        labels.add(label1);
        labels.add(label2);

        LabelsApiServiceImpl labelService = new LabelsApiServiceImpl();
        Mockito.when(labelService.labelsGet(null, null, getRequest())).thenReturn(Response.status(Response.Status.OK).
                entity(LabelMappingUtil.fromLabelArrayToListDTO(labels)).build());

         Response response = labelService.labelsGet(null, null, getRequest());
         Assert.assertEquals(response.getEntity(), LabelMappingUtil.fromLabelArrayToListDTO(labels));

    }

    @Test
    public void testLabelsGetWithLabelId() throws Exception {

        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);

        List<Label> labels = new ArrayList<>();
        Label label1 =  new Label.Builder().id("1").name("label1").type("GATEWAY").build();
        labels.add(label1);

        LabelsApiServiceImpl labelService = new LabelsApiServiceImpl();
        Mockito.when(labelService.labelsGet("1", null, getRequest())).thenReturn(Response.status(Response.Status.OK).
                entity(LabelMappingUtil.fromLabelArrayToListDTO(labels)).build());

        Response response = labelService.labelsGet(null, null, getRequest());
        Assert.assertEquals(response.getEntity(), LabelMappingUtil.fromLabelArrayToListDTO(labels));

    }

    @Test
    public void testLabelsDelete() throws Exception {
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);

        LabelsApiServiceImpl labelService = new LabelsApiServiceImpl();
        Mockito.doNothing().when(labelService.labelsLabelIdDelete("1", getRequest()));
        Response response = labelService.labelsLabelIdDelete("1", getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.OK);

    }


    private Request getRequest() throws Exception {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request request = new Request(carbonMessage);
        PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(request);
        return request;
    }

    @Test
    public void testLabelsLabelIdDelete() throws Exception {
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);

        LabelsApiServiceImpl labelService = new LabelsApiServiceImpl();
        Mockito.doNothing().when(labelService.labelsLabelIdDelete("1", getRequest()));
        Response response = labelService.labelsLabelIdDelete("1", getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.OK);
    }

    @Test
    public void testLabelsLabelIdPut() throws Exception {
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);

        List<Label> labels = new ArrayList<>();
        Label label1 =  new Label.Builder().id("1").name("label1").type("GATEWAY").build();
        Label label2 =  new Label.Builder().id("2").name("label2").type("STORE").build();
        labels.add(label1);
        labels.add(label2);

        LabelsApiServiceImpl labelService = new LabelsApiServiceImpl();
        Mockito.when(labelService.labelsLabelIdPut("1", LabelMappingUtil.fromLabelToDTO(label1), "application/Json",
                getRequest())).thenReturn(Response.status(Response.Status.OK).
                entity(LabelMappingUtil.fromLabelArrayToListDTO(labels)).build());

        Response response = labelService.labelsLabelIdPut("1", LabelMappingUtil.fromLabelToDTO(label1),
                "application/Json", getRequest());
        Assert.assertEquals(response.getEntity(), LabelMappingUtil.fromLabelArrayToListDTO(labels));
    }

    @Test
    public void testLabelsPost() throws Exception {
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);

        Label label1 =  new Label.Builder().id("1").name("label1").type("GATEWAY").build();

        LabelsApiServiceImpl labelService = new LabelsApiServiceImpl();
        Mockito.when(labelService.labelsPost(LabelMappingUtil.fromLabelToDTO(label1), "application/json", getRequest()))
                .thenReturn(Response.status(Response.Status.CREATED).
                        entity(LabelMappingUtil.fromLabelToDTO(label1)).build());

        Response response = labelService.labelsPost(LabelMappingUtil.fromLabelToDTO(label1), "application/json",
                getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED);
    }
}