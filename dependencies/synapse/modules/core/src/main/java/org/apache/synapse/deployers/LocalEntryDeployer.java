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
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.xml.EntryFactory;
import org.apache.synapse.config.xml.EntrySerializer;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;

import java.io.File;
import java.util.Properties;

/**
 *  Handles the <code>LocalEntry</code> deployment and undeployment tasks
 *
 * @see org.apache.synapse.deployers.AbstractSynapseArtifactDeployer
 */
public class LocalEntryDeployer extends AbstractSynapseArtifactDeployer {

    private static Log log = LogFactory.getLog(LocalEntryDeployer.class);

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig, String fileName,
                                        Properties properties) {

        if (log.isDebugEnabled()) {
            log.debug("LocalEntry Deployment from file : " + fileName + " : Started");
        }

        try {
            Entry e = EntryFactory.createEntry(artifactConfig, properties);
            if (e != null) {
                e.setFileName((new File(fileName)).getName());
                if (log.isDebugEnabled()) {
                    log.debug("LocalEntry with key '" + e.getKey()
                            + "' has been built from the file " + fileName);
                }
                getSynapseConfiguration().addEntry(e.getKey(), e);
                if (log.isDebugEnabled()) {
                    log.debug("LocalEntry Deployment from file : " + fileName + " : Completed");
                }
                log.info("LocalEntry named '" + e.getKey()
                        + "' has been deployed from file : " + fileName);
                return e.getKey();
            } else {
                handleSynapseArtifactDeploymentError("LocalEntry Deployment Failed. The artifact " +
                        "described in the file " + fileName + " is not a LocalEntry");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "LocalEntry Deployment from the file : " + fileName + " : Failed.", e);
        }

        return null;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName,
                                        String existingArtifactName, Properties properties) {

        if (log.isDebugEnabled()) {
            log.debug("LocalEntry update from file : " + fileName + " has started");
        }

        try {
            Entry e = EntryFactory.createEntry(artifactConfig, properties);
            if (e == null) {
                handleSynapseArtifactDeploymentError("Local entry update failed. The artifact " +
                        "defined in the file: " + fileName + " is not a valid local entry.");
                return null;
            }
            e.setFileName(new File(fileName).getName());

            if (log.isDebugEnabled()) {
                log.debug("Local entry: " + e.getKey() + " has been built from the file: " + fileName);
            }

            if (existingArtifactName.equals(e.getKey())) {
                getSynapseConfiguration().updateEntry(existingArtifactName, e);
            } else {
                // The user has changed the name of the entry
                // We should add the updated entry as a new entry and remove the old one
                getSynapseConfiguration().addEntry(e.getKey(), e);
                getSynapseConfiguration().removeEntry(existingArtifactName);
                log.info("Local entry: " + existingArtifactName + " has been undeployed");
            }

            log.info("Endpoint: " + e.getKey() + " has been updated from the file: " + fileName);
            return e.getKey();

        } catch (DeploymentException e) {
            handleSynapseArtifactDeploymentError("Error while updating the local entry from the " +
                    "file: " + fileName);
        }

        return null;
    }

    @Override
    public void undeploySynapseArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("LocalEntry Undeployment of the entry named : "
                    + artifactName + " : Started");
        }
        
        try {
            Entry e = getSynapseConfiguration().getDefinedEntries().get(artifactName);
            if (e != null && e.getType() != Entry.REMOTE_ENTRY) {
                getSynapseConfiguration().removeEntry(artifactName);
                if (log.isDebugEnabled()) {
                    log.debug("LocalEntry Undeployment of the entry named : "
                            + artifactName + " : Completed");
                }
                log.info("LocalEntry named '" + e.getKey() + "' has been undeployed");
            } else if (log.isDebugEnabled()) {
                log.debug("Local entry " + artifactName + " has already been undeployed");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "LocalEntry Undeployement of entry named : " + artifactName + " : Failed", e);
        }
    }

    @Override
    public void restoreSynapseArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("LocalEntry the Sequence with name : " + artifactName + " : Started");
        }

        try {
            Entry e = getSynapseConfiguration().getDefinedEntries().get(artifactName);
            OMElement entryElem = EntrySerializer.serializeEntry(e, null);
            if (e.getFileName() != null) {
                String fileName = getServerConfigurationInformation().getSynapseXMLLocation()
                        + File.separator + MultiXMLConfigurationBuilder.LOCAL_ENTRY_DIR
                        + File.separator + e.getFileName();
                writeToFile(entryElem, fileName);
                if (log.isDebugEnabled()) {
                    log.debug("Restoring the LocalEntry with name : " + artifactName + " : Completed");
                }
                log.info("LocalEntry named '" + artifactName + "' has been restored");
            } else {
                handleSynapseArtifactDeploymentError("Couldn't restore the LocalEntry named '"
                        + artifactName + "', filename cannot be found");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Restoring of the LocalEntry named '" + artifactName + "' has failed", e);
        }
    }
}
