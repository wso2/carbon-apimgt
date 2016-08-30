/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.Model;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.swagger.codegen.config.CodegenConfigurator;
import io.swagger.codegen.ClientOptInput;
import io.swagger.codegen.DefaultGenerator;

/*
 * This class is used to generate SDKs for subscribed APIs
 */

public class APIClientGenerationManager {

    private static final Log log = LogFactory.getLog(APIClientGenerationManager.class);
    private static final Map<String, String> langCodeGen= new HashMap<String, String>();

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
     * This method generates SDK for a given API subscriptions with a given application
     *
     * @param appName name of the application
     * @param sdkLanguage preferred SDK language
     * @param userName username of the logged in user
     * @param groupId group ID of the logged in user
     * @param apiName name of the subscribed API
     * @param apiVersion version of the subscribed API
     * @param apiProvider provider of the subscribed API
     * @return a map containing the zip file name and its' temporary location until it is downloaded
     * @throws APIClientGenerationException if failed to generate the SDK
     */
    public Map<String, String> generateSDK(String appName, String sdkLanguage, String userName, String groupId,
                                             String apiName, String apiVersion, String apiProvider)
            throws APIClientGenerationException {
        Subscriber currentSubscriber = null;
        String swaggerAPIDefinition = null;
        Set<SubscribedAPI> apiSet = null;
        String resourcePath = null;
        APIConsumerImpl consumer = null;
        try {
            consumer = (APIConsumerImpl) APIManagerFactory.getInstance().getAPIConsumer(userName);
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            currentSubscriber = apiMgtDAO.getSubscriber(userName);
            apiSet = apiMgtDAO.getSubscribedAPIs(currentSubscriber, appName, groupId);
        } catch (APIManagementException e) {
            log.error("Error while getting the subscribed API set for application : " + appName, e);
            throw new APIClientGenerationException("Error while getting the subscribed API set for application : " + appName, e);
        }

        if (apiSet.isEmpty()) {
            return null;
        }

        File spec = null;
        String specLocation = "tmp" + File.separator + "swaggerCodegen" + File.separator +
                                                                    UUID.randomUUID().toString() + ".json";
        String clientOutPutDir;
        String sourceToZip;
        String zipName;
        ZIPUtils zipUtils = new ZIPUtils();
        File tempFolder = new File("tmp" + File.separator + "swaggerCodegen");
        if (!tempFolder.exists()) {
            tempFolder.mkdir();
        } else {
            // On Windows OS, deleting the folder fails stating that a file within it is still open, attempting to close
            // the open file from the jaggery side has not been successful. For the time being we will avoid deleting
            // the directory. This is not an issue since existing zip files will be overwritten on the server side.
            // This issue is not encountered on Linux however.
            /*
            try {
                FileUtils.deleteDirectory(tempFolder);
            } catch (IOException e) {
                log.error("Problem deleting the temporary swaggerCodegen folder", e);
                throw new APIClientGenerationException("Problem deleting the temporary swaggerCodegen folder", e);
            }
            */
            tempFolder.mkdir();
        }

        Swagger initial = null;
        Map<String, Path> paths = null;
        Map<String, Path> tempPaths = null;
        Map<String, Model> definitions = null;
        Map<String, Model> tempDefinitions = null;
        Map<String, SecuritySchemeDefinition> securityDefinitions = null;
        Map<String, SecuritySchemeDefinition> tempSecurityDefinitions = null;
        Swagger temp;
        boolean isResourceExists = false;

        for (Iterator<SubscribedAPI> apiIterator = apiSet.iterator(); apiIterator.hasNext(); ) {

            SubscribedAPI subscribedAPI = apiIterator.next();

            //get the exact API for the subscription
            if (subscribedAPI.getApiId().getApiName().equals(apiName) &&
                    subscribedAPI.getApiId().getVersion().equals(apiVersion) &&
                    subscribedAPI.getApiId().getProviderName().equals(apiProvider)) {

                resourcePath = APIUtil.getSwagger20DefinitionFilePath(subscribedAPI.getApiId().getApiName(),
                        subscribedAPI.getApiId().getVersion(), subscribedAPI.getApiId().getProviderName());
                String swaggerJsonResourcePath = resourcePath + APIConstants.API_DOC_2_0_RESOURCE_NAME;



                try {
                    isResourceExists = consumer.registry.resourceExists(swaggerJsonResourcePath);
                } catch (RegistryException e) {
                    log.error("Error while checking the swagger resource exists or not in : "+swaggerJsonResourcePath, e);
                    throw new APIClientGenerationException("Error while checking the swagger resource exists or not in : "+swaggerJsonResourcePath, e);
                }
                if (isResourceExists) {
                    try {
                        swaggerAPIDefinition = consumer.definitionFromSwagger20.getAPIDefinition
                                (subscribedAPI.getApiId(), consumer.registry);
                    } catch (APIManagementException e) {
                        log.error("Error loading swagger file from registry", e);
                        throw new APIClientGenerationException("Error loading swagger file from registry", e);
                    }
                    initial = new SwaggerParser().parse(swaggerAPIDefinition);
                    paths = initial.getPaths();
                    definitions = initial.getDefinitions();
                    securityDefinitions = initial.getSecurityDefinitions();

                }

                isResourceExists = false;
                try {
                    isResourceExists = consumer.registry.resourceExists(resourcePath + APIConstants.API_DOC_2_0_RESOURCE_NAME);
                } catch (RegistryException e) {
                    log.error("Problem while checking whether the resource exists or not", e);
                    throw new APIClientGenerationException("Problem while checking whether the resource exists or not", e);
                }
                if (isResourceExists) {
                    try {
                        swaggerAPIDefinition = consumer.definitionFromSwagger20.getAPIDefinition
                                (subscribedAPI.getApiId(), consumer.registry);
                    } catch (APIManagementException e) {
                        log.error("Error loading swagger file from registry", e);
                        throw new APIClientGenerationException("Error loading swagger file from registry", e);
                    }

                    //initial = new SwaggerParser().parse(swaggerAPIDefinition);//for below code block

                    temp = new SwaggerParser().parse(swaggerAPIDefinition);
                    tempPaths = temp.getPaths();
                    if (paths == null && tempPaths != null) {
                        paths = tempPaths;
                    } else if (tempPaths != null) {
                        for (Map.Entry<String, Path> entryPath : tempPaths.entrySet()) {
                            paths.put(entryPath.getKey(), entryPath.getValue());
                        }
                    }
                    tempDefinitions = temp.getDefinitions();
                    if (definitions == null && tempDefinitions != null) {
                        definitions = tempDefinitions;
                    } else if (tempDefinitions != null) {
                        for (Map.Entry<String, Model> entryDef : tempDefinitions.entrySet()) {
                            definitions.put(entryDef.getKey(), entryDef.getValue());
                        }
                    }
                    tempSecurityDefinitions = temp.getSecurityDefinitions();
                    if (securityDefinitions == null && tempSecurityDefinitions != null) {
                        securityDefinitions = tempSecurityDefinitions;
                    } else if (tempSecurityDefinitions != null) {
                        for (Map.Entry<String, SecuritySchemeDefinition> entrySecurityDef : tempSecurityDefinitions
                                .entrySet()) {
                            securityDefinitions.put(entrySecurityDef.getKey(), entrySecurityDef.getValue());
                        }
                    }
                }

                initial.setPaths(paths);
                initial.setDefinitions(definitions);
                initial.setSecurityDefinitions(securityDefinitions);
                swaggerAPIDefinition = Json.pretty(initial);
                spec = new File(specLocation);
                FileWriter fileWriter = null;
                BufferedWriter bufferedWriter = null;
                try {
                    if (!spec.exists()) {
                        spec.createNewFile();
                    }
                    fileWriter = new FileWriter(spec.getAbsoluteFile());
                    bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write(swaggerAPIDefinition);
                } catch (IOException e) {
                    log.error("Error occurred while creating the temporary swagger file.", e);
                    throw new APIClientGenerationException("Error occurred while creating the temporary swagger file.", e);
                } finally {
                    IOUtils.closeQuietly(bufferedWriter);
                    IOUtils.closeQuietly(fileWriter);
                }
                break;
            }
        }
        clientOutPutDir = "tmp" + File.separator + "swaggerCodegen" + File.separator + appName + "_" + apiName + "_" +
                apiVersion + "_" + sdkLanguage;
        generateClient(appName, specLocation, sdkLanguage, clientOutPutDir);
        sourceToZip = "tmp" + File.separator + "swaggerCodegen" + File.separator + appName + "_" + apiName + "_" +
                apiVersion + "_" + sdkLanguage;
        zipName = "tmp" + File.separator + "swaggerCodegen" +
                File.separator + appName + "_" + apiName + "_" + apiVersion + "_" + sdkLanguage + ".zip";
        File deleteProject = new File(clientOutPutDir);
        try {
            zipUtils.zipDir(sourceToZip, zipName);
            FileUtils.deleteDirectory(deleteProject);
        } catch (IOException e) {
            log.error("Problem while archiving the generated SDK", e);
            throw new APIClientGenerationException("Problem while archiving the generated SDK", e);
        }
        spec.delete();
        File zipFile = new File(zipName);
        Map<String, String> result = new HashMap<String, String>();
        result.put("path", zipFile.getAbsolutePath());
        result.put("fileName", appName + "_" + apiName + "_" + apiVersion + "_" + sdkLanguage + ".zip");
        return result;
    }

    /**
     * This method is used to retrieve the supported languages for SDK generation
     *
     * @return supported languages for SDK generation
     */
    public String getSupportedSDKLanguages()    {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();

        String supportedLanguages = config.getFirstProperty(APIConstants.CLIENT_CODEGEN_SUPPORTED_LANGUAGES);

        return supportedLanguages;

    }

    /**
     *
     * @param appName
     * @param spec
     * @param lang
     * @param outPutDir
     */
    private void generateClient(String appName, String spec, String lang, String outPutDir) {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();

        CodegenConfigurator codegenConfigurator = new CodegenConfigurator();
        codegenConfigurator.setGroupId(config.getFirstProperty(APIConstants.CLIENT_CODEGEN_GROUPID));
        codegenConfigurator.setArtifactId(config.getFirstProperty(APIConstants.CLIENT_CODEGEN_ARTIFACTID) + appName);
        codegenConfigurator
                .setModelPackage(config.getFirstProperty(APIConstants.CLIENT_CODEGEN_MODAL_PACKAGE) + appName);
        codegenConfigurator.setApiPackage(config.getFirstProperty(APIConstants.CLIENT_CODEGEN_API_PACKAGE) + appName);
        codegenConfigurator.setInputSpec(spec);
        codegenConfigurator.setLang(langCodeGen.get(lang));
        codegenConfigurator.setOutputDir(outPutDir);
        final ClientOptInput clientOptInput = codegenConfigurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

    }
}
