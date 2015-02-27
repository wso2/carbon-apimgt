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
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.rest.APIFactory;
import org.apache.synapse.config.xml.rest.APISerializer;
import org.apache.synapse.rest.API;

import java.io.File;
import java.util.Properties;

public class APIDeployer extends AbstractSynapseArtifactDeployer {

    private static Log log = LogFactory.getLog(APIDeployer.class);

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig, String fileName, Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("API deployment from file : " + fileName + " : Started");
        }

        try {
            API api = APIFactory.createAPI(artifactConfig, properties);
            if (api != null) {
                api.setFileName((new File(fileName)).getName());
                if (log.isDebugEnabled()) {
                    log.debug("API named '" + api.getName()
                            + "' has been built from the file " + fileName);
                }
                api.init(getSynapseEnvironment());
                if (log.isDebugEnabled()) {
                    log.debug("Initialized the API: " + api.getName());
                }
                getSynapseConfiguration().addAPI(api.getName(), api);
                if (log.isDebugEnabled()) {
                    log.debug("API deployment from file : " + fileName + " : Completed");
                }
                log.info("API named '" + api.getName() +
                        "' has been deployed from file : " + fileName);
                return api.getName();
            } else {
                handleSynapseArtifactDeploymentError("API deployment Failed. The artifact " +
                        "described in the file " + fileName + " is not a valid API");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError("API deployment from the file : "
                    + fileName + " : Failed.", e);
        }

        return null;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName, String existingArtifactName, Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("API update from file : " + fileName + " has started");
        }

        try {
            API api = APIFactory.createAPI(artifactConfig, properties);
            if (api == null) {
                handleSynapseArtifactDeploymentError("API update failed. The artifact " +
                        "defined in the file: " + fileName + " is not a valid API.");
                return null;
            }
            api.setFileName(new File(fileName).getName());

            if (log.isDebugEnabled()) {
                log.debug("API: " + api.getName() + " has been built from the file: " + fileName);
            }

            api.init(getSynapseEnvironment());
            API existingAPI = getSynapseConfiguration().getAPI(existingArtifactName);
            if (existingArtifactName.equals(api.getName())) {
                getSynapseConfiguration().updateAPI(existingArtifactName, api);
            } else {
                // The user has changed the name of the API
                // We should add the updated API as a new API and remove the old one
                getSynapseConfiguration().addAPI(api.getName(), api);
                getSynapseConfiguration().removeAPI(existingArtifactName);
                log.info("API: " + existingArtifactName + " has been undeployed");
            }

            log.info("API: " + api.getName() + " has been updated from the file: " + fileName);

            waitForCompletion();
            existingAPI.destroy();
            return api.getName();

        } catch (DeploymentException e) {
            handleSynapseArtifactDeploymentError("Error while updating the API from the " +
                    "file: " + fileName);
        }

        return null;
    }

    @Override
    public void undeploySynapseArtifact(String artifactName) {
        if (log.isDebugEnabled()) {
            log.debug("Undeployment of the API named : "
                    + artifactName + " : Started");
        }

        try {
            API api = getSynapseConfiguration().getAPI(artifactName);
            if (api != null) {
                getSynapseConfiguration().removeAPI(artifactName);
                if (log.isDebugEnabled()) {
                    log.debug("Undeployment of the API named : "
                            + artifactName + " : Completed");
                }
                log.info("API named '" + api.getName() + "' has been undeployed");
            } else if (log.isDebugEnabled()) {
                log.debug("API " + artifactName + " has already been undeployed");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Undeployment of API named : " + artifactName + " : Failed", e);
        }
    }

    @Override
    public void restoreSynapseArtifact(String artifactName) {
        if (log.isDebugEnabled()) {
            log.debug("Restoring the API with name : " + artifactName + " : Started");
        }

        try {
            API api = getSynapseConfiguration().getAPI(artifactName);
            OMElement apiElement = APISerializer.serializeAPI(api);
            if (api.getFileName() != null) {
                String fileName = getServerConfigurationInformation().getSynapseXMLLocation()
                        + File.separator + MultiXMLConfigurationBuilder.REST_API_DIR
                        + File.separator + api.getFileName();
                writeToFile(apiElement, fileName);
                if (log.isDebugEnabled()) {
                    log.debug("Restoring the API with name : " + artifactName + " : Completed");
                }
                log.info("API named '" + artifactName + "' has been restored");
            } else {
                handleSynapseArtifactDeploymentError("Couldn't restore the API named '"
                        + artifactName + "', filename cannot be found");
            }
        } catch (Exception e) {
            handleSynapseArtifactDeploymentError(
                    "Restoring of the API named '" + artifactName + "' has failed", e);
        }
    }
}
