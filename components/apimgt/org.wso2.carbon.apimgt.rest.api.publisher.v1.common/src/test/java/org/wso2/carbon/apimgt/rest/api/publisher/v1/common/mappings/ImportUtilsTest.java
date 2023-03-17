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

import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataDTO;

import java.io.File;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ImportUtils.class, APIConstants.class, APIProvider.class, CommonUtil.class,
        FileUtils.class, APIUtil.class })
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
                PowerMockito.method(APIUtil.class, "getMd5OfOperationPolicy", OperationPolicyData.class)).
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
}
