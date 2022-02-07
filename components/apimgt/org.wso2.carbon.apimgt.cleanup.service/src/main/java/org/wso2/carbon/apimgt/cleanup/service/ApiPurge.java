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
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactSaver;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.PersistenceManager;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import static org.wso2.carbon.apimgt.persistence.utils.PersistenceUtil.handleException;

/**
 * This class used to remove API data from organization
 */
@Component(
        name = "api.purge.service",
        immediate = true,
        service = OrganizationPurge.class
)
public class ApiPurge implements OrganizationPurge {

    private final ArtifactSaver artifactSaver;
    private final OrganizationPurgeDAO organizationPurgeDAO;
    private final GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO;
    LinkedHashMap<String, String> apiPurgeTaskMap = new LinkedHashMap<>();
    APIPersistence apiPersistenceInstance;
    private static final Log log = LogFactory.getLog(ApiPurge.class);

    public ApiPurge() {
        this.artifactSaver = ServiceReferenceHolder.getInstance().getArtifactSaver();
        this.gatewayArtifactsMgtDAO = GatewayArtifactsMgtDAO.getInstance();
        organizationPurgeDAO = OrganizationPurgeDAO.getInstance();
        setupPersistenceManager();
        initTaskList();
    }

    public ApiPurge(APIPersistence apiPersistence) {
        this();
        this.apiPersistenceInstance = apiPersistence;
    }

    private void initTaskList() {
        apiPurgeTaskMap.put(APIConstants.OrganizationDeletion.API_ORG_EXIST, APIConstants.OrganizationDeletion.PENDING);
        apiPurgeTaskMap.put(APIConstants.OrganizationDeletion.API_RETRIEVER, APIConstants.OrganizationDeletion.PENDING);
        apiPurgeTaskMap.put(APIConstants.OrganizationDeletion.API_DB_DATA_REMOVER,
                APIConstants.OrganizationDeletion.PENDING);
        apiPurgeTaskMap.put(APIConstants.OrganizationDeletion.ARTIFACT_SERVER_DATA_REMOVER,
                APIConstants.OrganizationDeletion.PENDING);
        apiPurgeTaskMap.put(APIConstants.OrganizationDeletion.GW_ARTIFACT_DATA_REMOVER,
                APIConstants.OrganizationDeletion.PENDING);
        apiPurgeTaskMap.put(APIConstants.OrganizationDeletion.API_ARTIFACT_DATA_REMOVER,
                APIConstants.OrganizationDeletion.PENDING);
    }

    private void setupPersistenceManager(){
        Map<String, String> configMap = new HashMap<>();
        Map<String, String> configs = APIManagerConfiguration.getPersistenceProperties();
        if (configs != null && !configs.isEmpty()) {
            configMap.putAll(configs);
        }
        configMap.put(APIConstants.ALLOW_MULTIPLE_STATUS,
                Boolean.toString(APIUtil.isAllowDisplayAPIsWithMultipleStatus()));

        Properties properties = new Properties();
        properties.put(APIConstants.ALLOW_MULTIPLE_STATUS, APIUtil.isAllowDisplayAPIsWithMultipleStatus());
        apiPersistenceInstance = PersistenceManager.getPersistenceInstance(configMap, properties);
    }

    /**
     * delete API data in given organization
     * @param organization Organization Id
     */
    @MethodStats
    @Override
    public LinkedHashMap<String, String> purge(String organization) {
        List<APIIdentifier> apiIdentifierList = new ArrayList<>();
        boolean isAPIOrganizationExist = true;
        for (Map.Entry<String, String> task : apiPurgeTaskMap.entrySet()) {
            int count = 0;
            int maxTries = 3;
            while (true) {
                try {
                    switch (task.getKey()) {
                    case APIConstants.OrganizationDeletion.API_ORG_EXIST:
                        isAPIOrganizationExist = organizationPurgeDAO.apiOrganizationExist(organization);
                        break;
                    case APIConstants.OrganizationDeletion.API_RETRIEVER:
                        apiIdentifierList = organizationPurgeDAO.getAPIIdList(organization);
                        break;
                    case APIConstants.OrganizationDeletion.API_DB_DATA_REMOVER:
                        organizationPurgeDAO.deleteOrganizationAPIList(organization);
                        break;
                    case APIConstants.OrganizationDeletion.ARTIFACT_SERVER_DATA_REMOVER:
                        removeArtifactsFromArtifactServer(apiIdentifierList, organization);
                        break;
                    case APIConstants.OrganizationDeletion.GW_ARTIFACT_DATA_REMOVER:
                        gatewayArtifactsMgtDAO.removeOrganizationGatewayArtifacts(organization);
                        break;
                    case APIConstants.OrganizationDeletion.API_ARTIFACT_DATA_REMOVER:
                        removeAllOrganizationAPIArtifacts(organization);
                        break;
                    }
                    apiPurgeTaskMap.put(task.getKey(), APIConstants.OrganizationDeletion.COMPLETED);
                    break;
                } catch (APIManagementException e) {
                    log.error("Error while deleting API Data in organization " + organization, e);
                    apiPurgeTaskMap.put(task.getKey(), APIConstants.OrganizationDeletion.FAIL);
                    log.info("Re-trying to execute " + task.getKey() + " process for organization" + organization, e);

                    if (++count == maxTries) {
                        log.error("Cannot execute " + task.getKey() + " process for organization" + organization, e);
                        String errorMessage = e.getMessage();
                        if (e.getCause() != null) {
                            errorMessage = errorMessage + ". Cause: " + e.getCause().getMessage();
                        }
                        apiPurgeTaskMap.put(task.getKey(), errorMessage);
                        break;
                    }
                }
            }
            if (!isAPIOrganizationExist) {
                String msg = "No api related entities exist for the organization: " + organization;
                log.warn(msg);
                apiPurgeTaskMap.put(task.getKey(), APIConstants.OrganizationDeletion.COMPLETED);
                moveStatusToCompleted();
                break;
            }
        }

        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.ORGANIZATION, new Gson().toJson(apiPurgeTaskMap),
                APIConstants.AuditLogConstants.DELETED, OrganizationPurgeConstants.ORG_CLEANUP_EXECUTOR);
        return apiPurgeTaskMap;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    private void removeAllOrganizationAPIArtifacts(String orgId) throws APIManagementException {
        try {
            apiPersistenceInstance.deleteAllAPIs(new Organization(orgId));
        } catch (APIPersistenceException e) {
            handleException("Failed to delete all api artifacts of organization " + orgId + "from artifact Store", e);
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

    private void moveStatusToCompleted() {
        apiPurgeTaskMap.put(APIConstants.OrganizationDeletion.API_RETRIEVER, APIConstants.OrganizationDeletion.COMPLETED);
        apiPurgeTaskMap.put(APIConstants.OrganizationDeletion.API_DB_DATA_REMOVER,
                APIConstants.OrganizationDeletion.COMPLETED);
        apiPurgeTaskMap.put(APIConstants.OrganizationDeletion.ARTIFACT_SERVER_DATA_REMOVER,
                APIConstants.OrganizationDeletion.COMPLETED);
        apiPurgeTaskMap.put(APIConstants.OrganizationDeletion.GW_ARTIFACT_DATA_REMOVER,
                APIConstants.OrganizationDeletion.COMPLETED);
        apiPurgeTaskMap.put(APIConstants.OrganizationDeletion.API_ARTIFACT_DATA_REMOVER,
                APIConstants.OrganizationDeletion.COMPLETED);
    }
}
