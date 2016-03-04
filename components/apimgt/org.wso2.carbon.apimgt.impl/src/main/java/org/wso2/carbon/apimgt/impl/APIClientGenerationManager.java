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

import org.apache.commons.io.FileUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.Set;
import java.util.Iterator;
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
    public String sdkGeneration(String appName, String sdkLanguage, String userName, String groupId)
            throws RegistryException, APIManagementException, IOException, InterruptedException {
        Subscriber currentSubscriber = null;
        String swagger;
        Set<SubscribedAPI> apiSet;
        String resourcePath = null;
        APIConsumerImpl consumer =  (APIConsumerImpl) APIManagerFactory.getInstance().getAPIConsumer(userName);
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        currentSubscriber = apiMgtDAO.getSubscriber(userName);
        apiSet = apiMgtDAO.getSubscribedAPIs(currentSubscriber, appName , groupId);
        if (apiSet.isEmpty()){
            return null;
        }
        File spec = null;
        String apiName;
        String apiVersion;
        String specLocation = "resources/swaggerCodegen/swagger.json";
        String clientOutPutDir;
        String sourceToZip;
        String zipName;
        ZIPUtils zipUtils = new ZIPUtils();
        File tempFolder = new File("resources/swaggerCodegen");
        if(!tempFolder.exists()){
            tempFolder.mkdir();
        }else{
            FileUtils.deleteDirectory(tempFolder);
            tempFolder.mkdir();
        }

        for (Iterator<SubscribedAPI> apiIterator = apiSet.iterator(); apiIterator.hasNext(); ) {
            SubscribedAPI subscribedAPI = apiIterator.next();
            resourcePath = APIUtil.getSwagger20DefinitionFilePath(subscribedAPI.getApiId().getApiName(),
                    subscribedAPI.getApiId().getVersion(), subscribedAPI.getApiId().getProviderName());
            if (consumer.registry.resourceExists(resourcePath + APIConstants.API_DOC_2_0_RESOURCE_NAME)) {
                swagger = consumer.definitionFromSwagger20.getAPIDefinition(subscribedAPI.getApiId(), consumer.registry);
                spec = new File(specLocation);
                if (!spec.exists()) {
                    spec.createNewFile();
                }
                FileWriter fileWriter = new FileWriter(spec.getAbsoluteFile());
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(swagger);
                bufferedWriter.close();
                apiName = subscribedAPI.getApiId().getApiName();
                apiVersion = subscribedAPI.getApiId().getVersion();
                clientOutPutDir = "resources/swaggerCodegen/"+appName+"/"+apiName+"/"+apiVersion;
                generateClient(apiName,apiVersion,specLocation,sdkLanguage,clientOutPutDir);
            }


        }
        sourceToZip = "resources/swaggerCodegen/"+appName;
        zipName = "resources/swaggerCodegen/"+appName+".zip";
        try {
            zipUtils.zipDir(sourceToZip,zipName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return appName;
    }

    private void generateClient(String apiName, String apiVersion,String spec,String lang,String outPutDir){
        String configClass;
        if (lang.equals("java")){
            configClass = "io.swagger.codegen.languages.JavaClientCodegen";
        }else if (lang.equals("android")){
            configClass = "io.swagger.codegen.languages.AndroidClientCodegen";
        }else{
            configClass = null;
        }

        try {
            CodegenConfigurator codegenConfigurator = new CodegenConfigurator();
            codegenConfigurator.setGroupId("org.wso2");
            codegenConfigurator.setArtifactId("org.wso2.client."+apiName+"."+apiVersion.replace(".",""));
            codegenConfigurator.setModelPackage("org.wso2.client.model."+apiName+"."+apiVersion.replace(".",""));
            codegenConfigurator.setApiPackage("org.wso2.client.api."+apiName+"."+apiVersion.replace(".",""));
            codegenConfigurator.setInputSpec(spec);
            codegenConfigurator.setLang(configClass);
            codegenConfigurator.setOutputDir(outPutDir);
            final ClientOptInput clientOptInput = codegenConfigurator.toClientOptInput();
            new DefaultGenerator().opts(clientOptInput).generate();
        } catch (Throwable e) {
           e.printStackTrace();
        }
    }
}
