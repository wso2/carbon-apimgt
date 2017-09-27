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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.definitions.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
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
        langCodeGen.put("android", "io.swagger.codegen.languages.AndroidClientCodegen");
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
     * @param apiName     name of the API
     * @param apiVersion  version of the API
     * @param apiProvider provider of the API
     * @return a map containing the zip file name and its' temporary location until it is downloaded
     * @throws APIClientGenerationException if failed to generate the SDK
     */
    public Map<String, String> generateSDK(String sdkLanguage, String apiName, String apiVersion, String apiProvider)
            throws APIClientGenerationException {

        if (StringUtils.isBlank(sdkLanguage) || StringUtils.isBlank(apiName) || StringUtils.isBlank(apiVersion) ||
                StringUtils.isBlank(apiProvider)) {
            handleSDKGenException("SDK Language, API Name, API Version or API Provider should not be null.");
        }
        //we should replace the '@' sign with '-AT-' hence it is needed to retrieve the API identifier
        String apiProviderNameWithReplacedEmailDomain = APIUtil.replaceEmailDomain(apiProvider);
        APIIdentifier apiIdentifier = new APIIdentifier(apiProviderNameWithReplacedEmailDomain, apiName, apiVersion);
        String requestedTenant = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(apiProvider));
        int tenantId = 0;

        try {
            tenantId = getTenantId(requestedTenant);
        } catch (UserStoreException e) {
            handleSDKGenException("Error occurred when retrieving the tenant ID for tenant : " + requestedTenant, e);
        }
        boolean isTenantFlowStarted = false;
        String swaggerAPIDefinition = null;

        if (StringUtils.isNotBlank(requestedTenant)) {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(requestedTenant, true);
            isTenantFlowStarted = true;
            try {
                APIUtil.loadTenantRegistry(tenantId);
            } catch (RegistryException e) {
                handleSDKGenException("Failed to load tenant registry for tenant ID : " + tenantId, e);
            }
            Registry requiredRegistry = null;
            try {
                requiredRegistry = getGovernanceUserRegistry(apiProvider, tenantId);
            } catch (RegistryException e) {
                handleSDKGenException("Error occurred when retrieving the tenant registry for tenant : " +
                        requestedTenant + " tenant ID : " + tenantId, e);
            }
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(apiProvider);
            APIDefinition definitionFromSwagger20 = new APIDefinitionFromSwagger20();
            try {
                swaggerAPIDefinition = definitionFromSwagger20.getAPIDefinition(apiIdentifier, requiredRegistry);
            } catch (APIManagementException e) {
                handleSDKGenException("Error loading swagger file for API " + apiName + " from registry.", e);
            }
        }

        if (isTenantFlowStarted) {
            PrivilegedCarbonContext.endTenantFlow();
        }

        Swagger swaggerDoc = new SwaggerParser().parse(swaggerAPIDefinition);
        //format the swagger definition as a string before writing to the file
        String formattedSwaggerAPIDefinition = Json.pretty(swaggerDoc);
        //create a temporary directory with a random name to store files created during generating the SDK
        String tempDirectoryLocation = APIConstants.TEMP_DIRECTORY_NAME + File.separator + UUID.randomUUID().toString();
        File tempDirectory = new File(tempDirectoryLocation);
        boolean isTempDirectoryCreated = tempDirectory.mkdir();

        if (!isTempDirectoryCreated) {
            handleSDKGenException("Unable to create temporary directory in : " + tempDirectoryLocation);
        }

        String specFileLocation = tempDirectoryLocation + File.separator + UUID.randomUUID().toString() +
                APIConstants.JSON_FILE_EXTENSION;
        //the below swaggerSpecFile will be deleted when cleaning the temp directory by the caller
        File swaggerSpecFile = new File(specFileLocation);
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try {
            boolean isSpecFileCreated = swaggerSpecFile.createNewFile();
            if (!isSpecFileCreated) {
                handleSDKGenException("Unable to create the swagger spec file for API : " + apiName + " in " +
                        specFileLocation);
            }
            fileWriter = new FileWriter(swaggerSpecFile.getAbsoluteFile());
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(formattedSwaggerAPIDefinition);
        } catch (IOException e) {
            handleSDKGenException("Error while storing the temporary swagger file in : " + specFileLocation, e);
        } finally {
            IOUtils.closeQuietly(bufferedWriter);
            IOUtils.closeQuietly(fileWriter);
        }

        String sdkDirectoryName = apiName + "_" + apiVersion + "_" + sdkLanguage;
        String temporaryOutputPath = tempDirectoryLocation + File.separator + sdkDirectoryName;
        generateClient(apiName, specFileLocation, sdkLanguage, temporaryOutputPath);
        String temporaryZipFilePath = temporaryOutputPath + APIConstants.ZIP_FILE_EXTENSION;
        try {
            ZIPUtils zipUtils = new ZIPUtils();
            zipUtils.zipDir(temporaryOutputPath, temporaryZipFilePath);
        } catch (IOException e) {
            handleSDKGenException("Error while generating .zip archive for the generated SDK.", e);
        }
        //The below file object is closed and deleted by the caller, so it should left open until the SDK is downloaded.
        File sdkArchive = new File(temporaryZipFilePath);
        Map<String, String> sdkDataMap = new HashMap<String, String>();
        sdkDataMap.put("zipFilePath", sdkArchive.getAbsolutePath());
        sdkDataMap.put("zipFileName", sdkDirectoryName + APIConstants.ZIP_FILE_EXTENSION);
        sdkDataMap.put("tempDirectoryPath", tempDirectoryLocation);
        return sdkDataMap;
    }


    /**
     * This method is used to retrieve the supported languages for SDK generation
     *
     * @return supported languages for SDK generation
     */
    public String getSupportedSDKLanguages() {
        APIManagerConfiguration config = getAPIManagerConfiguration();
        String supportedLanguages = config.getFirstProperty(APIConstants.CLIENT_CODEGEN_SUPPORTED_LANGUAGES);
        return supportedLanguages;

    }

    /**
     * This method is used to generate SDK for a API for a given language
     *
     * @param apiName             name of the API
     * @param specLocation        location of the swagger spec for the API
     * @param sdkLanguage         preferred SDK language
     * @param temporaryOutputPath temporary location where the SDK archive is saved until downloaded
     */
    private void generateClient(String apiName, String specLocation, String sdkLanguage, String temporaryOutputPath) {

        APIManagerConfiguration config = getAPIManagerConfiguration();
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

    /**
     * This method is to handle exceptions occurred when generating the SDK
     *
     * @param errorMessage error message to be printed in the log
     * @throws APIClientGenerationException
     */
    private void handleSDKGenException(String errorMessage) throws APIClientGenerationException {
        log.error(errorMessage);
        throw new APIClientGenerationException(errorMessage);
    }

    /**
     * This method is to handle exceptions occurred when generating the SDK (with a throwable exception)
     *
     * @param errorMessage error message to be printed in the log
     * @param throwable    throwable exception caught
     * @throws APIClientGenerationException
     */
    private void handleSDKGenException(String errorMessage, Throwable throwable) throws APIClientGenerationException {
        log.error(errorMessage, throwable);
        throw new APIClientGenerationException(errorMessage, throwable);
    }

    /**
     * Returns the tenantId given tenant name.
     *
     * @param requestedTenant Tenant domain
     * @return Tenant Id
     * @throws UserStoreException if an error occurs when getting tenant ID
     */
    protected int getTenantId(String requestedTenant) throws UserStoreException {
        return ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(requestedTenant);
    }

    /**
     * Get governance user registry
     *
     * @param apiProvider API Provider name
     * @param tenantId Tenant ID
     * @return User Registry
     * @throws RegistryException if an error occurs when getting UserRegistry
     */
    protected UserRegistry getGovernanceUserRegistry(String apiProvider, int tenantId) throws RegistryException {
        return ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry(apiProvider,
                tenantId);

    }

    /**
     * Returns API manager configurations.
     *
     * @return APIManagerConfiguration object
     */
    protected APIManagerConfiguration getAPIManagerConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
    }
}
