/*
 *
 *   Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataDTO;

import java.io.File;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ImportUtils.class, APIConstants.class, APIProvider.class, CommonUtil.class,
        FileUtils.class, APIUtil.class, RestApiCommonUtil.class })
public class ImportUtilsTest {
    private static final String ORGANIZATION = "carbon.super";
    private static final String POLICYNAME = "customCommonLogPolicy";
    private static final String POLICYVERSION = "v1";
    private static OperationPolicyData policyData;
    private final String pathToArchive = "/tmp/test/customCommonLogPolicy";
    private final String yamlFile = pathToArchive + "/customCommonLogPolicy.yaml";
    private final String jsonFile = pathToArchive + "/customCommonLogPolicy.json";
    private APIProvider apiProvider;
    private final JsonObject endpointConfigObject = new JsonObject();
    private final JsonObject config = new JsonObject();

    @Before
    public void init() throws Exception {
        PowerMockito.mockStatic(CommonUtil.class);
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.stub(
                PowerMockito.method(APIUtil.class, "getOperationPolicyDefinitionFromFile", String.class,
                        String.class,
                                String.class));
        apiProvider = Mockito.mock(APIProvider.class);
        policyData = Mockito.mock(OperationPolicyData.class);
        PowerMockito.mockStatic(APIConstants.class);
        endpointConfigObject.add(APIConstants.ENDPOINT_SPECIFIC_CONFIG, config);

    }

    @Test
    public void testImportAPIPolicy() throws Exception {

        String policyDefContent = "{\"type\":\"operation_policy_specification\",\"version\":\"v4.1.0\",\"data\":"
                + "{\"category\":\"Mediation\",\"name\":\"customCommonLogPolicy\",\"version\":\"v1\",\"displayName\""
                + ":\"CustomCommonLogPolicy\",\"description\":\"Usingthispolicy,youcanaddacustomlogmessage\""
                + ",\"applicableFlows\":[\"request\",\"response\",\"fault\"],\"supportedGateways\":[\"Synapse\"]"
                + ",\"supportedApiTypes\":[\"HTTP\"],\"policyAttributes\":[]}}";

        Mockito.when(CommonUtil.checkFileExistence(yamlFile)).thenReturn(false);
        Mockito.when(CommonUtil.checkFileExistence(jsonFile)).thenReturn(true);
        Mockito.when(FileUtils.readFileToString(new File(jsonFile))).thenReturn(policyDefContent);

        Mockito.when(apiProvider.getCommonOperationPolicyByPolicyName(POLICYNAME, POLICYVERSION, ORGANIZATION, false))
                .thenReturn(null);

        OperationPolicyDefinition gatewayDefinition = Mockito.mock(OperationPolicyDefinition.class);

        PowerMockito.stub(
                PowerMockito.method(APIUtil.class, "getOperationPolicyDefinitionFromFile", String.class,
                String.class,
                        String.class)).toReturn(gatewayDefinition);

        String md5Hash = RandomStringUtils.randomAlphanumeric(30);

        PowerMockito.stub(
                PowerMockito.method(APIUtil.class, "getHashOfOperationPolicy", OperationPolicyData.class)).
                toReturn(md5Hash);

        String policyId = RandomStringUtils.randomAlphanumeric(10);

        Mockito.when(apiProvider.addCommonOperationPolicy(ArgumentMatchers.any(OperationPolicyData.class),
                ArgumentMatchers.eq(ORGANIZATION))).thenReturn(policyId);

        try {
            OperationPolicyDataDTO operationPolicyDataDTO = ImportUtils.importPolicy(pathToArchive, ORGANIZATION,
                    apiProvider);
            Assert.assertNotNull(operationPolicyDataDTO);
        } catch (APIManagementException ex) {
            Assert.fail("Import Policy failed due to an exception!");
        }

        // error path
        Mockito.when(apiProvider.getCommonOperationPolicyByPolicyName(POLICYNAME, POLICYVERSION, ORGANIZATION, false))
                .thenReturn(policyData);

        String errorMsg = "Error while adding a common operation policy.Existing common operation policy found "
                + "for the same name.";

        try {
            ImportUtils.importPolicy(pathToArchive, ORGANIZATION, apiProvider);
            Assert.fail("Cannot create an existing API Policy!");
        } catch (APIManagementException ex) {
            Assert.assertEquals(errorMsg, ex.getMessage());
        }
    }

    @Test
    public void testGetUpdatedEndpointConfig() throws Exception {
        String activeDuration = "200";
        config.addProperty(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION, activeDuration);
        JsonObject actualConfig = ImportUtils.getUpdatedEndpointConfig(endpointConfigObject)
                .get(APIConstants.ENDPOINT_SPECIFIC_CONFIG).getAsJsonObject();
        String actualDuration = actualConfig.get(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION).getAsString();
        Assert.assertEquals(actualDuration, activeDuration);
    }

    @Test
    public void testGetUpdatedEndpointConfigWithEmptyActionDuration() throws Exception {
        String emptyActiveDuration = "";
        config.addProperty(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION, emptyActiveDuration);
        JsonObject actualConfig = ImportUtils.getUpdatedEndpointConfig(endpointConfigObject)
                .get(APIConstants.ENDPOINT_SPECIFIC_CONFIG).getAsJsonObject();
        Assert.assertNull(actualConfig.get(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION));
    }

    /**
     * An endpoint URL carried in a project archive must be checked against the network access control policy
     * before the endpoint is added, and the rejection must keep its own error code rather than being reported
     * as a generic failure to add endpoints.
     */
    @Test
    public void testArchiveEndpointBlockedByPolicyKeepsItsErrorCode() throws Exception {

        String endpointsJson = "{\"data\":[{\"id\":\"endpoint-1\",\"name\":\"ep1\","
                + "\"deploymentStage\":\"PRODUCTION\",\"endpointConfig\":{\"endpoint_type\":\"http\","
                + "\"production_endpoints\":{\"url\":\"http://blocked.test/store\"}}}]}";

        PowerMockito.when(CommonUtil.checkFileExistence(ArgumentMatchers.anyString())).thenReturn(true);
        PowerMockito.when(CommonUtil.yamlToJson(ArgumentMatchers.anyString())).thenReturn(endpointsJson);
        PowerMockito.when(FileUtils.readFileToString(ArgumentMatchers.any(File.class)))
                .thenReturn(endpointsJson);
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(ORGANIZATION);

        // Only the policy check is stubbed, so the real URL extraction still runs and the endpoint URL has
        // to reach the gate for this to throw at all.
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doThrow(new APIManagementException("blocked", ExceptionCodes.UNTRUSTED_URL))
                .when(APIUtil.class, "validateRemoteURL", ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyString());

        API api = Mockito.mock(API.class);
        Mockito.when(api.getUuid()).thenReturn("api-uuid-1");

        try {
            ImportUtils.populateAPIWithEndpoints(api, apiProvider, pathToArchive, ORGANIZATION);
            Assert.fail("Expected the archive endpoint URL to be rejected by the access control policy");
        } catch (APIManagementException e) {
            Assert.assertNotNull("The rejection must carry an error handler", e.getErrorHandler());
            Assert.assertEquals("The policy error code must survive the outer exception wrapping",
                    ExceptionCodes.UNTRUSTED_URL.getErrorCode(), e.getErrorHandler().getErrorCode());
        }
        // The endpoint must never be persisted once its URL is refused.
        Mockito.verify(apiProvider, Mockito.never())
                .addAPIEndpoint(ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.anyString());
    }

    @Test
    public void testGetUpdatedEndpointConfigConvertsNumericValuesToIntegerStrings() throws Exception {
        JsonObject endpoint = new JsonParser().parse("{\"url\":\"https://api.example.com\",\"config\":{"
                + "\"suspendDuration\":100,\"suspendMaxDuration\":10.0,\"retryTimeOut\":1,\"retryDelay\":5,"
                + "\"suspendErrorCode\":[101001,\"101500\"],\"retryErroCode\":[101001.0],"
                + "\"factor\":2.5,\"actionSelect\":\"fault\"}}").getAsJsonObject();

        JsonObject actualConfig = ImportUtils.getUpdatedEndpointConfig(endpoint)
                .get(APIConstants.ENDPOINT_SPECIFIC_CONFIG).getAsJsonObject();

        JsonPrimitive suspendDuration = actualConfig.getAsJsonPrimitive(APIConstants.ENDPOINT_CONFIG_SUSPEND_DURATION);
        Assert.assertEquals("100", suspendDuration.getAsString());
        Assert.assertTrue(suspendDuration.isString());
        Assert.assertEquals("10", actualConfig.get(APIConstants.ENDPOINT_CONFIG_SUSPEND_MAX_DURATION).getAsString());
        Assert.assertEquals("1", actualConfig.get(APIConstants.ENDPOINT_CONFIG_RETRY_TIMEOUT).getAsString());
        Assert.assertEquals("5", actualConfig.get(APIConstants.ENDPOINT_CONFIG_RETRY_DELAY).getAsString());
        JsonArray suspendErrorCodes = actualConfig.getAsJsonArray(APIConstants.ENDPOINT_CONFIG_SUSPEND_ERROR_CODE);
        Assert.assertEquals("101001", suspendErrorCodes.get(0).getAsString());
        Assert.assertTrue(suspendErrorCodes.get(0).getAsJsonPrimitive().isString());
        Assert.assertEquals("101500", suspendErrorCodes.get(1).getAsString());
        Assert.assertEquals("101001",
                actualConfig.getAsJsonArray(APIConstants.ENDPOINT_CONFIG_RETRY_ERROR_CODE).get(0).getAsString());
        // progression factor is a float in Synapse, so it is left untouched
        Assert.assertEquals(2.5, actualConfig.get("factor").getAsDouble(), 0);
        Assert.assertEquals("fault", actualConfig.get("actionSelect").getAsString());
    }

    @Test
    public void testGetUpdatedEndpointConfigKeepsStringValues() throws Exception {
        config.addProperty(APIConstants.ENDPOINT_CONFIG_SUSPEND_DURATION, "30");
        config.addProperty(APIConstants.ENDPOINT_CONFIG_RETRY_TIMEOUT, "");
        config.addProperty(APIConstants.ENDPOINT_CONFIG_RETRY_DELAY, "abc");
        JsonArray errorCodes = new JsonArray();
        errorCodes.add("101503");
        config.add(APIConstants.ENDPOINT_CONFIG_SUSPEND_ERROR_CODE, errorCodes);

        JsonObject actualConfig = ImportUtils.getUpdatedEndpointConfig(endpointConfigObject)
                .get(APIConstants.ENDPOINT_SPECIFIC_CONFIG).getAsJsonObject();

        Assert.assertEquals("30", actualConfig.get(APIConstants.ENDPOINT_CONFIG_SUSPEND_DURATION).getAsString());
        Assert.assertEquals("", actualConfig.get(APIConstants.ENDPOINT_CONFIG_RETRY_TIMEOUT).getAsString());
        Assert.assertEquals("abc", actualConfig.get(APIConstants.ENDPOINT_CONFIG_RETRY_DELAY).getAsString());
        Assert.assertEquals("101503",
                actualConfig.getAsJsonArray(APIConstants.ENDPOINT_CONFIG_SUSPEND_ERROR_CODE).get(0).getAsString());
    }

    @Test
    public void testGetUpdatedEndpointConfigWithoutAdvancedConfig() throws Exception {
        JsonObject endpoint = new JsonParser().parse("{\"url\":\"https://api.example.com\"}").getAsJsonObject();
        Assert.assertEquals(endpoint, ImportUtils.getUpdatedEndpointConfig(endpoint));

        JsonObject endpointWithNullConfig = new JsonParser()
                .parse("{\"url\":\"https://api.example.com\",\"config\":null}").getAsJsonObject();
        Assert.assertEquals(endpointWithNullConfig, ImportUtils.getUpdatedEndpointConfig(endpointWithNullConfig));
    }

    @Test
    public void testPreProcessEndpointConfigNormalizesAllEndpointTypes() throws Exception {
        String numericConfig = "{\"suspendDuration\":100,\"retryTimeOut\":1,\"retryErroCode\":[101001]}";
        JsonObject apiConfig = new JsonParser().parse("{\"endpointConfig\":{\"endpoint_type\":\"failover\","
                + "\"production_endpoints\":{\"url\":\"https://api.example.com/1\",\"config\":" + numericConfig + "},"
                + "\"sandbox_endpoints\":[{\"url\":\"https://api.example.com/2\",\"config\":" + numericConfig + "}],"
                + "\"production_failovers\":[{\"url\":\"https://api.example.com/3\",\"config\":" + numericConfig
                + "}],\"sandbox_failovers\":[{\"url\":\"https://api.example.com/4\",\"config\":" + numericConfig
                + "}]}}").getAsJsonObject();

        JsonObject processed = Whitebox.invokeMethod(ImportUtils.class, "preProcessEndpointConfig", apiConfig);

        // Same round trip the gateway artifact generation does: Gson into APIDTO, Jackson back to a string
        APIDTO apidto = new Gson().fromJson(processed, APIDTO.class);
        String endpointConfig = new ObjectMapper().writeValueAsString(apidto.getEndpointConfig());
        Assert.assertFalse(endpointConfig, endpointConfig.contains(".0"));
        Assert.assertEquals(endpointConfig, 4, StringUtils.countMatches(endpointConfig, "\"suspendDuration\":\"100\""));
        Assert.assertEquals(endpointConfig, 4, StringUtils.countMatches(endpointConfig, "\"retryTimeOut\":\"1\""));
        Assert.assertEquals(endpointConfig, 4,
                StringUtils.countMatches(endpointConfig, "\"retryErroCode\":[\"101001\"]"));
    }

    @Test
    public void testPreProcessEndpointConfigKeepsFailoverStringValues() throws Exception {
        JsonObject apiConfig = new JsonParser().parse("{\"endpointConfig\":{\"endpoint_type\":\"failover\","
                + "\"production_endpoints\":{\"url\":\"https://api.example.com/1\"},"
                + "\"production_failovers\":[{\"url\":\"https://api.example.com/2\",\"config\":"
                + "{\"actionDuration\":\"\",\"retryTimeOut\":\"\"}},{\"url\":\"https://api.example.com/3\","
                + "\"config\":{\"actionDuration\":\"abc\"}},{\"url\":\"https://api.example.com/4\","
                + "\"config\":{\"actionDuration\":300}}]}}").getAsJsonObject();

        JsonObject processed = Whitebox.invokeMethod(ImportUtils.class, "preProcessEndpointConfig", apiConfig);

        JsonArray failovers = processed.getAsJsonObject("endpointConfig").getAsJsonArray("production_failovers");
        JsonObject emptyValues = failovers.get(0).getAsJsonObject().getAsJsonObject("config");
        Assert.assertEquals("", emptyValues.get(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION).getAsString());
        Assert.assertEquals("", emptyValues.get(APIConstants.ENDPOINT_CONFIG_RETRY_TIMEOUT).getAsString());
        Assert.assertEquals("abc",
                failovers.get(1).getAsJsonObject().getAsJsonObject(APIConstants.ENDPOINT_SPECIFIC_CONFIG)
                        .get(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION).getAsString());
        JsonObject numericValues = failovers.get(2).getAsJsonObject().getAsJsonObject("config");
        Assert.assertTrue(numericValues.getAsJsonPrimitive(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION).isString());
        Assert.assertEquals("300", numericValues.get(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION).getAsString());
    }

    @Test
    public void testGetUpdatedEndpointConfigConvertsOnlyIntegralValuesInRange() throws Exception {
        JsonObject endpoint = new JsonParser().parse("{\"url\":\"https://api.example.com\",\"config\":{"
                + "\"suspendDuration\":1E+2,\"retryTimeOut\":1.5,\"retryDelay\":3000000000,"
                + "\"suspendMaxDuration\":9007199254740993,"
                + "\"suspendErrorCode\":[101001.5,101001],\"retryErroCode\":[3000000000]}}").getAsJsonObject();

        JsonObject actualConfig = ImportUtils.getUpdatedEndpointConfig(endpoint)
                .get(APIConstants.ENDPOINT_SPECIFIC_CONFIG).getAsJsonObject();

        // integral values are converted exactly, including exponent notation and values beyond double precision
        Assert.assertEquals("100", actualConfig.get(APIConstants.ENDPOINT_CONFIG_SUSPEND_DURATION).getAsString());
        Assert.assertEquals("9007199254740993",
                actualConfig.get(APIConstants.ENDPOINT_CONFIG_SUSPEND_MAX_DURATION).getAsString());
        Assert.assertTrue(
                actualConfig.getAsJsonPrimitive(APIConstants.ENDPOINT_CONFIG_SUSPEND_MAX_DURATION).isString());
        // fractional values are not rounded and values outside the int range are not converted
        assertUnchangedNumber(actualConfig.get(APIConstants.ENDPOINT_CONFIG_RETRY_TIMEOUT), "1.5");
        assertUnchangedNumber(actualConfig.get(APIConstants.ENDPOINT_CONFIG_RETRY_DELAY), "3000000000");
        JsonArray suspendErrorCodes = actualConfig.getAsJsonArray(APIConstants.ENDPOINT_CONFIG_SUSPEND_ERROR_CODE);
        assertUnchangedNumber(suspendErrorCodes.get(0), "101001.5");
        Assert.assertEquals("101001", suspendErrorCodes.get(1).getAsString());
        assertUnchangedNumber(actualConfig.getAsJsonArray(APIConstants.ENDPOINT_CONFIG_RETRY_ERROR_CODE).get(0),
                "3000000000");
    }

    @Test
    public void testPreProcessEndpointConfigConvertsFailoverActionDurationExactly() throws Exception {
        JsonObject apiConfig = new JsonParser().parse("{\"endpointConfig\":{\"endpoint_type\":\"failover\","
                + "\"production_endpoints\":{\"url\":\"https://api.example.com/1\"},"
                + "\"production_failovers\":[{\"url\":\"https://api.example.com/2\",\"config\":"
                + "{\"actionDuration\":9007199254740993}},{\"url\":\"https://api.example.com/3\","
                + "\"config\":{\"actionDuration\":300.5}}]}}").getAsJsonObject();

        JsonObject processed = Whitebox.invokeMethod(ImportUtils.class, "preProcessEndpointConfig", apiConfig);

        JsonArray failovers = processed.getAsJsonObject("endpointConfig").getAsJsonArray("production_failovers");
        Assert.assertEquals("9007199254740993", failovers.get(0).getAsJsonObject()
                .getAsJsonObject(APIConstants.ENDPOINT_SPECIFIC_CONFIG)
                .get(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION).getAsString());
        assertUnchangedNumber(failovers.get(1).getAsJsonObject()
                .getAsJsonObject(APIConstants.ENDPOINT_SPECIFIC_CONFIG)
                .get(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION), "300.5");
    }

    @Test
    public void testGetUpdatedEndpointConfigKeepsPrimaryActionDurationAboveIntRange() throws Exception {
        JsonObject numericEndpoint = new JsonParser().parse("{\"url\":\"https://api.example.com\","
                + "\"config\":{\"actionDuration\":3000000000}}").getAsJsonObject();
        JsonObject stringEndpoint = new JsonParser().parse("{\"url\":\"https://api.example.com\","
                + "\"config\":{\"actionDuration\":\"3000000000\"}}").getAsJsonObject();
        JsonObject fractionalEndpoint = new JsonParser().parse("{\"url\":\"https://api.example.com\","
                + "\"config\":{\"actionDuration\":300.5}}").getAsJsonObject();

        JsonPrimitive numericDuration = ImportUtils.getUpdatedEndpointConfig(numericEndpoint)
                .getAsJsonObject(APIConstants.ENDPOINT_SPECIFIC_CONFIG)
                .getAsJsonPrimitive(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION);
        Assert.assertTrue(numericDuration.isString());
        Assert.assertEquals("3000000000", numericDuration.getAsString());
        Assert.assertEquals("3000000000", ImportUtils.getUpdatedEndpointConfig(stringEndpoint)
                .getAsJsonObject(APIConstants.ENDPOINT_SPECIFIC_CONFIG)
                .get(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION).getAsString());
        Assert.assertEquals("301", ImportUtils.getUpdatedEndpointConfig(fractionalEndpoint)
                .getAsJsonObject(APIConstants.ENDPOINT_SPECIFIC_CONFIG)
                .get(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION).getAsString());
    }

    @Test
    public void testPreProcessEndpointConfigConvertsLoadBalanceSessionTimeout() throws Exception {
        JsonObject numericConfig = new JsonParser().parse("{\"endpointConfig\":{\"endpoint_type\":\"load_balance\","
                + "\"sessionManagement\":\"http\",\"sessionTimeOut\":3000000000,"
                + "\"production_endpoints\":[{\"url\":\"https://api.example.com/1\"}]}}").getAsJsonObject();
        JsonObject stringConfig = new JsonParser().parse("{\"endpointConfig\":{\"endpoint_type\":\"load_balance\","
                + "\"sessionManagement\":\"http\",\"sessionTimeOut\":\"\","
                + "\"production_endpoints\":[{\"url\":\"https://api.example.com/1\"}]}}").getAsJsonObject();

        JsonObject processed = Whitebox.invokeMethod(ImportUtils.class, "preProcessEndpointConfig", numericConfig);
        JsonPrimitive sessionTimeout = processed.getAsJsonObject("endpointConfig")
                .getAsJsonPrimitive(ImportExportConstants.LOAD_BALANCE_SESSION_TIME_OUT_PROPERTY);
        Assert.assertTrue(sessionTimeout.isString());
        Assert.assertEquals("3000000000", sessionTimeout.getAsString());

        processed = Whitebox.invokeMethod(ImportUtils.class, "preProcessEndpointConfig", stringConfig);
        Assert.assertEquals("", processed.getAsJsonObject("endpointConfig")
                .get(ImportExportConstants.LOAD_BALANCE_SESSION_TIME_OUT_PROPERTY).getAsString());
    }

    @Test
    public void testConvertNumericValuesInEndpointsOfEndpointConfig() throws Exception {
        // shape of the endpoint config of an API endpoint in endpoints.yaml
        JsonObject endpointConfig = new JsonParser().parse("{\"endpoint_type\":\"failover\","
                + "\"production_endpoints\":{\"url\":\"https://api.example.com/1\","
                + "\"config\":{\"suspendDuration\":100,\"retryTimeOut\":\"2\"}},"
                + "\"sandbox_endpoints\":[{\"url\":\"https://api.example.com/2\","
                + "\"config\":{\"suspendErrorCode\":[101504]}}],"
                + "\"production_failovers\":[{\"url\":\"https://api.example.com/3\","
                + "\"config\":{\"actionDuration\":30000}}]}").getAsJsonObject();

        Whitebox.invokeMethod(ImportUtils.class, "convertNumericValuesInEndpoints", endpointConfig);

        JsonObject productionConfig = endpointConfig.getAsJsonObject(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS)
                .getAsJsonObject(APIConstants.ENDPOINT_SPECIFIC_CONFIG);
        Assert.assertTrue(
                productionConfig.getAsJsonPrimitive(APIConstants.ENDPOINT_CONFIG_SUSPEND_DURATION).isString());
        Assert.assertEquals("100", productionConfig.get(APIConstants.ENDPOINT_CONFIG_SUSPEND_DURATION).getAsString());
        Assert.assertEquals("2", productionConfig.get(APIConstants.ENDPOINT_CONFIG_RETRY_TIMEOUT).getAsString());
        Assert.assertEquals("101504", endpointConfig.getAsJsonArray(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS).get(0)
                .getAsJsonObject().getAsJsonObject(APIConstants.ENDPOINT_SPECIFIC_CONFIG)
                .getAsJsonArray(APIConstants.ENDPOINT_CONFIG_SUSPEND_ERROR_CODE).get(0).getAsString());
        Assert.assertEquals("30000", endpointConfig.getAsJsonArray(APIConstants.ENDPOINT_PRODUCTION_FAILOVERS).get(0)
                .getAsJsonObject().getAsJsonObject(APIConstants.ENDPOINT_SPECIFIC_CONFIG)
                .get(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION).getAsString());
    }

    private static void assertUnchangedNumber(JsonElement element, String expected) {
        Assert.assertTrue(element.getAsJsonPrimitive().isNumber());
        Assert.assertEquals(expected, element.getAsString());
    }
}
