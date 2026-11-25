/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubtypeConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.util.exception.BadRequestException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Covers the upfront checks of {@link ApisApiServiceImpl#deployAPIRevision}: the revision must exist and belong to
 * the API, and a production- or sandbox-type Regular (Synapse) gateway environment must only receive APIs that
 * define the matching endpoint. The checks are private helpers, so they are invoked reflectively; no Carbon runtime
 * or PowerMock is needed.
 */
public class ApisApiServiceImplDeployRevisionValidationTestCase {

    private static final String API_ID = "6f1c2d3e-0000-4000-8000-000000000001";
    private static final String OTHER_API_ID = "6f1c2d3e-0000-4000-8000-000000000002";
    private static final String REVISION_ID = "7a8b9c0d-0000-4000-8000-000000000003";
    private static final String ENDPOINT_URL = "https://api.example.com/backend";

    private final ApisApiServiceImpl apisApiService = new ApisApiServiceImpl();

    // ---- validateAPIRevisionForDeployment ----

    @Test
    public void testUnknownRevisionIsRejectedWithRevisionNotFound() throws Throwable {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        Mockito.when(apiProvider.getAPIRevision(REVISION_ID)).thenReturn(null);

        assertRevisionNotFound(apiProvider);
    }

    @Test
    public void testRevisionOfAnotherApiIsRejectedWithRevisionNotFound() throws Throwable {

        APIProvider apiProvider = mockProviderWithRevisionOf(OTHER_API_ID);

        assertRevisionNotFound(apiProvider);
    }

    @Test
    public void testRevisionOfTheSameApiIsAccepted() throws Throwable {

        validateRevision(mockProviderWithRevisionOf(API_ID));
    }

    @Test
    public void testRevisionOwnershipCheckIgnoresUuidCase() throws Throwable {

        validateRevision(mockProviderWithRevisionOf(API_ID.toUpperCase()));
    }

    // ---- isEndpointValidationApplicable ----

    @Test
    public void testValidationAppliesToRegularProductionAndSandboxEnvironments() throws Throwable {

        Assert.assertTrue(isValidationApplicable(environment(APIConstants.GATEWAY_ENV_TYPE_PRODUCTION,
                APIConstants.API_GATEWAY_TYPE_REGULAR)));
        Assert.assertTrue(isValidationApplicable(environment(APIConstants.GATEWAY_ENV_TYPE_SANDBOX,
                APIConstants.API_GATEWAY_TYPE_REGULAR)));
        // the gateway type is matched case-insensitively
        Assert.assertTrue(isValidationApplicable(environment(APIConstants.GATEWAY_ENV_TYPE_SANDBOX,
                APIConstants.API_GATEWAY_TYPE_REGULAR.toLowerCase())));
    }

    @Test
    public void testValidationDoesNotApplyToHybridEnvironments() throws Throwable {

        Assert.assertFalse(isValidationApplicable(environment(APIConstants.GATEWAY_ENV_TYPE_HYBRID,
                APIConstants.API_GATEWAY_TYPE_REGULAR)));
    }

    @Test
    public void testValidationDoesNotApplyToNonSynapseGateways() throws Throwable {

        Assert.assertFalse(isValidationApplicable(environment(APIConstants.GATEWAY_ENV_TYPE_SANDBOX, "APK")));
        Assert.assertFalse(isValidationApplicable(environment(APIConstants.GATEWAY_ENV_TYPE_PRODUCTION, "AWS")));
        Assert.assertFalse(isValidationApplicable(environment(APIConstants.GATEWAY_ENV_TYPE_SANDBOX, null)));
    }

    @Test
    public void testValidationDoesNotApplyToUnknownEnvironment() throws Throwable {

        Assert.assertFalse(isValidationApplicable(null));
    }

    // ---- validateEndpointsForGatewayEnvironmentType ----

    @Test
    public void testProductionOnlyApiIsRejectedForSandboxEnvironment() throws Throwable {

        assertMissingEndpointRejection(APIConstants.GATEWAY_ENV_TYPE_SANDBOX,
                api(endpointConfig("http", true, false)));
    }

    @Test
    public void testSandboxOnlyApiIsRejectedForProductionEnvironment() throws Throwable {

        assertMissingEndpointRejection(APIConstants.GATEWAY_ENV_TYPE_PRODUCTION,
                api(endpointConfig("http", false, true)));
    }

    @Test
    public void testApiWithMatchingEndpointIsAccepted() throws Throwable {

        validateEndpoints(APIConstants.GATEWAY_ENV_TYPE_PRODUCTION, api(endpointConfig("http", true, false)));
        validateEndpoints(APIConstants.GATEWAY_ENV_TYPE_SANDBOX, api(endpointConfig("http", false, true)));
        validateEndpoints(APIConstants.GATEWAY_ENV_TYPE_SANDBOX, api(endpointConfig("http", true, true)));
        validateEndpoints(APIConstants.GATEWAY_ENV_TYPE_PRODUCTION, api(endpointConfig("http", true, true)));
    }

    @Test
    public void testHybridEnvironmentImposesNoEndpointRequirement() throws Throwable {

        validateEndpoints(APIConstants.GATEWAY_ENV_TYPE_HYBRID, api(endpointConfig("http", true, false)));
        validateEndpoints(APIConstants.GATEWAY_ENV_TYPE_HYBRID, api(endpointConfig("http", false, true)));
    }

    @Test
    public void testAiApiIsLeftToTheGenerationTimeGuard() throws Throwable {

        // AI APIs can define their endpoints outside endpointConfig, so a missing endpointConfig entry is not proof
        // that the endpoint is missing
        APIDTO aiApi = api(endpointConfig("http", true, false));
        SubtypeConfigurationDTO subtypeConfiguration = new SubtypeConfigurationDTO();
        subtypeConfiguration.setSubtype(APIConstants.API_SUBTYPE_AI_API);
        aiApi.setSubtypeConfiguration(subtypeConfiguration);

        validateEndpoints(APIConstants.GATEWAY_ENV_TYPE_SANDBOX, aiApi);
    }

    @Test
    public void testApiWithAnotherSubtypeIsStillValidated() throws Throwable {

        APIDTO api = api(endpointConfig("http", true, false));
        SubtypeConfigurationDTO subtypeConfiguration = new SubtypeConfigurationDTO();
        subtypeConfiguration.setSubtype("DEFAULT");
        api.setSubtypeConfiguration(subtypeConfiguration);

        assertMissingEndpointRejection(APIConstants.GATEWAY_ENV_TYPE_SANDBOX, api);
    }

    @Test
    public void testAwsLambdaEndpointIsExemptRegardlessOfCase() throws Throwable {

        validateEndpoints(APIConstants.GATEWAY_ENV_TYPE_SANDBOX,
                api(endpointConfig(APIConstants.ENDPOINT_TYPE_AWSLAMBDA, false, false)));
        validateEndpoints(APIConstants.GATEWAY_ENV_TYPE_SANDBOX, api(endpointConfig("AWSLambda", false, false)));
        validateEndpoints(APIConstants.GATEWAY_ENV_TYPE_PRODUCTION, api(endpointConfig("AWSLAMBDA", false, false)));
    }

    @Test
    public void testEndpointConfigWithoutEndpointTypeIsNotRejected() throws Throwable {

        validateEndpoints(APIConstants.GATEWAY_ENV_TYPE_SANDBOX, api(endpointConfig(null, true, false)));
    }

    @Test
    public void testMissingOrNonMapEndpointConfigIsNotRejected() throws Throwable {

        validateEndpoints(APIConstants.GATEWAY_ENV_TYPE_SANDBOX, api(null));
        validateEndpoints(APIConstants.GATEWAY_ENV_TYPE_SANDBOX, api("not-a-map"));
    }

    @Test
    public void testMissingEnvironmentOrApiIsNotRejected() throws Throwable {

        invoke("validateEndpointsForGatewayEnvironmentType", new Class[]{Environment.class, APIDTO.class},
                null, api(endpointConfig("http", true, false)));
        invoke("validateEndpointsForGatewayEnvironmentType", new Class[]{Environment.class, APIDTO.class},
                environment(APIConstants.GATEWAY_ENV_TYPE_SANDBOX, APIConstants.API_GATEWAY_TYPE_REGULAR), null);
    }

    // ---- helpers ----

    private void assertRevisionNotFound(APIProvider apiProvider) throws Throwable {

        try {
            validateRevision(apiProvider);
            Assert.fail("The revision should be rejected");
        } catch (APIMgtResourceNotFoundException e) {
            Assert.assertEquals(ExceptionCodes.API_REVISION_NOT_FOUND.getErrorCode(),
                    e.getErrorHandler().getErrorCode());
            Assert.assertEquals(404, e.getErrorHandler().getHttpStatusCode());
        }
    }

    private void assertMissingEndpointRejection(String environmentType, APIDTO api) throws Throwable {

        try {
            validateEndpoints(environmentType, api);
            Assert.fail("Deployment to a '" + environmentType + "' environment should be rejected");
        } catch (BadRequestException e) {
            ErrorDTO error = (ErrorDTO) e.getResponse().getEntity();
            Assert.assertEquals(Long.valueOf(ExceptionCodes.MISSING_ENDPOINT_FOR_GATEWAY_ENV_TYPE.getErrorCode()),
                    error.getCode());
            Assert.assertEquals(400, e.getResponse().getStatus());
            Assert.assertTrue("The error should name the environment type",
                    error.getDescription().contains("'" + environmentType + "'"));
        }
    }

    private APIProvider mockProviderWithRevisionOf(String apiId) throws APIManagementException {

        APIRevision apiRevision = new APIRevision();
        apiRevision.setApiUUID(apiId);
        apiRevision.setRevisionUUID(REVISION_ID);
        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        Mockito.when(apiProvider.getAPIRevision(REVISION_ID)).thenReturn(apiRevision);
        return apiProvider;
    }

    private void validateRevision(APIProvider apiProvider) throws Throwable {

        invoke("validateAPIRevisionForDeployment", new Class[]{String.class, String.class, APIProvider.class},
                API_ID, REVISION_ID, apiProvider);
    }

    private boolean isValidationApplicable(Environment environment) throws Throwable {

        return (boolean) invoke("isEndpointValidationApplicable", new Class[]{Environment.class}, environment);
    }

    private void validateEndpoints(String environmentType, APIDTO api) throws Throwable {

        invoke("validateEndpointsForGatewayEnvironmentType", new Class[]{Environment.class, APIDTO.class},
                environment(environmentType, APIConstants.API_GATEWAY_TYPE_REGULAR), api);
    }

    private Object invoke(String methodName, Class<?>[] parameterTypes, Object... args) throws Throwable {

        Method method = ApisApiServiceImpl.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        try {
            return method.invoke(apisApiService, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private static Environment environment(String type, String gatewayType) {

        Environment environment = new Environment();
        environment.setName("TestEnv");
        environment.setType(type);
        environment.setGatewayType(gatewayType);
        return environment;
    }

    private static APIDTO api(Object endpointConfig) {

        APIDTO api = new APIDTO();
        api.setId(API_ID);
        api.setEndpointConfig(endpointConfig);
        return api;
    }

    private static Map<String, Object> endpointConfig(String endpointType, boolean production, boolean sandbox) {

        Map<String, Object> endpointConfig = new HashMap<>();
        if (endpointType != null) {
            endpointConfig.put(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE, endpointType);
        }
        if (production) {
            endpointConfig.put(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS, endpoint());
        }
        if (sandbox) {
            endpointConfig.put(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS, endpoint());
        }
        return endpointConfig;
    }

    private static Map<String, Object> endpoint() {

        Map<String, Object> endpoint = new HashMap<>();
        endpoint.put("url", ENDPOINT_URL);
        return endpoint;
    }
}
