/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl;

import io.swagger.codegen.ClientOptInput;
import io.swagger.codegen.DefaultGenerator;
import io.swagger.codegen.config.CodegenConfigurator;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/*
 * This class is used to generate SDKs for subscribed APIs for a given application.
 */

public class APIClientGenerationManager {

    private static final Log log = LogFactory.getLog(APIClientGenerationManager.class);
    private static final Map<String, String> langCodeGen = new HashMap<String, String>();

    public APIClientGenerationManager() {
        langCodeGen.put("java", "io.swagger.codegen.languages.JavaClientCodegen");
        langCodeGen.put("android", "io.swagger.codegen.languages.JavaClientCodegen");
        langCodeGen.put("csharp", "io.swagger.codegen.languages.CSharpClientCodegen");
        langCodeGen.put("cpp", "io.swagger.codegen.languages.CppRestClientCodegen");
        langCodeGen.put("dart", "io.swagger.codegen.languages.DartClientCodegen");
        langCodeGen.put("flash", "io.swagger.codegen.languages.FlashClientCodegen");
        langCodeGen.put("go", "io.swagger.codegen.languages.GoClientCodegen");
        langCodeGen.put("groovy", "io.swagger.codegen.languages.GroovyClientCodegen");
        langCodeGen.put("javascript", "io.swagger.codegen.languages.JavascriptClientCodegen");
        langCodeGen.put("jmeter", "io.swagger.codegen.languages.JMeterCodegen");
        langCodeGen.put("nodejs", "io.swagger.codegen.languages.NodeJSServerCodegen");
        langCodeGen.put("perl", "io.swagger.codegen.languages.PerlClientCodegen");
        langCodeGen.put("php", "io.swagger.codegen.languages.PhpClientCodegen");
        langCodeGen.put("python", "io.swagger.codegen.languages.PythonClientCodegen");
        langCodeGen.put("ruby", "io.swagger.codegen.languages.RubyClientCodegen");
        langCodeGen.put("scala", "io.swagger.codegen.languages.ScalaClientCodegen");
        langCodeGen.put("swift", "io.swagger.codegen.languages.SwiftCodegen");
        langCodeGen.put("clojure", "io.swagger.codegen.languages.ClojureClientCodegen");
        langCodeGen.put("aspNet5", "io.swagger.codegen.languages.AspNet5ServerCodegen");
        langCodeGen.put("asyncScala", "io.swagger.codegen.languages.AsyncScalaClientCodegen");
        langCodeGen.put("spring", "io.swagger.codegen.languages.SpringCodegen");
        langCodeGen.put("csharpDotNet2", "io.swagger.codegen.languages.CsharpDotNet2ClientCodegen");
        langCodeGen.put("haskell", "io.swagger.codegen.languages.HaskellServantCodegen");
    }

    /**
     * This method generates client side SDK for a given application
     *
     * @param appName     name of the application
     * @param sdkLanguage preferred language to generate the SDK
     * @param userName    username of the logged in user
     * @param groupId     group ID of the logged in user
     * @return a map containing the zip file name and its' temporary location until it is downloaded
     * @throws APIClientGenerationException if failed to generate the SDK
     */
    public Map<String, String> generateSDK(String appName, String sdkLanguage, String userName, String groupId)
            throws APIClientGenerationException {

        if (appName == null || appName.isEmpty()) {
            log.error("Application name should not be null or empty.");
            throw new APIClientGenerationException("Application name should not be null or empty.");
        }
        Set<SubscribedAPI> subscribedAPIs;
        APIConsumerImpl consumerImplInstance;
        try {
            consumerImplInstance = (APIConsumerImpl) APIManagerFactory.getInstance().getAPIConsumer(userName);
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            Subscriber currentSubscriber = apiMgtDAO.getSubscriber(userName);
            if (currentSubscriber == null) {
                log.error("No subscription exists for application : " + appName);
                throw new APIClientGenerationException("No subscription exists for application : " + appName);
            }
            subscribedAPIs = apiMgtDAO.getSubscribedAPIs(currentSubscriber, appName, groupId);
        } catch (APIManagementException e) {
            log.error("Error while getting the subscribed API set for application : " + appName, e);
            throw new APIClientGenerationException("Error while getting the subscribed API set for application : " +
                    appName, e);
        }

        if (subscribedAPIs.isEmpty()) {
            log.info("No subscription exists for application : " + appName);
            return null;
        }

        File tempFolder = new File(APIConstants.TEMP_DIRECTORY_NAME + File.separator + appName);
        // On Windows OS, deleting the folder fails stating that a file within it is still open, attempting to close
        // the open file from the jaggery side has not been successful. For the time being we will avoid deleting
        // the directory. This is not an issue since existing zip files will be overwritten on the server side.
        // This issue is not encountered on Linux however. Hence we are not going to delete the directory using
        // FileUtils.deleteDirectory(tempFolder). Reference :  APIMANAGER-4981
        boolean isTempFolderCreated = tempFolder.mkdir();
        if (!isTempFolderCreated) {
            log.error("Error while creating the temporary folder for SDK generation for application : " + appName);
            throw new APIClientGenerationException
                    ("Error while creating the temporary folder for SDK generation for application : " + appName);
        }
        File swaggerSpecFile = null;
        String specLocation = APIConstants.TEMP_DIRECTORY_NAME + File.separator + appName + File.separator +
                UUID.randomUUID().toString() + APIConstants.JSON_FILE_EXTENSION;

        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
            String subscribedAPIName = subscribedAPI.getApiId().getApiName();
            String subscribedAPIVersion = subscribedAPI.getApiId().getVersion();
            String subscribedAPIProvider = subscribedAPI.getApiId().getProviderName();
            boolean isResourceExists;
            String registryResourcePath = APIUtil.getSwagger20DefinitionFilePath(subscribedAPIName,
                    subscribedAPIVersion, subscribedAPIProvider);
            String swaggerResourceAbsolutePath = registryResourcePath + APIConstants.API_DOC_2_0_RESOURCE_NAME;

            try {
                isResourceExists = consumerImplInstance.registry.resourceExists(swaggerResourceAbsolutePath);
            } catch (RegistryException e) {
                log.error("Error while checking whether the resource exists or not.", e);
                throw new APIClientGenerationException
                        ("Error while checking whether the resource exists or not.", e);
            }

            String swaggerAPIDefinition;
            Swagger swaggerDoc;
            if (isResourceExists) {
                try {
                    swaggerAPIDefinition = consumerImplInstance.getSwagger20Definition(subscribedAPI.getApiId());
                } catch (APIManagementException e) {
                    log.error("Error loading swagger file from registry.", e);
                    throw new APIClientGenerationException("Error loading swagger file from registry.", e);
                }
                swaggerDoc = new SwaggerParser().parse(swaggerAPIDefinition);
            } else {
                log.error("Resource does not exists in : " + swaggerResourceAbsolutePath);
                throw new APIClientGenerationException("Resource does not exists in : " +
                        swaggerResourceAbsolutePath);
            }
            //format the swagger definition before writing to the file
            String formattedSwaggerAPIDefinition = Json.pretty(swaggerDoc);
            swaggerSpecFile = new File(specLocation);
            FileWriter fileWriter = null;
            BufferedWriter bufferedWriter = null;
            try {
                if (!swaggerSpecFile.exists()) {
                    boolean isSpecFileCreated = swaggerSpecFile.createNewFile();
                    if (!isSpecFileCreated) {
                        log.error("Unable to create the swagger spec file for API : " + subscribedAPIName +
                                " in " + specLocation);
                        throw new APIClientGenerationException("Unable to create the swagger spec file for API : " +
                                subscribedAPIName + " in " + specLocation);
                    }
                }
                fileWriter = new FileWriter(swaggerSpecFile.getAbsoluteFile());
                bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(formattedSwaggerAPIDefinition);
            } catch (IOException e) {
                log.error("Error while storing the temporary swagger file.", e);
                throw new APIClientGenerationException("Error while storing the temporary swagger file.", e);
            } finally {
                IOUtils.closeQuietly(bufferedWriter);
                IOUtils.closeQuietly(fileWriter);
            }
            String outputDirectoryName = appName + "_" + subscribedAPIName + "_" + subscribedAPIVersion + "_" +
                    sdkLanguage;
            String temporaryOutputPath = APIConstants.TEMP_DIRECTORY_NAME + File.separator + appName + File.separator +
                    outputDirectoryName;
            generateClient(appName, subscribedAPIName, subscribedAPIVersion, specLocation, sdkLanguage,
                    temporaryOutputPath);
        }
        FileUtils.deleteQuietly(swaggerSpecFile);
        String temporaryOutputPath = APIConstants.TEMP_DIRECTORY_NAME + File.separator + appName;
        String temporaryZipFilePath = temporaryOutputPath + APIConstants.ZIP_FILE_EXTENSION;
        try {
            ZIPUtils zipUtils = new ZIPUtils();
            zipUtils.zipDir(temporaryOutputPath, temporaryZipFilePath);
            FileUtils.deleteDirectory(new File(temporaryOutputPath));
        } catch (IOException e) {
            log.error("Error while generating .zip archive for the generated SDK.", e);
            throw new APIClientGenerationException("Error while generating .zip archive for the generated SDK.", e);
        }
        //The below file object is closed and deleted by the caller, so it should left open until the SDK is downloaded.
        File sdkArchive = new File(temporaryZipFilePath);
        Map<String, String> sdkDataMap = new HashMap<String, String>();
        sdkDataMap.put("path", sdkArchive.getAbsolutePath());
        sdkDataMap.put("fileName", appName + APIConstants.ZIP_FILE_EXTENSION);
        return sdkDataMap;
    }

    /**
     * This method is used to retrieve the supported languages for SDK generation
     *
     * @return supported languages for SDK generation
     */
    public String getSupportedSDKLanguages() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();

        String supportedLanguages = config.getFirstProperty(APIConstants.CLIENT_CODEGEN_SUPPORTED_LANGUAGES);
        return supportedLanguages;

    }

    /**
     * This method is used to generate SDKs for subscribed APIs for a given application
     *
     * @param appName             name of the application
     * @param specLocation        location of the swagger spec for the API
     * @param sdkLanguage         preferred SDK language
     * @param temporaryOutputPath temporary location where the SDK archive is saved until downloaded
     */
    private void generateClient(String appName, String apiName, String apiVersion, String specLocation,
                                String sdkLanguage, String temporaryOutputPath) {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();

        CodegenConfigurator codegenConfigurator = new CodegenConfigurator();
        codegenConfigurator.setGroupId(config.getFirstProperty(APIConstants.CLIENT_CODEGEN_GROUPID));
        codegenConfigurator.setArtifactId(config.getFirstProperty(APIConstants.CLIENT_CODEGEN_ARTIFACTID) + appName);
        codegenConfigurator
                .setModelPackage(config.getFirstProperty(APIConstants.CLIENT_CODEGEN_MODAL_PACKAGE) + appName);
        codegenConfigurator.setApiPackage(config.getFirstProperty(APIConstants.CLIENT_CODEGEN_API_PACKAGE) + appName);
        codegenConfigurator.setInputSpec(specLocation);
        codegenConfigurator.setLang(langCodeGen.get(sdkLanguage));
        codegenConfigurator.setOutputDir(temporaryOutputPath);
        final ClientOptInput clientOptInput = codegenConfigurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

    }
}
