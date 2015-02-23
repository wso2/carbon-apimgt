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
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.MediatorFactoryFinder;
import org.apache.synapse.config.xml.MediatorSerializerFinder;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.mediators.base.SequenceMediator;

import java.io.File;
import java.util.Properties;

/**
 *  Handles the <code>Sequence</code> deployment and undeployment tasks
 *
 * @see AbstractSynapseArtifactDeployer
 */
public class SequenceDeployer extends AbstractSynapseArtifactDeployer {

    private static Log log = LogFactory.getLog(SequenceDeployer.class);
    
    @Override
    public String deploySynapseArtifact(OMElement artifactConfig, String fileName,
                                        Properties properties) {

        if (log.isDebugEnabled()) {
            log.debug("Sequence Deployment from file : " + fileName + " : Started");
        }

        try {
            MediatorFactoryFinder.getInstance().setSynapseLibraryMap(getSynapseConfiguration().getSynapseLibraries());
            Mediator m = MediatorFactoryFinder.getInstance().getMediator(
                    artifactConfig, properties);
            if (m instanceof SequenceMediator) {
                SequenceMediator seq = (SequenceMediator) m;
                seq.setFileName((new File(fileName)).getName());
                if (log.isDebugEnabled()) {
                    log.debug("Sequence named '" + seq.getName()
                            + "' has been built from the file " + fileName);
                }
                seq.init(getSynapseEnvironment());
                if (log.isDebugEnabled()) {
                    log.debug("Initialized the sequence : " + seq.getName());
                }
                getSynapseConfiguration().addSequence(seq.getName(), seq);
                if (log.isDebugEnabled()) {
                    log.debug("Sequence Deployment from file : " + fileName + " : Completed");
                }
                log.info("Sequence named '" + seq.getName()
                        + "' has been deployed from file : " + fileName);
                return seq.getName();
            } else {
                handleSynapseArtifactDeploymentError("Sequence Deployment Failed. " +
                        "The artifact described in the file " + fileName + " is not a Sequence");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Sequence Deployment from the file : " + fileName + " : Failed.", e);
        }

        return null;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName,
                                        String existingArtifactName, Properties properties) {
        
        if (log.isDebugEnabled()) {
            log.debug("Sequence update from file : " + fileName + " has started");
        }

        try {
            Mediator m = MediatorFactoryFinder.getInstance().getMediator(artifactConfig, properties);
            if (m == null || !(m instanceof  SequenceMediator)) {
                handleSynapseArtifactDeploymentError("Sequence update failed. The artifact " +
                        "defined in the file: " + fileName + " is not a valid sequence.");
                return null;
            }

            SequenceMediator seq = (SequenceMediator) m;
            seq.setFileName(new File(fileName).getName());

            if ((SynapseConstants.MAIN_SEQUENCE_KEY.equals(existingArtifactName) ||
                    SynapseConstants.FAULT_SEQUENCE_KEY.equals(existingArtifactName)) &&
                    !existingArtifactName.equals(seq.getName())) {
                handleSynapseArtifactDeploymentError(existingArtifactName + " sequence cannot be renamed");
            }

            if (log.isDebugEnabled()) {
                log.debug("Sequence: " + seq.getName() + " has been built from the file: " + fileName);
            }

            seq.init(getSynapseEnvironment());
            SequenceMediator existingSeq = getSynapseConfiguration().getDefinedSequences().
                    get(existingArtifactName);
            if (existingArtifactName.equals(seq.getName())) {
                getSynapseConfiguration().updateSequence(existingArtifactName, seq);
            } else {
                getSynapseConfiguration().addSequence(seq.getName(), seq);
                getSynapseConfiguration().removeSequence(existingArtifactName);
                log.info("Sequence: " + existingArtifactName + " has been undeployed");
            }

            log.info("Sequence: " + seq.getName() + " has been updated from the file: " + fileName);

            waitForCompletion(); // Give some time for worker threads to release the old sequence
            existingSeq.destroy();
            return seq.getName();

        } catch (DeploymentException e) {
            handleSynapseArtifactDeploymentError("Error while updating the sequence from the " +
                    "file: " + fileName);
        }

        return null;
    }

    @Override
    public void undeploySynapseArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("Sequence Undeployment of the sequence named : "
                    + artifactName + " : Started");
        }
        
        try {
            SequenceMediator seq
                    = getSynapseConfiguration().getDefinedSequences().get(artifactName);
            if (seq != null) {
                if (SynapseConstants.MAIN_SEQUENCE_KEY.equals(seq.getName())
                        || SynapseConstants.FAULT_SEQUENCE_KEY.equals(seq.getName())) {
                    handleSynapseArtifactDeploymentError(
                            "Cannot Undeploy the " + seq.getName() + " sequence");
                }
                getSynapseConfiguration().removeSequence(artifactName);
                if (log.isDebugEnabled()) {
                    log.debug("Destroying the sequence named : " + artifactName);
                }
                seq.destroy();
                if (log.isDebugEnabled()) {
                    log.debug("Sequence Undeployment of the sequence named : "
                            + artifactName + " : Completed");
                }
                log.info("Sequence named '" + seq.getName() + "' has been undeployed");
            } else if (log.isDebugEnabled()) {
                log.debug("Sequence " + artifactName + " has already been undeployed");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Sequence Undeployement of sequence named : " + artifactName + " : Failed", e);
        }
    }

    @Override
    public void restoreSynapseArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("Restoring the Sequence with name : " + artifactName + " : Started");
        }

        try {
            SequenceMediator seq
                    = getSynapseConfiguration().getDefinedSequences().get(artifactName);
            OMElement seqElem = MediatorSerializerFinder.getInstance().getSerializer(seq).
                    serializeMediator(null, seq);
            if (seq.getFileName() != null) {
                String fileName = getServerConfigurationInformation().getSynapseXMLLocation()
                        + File.separator + MultiXMLConfigurationBuilder.SEQUENCES_DIR
                        + File.separator + seq.getFileName();
                writeToFile(seqElem, fileName);
                if (log.isDebugEnabled()) {
                    log.debug("Restoring the Sequence with name : " + artifactName + " : Completed");
                }
                log.info("Sequence named '" + artifactName + "' has been restored");
            } else {
                handleSynapseArtifactDeploymentError("Couldn't restore the sequence named '"
                        + artifactName + "', filename cannot be found");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Restoring of the sequence named '" + artifactName + "' has failed", e);
        }
    }
}
