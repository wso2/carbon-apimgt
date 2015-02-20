/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.apache.synapse.deployers;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.xml.MessageProcessorFactory;
import org.apache.synapse.config.xml.MessageProcessorSerializer;
import org.apache.synapse.config.xml.MessageStoreSerializer;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.message.processor.MessageProcessor;

import java.io.File;
import java.util.Properties;

public class    MessageProcessorDeployer extends AbstractSynapseArtifactDeployer {

    private static Log log = LogFactory.getLog(MessageProcessorDeployer.class);

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig, String fileName, Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Message Processor Deployment from file : " + fileName + " : Started");
        }

        try{

            MessageProcessor mp = MessageProcessorFactory.createMessageProcessor(artifactConfig);
            if(mp != null) {
                mp.setFileName((new File(fileName)).getName());
                 if (log.isDebugEnabled()) {
                    log.debug("Message Processor named '" + mp.getName()
                            + "' has been built from the file " + fileName);
                }
                mp.init(getSynapseEnvironment());
                if (log.isDebugEnabled()) {
                    log.debug("Initialized the Message Processor : " + mp.getName());
                }
                getSynapseConfiguration().addMessageProcessor(mp.getName(), mp);
                if (log.isDebugEnabled()) {
                    log.debug("Message Processor Deployment from file : " + fileName +
                            " : Completed");
                }
                log.info("Message Processor named '" + mp.getName()
                        + "' has been deployed from file : " + fileName);
                return mp.getName();
            } else {
                handleSynapseArtifactDeploymentError("Message Processor Deployment from the file : "
                    + fileName + " : Failed. The artifact " +
                        "described in the file  is not a Message Processor");
            }

        } catch (Exception e) {
            handleSynapseArtifactDeploymentError("Message Processor Deployment from the file : "
                    + fileName + " : Failed.", e);
        }

        return null;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName,
                                        String existingArtifactName, Properties properties) {
       if (log.isDebugEnabled()) {
            log.debug("Message Processor update from file : " + fileName + " has started");
        }

        try {
            MessageProcessor mp = MessageProcessorFactory.createMessageProcessor(artifactConfig);
            if (mp == null) {
                handleSynapseArtifactDeploymentError("Message Processor update failed. The artifact " +
                        "defined in the file: " + fileName + " is not valid");
                return null;
            }
            mp.setFileName(new File(fileName).getName());

            if (log.isDebugEnabled()) {
                log.debug("MessageProcessor: " + mp.getName() + " has been built from the file: "
                        + fileName);
            }


            MessageProcessor existingMp = getSynapseConfiguration().getMessageProcessors().
                    get(existingArtifactName);
            existingMp.destroy();
            // We should add the updated MessageProcessor as a new MessageProcessor
            // and remove the old one

            mp.init(getSynapseEnvironment());

            getSynapseConfiguration().removeMessageProcessor(existingArtifactName);
            log.info("MessageProcessor: " + existingArtifactName + " has been undeployed");

            getSynapseConfiguration().addMessageProcessor(mp.getName(), mp);
            log.info("MessageProcessor: " + mp.getName() + " has been updated from the file: " + fileName);

            waitForCompletion();
            return mp.getName();

        } catch (DeploymentException e) {
            handleSynapseArtifactDeploymentError("Error while updating the MessageProcessor from the " +
                    "file: " + fileName);
        }

        return null;
    }

    @Override
    public void undeploySynapseArtifact(String artifactName) {
         if (log.isDebugEnabled()) {
            log.debug("MessageProcessor Undeployment of the MessageProcessor named : "
                    + artifactName + " : Started");
        }

        try {
            MessageProcessor mp =
                    getSynapseConfiguration().getMessageProcessors().get(artifactName);
            if (mp != null) {
                getSynapseConfiguration().removeMessageProcessor(artifactName);
                if (log.isDebugEnabled()) {
                    log.debug("Destroying the MessageProcessor named : " + artifactName);
                }
                mp.destroy();
                if (log.isDebugEnabled()) {
                    log.debug("MessageProcessor Undeployment of the endpoint named : "
                            + artifactName + " : Completed");
                }
                log.info("MessageProcessor named '" + mp.getName() + "' has been undeployed");
            } else if (log.isDebugEnabled()) {
                log.debug("MessageProcessor " + artifactName + " has already been undeployed");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "MessageProcessor Undeployement of MessageProcessor named : "
                    + artifactName + " : Failed", e);
        }
    }

    @Override
    public void restoreSynapseArtifact(String artifactName) {
        if (log.isDebugEnabled()) {
            log.debug("Restoring the MessageProcessor with name : " + artifactName + " : Started");
        }

        try {
            MessageProcessor mp
                    = getSynapseConfiguration().getMessageProcessors().get(artifactName);
            OMElement msElem = MessageProcessorSerializer.serializeMessageProcessor(null,mp);
            if (mp.getFileName() != null) {
                String fileName = getServerConfigurationInformation().getSynapseXMLLocation()
                        + File.separator + MultiXMLConfigurationBuilder.MESSAGE_PROCESSOR_DIR
                        + File.separator + mp.getFileName();
                writeToFile(msElem, fileName);
                if (log.isDebugEnabled()) {
                    log.debug("Restoring the MessageProcessor with name : "
                            + artifactName + " : Completed");
                }
                log.info("MessageProcessor named '" + artifactName + "' has been restored");
            } else {
                handleSynapseArtifactDeploymentError("Couldn't restore the MessageProcessor named '"
                        + artifactName + "', filename cannot be found");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Restoring of the MessageProcessor named '" + artifactName + "' has failed", e);
        }
    }
}
