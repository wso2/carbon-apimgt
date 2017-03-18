/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.core.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.models.WorkflowConfig;
import org.wso2.carbon.apimgt.core.models.WorkflowConfigProperties;
import org.wso2.carbon.apimgt.core.models.WorkflowExecutorInfo;
import org.wso2.carbon.kernel.utils.Utils;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This builder can be used to build {@link WorkflowConfig} object based on the workflow extension configuration file
 */
public class WorkflowExtensionsConfigBuilder {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowExtensionsConfigBuilder.class);
    public static final String WORKFLOW_CONFIG_YML = "workflow-extensions.yml";

    public static final String WF_DEFAULT_APPCREATION_EXEC = 
            "org.wso2.carbon.apimgt.core.workflow.ApplicationCreationSimpleWorkflowExecutor";
    private static final String WF_DEFAULT_APISTATE_EXEC = 
            "org.wso2.carbon.apimgt.core.workflow.APIStateChangeSimpleWorkflowExecutor";
    private static final String WF_DEFAULT_PRODAPP_EXEC = 
            "org.wso2.carbon.apimgt.core.workflow.ApplicationRegistrationSimpleWorkflowExecutor";
    private static final String WF_DEFAULT_SANDBOXAPP_EXEC = 
            "org.wso2.carbon.apimgt.core.workflow.ApplicationRegistrationSimpleWorkflowExecutor";
    private static final String WF_DEFAULT_APPDELETE_EXEC = 
            "org.wso2.carbon.apimgt.core.workflow.ApplicationDeletionSimpleWorkflowExecutor";
    private static final String WF_DEFAULT_SUBCREATION_EXEC = 
            "org.wso2.carbon.apimgt.core.workflow.SubscriptionCreationSimpleWorkflowExecutor";
    private static final String WF_DEFAULT_SUBDELETE_EXEC = 
            "org.wso2.carbon.apimgt.core.workflow.SubscriptionDeletionSimpleWorkflowExecutor";
    private static final String WF_DEFAULT_SIGNUP_EXEC = 
            "org.wso2.carbon.apimgt.core.workflow.UserSignUpSimpleWorkflowExecutor";

    private static WorkflowConfig workflowConfig;

    public static WorkflowConfig getWorkflowConfig() {

        if (workflowConfig == null) {
            build(generateDefaultConfigurations());
        }
        return workflowConfig;
    }

    public static void clearConfig() {
        workflowConfig = null;
    }
    public static void build(WorkflowConfig defaultConfig) {
        Optional<String> workflowConfigFileContent = readFile(WORKFLOW_CONFIG_YML);

        if (workflowConfigFileContent.isPresent()) {

            Constructor constructor = new Constructor(WorkflowConfig.class);
            TypeDescription workflowConfigType = new TypeDescription(WorkflowConfig.class);
            constructor.addTypeDescription(workflowConfigType);

            TypeDescription wfexecutorInfo = new TypeDescription(WorkflowExecutorInfo.class);
            wfexecutorInfo.putListPropertyType("property", WorkflowConfigProperties.class);
            constructor.addTypeDescription(wfexecutorInfo);

            Yaml yaml = new Yaml(constructor);
            workflowConfig = (WorkflowConfig) yaml.load(workflowConfigFileContent.get());
            if (logger.isDebugEnabled()) {
                logger.debug("WorkflowConfiguration file content: " + workflowConfigFileContent.get());
                logger.debug("WorkflowConfig  object :" + workflowConfig.toString());
            }

        } else {
            logger.warn("Could not find the workflow-extensions.yml in conf location. Using default configurations");
            workflowConfig = defaultConfig;
        }
    }

    /**
     * Read file content to a String. The optional will have the file content from the given file in a {@link String}
     * when the file exists in the system or when it is found in the classpath.
     *
     * @param fileName The file name
     * @return An optional {@link String}
     */
    private static Optional<String> readFile(final String fileName) {
        Optional<File> configFile = getConfigFile(fileName);
        try (final InputStream in = configFile.isPresent() ? new FileInputStream(configFile.get())
                : Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            if (in != null) {
                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                        Stream<String> stream = buffer.lines()) {
                    String fileContent = stream.map(Utils::substituteVariables)
                            .collect(Collectors.joining(System.lineSeparator()));
                    return Optional.of(fileContent);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read lines from the file: " + fileName, e);
        }

        return Optional.empty();
    }

    /**
     * Get the configuration file. The optional will have the file only if it exists and if it is a valid file.
     *
     * @param fileName The file name
     * @return An optional {@link File}
     */
    private static Optional<File> getConfigFile(final String fileName) {
        File file = new File(Utils.getCarbonConfigHome().resolve(fileName).toString());

        if (file.exists() && file.isFile()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Configuration file found at {}", file.getAbsolutePath());
            }
            return Optional.of(file);
        } else {
            return Optional.empty();
        }
    }

    private static WorkflowConfig generateDefaultConfigurations() {
        WorkflowConfig defaultConfig = new WorkflowConfig();
        WorkflowExecutorInfo applicationCreation = new WorkflowExecutorInfo();
        applicationCreation.setExecutor(WF_DEFAULT_APPCREATION_EXEC);
        defaultConfig.setApplicationCreation(applicationCreation);

        WorkflowExecutorInfo apiStateChange = new WorkflowExecutorInfo();
        apiStateChange.setExecutor(WF_DEFAULT_APISTATE_EXEC);
        defaultConfig.setApiStateChange(apiStateChange);

        WorkflowExecutorInfo productionApplicationRegistration = new WorkflowExecutorInfo();
        productionApplicationRegistration.setExecutor(WF_DEFAULT_PRODAPP_EXEC);
        defaultConfig.setProductionApplicationRegistration(productionApplicationRegistration);

        WorkflowExecutorInfo applicationDeletion = new WorkflowExecutorInfo();
        applicationDeletion.setExecutor(WF_DEFAULT_APPDELETE_EXEC);
        defaultConfig.setApplicationDeletion(applicationDeletion);

        WorkflowExecutorInfo sandboxApplicationRegistration = new WorkflowExecutorInfo();
        sandboxApplicationRegistration.setExecutor(WF_DEFAULT_SANDBOXAPP_EXEC);
        defaultConfig.setSandboxApplicationRegistration(sandboxApplicationRegistration);

        WorkflowExecutorInfo subscriptionCreation = new WorkflowExecutorInfo();
        subscriptionCreation.setExecutor(WF_DEFAULT_SUBCREATION_EXEC);
        defaultConfig.setSubscriptionCreation(subscriptionCreation);

        WorkflowExecutorInfo subscriptionDeletion = new WorkflowExecutorInfo();
        subscriptionDeletion.setExecutor(WF_DEFAULT_SUBDELETE_EXEC);
        defaultConfig.setSubscriptionDeletion(subscriptionDeletion);

        WorkflowExecutorInfo userSignUp = new WorkflowExecutorInfo();
        userSignUp.setExecutor(WF_DEFAULT_SIGNUP_EXEC);
        defaultConfig.setUserSignUp(userSignUp);

        return defaultConfig;
    }
}
