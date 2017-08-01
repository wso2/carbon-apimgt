/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.rest.api.commons.util;


import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ErrorHandler;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.carbon.apimgt.rest.api.common.exception.BadRequestException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@PrepareForTest(IOUtils.class)
public class RestApiUtilTestCase {

    private static final Logger log = LoggerFactory.getLogger(RestApiUtil.class);

    @Test(description = "Test logged in user name retrieved")
    public void testGetLoggedInUsername() throws Exception {
        // component method is to be implemented
    }

    @Test(description = "Test handling bad Request")
    public void testHandleBadRequest() throws Exception {

        final String message = "Test Message";

        PowerMockito.mockStatic(RestApiUtil.class);
        try {
            RestApiUtil.handleBadRequest(message, log);
        } catch (BadRequestException e) {
            Assert.assertEquals(e.getMessage(), message);
        }
    }

    @Test(description = "Testing get Error DTO")
    public void testGetErrorDTO() throws Exception {

        ErrorHandler errorHandler = Mockito.mock(ErrorHandler.class);
        when(errorHandler.getErrorCode()).thenReturn((long) 900300);
        when(errorHandler.getErrorMessage()).thenReturn("Lifecycle exception occurred");
        when(errorHandler.getErrorDescription()).thenReturn("Error occurred while changing lifecycle state");

        ErrorDTO errorDTOExpected = new ErrorDTO();
        errorDTOExpected.setCode((long) 900300);
        errorDTOExpected.setMessage("Lifecycle exception occurred");
        errorDTOExpected.setDescription("Error occurred while changing lifecycle state");

        ErrorDTO errorDTO1 = RestApiUtil.getErrorDTO(errorHandler);
        Assert.assertEquals(errorDTO1.getCode(), errorDTOExpected.getCode());
        Assert.assertEquals(errorDTO1.getMessage(), errorDTOExpected.getMessage());
        Assert.assertEquals(errorDTO1.getDescription(), errorDTOExpected.getDescription());
    }

    @Test(description = "Test get Error DTO")
    public void testGetErrorDTO1() throws Exception {

        Map<String, String> paramList = new HashMap<>();
        ErrorDTO errorDTOExpected = new ErrorDTO();
        errorDTOExpected.setCode((long) 900300);
        errorDTOExpected.setMessage("Lifecycle exception occurred");
        errorDTOExpected.setDescription("Error occurred while changing lifecycle state");

        ErrorHandler errorHandler = Mockito.mock(ErrorHandler.class);
        when(errorHandler.getErrorCode()).thenReturn((long) 900300);
        when(errorHandler.getErrorMessage()).thenReturn("Lifecycle exception occurred");
        when(errorHandler.getErrorDescription()).thenReturn("Error occurred while changing lifecycle state");

        ErrorDTO errorDTO1 = RestApiUtil.getErrorDTO(errorHandler, paramList);
        Assert.assertEquals(errorDTO1.getCode(), errorDTOExpected.getCode());
        Assert.assertEquals(errorDTO1.getMessage(), errorDTOExpected.getMessage());
        Assert.assertEquals(errorDTO1.getMoreInfo(), new HashMap<String, String>());
     }

    @Test(description = "Test get Error DTO as String")
    public void testGetErrorDTO2() throws Exception {

        ErrorHandler errorHandler = Mockito.mock(ErrorHandler.class);
        Map<String, String> paramList = new HashMap<>();
        APIManagementException ex = Mockito.mock(APIManagementException.class);

        paramList.put("param1", "test1");
        paramList.put("param2", "test2");
        paramList.put("param3", "test3");
        when(errorHandler.getErrorDescription()).thenReturn("Test Error Description");
        when(ex.getMessage()).thenReturn("Error Message");
        when(errorHandler.getErrorCode()).thenReturn((long) 900300);

        final String expectedErrorDTOString1 = "class ErrorDTO {\n" +
                "  code: 900300\n" +
                "  message: Error Message\n" +
                "  description: Test Error Description\n" +
                "  moreInfo: {param3=test3, param1=test1, param2=test2}\n" +
                "  error: []\n" +
                "}\n";

        final String expectedErrorDTOString2 = "class ErrorDTO {\n" +
                "  code: 900300\n" +
                "  message: null\n" +
                "  description: Test Error Description\n" +
                "  moreInfo: {param3=test3, param1=test1, param2=test2}\n" +
                "  error: []\n" +
                "}\n";

        ErrorDTO errorDTO1 = RestApiUtil.getErrorDTO(errorHandler, (HashMap<String, String>) paramList, ex);
        Assert.assertEquals(errorDTO1.toString(), expectedErrorDTOString1);

        when(ex.getMessage()).thenReturn(null);
        ErrorDTO errorDTO2 = RestApiUtil.getErrorDTO(errorHandler, (HashMap<String, String>) paramList, ex);
        Assert.assertEquals(errorDTO2.toString(), expectedErrorDTOString2);
    }

    @Test(description = "Test Get Paginated parameters")
    public void testGetPaginationParams() throws Exception {

        Map<String, Integer> expectedPaginatedParams = new HashMap<>();
        expectedPaginatedParams.put("previous_limit", 10);
        expectedPaginatedParams.put("next_limit", 10);
        expectedPaginatedParams.put("previous_offset", 0);
        expectedPaginatedParams.put("next_offset", 15);

        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(5, 10, 20);
        boolean actualMatch = false;

        if (expectedPaginatedParams.entrySet().containsAll(paginatedParams.entrySet())) {
            actualMatch = true;
        }
        Assert.assertEquals(actualMatch, true);
      }

    @Test(description = "Test Application paginated URL")
    public void testGetApplicationPaginatedURL() throws Exception {
        String expectedPaginatedUrl = "/applications?limit=10&offset=5&groupId={groupId}";
        String paginatedUrl =  RestApiUtil.getApplicationPaginatedURL(5, 10);
        Assert.assertEquals(paginatedUrl, expectedPaginatedUrl);
    }
    @Test(description = "Test get gateway config URL")
    public void testGetGatewayConfigGetURL() throws Exception {

        String uuid = "7a94efa3-7626-4add-b5a5-0d93a8640c0e";
        String pathExpected = RestApiConstants.GATEWAY_CONFIG_GET_URL + "/" + uuid + "/gateway-config";
        String path = RestApiUtil.getGatewayConfigGetURL(uuid);
        Assert.assertEquals(path, pathExpected);
    }
    @Test(description = "Test get gateway config URL")
    public void testGetGetSwaggerGetURL() throws Exception {

        String uuid = "7a94efa3-7626-4add-b5a5-0d93a8640c0e";
        String pathExpected = RestApiConstants.SWAGGER_GET_URL + "/" + uuid + "/swagger";
        String path = RestApiUtil.getSwaggerGetURL(uuid);
        Assert.assertEquals(path, pathExpected);
    }

    @Test
    public void testIsUrl() throws Exception {

        String url = "/api/am/publisher/v1/apis/7a94efa3-7626-4add-b5a5-0d93a8640c0e/swagger";
        Assert.assertTrue(RestApiUtil.isURL(url));
    }

    @Test(description = "Test Find Policy given tier name")
    public void testFindPolicy() throws Exception {

        Policy mockedPolicy = Mockito.mock(Policy.class);
        when(mockedPolicy.getPolicyName()).thenReturn("silver");

        List<Policy> policyList = new ArrayList<>();

        Policy silverPolicy = Mockito.mock(Policy.class);
        silverPolicy.setPolicyName("silver");
        when(silverPolicy.getPolicyName()).thenReturn("silver");
        policyList.add(silverPolicy);

        Policy policyFound1 = RestApiUtil.findPolicy(policyList, "silver");
        Assert.assertEquals(policyFound1, silverPolicy);

        Policy policyFound2 = RestApiUtil.findPolicy(policyList, null);
        Assert.assertEquals(policyFound2, null);

        Policy policyFound3 = RestApiUtil.findPolicy(policyList, "testTier");
        Assert.assertEquals(policyFound3, null);
    }

    @Test(description = "Test get Publisher REST API Resource")
    public void testGetPublisherRestAPIResource() throws Exception {

        PowerMockito.mockStatic(IOUtils.class);
        try {
            RestApiUtil.getPublisherRestAPIResource();

        } catch (APIMgtSecurityException ex) {
            Assert.assertEquals(ex.getMessage(), "Error reading swagger definition of Publisher REST API");
        }
    }

    @Test(description = "Test get Store REST API Resource")
    public void testGetStoreRestAPIResource() throws Exception {

        PowerMockito.mockStatic(IOUtils.class);
        try {
            RestApiUtil.getStoreRestAPIResource();

         } catch (APIMgtSecurityException ex) {
            Assert.assertEquals(ex.getMessage(), "Error reading swagger definition of Store REST API");
        }
    }

    @Test(description = "Test get Admin REST API Resource")
    public void testGetAdminRestAPIResource() throws Exception {

        PowerMockito.mockStatic(IOUtils.class);
        try {
            RestApiUtil.getAdminRestAPIResource();


        } catch (APIMgtSecurityException ex) {
            Assert.assertEquals(ex.getMessage(), "Error while reading the swagger definition of Admin Rest API");
        }
    }

    @Test(description = "Test convert Yaml to JSON")
    public void testConvertYmlToJson() throws Exception {
        final String testYaml =
                "list:\n" +
                "    item 1\n" +
                "    item 2\n" +
                "items:\n" +
                "    - name: item1\n" +
                "      price: 10\n" +
                "\n" +
                "    - name: item2\n" +
                "      price: 20";
        final String expectedJson =
                "{\"list\":\"item 1 item 2\",\"items\":[{\"name\":\"item1\",\"price\":10},{\"name\":\"item2\",\"" +
                        "price\":20}]}";
        String actualJson = RestApiUtil.convertYmlToJson(testYaml);
        Assert.assertEquals(actualJson, expectedJson);
    }

    @Test(description = "Test get Context")
    public void testGetContext() throws Exception {
        APIMConfigurations apimConfigurations = new APIMConfigurations();

        String actualPubContext = RestApiUtil.getContext("publisher");
        String expectedPubContext = apimConfigurations.getPublisherContext();
        Assert.assertEquals(actualPubContext, expectedPubContext);

        String actualStoreContext = RestApiUtil.getContext("store");
        String expectedStoreStoreContext = apimConfigurations.getStoreContext();
        Assert.assertEquals(actualStoreContext, expectedStoreStoreContext);

        String actualAdminContext = RestApiUtil.getContext("admin");
        String expectedAdminContext = apimConfigurations.getAdminContext();
        Assert.assertEquals(actualAdminContext, expectedAdminContext);

        String actualContext = RestApiUtil.getContext("test");
        Assert.assertEquals(actualContext, null);
    }

    @Test(description = "Test Map REST API Policy Level to Policy Level Enum")
    public void testMapRestApiPolicyLevelToPolicyLevelEnum() throws Exception {

        String[] levels = {"api", "application", "subscription", "custom"};
        try {
            APIMgtAdminService.PolicyLevel actualValue1 = RestApiUtil.mapRestApiPolicyLevelToPolicyLevelEnum("api");
            Assert.assertEquals(actualValue1, APIMgtAdminService.PolicyLevel.api);

        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Policy Level " + levels[0] + " not supported");
        }
        try {
            APIMgtAdminService.PolicyLevel actualValue2 =
                    RestApiUtil.mapRestApiPolicyLevelToPolicyLevelEnum("application");
            Assert.assertEquals(actualValue2, APIMgtAdminService.PolicyLevel.application);

        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Policy Level " + levels[1] + " not supported");
        }
        try {
            APIMgtAdminService.PolicyLevel actualValue3 =
                    RestApiUtil.mapRestApiPolicyLevelToPolicyLevelEnum("subscription");
            Assert.assertEquals(actualValue3, APIMgtAdminService.PolicyLevel.subscription);

        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Policy Level " + levels[2] + " not supported");
        }

        try {
            APIMgtAdminService.PolicyLevel actualValue4 = RestApiUtil.mapRestApiPolicyLevelToPolicyLevelEnum("custom");
            Assert.assertEquals(actualValue4, APIMgtAdminService.PolicyLevel.custom);

        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Policy Level " + levels[3] + " not supported");
        }
    }

}
