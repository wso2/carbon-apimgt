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

package org.wso2.carbon.apimgt.core.impl;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ApiStoreSdkGenerationException;
import org.wso2.carbon.apimgt.core.models.API;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest(APIManagerFactory.class)
public class ApiStoreSdkGenerationManagerTestCase {
    private static Logger log = LoggerFactory.getLogger(ApiStoreSdkGenerationManagerTestCase.class);

    private static final String USER = "admin";
    private static final String LANGUAGE = "java";
    private static final int MIN_SDK_SIZE = 0;
    private static String swaggerPetStore;
    private static InputStream inputStream;

    static {
        try {
            inputStream = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("swaggerPetStoreCorrect.json");
            swaggerPetStore = IOUtils.toString(inputStream);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGenerateSdkForApi() throws APIManagementException, ApiStoreSdkGenerationException {
        String apiId = UUID.randomUUID().toString();

        APIManagerFactory instance = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(instance);
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        Mockito.when(instance.getAPIConsumer(USER)).thenReturn(apiStore);

        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiStore.getAPIbyUUID(apiId)).thenReturn(api);
        Mockito.when(apiStore.getApiSwaggerDefinition(apiId)).thenReturn(swaggerPetStore);

        ApiStoreSdkGenerationManager sdkGenerationManager = new ApiStoreSdkGenerationManager();
        String pathToZip = sdkGenerationManager.generateSdkForApi(apiId, LANGUAGE, USER);

        File sdkZipFile = new File(pathToZip);
        Assert.assertTrue(sdkZipFile.exists() && sdkZipFile.length() > MIN_SDK_SIZE);
    }

    @Test
    public void testGetSdkGenLanguages() {
        ApiStoreSdkGenerationManager sdkGenerationManager = new ApiStoreSdkGenerationManager();
        Map<String, String> map = new HashMap<String, String>() {
            {
                put("python", "io.swagger.codegen.languages.PythonClientCodegen");
                put("java", "io.swagger.codegen.languages.JavaClientCodegen");
                put("android", "io.swagger.codegen.languages.AndroidClientCodegen");
            }
        };
        Assert.assertEquals(map, sdkGenerationManager.getSdkGenLanguages());
    }

    @Test(expected = APIManagementException.class)
    public void testGenerateSdkForApiBlankApiId() throws APIManagementException, ApiStoreSdkGenerationException {

        ApiStoreSdkGenerationManager sdkGenerationManager = new ApiStoreSdkGenerationManager();
        sdkGenerationManager.generateSdkForApi("", LANGUAGE, USER);
    }

    @Test(expected = APIManagementException.class)
    public void testGenerateSdkForApiBlankLanguage() throws APIManagementException, ApiStoreSdkGenerationException {

        String apiId = UUID.randomUUID().toString();
        ApiStoreSdkGenerationManager sdkGenerationManager = new ApiStoreSdkGenerationManager();
        sdkGenerationManager.generateSdkForApi(apiId, "", USER);
    }

    @Test(expected = APIManagementException.class)
    public void testGenerateSdkForApiNullApi() throws APIManagementException, ApiStoreSdkGenerationException {
        String apiId = UUID.randomUUID().toString();

        APIManagerFactory instance = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(instance);
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        Mockito.when(instance.getAPIConsumer(USER)).thenReturn(apiStore);

        Mockito.when(apiStore.getAPIbyUUID(apiId)).thenReturn(null);

        ApiStoreSdkGenerationManager sdkGenerationManager = new ApiStoreSdkGenerationManager();
        sdkGenerationManager.generateSdkForApi(apiId, LANGUAGE, USER);

    }

    @Test(expected = NullPointerException.class)
    public void testGenerateSdkForApiIncorrectSwagger() throws APIManagementException, ApiStoreSdkGenerationException {
        String apiId = UUID.randomUUID().toString();

        APIManagerFactory instance = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(instance);
        APIStore apiStore = Mockito.mock(APIStoreImpl.class);

        Mockito.when(instance.getAPIConsumer(USER)).thenReturn(apiStore);

        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiStore.getAPIbyUUID(apiId)).thenReturn(api);
        Mockito.when(apiStore.getApiSwaggerDefinition(apiId)).thenReturn(null);

        ApiStoreSdkGenerationManager sdkGenerationManager = new ApiStoreSdkGenerationManager();
        sdkGenerationManager.generateSdkForApi(apiId, LANGUAGE, USER);

    }


}
