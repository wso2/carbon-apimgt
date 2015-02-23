/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.deployers;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Startup;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.StartupFinder;

import java.io.File;
import java.util.Properties;

/**
 *  Handles the <code>Startup Task</code> deployment and undeployment
 *
 * @see org.apache.synapse.deployers.AbstractSynapseArtifactDeployer
 */
public class TaskDeployer extends AbstractSynapseArtifactDeployer {

    private static Log log = LogFactory.getLog(TaskDeployer.class);

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig, String fileName, Properties properties) {

        if (log.isDebugEnabled()) {
            log.debug("StartupTask Deployment from file : " + fileName + " : Started");
        }

        try {
            Startup st = StartupFinder.getInstance().getStartup(artifactConfig, properties);
                st.setFileName((new File(fileName)).getName());
                if (log.isDebugEnabled()) {
                    log.debug("StartupTask named '" + st.getName()
                            + "' has been built from the file " + fileName);
                }
                st.init(getSynapseEnvironment());
                if (log.isDebugEnabled()) {
                    log.debug("Initialized the StartupTask : " + st.getName());
                }
                getSynapseConfiguration().addStartup(st);
                if (log.isDebugEnabled()) {
                    log.debug("StartupTask Deployment from file : " + fileName + " : Completed");
                }
                log.info("StartupTask named '" + st.getName()
                        + "' has been deployed from file : " + fileName);
                return st.getName();
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "StartupTask Deployment from the file : " + fileName + " : Failed.", e);
        }

        return null;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName,
                                        String existingArtifactName, Properties properties) {

        if (log.isDebugEnabled()) {
            log.debug("StartupTask update from file : " + fileName + " has started");
        }

        try {
            Startup st = StartupFinder.getInstance().getStartup(artifactConfig, properties);
            st.setFileName((new File(fileName)).getName());

            if (log.isDebugEnabled()) {
                log.debug("StartupTask: " + st.getName() + " has been built from the file: " + fileName);
            }

            Startup existingSt = getSynapseConfiguration().getStartup(existingArtifactName);
            existingSt.destroy();


            st.init(getSynapseEnvironment());
            if (existingArtifactName.equals(st.getName())) {
                getSynapseConfiguration().updateStartup(st);
            } else {
                getSynapseConfiguration().addStartup(st);
                getSynapseConfiguration().removeStartup(existingArtifactName);
                log.info("StartupTask: " + existingArtifactName + " has been undeployed");
            }


            log.info("StartupTask: " + st.getName() + " has been updated from the file: " + fileName);
            return st.getName();

        } catch (DeploymentException e) {
            handleSynapseArtifactDeploymentError("Error while updating the startup task from the " +
                    "file: " + fileName);
        }

        return null;
    }

    @Override
    public void undeploySynapseArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("StartupTask Undeployment of the task named : "
                    + artifactName + " : Started");
        }

        try {
            Startup st = getSynapseConfiguration().getStartup(artifactName);
            if (st != null) {
                getSynapseConfiguration().removeStartup(artifactName);
                if (log.isDebugEnabled()) {
                    log.debug("Destroying the StartupTask named : " + artifactName);
                }
                st.destroy();
                if (log.isDebugEnabled()) {
                    log.debug("StartupTask Undeployment of the sequence named : "
                            + artifactName + " : Completed");
                }
                log.info("StartupTask named '" + st.getName() + "' has been undeployed");
            } else if (log.isDebugEnabled()) {
                log.debug("Startup task " + artifactName + " has already been undeployed");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "StartupTask Undeployement of task named : " + artifactName + " : Failed", e);
        }
    }

    @Override
    public void restoreSynapseArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("Restoring the StartupTask with name : " + artifactName + " : Started");
        }

        try {
            Startup st = getSynapseConfiguration().getStartup(artifactName);
            OMElement stElem = StartupFinder.getInstance().serializeStartup(null, st);
            if (st.getFileName() != null) {
                String fileName = getServerConfigurationInformation().getSynapseXMLLocation()
                        + File.separator + MultiXMLConfigurationBuilder.TASKS_DIR
                        + File.separator + st.getFileName();
                writeToFile(stElem, fileName);
                if (log.isDebugEnabled()) {
                    log.debug("Restoring the StartupTask with name : " + artifactName + " : Completed");
                }
                log.info("StartupTask named '" + artifactName + "' has been restored");
            } else {
                handleSynapseArtifactDeploymentError("Couldn't restore the StartupTask named '"
                        + artifactName + "', filename cannot be found");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Restoring of the StartupTask named '" + artifactName + "' has failed", e);
        }
    }
}
