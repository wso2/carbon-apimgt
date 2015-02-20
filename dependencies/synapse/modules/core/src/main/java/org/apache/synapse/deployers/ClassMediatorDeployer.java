/*
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.synapse.deployers;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.Parameter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.SynapseConfiguration;

import java.io.File;

public class ClassMediatorDeployer extends AbstractDeployer {

    /**
     * Holds the log variable for logging purposes
     */
    private static final Log log = LogFactory.getLog(ClassMediatorDeployer.class);

    /**
     * ConfigurationContext of Axis2
     */
    private ConfigurationContext cfgCtx = null;

    /**
     * Initializes the Deployer
     *
     * @param configurationContext - ConfigurationContext of Axis2 from which
     *                             the deployer is initialized
     */
    public void init(ConfigurationContext configurationContext) {
        this.cfgCtx = configurationContext;
    }

    /**
     * This will be called when there is a change in the specified deployment
     * folder (in the axis2.xml) and this will register class loader into the deployement store
     *
     * @param deploymentFileData - describes the updated file
     * @throws DeploymentException - in case an error on the deployment
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

    	String mediatorPath = FilenameUtils.normalize(deploymentFileData.getAbsolutePath());

        log.info("Deploying Class mediators from file : " + mediatorPath);
        ClassLoader mediatorLoader = Utils.getClassLoader(ClassMediatorDeployer.class.getClassLoader(),
                                                          mediatorPath, false);
        getDeploymentStore().addClassMediatorClassLoader(mediatorPath, mediatorLoader);

    }

    /**
     * This will be called when a particular jar file is deleted from the specified folder.
     *
     * @param fileName - filename of the deleted file
     * @throws DeploymentException - in case of an error in undeployment
     */
    public void undeploy(String fileName) throws DeploymentException {
        log.info("Undeploying Class mediator : " +
                 fileName.substring(fileName.lastIndexOf(File.separator)+1));
        getDeploymentStore().removeClassMediatorClassLoader(fileName);
    }

    private SynapseArtifactDeploymentStore getDeploymentStore() throws DeploymentException {
        Parameter synCfgParam =
                cfgCtx.getAxisConfiguration().getParameter(SynapseConstants.SYNAPSE_CONFIG);
        if (synCfgParam == null) {
            throw new DeploymentException("Error retrieving deployment store. SynapseConfiguration not found");
        }
        return ((SynapseConfiguration) synCfgParam.getValue()).getArtifactDeploymentStore();
    }

    public void setDirectory(String s) {
        // Changing the directory is not supported
    }

    public void setExtension(String s) {
       // Changing the extension is not supported
    }

}
