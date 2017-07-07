/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.core.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.Files;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.CustomPolicy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;

/**
 * Test cases for PolicyExportManager.
 */
public class PolicyExportManagerTest {

    private static String exportRootDirectory = System.getProperty("java.io.tmpdir") + File.separator + "export-policies";

    @BeforeClass
    void init() {
        File dir = new File(exportRootDirectory);
        dir.mkdir();
        dir.deleteOnExit();
        File temp = Files.createTempDir();
        temp.deleteOnExit();
        System.setProperty("gwHome", temp.getAbsolutePath());
        //Set the resource path, where contain composer test JS
        System.setProperty("carbon.home", new File("src/test/").getAbsolutePath());
    }

    @Test(description = "testing creating archive from execution plans")
    public void testCreateArchiveFromExecutionPlans() throws APIManagementException {

        APIMgtAdminService apiMgtAdminService = Mockito.mock(APIMgtAdminService.class);
        List<APIPolicy> apiPolicies = new ArrayList<>();
        List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
        List<SubscriptionPolicy> subscriptionPolicies = new ArrayList<>();
        List<CustomPolicy> customPolicies = new ArrayList<>();

        apiPolicies.add(SampleTestObjectCreator.createDefaultAPIPolicy());
        applicationPolicies.add(SampleTestObjectCreator.createDefaultApplicationPolicy());
        subscriptionPolicies.add(SampleTestObjectCreator.createDefaultSubscriptionPolicy());
        customPolicies.add(SampleTestObjectCreator.createDefaultCustomPolicy());
        Mockito.when(apiMgtAdminService.getApiPolicies()).thenReturn(apiPolicies);
        Mockito.when(apiMgtAdminService.getApplicationPolicies()).thenReturn(applicationPolicies);
        Mockito.when(apiMgtAdminService.getCustomRules()).thenReturn(customPolicies);
        Mockito.when(apiMgtAdminService.getSubscriptionPolicies()).thenReturn(subscriptionPolicies);

        PolicyExportManager policyExportManager = new PolicyExportManager(apiMgtAdminService);
        String path = policyExportManager
                .createArchiveFromExecutionPlans("exportDir", exportRootDirectory, "policies");
        String unzippedPath = exportRootDirectory + File.separator + "export-policies";
        APIFileUtils.extractArchive(path, unzippedPath);
        Assert.assertEquals(
                new File(APIFileUtils.getDirectoryList(unzippedPath).iterator().next()).listFiles().length == 6, true,
                "Exported policy count is not equal to ");
    }
}
