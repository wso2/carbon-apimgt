package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import javax.ws.rs.core.Response;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestApiUtil.class, APIUtil.class})
public class ImportApiServiceImplTestCase {
    private final String USER = "admin";
    private ImportApiService importApiService;
    private APIConsumer apiConsumer;

    @Before
    public void init() throws Exception {
        importApiService = new ImportApiServiceImpl();
        apiConsumer = Mockito.mock(APIConsumer.class);
    }

    @Test
    public void testImportApplicationsPost() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("sampleApp.zip").getFile());
        FileInputStream fis;
        fis = new FileInputStream(file);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiConsumer);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);
        Attachment fileInfo = Mockito.mock(Attachment.class);
        Map<String, Object> matchedAPIs = Mockito.mock(Map.class);
        Mockito.when(apiConsumer.addApplication(Mockito.any(Application.class), Mockito.anyString())).thenReturn(1);
        PowerMockito.when(RestApiUtil.isTenantAvailable("carbon.super")).thenReturn(true);
        Mockito.when(apiConsumer.searchPaginatedAPIs("name=*sampleAPI*&version=*1.0.0*",
                "carbon.super", 0, Integer.MAX_VALUE, false)).thenReturn(matchedAPIs);
        Mockito.when(apiConsumer.getApplicationById(1)).thenReturn(new Application(1));
        Response response = importApiService.importApplicationsPost(fis, fileInfo, true, true);
        Assert.assertEquals(response.getStatus(), 201);
    }

    @Test
    public void testImportApplicationsPostError() throws Exception {
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiConsumer);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("sampleApp.zip").getFile());
        FileInputStream fis;
        fis = new FileInputStream(file);
        Mockito.when(apiConsumer.addApplication(Mockito.any(Application.class), Mockito.anyString()))
                .thenThrow(APIManagementException.class);
        Response response = importApiService.importApplicationsPost(fis, null, false, false);
        Assert.assertNull("Error while importing Application" + USER, response);
    }
}
