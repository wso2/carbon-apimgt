package org.wso2.carbon.apimgt.core.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ApiStoreSdkGenerationException;
import org.wso2.carbon.apimgt.core.impl.APIStoreImpl;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.msf4j.Request;

import java.io.File;
import java.util.UUID;


public class ApiStoreSdkGenerationManagerTestCase {
    /*private static Logger log = LoggerFactory.getLogger(ApiStoreSdkGenerationManagerTestCase.class);

    private static final String USER = "admin";
    private static final String LANGUAGE = PetStoreSwaggerTestCase.CORRECT_LANGUAGE;
    private static final int MIN_SDK_SIZE =0;
    private static final String SWAGGER_PET_STORE = PetStoreSwaggerTestCase.SWAGGER_PET_STORE_CORRECT;
    @Test
    public void testGenerateSdkForApi() throws APIManagementException,ApiStoreSdkGenerationException{
        String apiId = UUID.randomUUID().toString();

        ApiStoreSdkGenerationManager sdkGenerationManager =new ApiStoreSdkGenerationManager();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request =getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Endpoint api1SandBoxEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("abcd").build();
        Endpoint api1ProdEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("cdef").build();
        API api = TestUtil.createApi("provider1", apiId, "testapi1", "1.0.0", "Test API 1 - version 1.0.0",
                TestUtil.createEndpointTypeToIdMap(api1SandBoxEndpointId, api1ProdEndpointId)).build();
        Mockito.when(apiStore.getAPIbyUUID(apiId)).thenReturn(api);
        Mockito.when(apiStore.getApiSwaggerDefinition(apiId)).thenReturn(SWAGGER_PET_STORE);
        String pathToZip = sdkGenerationManager.generateSdkForApi(apiId,LANGUAGE,USER);

        File sdkZipFile =new File(pathToZip);
        Assert.assertTrue(sdkZipFile.exists() && sdkZipFile.length()> MIN_SDK_SIZE);*/
    }


    //Sample request to be used by tests
    private Request getRequest(){
        CarbonMessage carbonMessage = new HTTPCarbonMessage();
        carbonMessage.setProperty("LOGGED_IN_USER",USER);
        Request request =new Request(carbonMessage);
        return request;
    }
    private static void printTestMethodName(){
        log.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }
}
