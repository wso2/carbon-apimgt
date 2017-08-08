package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIPublisherImpl;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.rest.api.publisher.common.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestAPIPublisherUtil.class)
public class ApplicationsApiServiceImplTestCase {

    private static final Logger log = LoggerFactory.getLogger(ApplicationsApiServiceImplTestCase.class);
    private static final String USER = "admin";

    @Test
    public void testApplicationsApplicationIdGet() throws Exception {
        printTestMethodName();
        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Application application = SampleTestObjectCreator.createDefaultApplication();
        String applicationUuid = application.getUuid();
        Mockito.doReturn(application).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getApplication(applicationUuid, USER);
        Response response = applicationsApiService.
                                      applicationsApplicationIdGet(applicationUuid, null, null, null, getRequest());
        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity().toString().contains(applicationUuid));
    }

    @Test
    public void testApplicationsApplicationIdGetNotExist() throws Exception {
        printTestMethodName();
        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Application application = SampleTestObjectCreator.createDefaultApplication();
        String applicationUuid = application.getUuid();
        Mockito.doReturn(null).doThrow(new IllegalArgumentException())
                .when(apiPublisher).getApplication(applicationUuid, USER);
        Response response = applicationsApiService.
                applicationsApplicationIdGet(applicationUuid, null, null, null, getRequest());
        assertEquals(response.getStatus(), 404);
        assertTrue(response.getEntity().toString().contains("Application not found"));
    }

    @Test
    public void testApplicationsApplicationIdGetException() throws Exception {
        printTestMethodName();
        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIPublisher apiPublisher = Mockito.mock(APIPublisherImpl.class);
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        PowerMockito.when(RestAPIPublisherUtil.getApiPublisher(USER)).
                thenReturn(apiPublisher);
        Application application = SampleTestObjectCreator.createDefaultApplication();
        String applicationUuid = application.getUuid();
        Mockito.doThrow(new APIManagementException("Error occurred", ExceptionCodes.APPLICATION_INACTIVE))
                .when(apiPublisher).getApplication(applicationUuid, USER);
        Response response = applicationsApiService.
                applicationsApplicationIdGet(applicationUuid, null, null, null, getRequest());
        assertEquals(response.getStatus(), 400);
        assertTrue(response.getEntity().toString().contains("Application is not active"));
    }

    // Sample request to be used by tests
    private Request getRequest() throws Exception {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request request = new Request(carbonMessage);
        PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(request);
        return request;
    }

    private static void printTestMethodName () {
        log.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }
}
