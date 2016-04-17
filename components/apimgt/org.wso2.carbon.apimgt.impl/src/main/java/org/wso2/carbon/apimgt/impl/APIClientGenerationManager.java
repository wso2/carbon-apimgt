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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIClientGenerationException;

import java.util.*;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import io.swagger.codegen.config.CodegenConfigurator;
import io.swagger.codegen.ClientOptInput;
import io.swagger.codegen.DefaultGenerator;

/*
 * This class is used to generate sdks for subscribed APIs
 */
public class APIClientGenerationManager {
    private static final Log log = LogFactory.getLog(APIClientGenerationManager.class);

    /**
     * Get access token key for given userId and API Identifier
     *
     * @param appName          name of the application
     * @param sdkLanguage preffered SDK language
     * @param userName      username of the logged in user
     * @param groupId         group ID of the logged in user
     * @return Name of the generated SDK
     * @throws APIClientGenerationException if failed to generate the SDK
     */
    public String sdkGeneration(String appName, String sdkLanguage, String userName, String groupId)
            throws APIClientGenerationException, IOException {
        Subscriber currentSubscriber = null;
        String swagger = null;
        Set<SubscribedAPI> apiSet = null;
        String resourcePath = null;
        APIConsumerImpl consumer = null;
        try {
            consumer = (APIConsumerImpl) APIManagerFactory.getInstance().getAPIConsumer(userName);
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            currentSubscriber = apiMgtDAO.getSubscriber(userName);
            apiSet = apiMgtDAO.getSubscribedAPIs(currentSubscriber, appName, groupId);
        } catch (APIManagementException e) {
            log.error("Unexpected error when getting the subscribed api set", e);
            throw new APIClientGenerationException("Unexpected error when getting the subscribed api set", e);
        }

        if (apiSet.isEmpty()) {
            return null;
        }
        File spec = null;
        boolean isFirstApi = true;
        String specLocation = "tmp" + File.separator + "swaggerCodegen" + File.separator + userName + ".json";
        String clientOutPutDir;
        String sourceToZip;
        String zipName;
        ZIPUtils zipUtils = new ZIPUtils();
        File tempFolder = new File("tmp" + File.separator + "swaggerCodegen");
        if (!tempFolder.exists()) {
            tempFolder.mkdir();
        } else {
            try {
                FileUtils.deleteDirectory(tempFolder);
            } catch (IOException e) {
                log.error("Problem deleting the temporary swaggerCodegen folder", e);
                throw new APIClientGenerationException("Problem deleting the temporary swaggerCodegen folder", e);
            }
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
            resourcePath = APIUtil.getSwagger20DefinitionFilePath(subscribedAPI.getApiId().getApiName(),
                    subscribedAPI.getApiId().getVersion(), subscribedAPI.getApiId().getProviderName());
            try {
                isResourceExists = consumer.registry
                        .resourceExists(resourcePath + APIConstants.API_DOC_2_0_RESOURCE_NAME);
            } catch (RegistryException e) {
                log.error("Problem while checking weather the resource exists or not", e);
                throw new APIClientGenerationException("Problem while checking weather the resource exists or not", e);
            }
            if (isFirstApi && isResourceExists) {
                try {
                    swagger = consumer.definitionFromSwagger20
                            .getAPIDefinition(subscribedAPI.getApiId(), consumer.registry);
                } catch (APIManagementException e) {
                    log.error("Error loading swagger file from registry", e);
                    throw new APIClientGenerationException("Error loading swagger file from registry", e);

                }
                initial = new SwaggerParser().parse(swagger);
                paths = initial.getPaths();
                definitions = initial.getDefinitions();
                securityDefinitions = initial.getSecurityDefinitions();
                isFirstApi = false;
            }

            isResourceExists = false;
            try {
                isResourceExists = consumer.registry
                        .resourceExists(resourcePath + APIConstants.API_DOC_2_0_RESOURCE_NAME);
            } catch (RegistryException e) {
                log.error("Problem while checking weather the resource exists or not", e);
                throw new APIClientGenerationException("Problem while checking weather the resource exists or not", e);
            }
            if (!isFirstApi && isResourceExists) {
                try {
                    swagger = consumer.definitionFromSwagger20
                            .getAPIDefinition(subscribedAPI.getApiId(), consumer.registry);
                } catch (APIManagementException e) {
                    log.error("Error loading swagger file from registry", e);
                    throw new APIClientGenerationException("Error loading swagger file from registry", e);
                }
                temp = new SwaggerParser().parse(swagger);

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

            swagger = Json.pretty(initial);
            spec = new File(specLocation);

            FileWriter fileWriter = null;
            BufferedWriter bufferedWriter = null;
            try {
                if (!spec.exists()) {
                    spec.createNewFile();
                }
                fileWriter = new FileWriter(spec.getAbsoluteFile());
                bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(swagger);
            } catch (IOException e) {
                log.error("problem when storing the temporary swagger file", e);
                throw new APIClientGenerationException("problem when storing the temporary swagger file", e);
            } finally {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            }

        }
        clientOutPutDir = "tmp" + File.separator + "swaggerCodegen" + File.separator + appName;
        generateClient(appName, specLocation, sdkLanguage, clientOutPutDir);
        sourceToZip = "tmp" + File.separator + "swaggerCodegen" + File.separator + appName;
        zipName = "tmp" + File.separator + "swaggerCodegen" + File.separator + appName + "_" + sdkLanguage + ".zip";
        File deleteProject = new File(clientOutPutDir);
        try {
            zipUtils.zipDir(sourceToZip, zipName);
            FileUtils.deleteDirectory(deleteProject);
        } catch (IOException e) {
            log.error("Problem while archiving the generated SDK", e);
            throw new APIClientGenerationException("Problem while archiving the generated SDK", e);
        }

        spec.delete();
        return appName + "_" + sdkLanguage;
    }

    private void generateClient(String appName, String spec, String lang, String outPutDir) {

        String configClass;
        if (lang.equals("java")) {
            configClass = "io.swagger.codegen.languages.JavaClientCodegen";
        } else if (lang.equals("android")) {
            configClass = "io.swagger.codegen.languages.AndroidClientCodegen";
        } else {
            configClass = null;
        }
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();

        CodegenConfigurator codegenConfigurator = new CodegenConfigurator();
        codegenConfigurator.setGroupId(config.getFirstProperty(APIConstants.CLIENT_CODEGEN_GROUPID));
        codegenConfigurator.setArtifactId(config.getFirstProperty(APIConstants.CLIENT_CODEGEN_ARTIFACTID) + appName);
        codegenConfigurator
                .setModelPackage(config.getFirstProperty(APIConstants.CLIENT_CODEGEN_MODAL_PACKAGE) + appName);
        codegenConfigurator.setApiPackage(config.getFirstProperty(APIConstants.CLIENT_CODEGEN_API_PACKAGE) + appName);
        codegenConfigurator.setInputSpec(spec);
        codegenConfigurator.setLang(configClass);
        codegenConfigurator.setOutputDir(outPutDir);
        final ClientOptInput clientOptInput = codegenConfigurator.toClientOptInput();
        new DefaultGenerator().opts(clientOptInput).generate();

    }
}
