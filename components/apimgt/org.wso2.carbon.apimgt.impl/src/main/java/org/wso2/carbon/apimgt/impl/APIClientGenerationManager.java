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
import java.io.InterruptedIOException;



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
        APIIdentifier api;
        ProcessBuilder processBuilder;
        Process processShellCommands;
        APIConsumerImpl consumer =  (APIConsumerImpl) APIManagerFactory.getInstance().getAPIConsumer(userName);
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        //currentSubscriber = ApiMgtDAO.getSubscriber(userName);
        currentSubscriber = apiMgtDAO.getSubscriber(userName);
        apiSet = apiMgtDAO.getSubscribedAPIs(currentSubscriber, appName , groupId);
        File spec = null;
        String[] commandsToGen =  new String[4];
        String[] commandsToZip =  new String[3];
        commandsToGen[0] = "sh";
        commandsToGen[1] = "resources/swaggerCodegen/generate.sh";
        commandsToZip[0] = "sh";
        commandsToZip[1] = "resources/swaggerCodegen/toZip.sh";
        for (Iterator<SubscribedAPI> apiIterator = apiSet.iterator(); apiIterator.hasNext(); ) {
            SubscribedAPI subscribedAPI = apiIterator.next();
            resourcePath = APIUtil.getSwagger20DefinitionFilePath(subscribedAPI.getApiId().getApiName(),
                    subscribedAPI.getApiId().getVersion(), subscribedAPI.getApiId().getProviderName());
            if (consumer.registry.resourceExists(resourcePath + APIConstants.API_DOC_2_0_RESOURCE_NAME)) {
                swagger = consumer.definitionFromSwagger20.getAPIDefinition(subscribedAPI.getApiId(), consumer.registry);
                spec = new File("resources/swaggerCodegen/swagger.json");
                if (!spec.exists()) {
                    spec.createNewFile();
                }
                FileWriter fileWriter = new FileWriter(spec.getAbsoluteFile());
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(swagger);
                bufferedWriter.close();
                commandsToGen[2] = subscribedAPI.getApiId().getApiName();
                commandsToGen[3] = appName;
                processBuilder = new ProcessBuilder(commandsToGen);
                processShellCommands = processBuilder.start();     // Start the process.
                processShellCommands.waitFor();
            }


        }
        commandsToZip[2] = appName;
        processBuilder = new ProcessBuilder(commandsToZip);
        processShellCommands = processBuilder.start();     // Start the process.
        processShellCommands.waitFor();
        return appName;
    }
}
