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
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.MediatorFactoryFinder;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.TemplateMediatorSerializer;
import org.apache.synapse.config.xml.endpoints.TemplateSerializer;
import org.apache.synapse.endpoints.Template;
import org.apache.synapse.mediators.template.TemplateMediator;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.Properties;

public class TemplateDeployer extends AbstractSynapseArtifactDeployer {
    private static Log log = LogFactory.getLog(TemplateDeployer.class);

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig, String fileName, Properties properties) {

        if (log.isDebugEnabled()) {
            log.debug("Template Deployment from file : " + fileName + " : Started");
        }

        try {

            OMElement element = artifactConfig.getFirstChildWithName(
                    new QName(SynapseConstants.SYNAPSE_NAMESPACE, "endpoint"));
            if (element != null) {
                org.apache.synapse.config.xml.endpoints.TemplateFactory templateFactory =
                        new org.apache.synapse.config.xml.endpoints.TemplateFactory();

                Template tm = templateFactory.createEndpointTemplate(artifactConfig, properties);

                tm.setFileName(new File(fileName).getName());
                if (log.isDebugEnabled()) {
                    log.debug("Endpoint Template named '" + tm.getName()
                            + "' has been built from the file " + fileName);
                }

                getSynapseConfiguration().addEndpointTemplate(tm.getName(), tm);
                if (log.isDebugEnabled()) {
                    log.debug("Template Deployment from file : " + fileName + " : Completed");
                }
                log.info("Endpoint Template named '" + tm.getName()
                        + "' has been deployed from file : " + fileName);

                return tm.getName();
            } else {
                element = artifactConfig.getFirstChildWithName(
                        new QName(SynapseConstants.SYNAPSE_NAMESPACE, "sequence"));
                if (element != null) {
                    Mediator mediator = MediatorFactoryFinder.getInstance().
                            getMediator(artifactConfig, properties);
                    if (mediator instanceof TemplateMediator) {
                        TemplateMediator tm = (TemplateMediator) mediator;

                        tm.setFileName((new File(fileName)).getName());
                        if (log.isDebugEnabled()) {
                            log.debug("Sequence Template named '" + tm.getName()
                                    + "' has been built from the file " + fileName);
                        }

                        tm.init(getSynapseEnvironment());
                        if (log.isDebugEnabled()) {
                            log.debug("Initialized the Template : " + tm.getName());
                        }

                        getSynapseConfiguration().addSequenceTemplate(tm.getName(), tm);
                        if (log.isDebugEnabled()) {
                            log.debug("Template Deployment from file : " + fileName + " : Completed");
                        }
                        log.info("Template named '" + tm.getName()
                                + "' has been deployed from file : " + fileName);

                        return tm.getName();
                    }
                }
            }

        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Template Deployment from the file : " + fileName + " : Failed.", e);
        }

        return null;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName,
                                        String existingArtifactName, Properties properties) {

        if (log.isDebugEnabled()) {
            log.debug("Template update from file : " + fileName + " has started");
        }

        try {
            OMElement element = artifactConfig.getFirstChildWithName(
                    new QName(SynapseConstants.SYNAPSE_NAMESPACE, "endpoint"));
            if (element != null) {
                org.apache.synapse.config.xml.endpoints.TemplateFactory templateFactory =
                        new org.apache.synapse.config.xml.endpoints.TemplateFactory();

                Template tm = templateFactory.createEndpointTemplate(artifactConfig, properties);

                tm.setFileName(new File(fileName).getName());
                if (log.isDebugEnabled()) {
                    log.debug("Endpoint Template named '" + tm.getName()
                            + "' has been built from the file " + fileName);
                }

                Template existingSt = getSynapseConfiguration().
                        getEndpointTemplate(existingArtifactName);

                if (existingArtifactName.equals(tm.getName())) {

                    getSynapseConfiguration().updateEndpointTemplate(tm.getName(), tm);
                } else {
                    getSynapseConfiguration().addEndpointTemplate(tm.getName(), tm);
                    getSynapseConfiguration().removeEndpointTemplate(existingSt.getName());
                    log.info("Template: " + existingArtifactName + " has been undeployed");
                }

                log.info("Template: " + tm.getName() + " has been updated from the file: " + fileName);
                return tm.getName();
            } else {
                element = artifactConfig.getFirstChildWithName(
                        new QName(SynapseConstants.SYNAPSE_NAMESPACE, "sequence"));
                if (element != null) {
                    Mediator mediator = MediatorFactoryFinder.getInstance().
                            getMediator(artifactConfig, properties);
                    if (mediator instanceof TemplateMediator) {
                        TemplateMediator tm = (TemplateMediator) mediator;

                        tm.setFileName((new File(fileName)).getName());
                        if (log.isDebugEnabled()) {
                            log.debug("Sequence Template named '" + tm.getName()
                                    + "' has been built from the file " + fileName);
                        }

                        tm.init(getSynapseEnvironment());
                        if (log.isDebugEnabled()) {
                            log.debug("Initialized the Template : " + tm.getName());
                        }

                        TemplateMediator existingSt = getSynapseConfiguration().
                                getSequenceTemplate(existingArtifactName);

                        if (existingArtifactName.equals(tm.getName())) {
                            getSynapseConfiguration().updateSequenceTemplate(tm.getName(), tm);
                        } else {
                            getSynapseConfiguration().addSequenceTemplate(tm.getName(), tm);
                            getSynapseConfiguration().removeSequenceTemplate(existingSt.getName());
                            log.info("Template: " + existingArtifactName + " has been undeployed");
                        }

                        existingSt.destroy();
                        log.info("Template: " + tm.getName() + " has been updated from the file: " + fileName);
                        return tm.getName();
                    }
                }
            }

        } catch (DeploymentException e) {
            handleSynapseArtifactDeploymentError("Error while updating the Template from the " +
                    "file: " + fileName);
        }

        return null;
    }

    @Override
    public void undeploySynapseArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("Template Undeployment of the Template named : "
                    + artifactName + " : Started");
        }

        try {
            Template st = null;
            try {
                st = getSynapseConfiguration().getEndpointTemplate(artifactName);
            } catch (SynapseException e) {
                //could not locate an endpoint template for this particular un-delpoyment. This name refers
                //probably to a sequence tempalte. Thus if  throws a Synapse exception with following message
                //catch n log that and continue this process for undeployment of a sequence template
                if (e.getMessage().indexOf("Cannot locate an either local or remote entry for key") != -1) {
                    if (log.isDebugEnabled()) {
                        log.debug("Undeploying template is not of endpoint type. Undeployer will now check " +
                                  "for sequence template for the key: " + artifactName);
                    }
                } else {
                    //different error hence stop undeployment/report failure
                    throw e;
                }
            }
            if (st != null) {
                getSynapseConfiguration().removeEndpointTemplate(artifactName);
                if (log.isDebugEnabled()) {
                    log.debug("Destroying the Template named : " + artifactName);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Template Undeployment of the template named : "
                            + artifactName + " : Completed");
                }
                log.info("Template named '" + st.getName() + "' has been undeployed");
            } else {
                TemplateMediator tm = getSynapseConfiguration().getSequenceTemplate(artifactName);
                if (tm != null) {
                    getSynapseConfiguration().removeSequenceTemplate(artifactName);
                    if (log.isDebugEnabled()) {
                        log.debug("Destroying the Template named : " + artifactName);
                    }
                    tm.destroy();
                    if (log.isDebugEnabled()) {
                        log.debug("Template Undeployment of the template named : "
                                + artifactName + " : Completed");
                    }
                    log.info("Template named '" + tm.getName() + "' has been undeployed");
                } else {
                    log.debug("Template task " + artifactName + " has already been undeployed");
                }
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Template Undeployement of template named : " + artifactName + " : Failed", e);
        }
    }

    @Override
    public void restoreSynapseArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("Restoring the Template with name : " + artifactName + " : Started");
        }

        try {
            Template st = getSynapseConfiguration().getEndpointTemplate(artifactName);
            if (st != null) {
                TemplateSerializer ts = new TemplateSerializer();
                OMElement stElem = ts.serializeEndpointTemplate(st, null);
                if (st.getFileName() != null) {
                    String fileName = getServerConfigurationInformation().getSynapseXMLLocation()
                            + File.separator + MultiXMLConfigurationBuilder.TEMPLATES_DIR
                            + File.separator + st.getFileName();
                    writeToFile(stElem, fileName);
                    if (log.isDebugEnabled()) {
                        log.debug("Restoring the Endpoint Template with name : " +
                                artifactName + " : Completed");
                    }
                    log.info("Template named '" + artifactName + "' has been restored");
                }
            } else {
                TemplateMediator mt = getSynapseConfiguration().getSequenceTemplate(artifactName);
                if (mt != null) {
                    TemplateMediatorSerializer ts = new TemplateMediatorSerializer();
                    OMElement stElem = ts.serializeMediator(null, mt);
                    if (mt.getFileName() != null) {
                        String fileName = getServerConfigurationInformation().getSynapseXMLLocation()
                                + File.separator + MultiXMLConfigurationBuilder.TEMPLATES_DIR
                                + File.separator + st.getFileName();
                        writeToFile(stElem, fileName);
                        if (log.isDebugEnabled()) {
                            log.debug("Restoring the Sequence Template with name : " +
                                    artifactName + " : Completed");
                        }
                        log.info("Template named '" + artifactName + "' has been restored");
                    }
                } else {
                    handleSynapseArtifactDeploymentError("Couldn't restore the Template named '"
                            + artifactName + "', filename cannot be found");
                }
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Restoring of the Template named '" + artifactName + "' has failed", e);
        }
    }
}
