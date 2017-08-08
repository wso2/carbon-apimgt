package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIStoreImpl;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestApiUtil.class)
public class ApplicationsApiServiceImplTestCase {

    private final static Logger logger = LoggerFactory.getLogger(ApplicationsApiService.class);

    private static final String USER = "admin";
    private static final String contentType = "application/json";


    @Test
    public void testApplicationsApplicationIdDelete() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);

        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(apiStore)
                .deleteApplication(applicationId);

        Response response = applicationsApiService.applicationsApplicationIdDelete
                (applicationId, null, null, TestUtil.getRequest());

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdGet() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);

        Application application = new Application("app1", USER);

        Mockito.when(apiStore.getApplication(applicationId, USER)).thenReturn(application);

        Response response = applicationsApiService.applicationsApplicationIdGet
                (applicationId, contentType, null, null, getRequest());

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdGenerateKeysPost() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn(USER);

        List<String> grantTypes = new ArrayList<>();
        grantTypes.add("password");
        grantTypes.add("jwt");

        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setKeyType("PRODUCTION");
        oAuthApplicationInfo.setClientId(UUID.randomUUID().toString());
        oAuthApplicationInfo.setClientSecret(UUID.randomUUID().toString());
        oAuthApplicationInfo.setGrantTypes(grantTypes);

        Mockito.when(apiStore.generateApplicationKeys
                (applicationId, "PRODUCTION", null, grantTypes))
                .thenReturn(oAuthApplicationInfo);

        ApplicationKeyGenerateRequestDTO applicationKeyGenerateRequestDTO = new ApplicationKeyGenerateRequestDTO();
        applicationKeyGenerateRequestDTO.setKeyType(ApplicationKeyGenerateRequestDTO.KeyTypeEnum.PRODUCTION);
        applicationKeyGenerateRequestDTO.setCallbackUrl(null);
        applicationKeyGenerateRequestDTO.setGrantTypesToBeSupported(grantTypes);

        Response response = applicationsApiService.applicationsApplicationIdGenerateKeysPost
                (applicationId, applicationKeyGenerateRequestDTO, contentType, null, null,
                        getRequest());

        Assert.assertEquals(200, response.getStatus());
    }

    // Sample request to be used by tests
    private Request getRequest() throws APIMgtSecurityException {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request request = new Request(carbonMessage);

        try {
            PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(request);
        } catch (Exception e) {
            throw new APIMgtSecurityException("Error while mocking Request Object ", e);
        }
        return request;
    }

    private static void printTestMethodName() {
        logger.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }
}
