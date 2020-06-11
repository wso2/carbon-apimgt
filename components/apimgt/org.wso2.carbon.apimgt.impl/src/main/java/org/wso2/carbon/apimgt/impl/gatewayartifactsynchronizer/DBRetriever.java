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

import com.google.common.io.ByteStreams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DBRetriever implements ArtifactRetriever {

    private static final Log log = LogFactory.getLog(DBRetriever.class);
    protected ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    @Override
    public void init() throws ArtifactSynchronizerException {
        //not required
    }

    @Override
    public String retrieveArtifact(String APIId, String gatewayLabel, String gatewayInstruction)
            throws ArtifactSynchronizerException {

        String gatewayRuntimeArtifacts;
        try {
            ByteArrayInputStream byteStream =
                    apiMgtDAO.getGatewayPublishedAPIArtifacts(APIId, gatewayLabel, gatewayInstruction);
            byte[] bytes = ByteStreams.toByteArray(byteStream);
            gatewayRuntimeArtifacts = new String(bytes);
            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved Artifact of " + APIId);
            }
        } catch (APIManagementException | IOException e) {
            throw new ArtifactSynchronizerException("Error retrieving Artifact belongs to  " + APIId + " from DB", e);
        }
        return gatewayRuntimeArtifacts;
    }

    @Override
    public List<String> retrieveAllArtifacts(String label) throws ArtifactSynchronizerException {
        List<String> gatewayRuntimeArtifactsArray = new ArrayList<>();
        try {
            List<ByteArrayInputStream> baip = apiMgtDAO.getAllGatewayPublishedAPIArtifacts(label);
            for (ByteArrayInputStream byteStream :baip){
                byte[] bytes = ByteStreams.toByteArray(byteStream);
                String  gatewayRuntimeArtifacts = new String(bytes);
                gatewayRuntimeArtifactsArray.add(gatewayRuntimeArtifacts);
            }
            if (log.isDebugEnabled()){
                log.debug("Successfully retrieved Artifacts from DB");
            }
        } catch (APIManagementException | IOException e) {
            throw new ArtifactSynchronizerException("Error retrieving Artifact from DB", e);
        }
        return gatewayRuntimeArtifactsArray;
    }

    @Override
    public void disconnect() {
        //not required
    }

    @Override
    public String getName() {

        return APIConstants.GatewayArtifactSynchronizer.DB_RETRIEVER_NAME;
    }
}
