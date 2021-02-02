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

    @Override
    public void saveArtifact(String apiId, String name, String version, String revision, String tenantDomain,
                             File artifact) throws ArtifactSynchronizerException {
        try (FileInputStream fileInputStream = new FileInputStream(artifact)) {
            gatewayArtifactsMgtDAO.addGatewayPublishedAPIArtifacts(apiId, revision, fileInputStream);
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
            gatewayArtifactsMgtDAO.deleteGatewayArtifact(apiId, revision);
        } catch (APIManagementException e) {
            throw new ArtifactSynchronizerException("Error removing Artifacts from db", e);
        }
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