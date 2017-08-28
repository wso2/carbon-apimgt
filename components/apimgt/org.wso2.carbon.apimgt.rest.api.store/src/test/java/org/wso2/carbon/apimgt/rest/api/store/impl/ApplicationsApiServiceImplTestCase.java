/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIStoreImpl;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.ApplicationToken;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.workflow.ApplicationCreationResponse;
import org.wso2.carbon.apimgt.core.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeysDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationTokenDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationTokenGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.ApplicationMappingUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
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


    @Test
    public void testApplicationsApplicationIdDelete() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        WorkflowResponse workflowResponse = new GeneralWorkflowResponse();
        workflowResponse.setWorkflowStatus(WorkflowStatus.APPROVED);

        Mockito.when(apiStore.deleteApplication(applicationId)).thenReturn(workflowResponse);

        Response response = applicationsApiService.applicationsApplicationIdDelete
                (applicationId, null, null, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdDeleteErrorCase() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Mockito.doThrow(new APIManagementException("Error Occurred", ExceptionCodes.APPLICATION_NOT_FOUND))
                .when(apiStore).deleteApplication(applicationId);

        Response response = applicationsApiService.applicationsApplicationIdDelete
                (applicationId, null, null,request);

        Assert.assertEquals(404, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdGet() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Application application = getSampleApplication(applicationId);

        Mockito.when(apiStore.getApplication(applicationId, USER)).thenReturn(application);

        Response response = applicationsApiService.applicationsApplicationIdGet
                (applicationId, null, null, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdGetErrorCase() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Mockito.doThrow(new APIManagementException("Error Occurred", ExceptionCodes.APPLICATION_NOT_FOUND))
                .when(apiStore).getApplication(applicationId, USER);

        Response response = applicationsApiService.applicationsApplicationIdGet
                (applicationId, null, null, request);

        Assert.assertEquals(404, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdGenerateKeysPost() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

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
                (applicationId, applicationKeyGenerateRequestDTO, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdGenerateKeysPostErrorCase() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        List<String> grantTypes = new ArrayList<>();
        grantTypes.add("password");
        grantTypes.add("jwt");

        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setKeyType("PRODUCTION");
        oAuthApplicationInfo.setClientId(UUID.randomUUID().toString());
        oAuthApplicationInfo.setClientSecret(UUID.randomUUID().toString());
        oAuthApplicationInfo.setGrantTypes(grantTypes);

        Mockito.doThrow(new APIManagementException("Error Occurred", ExceptionCodes.APPLICATION_TOKEN_GENERATION_FAILED))
                .when(apiStore).generateApplicationKeys(applicationId, "PRODUCTION", null, grantTypes);

        ApplicationKeyGenerateRequestDTO applicationKeyGenerateRequestDTO = new ApplicationKeyGenerateRequestDTO();
        applicationKeyGenerateRequestDTO.setKeyType(ApplicationKeyGenerateRequestDTO.KeyTypeEnum.PRODUCTION);
        applicationKeyGenerateRequestDTO.setCallbackUrl(null);
        applicationKeyGenerateRequestDTO.setGrantTypesToBeSupported(grantTypes);

        Response response = applicationsApiService.applicationsApplicationIdGenerateKeysPost
                (applicationId, applicationKeyGenerateRequestDTO, request);

        Assert.assertEquals(500, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdKeysGet() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        List<OAuthApplicationInfo> oAuthApplicationInfoList = new ArrayList<>();

        Mockito.when(apiStore.getApplicationKeys(applicationId)).thenReturn(oAuthApplicationInfoList);

        Response response = applicationsApiService.applicationsApplicationIdKeysGet(applicationId, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdKeysGetErrorCase() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Mockito.doThrow(new APIManagementException("Error Occurred", ExceptionCodes.APPLICATION_KEY_MAPPING_NOT_FOUND))
                .when(apiStore).getApplicationKeys(applicationId);

        Response response = applicationsApiService.applicationsApplicationIdKeysGet(applicationId,request);

        Assert.assertEquals(404, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdKeysKeyTypeGet() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();
        String keyType = "PRODUCTION";

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        List<String> grantTypes = new ArrayList<>();
        grantTypes.add("password");
        grantTypes.add("jwt");

        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setKeyType(keyType);
        oAuthApplicationInfo.setClientId(UUID.randomUUID().toString());
        oAuthApplicationInfo.setClientSecret(UUID.randomUUID().toString());
        oAuthApplicationInfo.setGrantTypes(grantTypes);

        Mockito.when(apiStore.getApplicationKeys(applicationId, keyType)).thenReturn(oAuthApplicationInfo);

        Response response = applicationsApiService.applicationsApplicationIdKeysKeyTypeGet
                (applicationId, keyType, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdKeysKeyTypeGetErrorCase() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();
        String keyType = "PRODUCTION";

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Mockito.doThrow(new APIManagementException("Error Occurred", ExceptionCodes.APPLICATION_KEY_MAPPING_NOT_FOUND))
                .when(apiStore).getApplicationKeys(applicationId, keyType);

        Response response = applicationsApiService.applicationsApplicationIdKeysKeyTypeGet
                (applicationId, keyType, request);

        Assert.assertEquals(404, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdKeysKeyTypePut() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();
        String accessToken = UUID.randomUUID().toString();
        String clientID = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();
        String keyType = "PRODUCTION";

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        ApplicationTokenDTO applicationTokenDTO = new ApplicationTokenDTO();
        applicationTokenDTO.setAccessToken(accessToken);
        applicationTokenDTO.setTokenScopes("SCOPE1");
        applicationTokenDTO.setValidityTime((long) 100000);

        List<String> grantTypes = new ArrayList<>();
        grantTypes.add("password");
        grantTypes.add("jwt");

        ApplicationKeysDTO applicationKeysDTO = new ApplicationKeysDTO();
        applicationKeysDTO.setConsumerKey(clientID);
        applicationKeysDTO.setConsumerSecret(clientSecret);
        applicationKeysDTO.setKeyType(ApplicationKeysDTO.KeyTypeEnum.PRODUCTION);
        applicationKeysDTO.setCallbackUrl(null);
        applicationKeysDTO.setSupportedGrantTypes(grantTypes);

        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setKeyType(keyType);
        oAuthApplicationInfo.setClientId(UUID.randomUUID().toString());
        oAuthApplicationInfo.setClientSecret(UUID.randomUUID().toString());
        oAuthApplicationInfo.setGrantTypes(grantTypes);

        Mockito.when(apiStore.updateGrantTypesAndCallbackURL(applicationId, keyType, grantTypes, null))
                .thenReturn(oAuthApplicationInfo);

        Response response = applicationsApiService.applicationsApplicationIdKeysKeyTypePut
                (applicationId, keyType, applicationKeysDTO, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdKeysKeyTypePutErrorCase() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();
        String accessToken = UUID.randomUUID().toString();
        String clientID = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();
        String keyType = "PRODUCTION";

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        ApplicationTokenDTO applicationTokenDTO = new ApplicationTokenDTO();
        applicationTokenDTO.setAccessToken(accessToken);
        applicationTokenDTO.setTokenScopes("SCOPE1");
        applicationTokenDTO.setValidityTime((long) 100000);

        List<String> grantTypes = new ArrayList<>();
        grantTypes.add("password");
        grantTypes.add("jwt");

        ApplicationKeysDTO applicationKeysDTO = new ApplicationKeysDTO();
        applicationKeysDTO.setConsumerKey(clientID);
        applicationKeysDTO.setConsumerSecret(clientSecret);
        applicationKeysDTO.setKeyType(ApplicationKeysDTO.KeyTypeEnum.PRODUCTION);
        applicationKeysDTO.setCallbackUrl(null);
        applicationKeysDTO.setSupportedGrantTypes(grantTypes);

        Mockito.doThrow(new APIManagementException("Error Occurred", ExceptionCodes.INTERNAL_ERROR))
                .when(apiStore).updateGrantTypesAndCallbackURL(applicationId, keyType, grantTypes, null);

        Response response = applicationsApiService.applicationsApplicationIdKeysKeyTypePut
                (applicationId, keyType, applicationKeysDTO, request);

        Assert.assertEquals(500, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdGenerateTokenPost() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();
        String accessToken = UUID.randomUUID().toString();
        String clientID = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        ApplicationToken applicationToken = new ApplicationToken();
        applicationToken.setAccessToken(accessToken);
        applicationToken.setValidityPeriod(10000);
        applicationToken.setScopes("SCOPE1");

        Mockito.when(apiStore.generateApplicationToken
                (clientID, clientSecret, "SCOPE1", 1000, "revokeToken"))
                .thenReturn(applicationToken);

        ApplicationTokenGenerateRequestDTO generateRequestDTO = new ApplicationTokenGenerateRequestDTO();
        generateRequestDTO.setConsumerKey(clientID);
        generateRequestDTO.setConsumerSecret(clientSecret);
        generateRequestDTO.setRevokeToken("revokeToken");
        generateRequestDTO.setScopes("SCOPE1");
        generateRequestDTO.setValidityPeriod(10000);

        Response response = applicationsApiService.applicationsApplicationIdGenerateTokenPost
                (applicationId, generateRequestDTO, null, null, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdGenerateTokenPostErrorCase() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();
        String accessToken = UUID.randomUUID().toString();
        String clientID = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        ApplicationTokenGenerateRequestDTO generateRequestDTO = new ApplicationTokenGenerateRequestDTO();
        generateRequestDTO.setConsumerKey(clientID);
        generateRequestDTO.setConsumerSecret(clientSecret);
        generateRequestDTO.setRevokeToken("revokeToken");
        generateRequestDTO.setScopes("SCOPE1");
        generateRequestDTO.setValidityPeriod(10000);

        Mockito.doThrow(new APIManagementException("Error Occurred", ExceptionCodes.APPLICATION_TOKEN_GENERATION_FAILED))
                .when(apiStore).generateApplicationToken
                (clientID, clientSecret, "SCOPE1", 10000, "revokeToken");

        Response response = applicationsApiService.applicationsApplicationIdGenerateTokenPost
                (applicationId, generateRequestDTO, null, null, request);

        Assert.assertEquals(500, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdPut() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();
        String accessToken = UUID.randomUUID().toString();
        String clientID = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        ApplicationTokenDTO applicationTokenDTO = new ApplicationTokenDTO();
        applicationTokenDTO.setAccessToken(accessToken);
        applicationTokenDTO.setTokenScopes("SCOPE1");
        applicationTokenDTO.setValidityTime((long) 100000);

        List<String> grantTypes = new ArrayList<>();
        grantTypes.add("password");
        grantTypes.add("jwt");

        ApplicationKeysDTO applicationKeysDTO = new ApplicationKeysDTO();
        applicationKeysDTO.setConsumerKey(clientID);
        applicationKeysDTO.setConsumerSecret(clientSecret);
        applicationKeysDTO.setKeyType(ApplicationKeysDTO.KeyTypeEnum.PRODUCTION);
        applicationKeysDTO.setCallbackUrl(null);
        applicationKeysDTO.setSupportedGrantTypes(grantTypes);

        List<ApplicationKeysDTO> applicationKeysDTOList = new ArrayList<>();
        applicationKeysDTOList.add(applicationKeysDTO);

        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setApplicationId(applicationId);
        applicationDTO.setDescription("sample application");
        applicationDTO.setName("app1");
        applicationDTO.setSubscriber("subscriber");
        applicationDTO.setPermission("permission");
        applicationDTO.setLifeCycleStatus("APPROVED");
        applicationDTO.setThrottlingTier("UNLIMITED");
        applicationDTO.setToken(applicationTokenDTO);
        applicationDTO.setKeys(applicationKeysDTOList);

        WorkflowResponse workflowResponse = new GeneralWorkflowResponse();
        workflowResponse.setWorkflowStatus(WorkflowStatus.APPROVED);

        Mockito.when(apiStore.getApplication(applicationId, USER))
                .thenReturn(getSampleApplication(applicationId));
        Mockito.when
                (apiStore.updateApplication(applicationId, getSampleApplication(applicationId)))
                .thenReturn(workflowResponse);
        Mockito.when(apiStore.getApplication(applicationId, USER)).thenReturn(getSampleApplication(applicationId));

        Response response = applicationsApiService.applicationsApplicationIdPut
                (applicationId, applicationDTO, null, null, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApplicationsApplicationIdPutErrorCase() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();
        String accessToken = UUID.randomUUID().toString();
        String clientID = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        ApplicationTokenDTO applicationTokenDTO = new ApplicationTokenDTO();
        applicationTokenDTO.setAccessToken(accessToken);
        applicationTokenDTO.setTokenScopes("SCOPE1");
        applicationTokenDTO.setValidityTime((long) 100000);

        List<String> grantTypes = new ArrayList<>();
        grantTypes.add("password");
        grantTypes.add("jwt");

        ApplicationKeysDTO applicationKeysDTO = new ApplicationKeysDTO();
        applicationKeysDTO.setConsumerKey(clientID);
        applicationKeysDTO.setConsumerSecret(clientSecret);
        applicationKeysDTO.setKeyType(ApplicationKeysDTO.KeyTypeEnum.PRODUCTION);
        applicationKeysDTO.setCallbackUrl(null);
        applicationKeysDTO.setSupportedGrantTypes(grantTypes);

        List<ApplicationKeysDTO> applicationKeysDTOList = new ArrayList<>();
        applicationKeysDTOList.add(applicationKeysDTO);

        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setApplicationId(applicationId);
        applicationDTO.setDescription("sample application");
        applicationDTO.setName("app1");
        applicationDTO.setSubscriber("subscriber");
        applicationDTO.setPermission("permission");
        applicationDTO.setLifeCycleStatus("APPROVED");
        applicationDTO.setThrottlingTier("UNLIMITED");
        applicationDTO.setToken(applicationTokenDTO);
        applicationDTO.setKeys(applicationKeysDTOList);

        WorkflowResponse workflowResponse = new GeneralWorkflowResponse();
        workflowResponse.setWorkflowStatus(WorkflowStatus.APPROVED);

        Mockito.when(apiStore.getApplication(applicationId, USER))
                .thenReturn(getSampleApplication(applicationId));
        Mockito.doThrow(new APIManagementException("Error Occurred", ExceptionCodes.INTERNAL_ERROR))
                .when(apiStore).updateApplication(applicationId, getSampleApplication(applicationId));

        Response response = applicationsApiService.applicationsApplicationIdPut
                (applicationId, applicationDTO, null, null, request);

        Assert.assertEquals(500, response.getStatus());
    }

    @Test
    public void testApplicationsGetBlankQuery() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId1 = UUID.randomUUID().toString();
        String applicationId2 = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Application application1 = getSampleApplication(applicationId1);
        Application application2 = getSampleApplication(applicationId2);

        List<Application> applicationList = new ArrayList<>();
        applicationList.add(application1);
        applicationList.add(application2);

        Mockito.when(apiStore.getApplications(USER)).thenReturn(applicationList);

        Response response = applicationsApiService.applicationsGet
                (null, 10, 0, null, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApplicationsGetQuery() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId1 = UUID.randomUUID().toString();
        String applicationId2 = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Application application1 = getSampleApplication(applicationId1);
        Application application2 = getSampleApplication(applicationId2);

        List<Application> applicationList = new ArrayList<>();
        applicationList.add(application1);
        applicationList.add(application2);

        // TODO - Seems there is an issue here. Need to check
        Mockito.when(apiStore.getApplications(USER)).thenReturn(applicationList);

        Response response = applicationsApiService.applicationsGet
                ("*", 10, 0, null, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testApplicationsGetQueryErrorCase() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Mockito.doThrow(new APIManagementException("Error Occurred", ExceptionCodes.APPLICATION_NOT_FOUND))
                .when(apiStore).getApplicationByName(USER, "*");

        Response response = applicationsApiService.applicationsGet
                ("*", 10, 0, null, request);

        Assert.assertEquals(404, response.getStatus());
    }

    @Test
    public void testApplicationsPost() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();
        String accessToken = UUID.randomUUID().toString();
        String clientID = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Application application = getSampleApplication(applicationId);

        ApplicationTokenDTO applicationTokenDTO = new ApplicationTokenDTO();
        applicationTokenDTO.setAccessToken(accessToken);
        applicationTokenDTO.setTokenScopes("SCOPE1");
        applicationTokenDTO.setValidityTime((long) 100000);

        List<String> grantTypes = new ArrayList<>();
        grantTypes.add("password");
        grantTypes.add("jwt");

        ApplicationKeysDTO applicationKeysDTO = new ApplicationKeysDTO();
        applicationKeysDTO.setConsumerKey(clientID);
        applicationKeysDTO.setConsumerSecret(clientSecret);
        applicationKeysDTO.setKeyType(ApplicationKeysDTO.KeyTypeEnum.PRODUCTION);
        applicationKeysDTO.setCallbackUrl(null);
        applicationKeysDTO.setSupportedGrantTypes(grantTypes);

        List<ApplicationKeysDTO> applicationKeysDTOList = new ArrayList<>();
        applicationKeysDTOList.add(applicationKeysDTO);

        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setApplicationId(applicationId);
        applicationDTO.setDescription("sample application");
        applicationDTO.setName("app1");
        applicationDTO.setSubscriber("subscriber");
        applicationDTO.setPermission("permission");
        applicationDTO.setLifeCycleStatus("APPROVED");
        applicationDTO.setThrottlingTier("UNLIMITED");
        applicationDTO.setToken(applicationTokenDTO);
        applicationDTO.setKeys(applicationKeysDTOList);

        Mockito.doThrow(new APIManagementException("Error Occurred", ExceptionCodes.INTERNAL_ERROR))
                .when(apiStore).addApplication(application);

        Response response = applicationsApiService.applicationsPost(applicationDTO, request);

        Assert.assertEquals(500, response.getStatus());
    }

    @Test
    public void testApplicationsPostErrorCase() throws APIManagementException, NotFoundException {
        TestUtil.printTestMethodName();
        String applicationId = UUID.randomUUID().toString();
        String accessToken = UUID.randomUUID().toString();
        String clientID = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();

        ApplicationsApiServiceImpl applicationsApiService = new ApplicationsApiServiceImpl();
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getConsumer(USER)).thenReturn(apiStore);
        Request request = getRequest();
        PowerMockito.when(RestApiUtil.getLoggedInUsername(request)).thenReturn(USER);

        Application application = getSampleApplication(applicationId);

        WorkflowResponse workflowResponse = new GeneralWorkflowResponse();
        workflowResponse.setWorkflowStatus(WorkflowStatus.APPROVED);

        ApplicationCreationResponse creationResponse = new ApplicationCreationResponse(UUID.randomUUID().toString(), workflowResponse);

        Mockito.when(apiStore.addApplication(application)).thenReturn(creationResponse);
        Mockito.when(apiStore.getApplication(creationResponse.getApplicationUUID(), USER)).thenReturn(application);

        ApplicationTokenDTO applicationTokenDTO = new ApplicationTokenDTO();
        applicationTokenDTO.setAccessToken(accessToken);
        applicationTokenDTO.setTokenScopes("SCOPE1");
        applicationTokenDTO.setValidityTime((long) 100000);

        List<String> grantTypes = new ArrayList<>();
        grantTypes.add("password");
        grantTypes.add("jwt");

        ApplicationKeysDTO applicationKeysDTO = new ApplicationKeysDTO();
        applicationKeysDTO.setConsumerKey(clientID);
        applicationKeysDTO.setConsumerSecret(clientSecret);
        applicationKeysDTO.setKeyType(ApplicationKeysDTO.KeyTypeEnum.PRODUCTION);
        applicationKeysDTO.setCallbackUrl(null);
        applicationKeysDTO.setSupportedGrantTypes(grantTypes);

        List<ApplicationKeysDTO> applicationKeysDTOList = new ArrayList<>();
        applicationKeysDTOList.add(applicationKeysDTO);

        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setApplicationId(applicationId);
        applicationDTO.setDescription("sample application");
        applicationDTO.setName("app1");
        applicationDTO.setSubscriber("subscriber");
        applicationDTO.setPermission("permission");
        applicationDTO.setLifeCycleStatus("APPROVED");
        applicationDTO.setThrottlingTier("UNLIMITED");
        applicationDTO.setToken(applicationTokenDTO);
        applicationDTO.setKeys(applicationKeysDTOList);

        Response response = applicationsApiService.applicationsPost(applicationDTO, request);

        Assert.assertEquals(201, response.getStatus());
    }

    // Sample request to be used by tests
    private Request getRequest() throws APIMgtSecurityException {
        CarbonMessage carbonMessage = new HTTPCarbonMessage();
        carbonMessage.setProperty("LOGGED_IN_USER", USER);
        Request request = new Request(carbonMessage);
        return request;
    }

    private static void printTestMethodName() {
        logger.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }

    private Application getSampleApplication(String applicationId) {
        String accessToken = UUID.randomUUID().toString();
        String clientID = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();

        ApplicationTokenDTO applicationTokenDTO = new ApplicationTokenDTO();
        applicationTokenDTO.setAccessToken(accessToken);
        applicationTokenDTO.setTokenScopes("SCOPE1");
        applicationTokenDTO.setValidityTime((long) 100000);

        List<String> grantTypes = new ArrayList<>();
        grantTypes.add("password");
        grantTypes.add("jwt");

        ApplicationKeysDTO applicationKeysDTO = new ApplicationKeysDTO();
        applicationKeysDTO.setConsumerKey(clientID);
        applicationKeysDTO.setConsumerSecret(clientSecret);
        applicationKeysDTO.setKeyType(ApplicationKeysDTO.KeyTypeEnum.PRODUCTION);
        applicationKeysDTO.setCallbackUrl(null);
        applicationKeysDTO.setSupportedGrantTypes(grantTypes);

        List<ApplicationKeysDTO> applicationKeysDTOList = new ArrayList<>();
        applicationKeysDTOList.add(applicationKeysDTO);

        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setApplicationId(applicationId);
        applicationDTO.setDescription("sample application");
        applicationDTO.setName("app1");
        applicationDTO.setSubscriber("subscriber");
        applicationDTO.setPermission("permission");
        applicationDTO.setLifeCycleStatus("APPROVED");
        applicationDTO.setThrottlingTier("UNLIMITED");
        applicationDTO.setToken(applicationTokenDTO);
        applicationDTO.setKeys(applicationKeysDTOList);

        return ApplicationMappingUtil.fromDTOtoApplication(applicationDTO, USER);
    }
}
