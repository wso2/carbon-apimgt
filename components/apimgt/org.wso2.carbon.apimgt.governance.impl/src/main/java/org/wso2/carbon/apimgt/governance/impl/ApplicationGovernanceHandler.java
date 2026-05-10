/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.governance.api.ArtifactGovernanceHandler;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements ArtifactGovernanceHandler for Devportal Applications.
 *
 * The synchronous governance gate (DevportalGovernanceValidationUtil) does not
 * call this handler — it evaluates Spectral directly against the request payload.
 * This handler provides the plumbing required by ArtifactGovernanceFactory so
 * ArtifactType.APPLICATION resolves to a concrete handler, enabling future
 * async compliance evaluation for applications.
 */
public class ApplicationGovernanceHandler implements ArtifactGovernanceHandler {

    private static final Log log = LogFactory.getLog(ApplicationGovernanceHandler.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Returns all application UUIDs for the given organization.
     * Currently returns an empty list: applications are governed synchronously at
     * request time via DevportalGovernanceValidationUtil; async compliance
     * scheduling is not yet enabled for applications.
     */
    @Override
    public List<String> getAllArtifacts(String organization) throws APIMGovernanceException {
        return new ArrayList<>();
    }

    /**
     * Returns all application UUIDs visible to the given user.
     * Currently returns an empty list (see {@link #getAllArtifacts(String)}).
     */
    @Override
    public List<String> getAllArtifacts(String username, String organization) throws APIMGovernanceException {
        return new ArrayList<>();
    }

    /**
     * Applications are not associated with governance labels.
     */
    @Override
    public List<String> getArtifactsByLabel(String label) throws APIMGovernanceException {
        return Collections.emptyList();
    }

    /**
     * Applications are not associated with governance labels.
     */
    @Override
    public List<String> getLabelsForArtifact(String artifactRefId) throws APIMGovernanceException {
        return Collections.emptyList();
    }

    @Override
    public boolean isArtifactAvailable(String artifactRefId, String organization) throws APIMGovernanceException {
        try {
            Application application = ApiMgtDAO.getInstance().getApplicationByUUID(artifactRefId);
            return application != null;
        } catch (APIManagementException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_CHECKING_APPLICATION_AVAILABILITY,
                    e, artifactRefId);
        }
    }

    @Override
    public boolean isArtifactVisibleToUser(String artifactRefId, String username,
                                           String organization) throws APIMGovernanceException {
        try {
            Application application = ApiMgtDAO.getInstance().getApplicationByUUID(artifactRefId);
            return application != null;
        } catch (APIManagementException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_CHECKING_APPLICATION_VISIBILITY,
                    e, artifactRefId, username);
        }
    }

    @Override
    public boolean isArtifactGovernable(String artifactRefId,
                                        List<APIMGovernableState> governableStates) throws APIMGovernanceException {
        return governableStates.contains(APIMGovernableState.APP_CREATE)
                || governableStates.contains(APIMGovernableState.APP_UPDATE);
    }

    @Override
    public boolean isArtifactGovernable(String artifactRefId) throws APIMGovernanceException {
        return true;
    }

    @Override
    public String getName(String artifactRefId, String organization) throws APIMGovernanceException {
        return getApplication(artifactRefId).getName();
    }

    /**
     * Applications do not have a version concept; returns an empty string.
     */
    @Override
    public String getVersion(String artifactRefId, String organization) throws APIMGovernanceException {
        return "";
    }

    @Override
    public String getOwner(String artifactRefId, String organization) throws APIMGovernanceException {
        return getApplication(artifactRefId).getOwner();
    }

    @Override
    public ExtendedArtifactType getExtendedArtifactType(String artifactRefId) throws APIMGovernanceException {
        return ExtendedArtifactType.APPLICATION;
    }

    /**
     * Returns the application serialized as UTF-8 JSON bytes.
     * The revisionId parameter is ignored: applications have no revision history.
     */
    @Override
    public byte[] getArtifactProject(String artifactRefId, String revisionId,
                                     String organization) throws APIMGovernanceException {
        Application application = getApplication(artifactRefId);
        try {
            Map<String, Object> appMap = new HashMap<>();
            appMap.put("applicationId", application.getUUID());
            appMap.put("name", application.getName());
            appMap.put("description", application.getDescription());
            appMap.put("throttlingPolicy", application.getTier());
            appMap.put("tokenType", application.getTokenType());
            appMap.put("owner", application.getOwner());
            appMap.put("organization", application.getOrganization());
            String json = OBJECT_MAPPER.writeValueAsString(appMap);
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_APPLICATION_INFO,
                    e, artifactRefId);
        }
    }

    /**
     * Wraps the raw application JSON bytes under {@link RuleType#APP_INFO} so the
     * Spectral engine can evaluate it with the corresponding ruleset type.
     */
    @Override
    public Map<RuleType, String> extractArtifactProject(byte[] artifactProject) throws APIMGovernanceException {
        Map<RuleType, String> contentMap = new HashMap<>();
        contentMap.put(RuleType.APP_INFO, new String(artifactProject, StandardCharsets.UTF_8));
        return contentMap;
    }

    @Override
    public ExtendedArtifactType getExtendedArtifactTypeFromProject(byte[] artifactProject)
            throws APIMGovernanceException {
        return ExtendedArtifactType.APPLICATION;
    }

    private Application getApplication(String uuid) throws APIMGovernanceException {
        try {
            Application application = ApiMgtDAO.getInstance().getApplicationByUUID(uuid);
            if (application == null) {
                throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_APPLICATION_INFO, uuid);
            }
            return application;
        } catch (APIManagementException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_APPLICATION_INFO, e, uuid);
        }
    }
}
