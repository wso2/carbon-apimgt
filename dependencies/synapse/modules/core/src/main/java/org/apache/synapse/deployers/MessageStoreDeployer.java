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
import org.apache.synapse.commons.jmx.MBeanRegistrar;
import org.apache.synapse.config.xml.MessageStoreFactory;
import org.apache.synapse.config.xml.MessageStoreSerializer;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.config.xml.endpoints.EndpointSerializer;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.message.store.MessageStore;

import java.io.File;
import java.util.Properties;

public class MessageStoreDeployer extends AbstractSynapseArtifactDeployer{

    private static Log log = LogFactory.getLog(MessageStoreDeployer.class);

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig, String fileName, Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Message Store Deployment from file : " + fileName + " : Started");
        }

        try{

            MessageStore ms = MessageStoreFactory.createMessageStore(artifactConfig,properties);
            if(ms != null) {
                ms.setFileName((new File(fileName)).getName());
                 if (log.isDebugEnabled()) {
                    log.debug("Message Store named '" + ms.getName()
                            + "' has been built from the file " + fileName);
                }
                ms.init(getSynapseEnvironment());
                if (log.isDebugEnabled()) {
                    log.debug("Initialized the Message Store : " + ms.getName());
                }
                getSynapseConfiguration().addMessageStore(ms.getName(), ms);
                if (log.isDebugEnabled()) {
                    log.debug("Message Store Deployment from file : " + fileName + " : Completed");
                }
                log.info("Message Store named '" + ms.getName()
                        + "' has been deployed from file : " + fileName);
                return ms.getName();
            } else {
                handleSynapseArtifactDeploymentError("Message Store Deployment from the file : "
                    + fileName + " : Failed. The artifact " +
                        "described in the file  is not a Message Store");
            }

        } catch (Exception e) {
            handleSynapseArtifactDeploymentError("Message Store Deployment from the file : "
                    + fileName + " : Failed.", e);
        }

        return null;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName, String existingArtifactName, Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Message Store update from file : " + fileName + " has started");
        }

        try {
            MessageStore ms = MessageStoreFactory.createMessageStore(artifactConfig, properties);
            if (ms == null) {
                handleSynapseArtifactDeploymentError("Message Store update failed. The artifact " +
                        "defined in the file: " + fileName + " is not valid");
                return null;
            }
            ms.setFileName(new File(fileName).getName());

            if (log.isDebugEnabled()) {
                log.debug("MessageStore: " + ms.getName() + " has been built from the file: "
                        + fileName);
            }

            ms.init(getSynapseEnvironment());
            MessageStore existingMs = getSynapseConfiguration().getMessageStore(existingArtifactName);

            // We should add the updated MessageStore as a new MessageStore and remove the old one
            getSynapseConfiguration().removeMessageStore(existingArtifactName);
            getSynapseConfiguration().addMessageStore(ms.getName(), ms);
            log.info("MessageStore: " + existingArtifactName + " has been undeployed");


            log.info("MessageStore: " + ms.getName() + " has been updated from the file: " + fileName);

            waitForCompletion();
            existingMs.destroy();
            return ms.getName();

        } catch (DeploymentException e) {
            handleSynapseArtifactDeploymentError("Error while updating the MessageStore from the " +
                    "file: " + fileName);
        }

        return null;
    }

    @Override
    public void undeploySynapseArtifact(String artifactName) {
          if (log.isDebugEnabled()) {
            log.debug("MessageStore Undeployment of the MessageStore named : "
                    + artifactName + " : Started");
        }

        try {
            MessageStore ms = getSynapseConfiguration().getMessageStore(artifactName);
            if (ms != null) {
                getSynapseConfiguration().removeMessageStore(artifactName);
                if (log.isDebugEnabled()) {
                    log.debug("Destroying the MessageStore named : " + artifactName);
                }
                ms.destroy();
                if (log.isDebugEnabled()) {
                    log.debug("MessageStore Undeployment of the endpoint named : "
                            + artifactName + " : Completed");
                }
                log.info("MessageStore named '" + ms.getName() + "' has been undeployed");
            } else if (log.isDebugEnabled()) {
                log.debug("MessageStore " + artifactName + " has already been undeployed");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError("MessageStore Undeployement of MessageStore named : "
                    + artifactName + " : Failed", e);
        }
    }

    @Override
    public void restoreSynapseArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("Restoring the MessageStore with name : " + artifactName + " : Started");
        }

        try {
            MessageStore ms
                    = getSynapseConfiguration().getMessageStore(artifactName);
            OMElement msElem = MessageStoreSerializer.serializeMessageStore(null,ms);
            if (ms.getFileName() != null) {
                String fileName = getServerConfigurationInformation().getSynapseXMLLocation()
                        + File.separator + MultiXMLConfigurationBuilder.MESSAGE_STORE_DIR
                        + File.separator + ms.getFileName();
                writeToFile(msElem, fileName);
                if (log.isDebugEnabled()) {
                    log.debug("Restoring the MessageStore with name : "
                            + artifactName + " : Completed");
                }
                log.info("MessageStore named '" + artifactName + "' has been restored");
            } else {
                handleSynapseArtifactDeploymentError("Couldn't restore the MessageStore named '"
                        + artifactName + "', filename cannot be found");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Restoring of the MessageStore named '" + artifactName + "' has failed", e);
        }
    }
}
