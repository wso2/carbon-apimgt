package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.rest.api.store.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.store.utils.FileBasedApplicationImportExportManager;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestApiUtil.class)
public class ExportApiServiceImplTestCase {
    private final String USER = "admin";
    private ExportApiService exportApiService;
    private APIConsumer apiConsumer;

    @Before
    public void init() throws Exception {
        exportApiService = new ExportApiServiceImpl();
        apiConsumer = Mockito.mock(APIConsumer.class);
    }

    @Test
    public void testExportApplicationsGetNotFound() throws Exception {
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiConsumer);
        Response response = exportApiService.exportApplicationsGet(null);
        Assert.assertEquals(response.getStatus(), 404);
    }

    @Test
    public void testExportApplicationsGet() throws Exception {
        Application testApp = new Application(1);
        testApp.setId(1);
        testApp.setUUID("testUUID");
        testApp.setDescription("testDesc");
        testApp.setStatus("APPROVED");
        testApp.setCreatedTime("testDateTime");
        testApp.setLastUpdatedTime("testDateTime");
        testApp.setGroupId("testId");
        testApp.setCallbackUrl("testURL");
        testApp.setIsBlackListed(false);
        testApp.setTier("Unlimited");
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiConsumer);
        Mockito.when(apiConsumer.getApplicationByUUID("testUUID")).thenReturn(testApp);
        FileBasedApplicationImportExportManager importExportManager =
                Mockito.mock(FileBasedApplicationImportExportManager.class);
        Mockito.when(importExportManager.exportApplication(testApp, "testDir")).thenReturn("testPath");
        Response response = exportApiService.exportApplicationsGet("testUUID");
        Assert.assertEquals(response.getStatus(), 200);
    }
}