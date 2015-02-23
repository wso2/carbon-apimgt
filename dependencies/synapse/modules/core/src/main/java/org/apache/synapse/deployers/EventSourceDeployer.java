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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.eventing.EventSourceFactory;
import org.apache.synapse.config.xml.eventing.EventSourceSerializer;
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.AxisFault;

import java.io.File;
import java.util.Properties;

/**
 *  Handles the <code>EventSource</code> deployment and undeployment tasks
 *
 * @see org.apache.synapse.deployers.AbstractSynapseArtifactDeployer
 */
public class EventSourceDeployer extends AbstractSynapseArtifactDeployer {

    private static Log log = LogFactory.getLog(EventSourceDeployer.class);

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig, String fileName,
                                        Properties properties) {

        if (log.isDebugEnabled()) {
            log.debug("EventSource Deployment from file : " + fileName + " : Started");
        }

        try {
            SynapseEventSource es = EventSourceFactory.createEventSource(
                    artifactConfig, properties);
            if (es != null) {
                es.setFileName((new File(fileName)).getName());
                if (log.isDebugEnabled()) {
                    log.debug("EventSource named '" + es.getName()
                            + "' has been built from the file " + fileName);
                }
                es.buildService(getSynapseConfiguration().getAxisConfiguration());
                if (log.isDebugEnabled()) {
                    log.debug("Initialized the EventSource : " + es.getName());
                }
                getSynapseConfiguration().addEventSource(es.getName(), es);
                if (log.isDebugEnabled()) {
                    log.debug("EventSource Deployment from file : " + fileName + " : Completed");
                }
                log.info("EventSource named '" + es.getName()
                        + "' has been deployed from file : " + fileName);
                return es.getName();
            } else {
                handleSynapseArtifactDeploymentError("EventSource Deployment Failed. The " +
                        "artifact described in the file " + fileName + " is not an EventSource");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "EventSource Deployment from the file : " + fileName + " : Failed.", e);
        }

        return null;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName,
                                        String existingArtifactName, Properties properties) {

        if (log.isDebugEnabled()) {
            log.debug("EventSource Update from file : " + fileName + " : Started");
        }

        try {
            SynapseEventSource es = EventSourceFactory.createEventSource(
                    artifactConfig, properties);
            if (es != null) {
                es.setFileName((new File(fileName)).getName());
                if (log.isDebugEnabled()) {
                    log.debug("EventSource named '" + es.getName()
                            + "' has been built from the file " + fileName);
                }
                getSynapseConfiguration().removeEventSource(existingArtifactName);
                if (!existingArtifactName.equals(es.getName())) {
                    log.info("EventSource named " + existingArtifactName + " has been Undeployed");
                }
                es.buildService(getSynapseConfiguration().getAxisConfiguration());
                if (log.isDebugEnabled()) {
                    log.debug("Initialized the EventSource : " + es.getName());
                }
                getSynapseConfiguration().addEventSource(es.getName(), es);
                if (log.isDebugEnabled()) {
                    log.debug("EventSource " + (existingArtifactName.equals(es.getName()) ?
                            "update" : "deployment") + " from file : " + fileName + " : Completed");
                }
                log.info("EventSource named '" + es.getName()
                        + "' has been " + (existingArtifactName.equals(es.getName()) ?
                            "update" : "deployed") + " from file : " + fileName);
                return es.getName();
            } else {
                handleSynapseArtifactDeploymentError("EventSource Update Failed. The artifact " +
                        "described in the file " + fileName + " is not a EventSource");
            }
        } catch (DeploymentException e) {
            handleSynapseArtifactDeploymentError(
                    "EventSource Update from the file : " + fileName + " : Failed.", e);
        } catch (AxisFault e) {
            handleSynapseArtifactDeploymentError("Error while initializing the event source", e);
        }

        return null;
    }

    @Override
    public void undeploySynapseArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("EventSource Undeployment of the event source named : "
                    + artifactName + " : Started");
        }
        
        try {
            SynapseEventSource es = getSynapseConfiguration().getEventSource(artifactName);
            if (es != null) {
                getSynapseConfiguration().removeEventSource(artifactName);
                if (log.isDebugEnabled()) {
                    log.debug("EventSource Undyou neeeployment of the EventSource named : "
                            + artifactName + " : Completed");
                }
                log.info("EventSource named '" + es.getName() + "' has been undeployed");
            } else if (log.isDebugEnabled()) {
                log.debug("Event source " + artifactName + " has already been undeployed");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError("EventSource Undeployement of EventSource named : "
                    + artifactName + " : Failed", e);
        }
    }

    @Override
    public void restoreSynapseArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("Restoring the EventSource with name : " + artifactName + " : Started");
        }

        try {
            SynapseEventSource es
                    = getSynapseConfiguration().getEventSource(artifactName);
            OMElement esElem = EventSourceSerializer.serializeEventSource(null, es);
            if (es.getFileName() != null) {
                String fileName = getServerConfigurationInformation().getSynapseXMLLocation()
                        + File.separator + MultiXMLConfigurationBuilder.EVENTS_DIR
                        + File.separator + es.getFileName();
                writeToFile(esElem, fileName);
                if (log.isDebugEnabled()) {
                    log.debug("Restoring the EventSource with name : " + artifactName + " : Completed");
                }
                log.info("EventSource named '" + artifactName + "' has been restored");
            } else {
                handleSynapseArtifactDeploymentError("Couldn't restore the EventSource named '"
                        + artifactName + "', filename cannot be found");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Restoring of the EventSource named '" + artifactName + "' has failed", e);
        }
    }
}
