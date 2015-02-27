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
import org.apache.synapse.commons.jmx.MBeanRegistrar;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.config.xml.endpoints.EndpointSerializer;
import org.apache.synapse.endpoints.Endpoint;

import java.io.File;
import java.util.Properties;

/**
 *  Handles the <code>Endpoint</code> deployment and undeployment tasks
 *
 * @see org.apache.synapse.deployers.AbstractSynapseArtifactDeployer
 */
public class EndpointDeployer extends AbstractSynapseArtifactDeployer {

    private static Log log = LogFactory.getLog(EndpointDeployer.class);

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig, String fileName,
                                        Properties properties) {

        if (log.isDebugEnabled()) {
            log.debug("Endpoint Deployment from file : " + fileName + " : Started");
        }

        try {
            Endpoint ep = EndpointFactory.getEndpointFromElement(artifactConfig, false, properties);
            if (ep != null) {
                ep.setFileName((new File(fileName)).getName());
                if (log.isDebugEnabled()) {
                    log.debug("Endpoint named '" + ep.getName()
                            + "' has been built from the file " + fileName);
                }
                ep.init(getSynapseEnvironment());
                if (log.isDebugEnabled()) {
                    log.debug("Initialized the endpoint : " + ep.getName());
                }
                getSynapseConfiguration().addEndpoint(ep.getName(), ep);
                if (log.isDebugEnabled()) {
                    log.debug("Endpoint Deployment from file : " + fileName + " : Completed");
                }
                log.info("Endpoint named '" + ep.getName()
                        + "' has been deployed from file : " + fileName);
                return ep.getName();
            } else {
                handleSynapseArtifactDeploymentError("Endpoint Deployment Failed. The artifact " +
                        "described in the file " + fileName + " is not an Endpoint");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError("Endpoint Deployment from the file : "
                    + fileName + " : Failed.", e);
        }

        return null;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName,
                                        String existingArtifactName, Properties properties) {

        if (log.isDebugEnabled()) {
            log.debug("Endpoint update from file : " + fileName + " has started");
        }

        try {
            Endpoint ep = EndpointFactory.getEndpointFromElement(artifactConfig, false, properties);
            if (ep == null) {
                handleSynapseArtifactDeploymentError("Endpoint update failed. The artifact " +
                        "defined in the file: " + fileName + " is not a valid endpoint.");
                return null;
            }
            ep.setFileName(new File(fileName).getName());

            if (log.isDebugEnabled()) {
                log.debug("Endpoint: " + ep.getName() + " has been built from the file: " + fileName);
            }

            ep.init(getSynapseEnvironment());
            Endpoint existingEp = getSynapseConfiguration().getDefinedEndpoints().get(existingArtifactName);
            if (existingArtifactName.equals(ep.getName())) {
                getSynapseConfiguration().updateEndpoint(existingArtifactName, ep);
            } else {
                // The user has changed the name of the endpoint
                // We should add the updated endpoint as a new endpoint and remove the old one
                getSynapseConfiguration().addEndpoint(ep.getName(), ep);
                getSynapseConfiguration().removeEndpoint(existingArtifactName);
                log.info("Endpoint: " + existingArtifactName + " has been undeployed");
            }

            log.info("Endpoint: " + ep.getName() + " has been updated from the file: " + fileName);

            waitForCompletion();
            existingEp.destroy();
            if (existingArtifactName.equals(ep.getName())) {
                // If the endpoint name was same as the old one, above method call (destroy)
                // will unregister the endpoint MBean - So we should register it again.
                MBeanRegistrar.getInstance().registerMBean(
                        ep.getMetricsMBean(), "Endpoint", ep.getName());
            }
            return ep.getName();

        } catch (DeploymentException e) {
            handleSynapseArtifactDeploymentError("Error while updating the endpoint from the " +
                    "file: " + fileName);
        }

        return null;
    }

    @Override
    public void undeploySynapseArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("Endpoint Undeployment of the endpoint named : "
                    + artifactName + " : Started");
        }
        
        try {
            Endpoint ep = getSynapseConfiguration().getDefinedEndpoints().get(artifactName);
            if (ep != null) {
                getSynapseConfiguration().removeEndpoint(artifactName);
                if (log.isDebugEnabled()) {
                    log.debug("Destroying the endpoint named : " + artifactName);
                }
                ep.destroy();
                if (log.isDebugEnabled()) {
                    log.debug("Endpoint Undeployment of the endpoint named : "
                            + artifactName + " : Completed");
                }
                log.info("Endpoint named '" + ep.getName() + "' has been undeployed");
            } else if (log.isDebugEnabled()) {
                log.debug("Endpoint " + artifactName + " has already been undeployed");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError("Endpoint Undeployement of endpoint named : "
                    + artifactName + " : Failed", e);
        }
    }

    @Override
    public void restoreSynapseArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("Restoring the Endpoint with name : " + artifactName + " : Started");
        }

        try {
            Endpoint ep
                    = getSynapseConfiguration().getDefinedEndpoints().get(artifactName);
            OMElement epElem = EndpointSerializer.getElementFromEndpoint(ep);
            if (ep.getFileName() != null) {
                String fileName = getServerConfigurationInformation().getSynapseXMLLocation()
                        + File.separator + MultiXMLConfigurationBuilder.ENDPOINTS_DIR
                        + File.separator + ep.getFileName();
                writeToFile(epElem, fileName);
                if (log.isDebugEnabled()) {
                    log.debug("Restoring the Endpoint with name : "
                            + artifactName + " : Completed");
                }
                log.info("Endpoint named '" + artifactName + "' has been restored");
            } else {
                handleSynapseArtifactDeploymentError("Couldn't restore the endpoint named '"
                        + artifactName + "', filename cannot be found");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Restoring of the endpoint named '" + artifactName + "' has failed", e);
        }
    }
}
