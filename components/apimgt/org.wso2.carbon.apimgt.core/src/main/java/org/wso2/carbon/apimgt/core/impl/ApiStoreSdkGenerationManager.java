/***********************************************************************************************************************
 * *
 * *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * *
 * *   WSO2 Inc. licenses this file to you under the Apache License,
 * *   Version 2.0 (the "License"); you may not use this file except
 * *   in compliance with the License.
 * *   You may obtain a copy of the License at
 * *
 * *     http://www.apache.org/licenses/LICENSE-2.0
 * *
 * *  Unless required by applicable law or agreed to in writing,
 * *  software distributed under the License is distributed on an
 * *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * *  KIND, either express or implied.  See the License for the
 * *  specific language governing permissions and limitations
 * *  under the License.
 * *
 */
package org.wso2.carbon.apimgt.core.impl;

import io.swagger.codegen.ClientOptInput;
import io.swagger.codegen.DefaultGenerator;
import io.swagger.codegen.config.CodegenConfigurator;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.configuration.APIMConfigurationService;
import org.wso2.carbon.apimgt.core.configuration.models.SdkLanguageConfigurations;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ApiStoreSdkGenerationException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to generate SDK's for a given API
 */
public class ApiStoreSdkGenerationManager {

    private static final Logger log = LoggerFactory.getLogger(ApiStoreSdkGenerationManager.class);
    private Map<String, String> sdkGenLanguages = new HashMap<>();

    public ApiStoreSdkGenerationManager() {
        /*Populate the sdkGenLanguages Map with the supported language configurations from the deployment.yaml
         or the default configurations.*/
        SdkLanguageConfigurations sdkLanguageConfigurations = APIMConfigurationService
                .getInstance()
                .getApimConfigurations()
                .getSdkLanguageConfigurations();
        sdkGenLanguages = sdkLanguageConfigurations.getSdkGenLanguages();
    }

    /*
    * This method generates the client side SDK for the API with API ID (apiID) and SDK language (language)
    *
    * @param apiId ID for the specific API
    * @param language preferred language to generate the SDK
    * @throws ApiStoreSdkGenerationException if failed to generate the SDK
    * */
    public String generateSdkForApi(String apiId, String language, String userName)
            throws ApiStoreSdkGenerationException
            , APIManagementException, RuntimeException {
        if (StringUtils.isBlank(apiId) || StringUtils.isBlank(language)) {
            handleSdkGenException("API ID or SDK Language should not be null!");
        }

        APIStore apiStore = APIManagerFactory.getInstance().getAPIConsumer(userName);
        API api = apiStore.getAPIbyUUID(apiId);
        if (api == null) {
            String errorMessage = "Cannot find API for specified API ID";
            throw new APIManagementException(errorMessage, ExceptionCodes.API_NOT_FOUND);
        }
        String apiName = api.getName();
        String apiVersion = api.getVersion();
        String swaggerDefinitionForApi = null;
        try {
            swaggerDefinitionForApi = apiStore.getApiSwaggerDefinition(apiId);
        } catch (APIManagementException e) {
            handleSdkGenException("Error retrieving swagger definition for API " + apiId + " from database.", e);
        }
        Swagger swaggerDoc = new SwaggerParser().parse(swaggerDefinitionForApi);

        //Format the swagger definition as a string before writing to the file.
        String formattedSwaggerDefinitionForSdk = Json.pretty(swaggerDoc);
        Path tempSdkGenDir = null;
        File swaggerDefJsonFile = null;
        try {
            //Create a temporary directory to store the API files
            tempSdkGenDir = Files.createTempDirectory(apiName + "_" + language + "_" + apiVersion);

            //Create a temporary file to store the swagger definition
            swaggerDefJsonFile = Files.createTempFile(tempSdkGenDir,
                    apiId + "_" + language,
                    APIMgtConstants.APIFileUtilConstants.JSON_EXTENSION).toFile();
        } catch (IOException e) {
            handleSdkGenException("Error creating temporary directory or json file for swagger definition!", e);
        }


        //FileOutputStream swaggerFileOutputStream=null;
        //OutputStreamWriter swaggerOutputStreamWriter=null;
        String tempZipFilePath = "";
        if (swaggerDefJsonFile.exists()) {

            try (Writer swaggerFileWriter = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(swaggerDefJsonFile.getAbsoluteFile()), "UTF-8"))) {

                //swaggerFileWriter.write(formattedSwaggerDefinitionForSdk);
                swaggerFileWriter.write(formattedSwaggerDefinitionForSdk);
            } catch (IOException e) {
                handleSdkGenException("Error writing swagger definition to file in " +
                        tempSdkGenDir, e);
            }

            //Generate the SDK for the specified language and save it as a zip file
            generateSdkForSwaggerDef(language, swaggerDefJsonFile.getAbsolutePath(), tempSdkGenDir.toString());

            String archiveName = apiName + "_" + language + "_" + apiVersion;
            tempZipFilePath = tempSdkGenDir + "/" + archiveName + ".zip";
            APIFileUtils.archiveDirectory(tempSdkGenDir.toString(),
                    tempSdkGenDir.toString(),
                    archiveName);
        } else {
            handleSdkGenException("Swagger definition file not found!");
        }


        return tempZipFilePath;

    }

    /*
    * This method generates the SDK for the specified swagger definition file
    *
    * @param language Preferred SDK language
    * @param swaggerDefLocation Location of the swagger definition file
    * @param tempOutputDirectory Output directory for the generated SDK files
    * */
    private void generateSdkForSwaggerDef(String language, String swaggerDefLocation, String tempOutputDirectory)
            throws RuntimeException {
        CodegenConfigurator codegenConfigurator = new CodegenConfigurator();
        codegenConfigurator.setInputSpec(swaggerDefLocation);
        codegenConfigurator.setOutputDir(tempOutputDirectory);
        codegenConfigurator.setLang(sdkGenLanguages.get(language));
        final ClientOptInput clientOptInput = codegenConfigurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();
    }

    /**
     * This method is to handle exceptions occurred when generating the SDK
     *
     * @param errorMessage error message to be printed in the log
     * @throws ApiStoreSdkGenerationException
     */
    private void handleSdkGenException(String errorMessage) throws ApiStoreSdkGenerationException {
        log.error(errorMessage);
        throw new ApiStoreSdkGenerationException(errorMessage);
    }

    /**
     * This method is to handle exceptions occurred when generating the SDK (with a throwable exception)
     *
     * @param errorMessage error message to be printed in the log
     * @param throwable    throwable exception caught
     * @throws ApiStoreSdkGenerationException
     */
    private void handleSdkGenException(String errorMessage, Throwable throwable) throws ApiStoreSdkGenerationException {
        log.error(errorMessage, throwable);
        throw new ApiStoreSdkGenerationException(errorMessage, throwable);
    }


    /*public static ApiStoreSdkGenerationManager getSdkGenerationManager(){
        return new ApiStoreSdkGenerationManager();
    }*/


}
