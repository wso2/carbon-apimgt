/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DBSaver implements ArtifactSaver {

    private static final Log log = LogFactory.getLog(DBSaver.class);
    private GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO = GatewayArtifactsMgtDAO.getInstance();

    @Override
    public void init() throws ArtifactSynchronizerException {
        //not required
    }

    /**
     * This method is used to save deployable artifact of an API to the storage in publisher profile. From Publisher
     * profile we can access DB.Thus we don't need an HTTP request to internal service like DB retriever
     *
     */
    @Override
    public void saveArtifact(String gatewayRuntimeArtifacts, String gatewayLabel, String gatewayInstruction)
            throws ArtifactSynchronizerException {

    }

    @Override
    public void saveArtifact(String apiId, String name, String version, String revision, String tenantDomain,
                             File artifact, String[] gatewayLabels,String type) throws ArtifactSynchronizerException {

        try (FileInputStream fileInputStream = new FileInputStream(artifact)) {
            if (!gatewayArtifactsMgtDAO.isAPIDetailsExists(apiId)) {
                gatewayArtifactsMgtDAO.addGatewayPublishedAPIDetails(apiId, name, version, tenantDomain, type);
            }

            if (gatewayArtifactsMgtDAO.isAPIArtifactExists(apiId, revision)) {
                gatewayArtifactsMgtDAO
                        .updateGatewayPublishedAPIArtifacts(apiId, revision, fileInputStream);
            } else {
                gatewayArtifactsMgtDAO.addGatewayPublishedAPIArtifacts(apiId, revision, fileInputStream);
            }
            gatewayArtifactsMgtDAO.addAndRemovePublishedGatewayLabels(apiId, revision, gatewayLabels);
            if (log.isDebugEnabled()) {
                log.debug("Successfully saved Artifacts of " + name);
            }
        } catch (IOException | APIManagementException ex) {
            throw new ArtifactSynchronizerException("Error saving Artifacts to the DB", ex);
        }
    }

    @Override
    public void removeArtifact(String apiId, String name, String version, String revision, String tenantDomain)
            throws ArtifactSynchronizerException {

        try {
            if (gatewayArtifactsMgtDAO.isAPIDetailsExists(apiId)) {
                if (StringUtils.isNotEmpty(apiId)) {
                    if (StringUtils.isNotEmpty(revision)) {
                        // Delete Specific revision.
                        gatewayArtifactsMgtDAO.deleteGatewayArtifact(apiId, revision);
                    } else {
                        // Delete API.
                        gatewayArtifactsMgtDAO.deleteGatewayArtifacts(apiId);
                    }
                }
            }
        } catch (APIManagementException e) {
            throw new ArtifactSynchronizerException("Error removing Artifacts from db", e);
        }

    }

    @Override
    public boolean isAPIPublished(String apiId, String revision) throws ArtifactSynchronizerException{

        return false;
    }

    public boolean isAPIPublished(String apiId) {

        try {
            return gatewayArtifactsMgtDAO.isAPIPublishedInAnyGateway(apiId);
        } catch (APIManagementException e) {
            log.error("Error checking API with ID " + apiId + " is published in any gateway", e);
        }
        return false;
    }

    @Override
    public void disconnect() {
        //not required
    }

    @Override
    public String getName() {

        return APIConstants.GatewayArtifactSynchronizer.DB_SAVER_NAME;
    }
}