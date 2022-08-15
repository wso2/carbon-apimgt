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
        policyData = Mockito.mock(OperationPolicyData.class);
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
                        + "[\"HTTP\"]," + "\"policyAttributes\":[]}";

        Mockito.when(CommonUtil.checkFileExistence(yamlFile)).thenReturn(false);
        Mockito.when(CommonUtil.checkFileExistence(jsonFile)).thenReturn(true);
        Mockito.when(FileUtils.readFileToString(new File(jsonFile))).thenReturn(policyDefContent);
        Mockito.when(CommonUtil.checkFileExistence(synapsePath)).thenReturn(true);
        Mockito.when(FileUtils.readFileToString(new File(synapsePath))).thenReturn(synapseDefFileString);

        String policyId = RandomStringUtils.randomAlphanumeric(10);

        Mockito.when(apiProvider.getCommonOperationPolicyByPolicyName(POLICYNAME, POLICYVERSION, ORGANIZATION, false))
                .thenReturn(null);
        Mockito.when(apiProvider.addCommonOperationPolicy(ArgumentMatchers.any(OperationPolicyData.class),
                ArgumentMatchers.eq(ORGANIZATION))).thenReturn(policyId);

        try {
            OperationPolicyDataDTO operationPolicyDataDTO = ImportUtils.importPolicy(pathToArchive, ORGANIZATION,
                    apiProvider);
            Assert.assertNotNull(operationPolicyDataDTO);
        } catch (APIManagementException ex) {
            Assert.fail("Import Policy failed due to an exception!");
        }

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
}
