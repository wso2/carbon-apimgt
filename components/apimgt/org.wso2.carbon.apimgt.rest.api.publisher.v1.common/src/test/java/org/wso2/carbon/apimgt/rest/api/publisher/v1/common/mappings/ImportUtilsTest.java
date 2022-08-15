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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecification;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataDTO;
import java.io.File;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ APIProvider.class, CommonUtil.class, FileUtils.class, APIUtil.class })
public class ImportUtilsTest {
    private static final String ORGANIZATION = "carbon.super";
    private APIProvider apiProvider;

    private final String pathToArchive = "/tmp/test/customCommonLogPolicy";
    private final String yamlFile = pathToArchive + "/customCommonLogPolicy.yaml";
    private final String jsonFile = pathToArchive + "/customCommonLogPolicy.json";
    private final String synapsePath = pathToArchive + "/customCommonLogPolicy.j2";
    private final String synapseDefFileString = "<log level=\"full\">\n"
            + "    <property name=\"MESSAGE\" value=\"MESSAGE\"/>\n" + "</log>";

    private static final String POLICYNAME = "customCommonLogPolicy";

    private static final String POLICYVERSION = "v1";

    private static OperationPolicyData policyData;

    @Before
    public void init() throws Exception {
        PowerMockito.mockStatic(CommonUtil.class);
        PowerMockito.mockStatic(FileUtils.class);
        apiProvider = Mockito.mock(APIProvider.class);
        // PowerMockito.mockStatic(APIUtil.class);

//        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getMd5OfOperationPolicyDefinition"));
//        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getMd5OfOperationPolicy"));

        // PowerMockito.mockStatic(RandomStringUtils.class);
    }

    @Test public void testImportAPIPolicy() throws Exception {

        String policyDefContent = "{\"type\":\"operation_policy_specification\",\"version\":\"v4.1.0\",\"data\":"
                + "{\"category\":\"Mediation\",\"name\":\"customCommonLogPolicy\",\"version\":\"v1\",\"displayName\""
                + ":\"CustomCommonLogPolicy\",\"description\":\"Usingthispolicy,youcanaddacustomlogmessage\""
                + ",\"applicableFlows\":[\"request\",\"response\",\"fault\"],\"supportedGateways\":[\"Synapse\"]"
                + ",\"supportedApiTypes\":[\"HTTP\"],\"policyAttributes\":[]}}";

        String policyDefContentModified =
                "{\"type\":\"operation_policy_specification\",\"version\":\"v1\",\"category\"" + ":"
                        + "\"Mediation\",\"name\":\"customCommonLogPolicy\",\"displayName\":\"CustomCommonLogPolicy\","
                        + "\"description\":\"Usingthispolicy,youcanaddacustomlogmessage\",\"applicableFlows\":"
                        + "[\"request\","
                        + "\"response\",\"fault\"],\"supportedGateways\":[\"Synapse\"],\"supportedApiTypes\":"
                        + "[\"HTTP\"],"
                        + "\"policyAttributes\":[]}";

        Mockito.when(CommonUtil.checkFileExistence(yamlFile)).thenReturn(false);
        Mockito.when(CommonUtil.checkFileExistence(jsonFile)).thenReturn(true);
        Mockito.when(FileUtils.readFileToString(new File(jsonFile))).thenReturn(policyDefContent);
        Mockito.when(CommonUtil.checkFileExistence(synapsePath)).thenReturn(true);
        Mockito.when(FileUtils.readFileToString(new File(synapsePath))).thenReturn(synapseDefFileString);

        String policyId = RandomStringUtils.randomAlphanumeric(10);
        String synapseMd5 = RandomStringUtils.randomAlphanumeric(30);
        String policyMD5 = RandomStringUtils.randomAlphanumeric(30);
        policyData = Mockito.mock(OperationPolicyData.class);

        OperationPolicySpecification operationPolicySpecification = APIUtil.getValidatedOperationPolicySpecification(
                policyDefContentModified);

        OperationPolicyData operationPolicyData = new OperationPolicyData();
        operationPolicyData.setOrganization(ORGANIZATION);
        operationPolicyData.setSpecification(operationPolicySpecification);

        OperationPolicyDefinition gatewayDefinition = new OperationPolicyDefinition();
        gatewayDefinition.setGatewayType(OperationPolicyDefinition.GatewayType.Synapse);
        gatewayDefinition.setContent(synapseDefFileString);

        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getMd5OfOperationPolicyDefinition",
                OperationPolicyDefinition.class)).toReturn(synapseMd5);

        // Mockito.when(APIUtil.getMd5OfOperationPolicyDefinition(gatewayDefinition)).thenReturn(synapseMd5);

        gatewayDefinition.setMd5Hash(synapseMd5);
        operationPolicyData.setSynapsePolicyDefinition(gatewayDefinition);

        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getMd5OfOperationPolicy", OperationPolicyData.class))
                .toReturn(policyMD5);

        // Mockito.when(APIUtil.getMd5OfOperationPolicy(operationPolicyData)).thenReturn(policyMD5);
        operationPolicyData.setMd5Hash(policyMD5);

        Mockito.when(apiProvider.getCommonOperationPolicyByPolicyName(POLICYNAME, POLICYVERSION, ORGANIZATION, false))
                .thenReturn(null);
        Mockito.when(apiProvider.addCommonOperationPolicy(policyData, ORGANIZATION)).thenReturn(policyId);

        try {
            OperationPolicyDataDTO operationPolicyDataDTO = ImportUtils.importPolicy(pathToArchive, ORGANIZATION,
                    apiProvider);
            Assert.assertNotNull(operationPolicyDataDTO);
        } catch (APIManagementException ex) {
            Assert.fail("Import Policy failed due to an exception!");
        }

        Mockito.when(apiProvider.getCommonOperationPolicyByPolicyName(POLICYNAME, POLICYVERSION, ORGANIZATION, false))
                .thenReturn(policyData);

        String unauthenticatedResponse = "{\"code\":401,\"message\":\"\",\"description\":\"Unauthenticated request\","
                + "\"moreInfo\":\"\",\"error\":[]}";
        String errorMsg =
                "Import API service call received unsuccessful response: " + unauthenticatedResponse + " status: "
                        + HttpStatus.SC_UNAUTHORIZED;

        try {
            ImportUtils.importPolicy(pathToArchive, ORGANIZATION, apiProvider);
            Assert.fail("Cannot create an existing API Policy!");
        } catch (APIManagementException ex) {
            Assert.assertEquals(errorMsg, ex.getMessage());
        }
    }
}
