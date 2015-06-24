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
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.ProxyServiceFactory;
import org.apache.synapse.config.xml.ProxyServiceSerializer;
import org.apache.synapse.core.axis2.ProxyService;

import java.io.File;
import java.util.Properties;

/**
 *  Handles the <code>ProxyService</code> deployment and undeployment tasks
 *
 * @see org.apache.synapse.deployers.AbstractSynapseArtifactDeployer
 */
public class ProxyServiceDeployer extends AbstractSynapseArtifactDeployer {

    private static Log log = LogFactory.getLog(ProxyServiceDeployer.class);
    public static String failSafeStr = "";

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig, String fileName,
                                        Properties properties) {

        /*boolean failSafeProxyEnabled = SynapseConfigUtils.isFailSafeEnabled(
                SynapseConstants.FAIL_SAFE_MODE_PROXY_SERVICES);*/

        if (log.isDebugEnabled()) {
            log.debug("ProxyService Deployment from file : " + fileName + " : Started");
        }

        try {
            ProxyService proxy = ProxyServiceFactory.createProxy(artifactConfig, properties);
            if (proxy != null) {
                if (getSynapseConfiguration().getProxyService(proxy.getName()) != null) {
                    log.warn("Hot deployment thread picked up an already deployed proxy - Ignoring");
                    return proxy.getName();
                }

                proxy.setFileName((new File(fileName)).getName());
                if (log.isDebugEnabled()) {
                    log.debug("ProxyService named '" + proxy.getName()
                            + "' has been built from the file " + fileName);
                }
                initializeProxy(proxy);
                if (log.isDebugEnabled()) {
                    log.debug("Initialized the ProxyService : " + proxy.getName());
                }

                proxy.buildAxisService(getSynapseConfiguration(),
                        getSynapseConfiguration().getAxisConfiguration());
                if (log.isDebugEnabled()) {
                    log.debug("Started the ProxyService : " + proxy.getName());
                }
                getSynapseConfiguration().addProxyService(proxy.getName(), proxy);
                if (log.isDebugEnabled()) {
                    log.debug("ProxyService Deployment from file : " + fileName + " : Completed");
                }
                log.info("ProxyService named '" + proxy.getName()
                        + "' has been deployed from file : " + fileName);
                return proxy.getName();
            } else {
                handleSynapseArtifactDeploymentError("ProxyService Deployment Failed. The " +
                        "artifact described in the file " + fileName + " is not a ProxyService");
            }
        } catch (Exception e) {
            /*if (failSafeProxyEnabled) {
                log.warn("Proxy service hot deployment from file: " + fileName + " failed - " +
                        "Continue in fail-safe mode", e);
            } else {
                handleSynapseArtifactDeploymentError(
                        "ProxyService Deployment from the file : " + fileName + " : Failed.", e);
            }*/
            handleSynapseArtifactDeploymentError(
                    "ProxyService Deployment from the file : " + fileName + " : Failed.", e);
        }
        return null;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName,
                                        String existingArtifactName, Properties properties) {

        if (log.isDebugEnabled()) {
            log.debug("ProxyService Update from file : " + fileName + " : Started");
        }

        try {
            ProxyService proxy = ProxyServiceFactory.createProxy(artifactConfig, properties);
            if (proxy != null) {
                proxy.setFileName((new File(fileName)).getName());
                if (log.isDebugEnabled()) {
                    log.debug("ProxyService named '" + proxy.getName()
                            + "' has been built from the file " + fileName);
                }
                initializeProxy(proxy);
                if (log.isDebugEnabled()) {
                    log.debug("Initialized the ProxyService : " + proxy.getName());
                }
                ProxyService currentProxy = getSynapseConfiguration().getProxyService(existingArtifactName);
                currentProxy.stop(getSynapseConfiguration());
                getSynapseConfiguration().removeProxyService(existingArtifactName);
                if (!existingArtifactName.equals(proxy.getName())) {
                    log.info("ProxyService named " + existingArtifactName + " has been Undeployed");
                }
                proxy.buildAxisService(getSynapseConfiguration(),
                        getSynapseConfiguration().getAxisConfiguration());
                if (log.isDebugEnabled()) {
                    log.debug("Started the ProxyService : " + proxy.getName());
                }
                getSynapseConfiguration().addProxyService(proxy.getName(), proxy);
                if (log.isDebugEnabled()) {
                    log.debug("ProxyService " + (existingArtifactName.equals(proxy.getName()) ?
                            "update" : "deployment") + " from file : " + fileName + " : Completed");
                }
                log.info("ProxyService named '" + proxy.getName()
                        + "' has been " + (existingArtifactName.equals(proxy.getName()) ?
                            "update" : "deployed") + " from file : " + fileName);
                return proxy.getName();
            } else {
                handleSynapseArtifactDeploymentError("ProxyService Update Failed. The artifact " +
                        "described in the file " + fileName + " is not a ProxyService");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "ProxyService Update from the file : " + fileName + " : Failed.", e);
        }

        return null;
    }

    @Override
    public void undeploySynapseArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("ProxyService Undeployment of the proxy named : "
                    + artifactName + " : Started");
        }
        
        try {
            ProxyService proxy = getSynapseConfiguration().getProxyService(artifactName);
            if (proxy != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Stopping the ProxyService named : " + artifactName);
                }
                proxy.stop(getSynapseConfiguration());
                getSynapseConfiguration().removeProxyService(artifactName);
                if (log.isDebugEnabled()) {
                    log.debug("ProxyService Undeployment of the proxy named : "
                            + artifactName + " : Completed");
                }
                log.info("ProxyService named '" + proxy.getName() + "' has been undeployed");
            } else if (log.isDebugEnabled()) {
                log.debug("Proxy service " + artifactName + " has already been undeployed");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "ProxyService Undeployement of proxy named : " + artifactName + " : Failed", e);
        }
    }

    @Override
    public void restoreSynapseArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("Restoring the ProxyService with name : " + artifactName + " : Started");
        }

        try {
            ProxyService proxy
                    = getSynapseConfiguration().getProxyService(artifactName);
            OMElement proxyElem = ProxyServiceSerializer.serializeProxy(null, proxy);
            if (proxy.getFileName() != null) {
                String fileName = getServerConfigurationInformation().getSynapseXMLLocation()
                        + File.separator + MultiXMLConfigurationBuilder.PROXY_SERVICES_DIR
                        + File.separator + proxy.getFileName();
                writeToFile(proxyElem, fileName);
                if (log.isDebugEnabled()) {
                    log.debug("Restoring the ProxyService with name : "
                            + artifactName + " : Completed");
                }
                log.info("ProxyService named '" + artifactName + "' has been restored");
            } else {
                handleSynapseArtifactDeploymentError("Couldn't restore the ProxyService named '"
                        + artifactName + "', filename cannot be found");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Restoring of the ProxyService named '" + artifactName + "' has failed", e);
        }
    }

    private void initializeProxy(ProxyService proxy) throws DeploymentException {
        if (proxy.getTargetInLineEndpoint() != null) {
            proxy.getTargetInLineEndpoint().init(getSynapseEnvironment());
        }
        if (proxy.getTargetInLineInSequence() != null) {
            proxy.getTargetInLineInSequence().init(getSynapseEnvironment());
        }
        if (proxy.getTargetInLineOutSequence() != null) {
            proxy.getTargetInLineOutSequence().init(getSynapseEnvironment());
        }
        if (proxy.getTargetInLineFaultSequence() != null) {
            proxy.getTargetInLineFaultSequence().init(getSynapseEnvironment());
        }
    }
}
