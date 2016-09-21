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
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
 * This class is used to generate SDKs for a given API
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
     * This method generates client side SDK for a given API
     *
     * @param sdkLanguage preferred language to generate the SDK
     * @param userName    username of the logged in user
     * @param apiName     name of the API
     * @param apiVersion  version of the API
     * @param apiProvider provider of the API
     * @return a map containing the zip file name and its' temporary location until it is downloaded
     * @throws APIClientGenerationException if failed to generate the SDK
     */
    public Map<String, String> generateSDK(String sdkLanguage, String userName, String apiName, String apiVersion,
                                           String apiProvider)
            throws APIClientGenerationException {

        if (StringUtils.isBlank(sdkLanguage) || StringUtils.isBlank(userName) || StringUtils.isBlank(apiName) ||
                StringUtils.isBlank(apiVersion) || StringUtils.isBlank(apiProvider)) {
            String errorMessage = "SDK Language, Username,API Name, API Version or API Provider should not be null.";
            log.error(errorMessage);
            throw new APIClientGenerationException(errorMessage);
        }


        //String apiTenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(apiProvider));

        APIConsumerImpl consumerImplInstance;
        APIProviderImpl apiProviderImpl;




        try {
            //consumerImplInstance = (APIConsumerImpl) APIManagerFactory.getInstance().getAPIConsumer(userName);
            consumerImplInstance = (APIConsumerImpl) APIManagerFactory.getInstance().getAPIConsumer();

            apiProviderImpl = new APIProviderImpl(apiProvider);



        } catch (APIManagementException e) {
            String errorMessage = "Error while getting API Consumer Impl instance for user : " + userName;
            log.error(errorMessage, e);
            throw new APIClientGenerationException(errorMessage, e);
        }
//
//        boolean isResourceExists;
//        String registryResourcePath = APIUtil.getSwagger20DefinitionFilePath(apiName, apiVersion, apiProvider);
//        String swaggerResourceAbsolutePath = registryResourcePath + APIConstants.API_DOC_2_0_RESOURCE_NAME;
//
//        try {
//            //todo: check for tenant registry
//            isResourceExists = consumerImplInstance.registry.resourceExists(swaggerResourceAbsolutePath);
//        } catch (RegistryException e) {
//            log.error("Error while checking the existence of the resource at : " + swaggerResourceAbsolutePath, e);
//            throw new APIClientGenerationException("Error while checking the existence of the resource at : " +
//                    swaggerResourceAbsolutePath, e);
//        }

        Swagger swaggerDoc;
        //if (isResourceExists) {
        String swaggerAPIDefinition = null;
        try {
            APIIdentifier apiIdentifier = new APIIdentifier(apiProvider, apiName, apiVersion);
            //swaggerAPIDefinition = consumerImplInstance.getSwagger20Definition(apiIdentifier);
            swaggerAPIDefinition = apiProviderImpl.getSwagger20Definition(apiIdentifier);
        } catch (APIManagementException e) {
            String errorMessage = "Error loading swagger file for API " + apiName + "from registry.";
            log.error(errorMessage, e);
            throw new APIClientGenerationException(errorMessage, e);
        }
        swaggerDoc = new SwaggerParser().parse(swaggerAPIDefinition);
//        } else {
//            log.error("Resource does not exists in : " + swaggerResourceAbsolutePath);
//            throw new APIClientGenerationException("Resource does not exists in : " + swaggerResourceAbsolutePath);
//        }

        //format the swagger definition as a string before writing to the file
        String formattedSwaggerAPIDefinition = Json.pretty(swaggerDoc);
        //create a temporary file with a random file name to store the swagger definition of the API
        String specLocation = APIConstants.TEMP_DIRECTORY_NAME + File.separator + UUID.randomUUID().toString() +
                APIConstants.JSON_FILE_EXTENSION;
        File swaggerSpecFile = new File(specLocation);
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            boolean isSpecFileCreated = swaggerSpecFile.createNewFile();
            if (!isSpecFileCreated) {
                String errorMessage = "Unable to create the swagger spec file for API : " + apiName +
                        " in " + specLocation;
                log.error(errorMessage);
                throw new APIClientGenerationException(errorMessage);
            }
            fileWriter = new FileWriter(swaggerSpecFile.getAbsoluteFile());
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(formattedSwaggerAPIDefinition);
        } catch (IOException e) {
            String errorMessage = "Error while storing the temporary swagger file in : " + specLocation;
            log.error(errorMessage, e);
            throw new APIClientGenerationException(errorMessage, e);
        } finally {
            IOUtils.closeQuietly(bufferedWriter);
            IOUtils.closeQuietly(fileWriter);
        }
        String outputDirectoryName = apiName + "_" + apiVersion + "_" + sdkLanguage;
        String temporaryOutputPath = APIConstants.TEMP_DIRECTORY_NAME + File.separator + UUID.randomUUID().toString() +
                File.separator + outputDirectoryName;
        generateClient(apiName, apiVersion, specLocation, sdkLanguage, temporaryOutputPath);
        FileUtils.deleteQuietly(swaggerSpecFile);
        String temporaryZipFilePath = temporaryOutputPath + APIConstants.ZIP_FILE_EXTENSION;
        try {
            ZIPUtils zipUtils = new ZIPUtils();
            zipUtils.zipDir(temporaryOutputPath, temporaryZipFilePath);
            // On Windows OS, deleting the folder fails stating that a file within it is still open, attempting to close
            // the open file from the jaggery side has not been successful. For the time being we will avoid deleting
            // the directory. This is not an issue since existing zip files will be overwritten on the server side.
            // This issue is not encountered on Linux however. Hence we are not going to delete the directory using
            // FileUtils.deleteDirectory. Reference :  APIMANAGER-4981
            // FileUtils.deleteDirectory(new File(temporaryOutputPath));
            //FileUtils.deleteDirectory(new File(temporaryOutputPath));

//todo - file delete to a java method
            //FileDeleteStrategy.FORCE.delete(new File(temporaryOutputPath));


        } catch (IOException e) {
            String errorMessage = "Error while generating .zip archive for the generated SDK.";
            log.error(errorMessage, e);
            throw new APIClientGenerationException(errorMessage, e);
        }
        //The below file object is closed and deleted by the caller, so it should left open until the SDK is downloaded.
        File sdkArchive = new File(temporaryZipFilePath);
        Map<String, String> sdkDataMap = new HashMap<String, String>();
        sdkDataMap.put("path", sdkArchive.getAbsolutePath());
        sdkDataMap.put("fileName", outputDirectoryName + APIConstants.ZIP_FILE_EXTENSION);
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
     * This method is used to generate SDK for a API for a given language
     *
     * @param apiName             name of the API
     * @param apiVersion          version of the API
     * @param specLocation        location of the swagger spec for the API
     * @param sdkLanguage         preferred SDK language
     * @param temporaryOutputPath temporary location where the SDK archive is saved until downloaded
     */
    private void generateClient(String apiName, String apiVersion, String specLocation,
                                String sdkLanguage, String temporaryOutputPath) {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        CodegenConfigurator codegenConfigurator = new CodegenConfigurator();
        codegenConfigurator.setGroupId(config.getFirstProperty(APIConstants.CLIENT_CODEGEN_GROUPID));
        codegenConfigurator.setArtifactId(config.getFirstProperty(APIConstants.CLIENT_CODEGEN_ARTIFACTID) + apiName);
        codegenConfigurator
                .setModelPackage(config.getFirstProperty(APIConstants.CLIENT_CODEGEN_MODAL_PACKAGE) + apiName);
        codegenConfigurator.setApiPackage(config.getFirstProperty(APIConstants.CLIENT_CODEGEN_API_PACKAGE) + apiName);
        codegenConfigurator.setInputSpec(specLocation);
        codegenConfigurator.setLang(langCodeGen.get(sdkLanguage));
        codegenConfigurator.setOutputDir(temporaryOutputPath);
        final ClientOptInput clientOptInput = codegenConfigurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

    }
}
