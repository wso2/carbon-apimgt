/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.cleanup.service;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactSaver;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.wso2.carbon.apimgt.persistence.utils.PersistenceUtil.handleException;

/**
 * This class used to remove API data from organization
 */
public class ApiPurge implements OrganizationPurge {

    private String username;
    private ArtifactSaver artifactSaver;
    private ApiMgtDAO apiMgtDAO;
    private GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO;
    APIPersistence apiPersistenceInstance;
    private static final Log log = LogFactory.getLog(ApiPurge.class);

    public ApiPurge(String username) {
        this.username = username;
        this.artifactSaver = ServiceReferenceHolder.getInstance().getArtifactSaver();
        this.gatewayArtifactsMgtDAO = GatewayArtifactsMgtDAO.getInstance();
        apiMgtDAO = ApiMgtDAO.getInstance();
    }

    /**
     * @param orgId Organization Id
     * @throws APIManagementException
     */
    public void deleteOrganization(String orgId) throws APIManagementException {

        List<APIIdentifier> apiIdentifierList = new ArrayList<>();
        ArrayList<String> taskListArray = new ArrayList<>(Arrays.asList(APIConstants.OrganizationDeletion.API_RETRIEVER,
                APIConstants.OrganizationDeletion.API_DB_DATA_REMOVER,
                APIConstants.OrganizationDeletion.ARTIFACT_SERVER_DATA_REMOVER,
                APIConstants.OrganizationDeletion.GW_ARTIFACT_DATA_REMOVER,
                APIConstants.OrganizationDeletion.API_ARTIFACT_DATA_REMOVER));

        for (String task : taskListArray) {
            int count = 0;
            int maxTries = 3;
            while (true) {
                try {
                    switch (task) {
                    case APIConstants.OrganizationDeletion.API_RETRIEVER:
                        apiIdentifierList = apiMgtDAO.getAPIIdList(orgId);
                        break;
                    case APIConstants.OrganizationDeletion.API_DB_DATA_REMOVER:
                        apiMgtDAO.deleteOrganizationAPIList(apiIdentifierList);
                        break;
                    case APIConstants.OrganizationDeletion.ARTIFACT_SERVER_DATA_REMOVER:
                        removeArtifactsFromArtifactServer(apiIdentifierList, orgId);
                        break;
                    case APIConstants.OrganizationDeletion.GW_ARTIFACT_DATA_REMOVER:
                        gatewayArtifactsMgtDAO.removeOrganizationGatewayArtifacts(apiIdentifierList);
                        break;
                    case APIConstants.OrganizationDeletion.API_ARTIFACT_DATA_REMOVER:
                        removeAllOrganizationAPIArtifacts(orgId);
                        break;
                    }
                } catch (APIManagementException e) {
                    if (++count == maxTries)
                        throw e;
                }
            }
        }

        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.API, new Gson().toJson(apiIdentifierList),
                APIConstants.AuditLogConstants.DELETED, username);
    }

    private void removeAllOrganizationAPIArtifacts(String orgId) throws APIManagementException {
        try {
            apiPersistenceInstance.deleteAllAPIs(new Organization(orgId));
        } catch (APIPersistenceException e) {
            log.error("Error while deleting api artifacts in organization" + orgId + "from artifact Store", e);
            handleException("Failed to delete all api artifacts of organization " + orgId, e);
        }
    }

    private void removeArtifactsFromArtifactServer(List<APIIdentifier> apiIdentifierList, String orgId)
            throws APIManagementException {

        if (artifactSaver != null) {
            try {
                for (APIIdentifier apiIdentifier : apiIdentifierList) {
                    artifactSaver.removeArtifact(apiIdentifier.getUUID(), apiIdentifier.getApiName(),
                            apiIdentifier.getVersion(), orgId);
                }
            } catch (ArtifactSynchronizerException e) {
                log.error("Error while deleting Runtime artifacts in organization" + orgId +
                        "from artifact Store", e);
                handleException("Failed to delete artifacts of organization " + orgId + " from artifact server.", e);
            }
        }
    }
}
