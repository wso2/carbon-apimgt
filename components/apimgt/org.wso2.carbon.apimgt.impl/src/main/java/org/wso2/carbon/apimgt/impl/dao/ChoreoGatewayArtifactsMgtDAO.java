/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dao;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.GatewayArtifactsMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.VHostUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * DAO for Choreo gateway artifacts.
 * **/
public class ChoreoGatewayArtifactsMgtDAO {

    private static final Log log = LogFactory.getLog(ChoreoGatewayArtifactsMgtDAO.class);
    private static ChoreoGatewayArtifactsMgtDAO choreoGatewayArtifactsMgtDAO = null;

    /**
     * Private constructor
     */
    private ChoreoGatewayArtifactsMgtDAO() {

    }

    /**
     * Method to get the instance of the GatewayArtifactsMgtDAO.
     *
     * @return {@link ChoreoGatewayArtifactsMgtDAO} instance
     */
    public static ChoreoGatewayArtifactsMgtDAO getInstance() {

        if (choreoGatewayArtifactsMgtDAO == null) {
            choreoGatewayArtifactsMgtDAO = new ChoreoGatewayArtifactsMgtDAO();
        }
        return choreoGatewayArtifactsMgtDAO;
    }

    public List<APIRuntimeArtifactDto> retrieveAllGatewayArtifactsByOrganizationAndDataPlaneId(
            ArrayList<String> organizations, String dataPlaneId) throws APIManagementException {

        String query = SQLConstants.RETRIEVE_ALL_ARTIFACTS_BY_ORGANIZATION_AND_DATA_PLANE_ID;
        query = query.replaceAll(SQLConstants.ORGANIZATION_IDS_REGEX, String.join(",",
                Collections.nCopies(organizations.size(), "?")));
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, dataPlaneId);
            int index = 2;
            for (String organization: organizations) {
                preparedStatement.setString(index, organization);
                index++;
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String apiId = resultSet.getString("API_ID");
                    String label = resultSet.getString("LABEL");
                    try {
                        APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                        apiRuntimeArtifactDto.setApiId(apiId);
                        String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                                resultSet.getString("VHOST"));
                        apiRuntimeArtifactDto.setLabel(label);
                        apiRuntimeArtifactDto.setVhost(resolvedVhost);
                        apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                        apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                        apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                        apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                        apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                        apiRuntimeArtifactDto.setContext(resultSet.getString("CONTEXT"));
                        InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                        if (artifact != null) {
                            byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                            try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                                apiRuntimeArtifactDto.setArtifact(newArtifact);
                            }
                        }
                        apiRuntimeArtifactDto.setFile(true);
                        apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                    } catch (APIManagementException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_APIM_ERROR,
                                apiId, label, e.getMessage()).toString());
                    } catch (IOException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_IO_ERROR,
                                apiId, label, e.getMessage()).toString());
                    } catch (SQLException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_SQL_ERROR,
                                apiId, label, e.getMessage()).toString());
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Failed to retrieve Gateway Artifact for DataPlaneId : "
                    + StringUtils.join(",", dataPlaneId), e);
        }
        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveAllGatewayArtifactsByOrganizationAndDataPlaneId(
            ArrayList<String> organizations, String dataPlaneId, String gatewayAccessibilityType) throws APIManagementException {

        String query = SQLConstants.RETRIEVE_ALL_ARTIFACTS_BY_ORGANIZATION_GATEWAY_ACCESSIBILITY_TYPE_AND_DATA_PLANE_ID;
        query = query.replaceAll(SQLConstants.ORGANIZATION_IDS_REGEX, String.join(",",
                Collections.nCopies(organizations.size(), "?")));
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, dataPlaneId);
            int index = 2;
            for (String organization: organizations) {
                preparedStatement.setString(index, organization);
                index++;
            }
            preparedStatement.setString(index, gatewayAccessibilityType);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String apiId = resultSet.getString("API_ID");
                    String label = resultSet.getString("LABEL");
                    try {
                        APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                        apiRuntimeArtifactDto.setApiId(apiId);
                        String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                                resultSet.getString("VHOST"));
                        apiRuntimeArtifactDto.setLabel(label);
                        apiRuntimeArtifactDto.setVhost(resolvedVhost);
                        apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                        apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                        apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                        apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                        apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                        apiRuntimeArtifactDto.setContext(resultSet.getString("CONTEXT"));
                        InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                        if (artifact != null) {
                            byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                            try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                                apiRuntimeArtifactDto.setArtifact(newArtifact);
                            }
                        }
                        apiRuntimeArtifactDto.setFile(true);
                        apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                    } catch (APIManagementException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_APIM_ERROR,
                                apiId, label, e.getMessage()).toString());
                    } catch (IOException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_IO_ERROR,
                                apiId, label, e.getMessage()).toString());
                    } catch (SQLException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_SQL_ERROR,
                                apiId, label, e.getMessage()).toString());
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Failed to retrieve Gateway Artifact for DataPlaneId : "
                    + StringUtils.join(",", dataPlaneId), e);
        }
        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveAllGatewayArtifactsByDataPlaneId(String dataPlaneId)
            throws APIManagementException {

        String query = SQLConstants.RETRIEVE_ALL_ARTIFACTS_BY_DATA_PLANE_ID;
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, dataPlaneId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String apiId = resultSet.getString("API_ID");
                    String label = resultSet.getString("LABEL");
                    try {
                        APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                        apiRuntimeArtifactDto.setApiId(apiId);
                        String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                                resultSet.getString("VHOST"));
                        apiRuntimeArtifactDto.setLabel(label);
                        apiRuntimeArtifactDto.setVhost(resolvedVhost);
                        apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                        apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                        apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                        apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                        apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                        apiRuntimeArtifactDto.setContext(resultSet.getString("CONTEXT"));
                        InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                        if (artifact != null) {
                            byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                            try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                                apiRuntimeArtifactDto.setArtifact(newArtifact);
                            }
                        }
                        apiRuntimeArtifactDto.setFile(true);
                        apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                    } catch (APIManagementException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_APIM_ERROR,
                                apiId, label, e.getMessage()).toString());
                    } catch (IOException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_IO_ERROR,
                                apiId, label, e.getMessage()).toString());
                    } catch (SQLException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_SQL_ERROR,
                                apiId, label, e.getMessage()).toString());
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Failed to retrieve Gateway Artifact for DataPlaneId : "
                    + StringUtils.join(",", dataPlaneId), e);
        }
        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveAllGatewayArtifactsByDataPlaneId(String dataPlaneId,
                                                                                String gatewayAccessibilityType)
            throws APIManagementException {

        String query = SQLConstants.RETRIEVE_ALL_ARTIFACTS_BY_DATA_PLANE_ID_AND_GATEWAY_ACCESSIBILITY_TYPE;
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, dataPlaneId);
            preparedStatement.setString(2, gatewayAccessibilityType);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String apiId = resultSet.getString("API_ID");
                    String label = resultSet.getString("LABEL");
                    try {
                        APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                        apiRuntimeArtifactDto.setApiId(apiId);
                        String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                                resultSet.getString("VHOST"));
                        apiRuntimeArtifactDto.setLabel(label);
                        apiRuntimeArtifactDto.setVhost(resolvedVhost);
                        apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                        apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                        apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                        apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                        apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                        apiRuntimeArtifactDto.setContext(resultSet.getString("CONTEXT"));
                        InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                        if (artifact != null) {
                            byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                            try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                                apiRuntimeArtifactDto.setArtifact(newArtifact);
                            }
                        }
                        apiRuntimeArtifactDto.setFile(true);
                        apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                    } catch (APIManagementException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_APIM_ERROR,
                                apiId, label, e.getMessage()).toString());
                    } catch (IOException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_IO_ERROR,
                                apiId, label, e.getMessage()).toString());
                    } catch (SQLException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_SQL_ERROR,
                                apiId, label, e.getMessage()).toString());
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Failed to retrieve Gateway Artifact for DataPlaneId : "
                    + StringUtils.join(",", dataPlaneId), e);
        }
        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveAllGatewayArtifactsByDataPlaneId(String[] dataPlaneIds)
            throws APIManagementException {

        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try {
            Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();

            // Retrieving artifacts for dynamic environments
            PreparedStatement preparedStatement = connection.prepareStatement(
                    SQLConstants.RETRIEVE_ALL_ARTIFACTS_BY_DATA_PLANE_IDS.replaceAll(SQLConstants.DATA_PLANE_IDS_REGEX,
                            String.join(",", Collections.nCopies(dataPlaneIds.length, "?"))));
            int index = 1;
            for (String dataPlaneId : dataPlaneIds) {
                preparedStatement.setString(index, dataPlaneId);
                index++;
            }
            ResultSet resultSetForDynamicEnvironments = preparedStatement.executeQuery();
            while (resultSetForDynamicEnvironments.next()) {
                String apiId = resultSetForDynamicEnvironments.getString("API_ID");
                String label = resultSetForDynamicEnvironments.getString("LABEL");
                String dataPlaneId = resultSetForDynamicEnvironments.getString("DATA_PLANE_ID");
                try {
                    APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                    apiRuntimeArtifactDto.setApiId(apiId);
                    String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                            resultSetForDynamicEnvironments.getString("VHOST"));
                    apiRuntimeArtifactDto.setLabel(label);
                    apiRuntimeArtifactDto.setVhost(resolvedVhost);
                    apiRuntimeArtifactDto.setName(resultSetForDynamicEnvironments.getString("API_NAME"));
                    apiRuntimeArtifactDto.setVersion(resultSetForDynamicEnvironments.getString("API_VERSION"));
                    apiRuntimeArtifactDto.setProvider(resultSetForDynamicEnvironments.getString("API_PROVIDER"));
                    apiRuntimeArtifactDto.setRevision(resultSetForDynamicEnvironments.getString("REVISION_ID"));
                    apiRuntimeArtifactDto.setType(resultSetForDynamicEnvironments.getString("API_TYPE"));
                    apiRuntimeArtifactDto.setContext(resultSetForDynamicEnvironments.getString("CONTEXT"));
                    InputStream artifact = resultSetForDynamicEnvironments.getBinaryStream("ARTIFACT");
                    if (artifact != null) {
                        byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                        try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                            apiRuntimeArtifactDto.setArtifact(newArtifact);
                        }
                    }
                    apiRuntimeArtifactDto.setFile(true);
                    apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                } catch (APIManagementException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Error resolving vhost while retrieving runtime artifact for API %s, "
                            + "gateway environment \"%s\", dataPlaneId: %s." +
                            "Skipping runtime artifact for the API.", apiId, label, dataPlaneId), e);
                } catch (IOException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Error occurred retrieving input stream from byte array of " +
                            "API: %s, gateway environment \"%s\", dataPlaneId: %s.", apiId, label, dataPlaneId), e);
                } catch (SQLException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Failed to retrieve Gateway Artifact of API: %s, " +
                            "gateway environment \"%s\", dataPlaneId: %s.", apiId, label, dataPlaneId), e);
                }
            }

            // Retrieving artifacts for read only environments
            Map<String, Environment> readOnlyEnvironments = APIUtil.getReadOnlyEnvironments();
            List<String> gatewayLabelsForReadOnlyEnvironments = new ArrayList<>();
            for (Map.Entry<String, Environment> entry : readOnlyEnvironments.entrySet()) {
                Environment environment = entry.getValue();
                if (Arrays.stream(dataPlaneIds).anyMatch(environment.getDataPlaneId()::equalsIgnoreCase)) {
                    gatewayLabelsForReadOnlyEnvironments.add(environment.getName());
                }
            }
            if (gatewayLabelsForReadOnlyEnvironments.size() == 0) {
                return apiRuntimeArtifactDtoList;
            }
            PreparedStatement preparedStatementForReadOnlyEnvironments = connection.prepareStatement(
                    SQLConstants.RETRIEVE_ALL_ARTIFACTS_BY_LABEL.replaceAll(SQLConstants.GATEWAY_LABEL_REGEX,
                            String.join(",", Collections.nCopies(gatewayLabelsForReadOnlyEnvironments.size(), "?"))));
            index = 1;
            for (String gatewayLabel : gatewayLabelsForReadOnlyEnvironments) {
                preparedStatementForReadOnlyEnvironments.setString(index, gatewayLabel);
                index++;
            }
            ResultSet resultSetForReadOnlyEnvironments = preparedStatementForReadOnlyEnvironments.executeQuery();
            while (resultSetForReadOnlyEnvironments.next()) {
                String apiId = resultSetForReadOnlyEnvironments.getString("API_ID");
                String label = resultSetForReadOnlyEnvironments.getString("LABEL");
                try {
                    APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                    apiRuntimeArtifactDto.setApiId(apiId);
                    String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                            resultSetForReadOnlyEnvironments.getString("VHOST"));
                    apiRuntimeArtifactDto.setLabel(label);
                    apiRuntimeArtifactDto.setVhost(resolvedVhost);
                    apiRuntimeArtifactDto.setName(resultSetForReadOnlyEnvironments.getString("API_NAME"));
                    apiRuntimeArtifactDto.setVersion(resultSetForReadOnlyEnvironments.getString("API_VERSION"));
                    apiRuntimeArtifactDto.setProvider(resultSetForReadOnlyEnvironments.getString("API_PROVIDER"));
                    apiRuntimeArtifactDto.setRevision(resultSetForReadOnlyEnvironments.getString("REVISION_ID"));
                    apiRuntimeArtifactDto.setType(resultSetForReadOnlyEnvironments.getString("API_TYPE"));
                    apiRuntimeArtifactDto.setContext(resultSetForReadOnlyEnvironments.getString("CONTEXT"));
                    InputStream artifact = resultSetForReadOnlyEnvironments.getBinaryStream("ARTIFACT");
                    if (artifact != null) {
                        byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                        try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                            apiRuntimeArtifactDto.setArtifact(newArtifact);
                        }
                    }
                    apiRuntimeArtifactDto.setFile(true);
                    apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                } catch (APIManagementException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Error resolving vhost while retrieving runtime artifact for API %s, "
                            + "gateway environment \"%s\". Skipping runtime artifact for the API.", apiId, label), e);
                } catch (IOException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Error occurred retrieving input stream from byte array of " +
                            "API: %s, gateway environment \"%s\".", apiId, label), e);
                } catch (SQLException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Failed to retrieve Gateway Artifact of API: %s, " +
                            "gateway environment \"%s\".", apiId, label), e);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve Gateway Artifacts for data-planes : " +
                    StringUtils.join(",", dataPlaneIds), e);
        }

        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveAllGatewayArtifactsByDataPlaneAndAPIIDs(String dataPlaneId,
                                                                                       String gatewayAccessibilityType,
                                                                                       List<String> apiUuids)
            throws APIManagementException {

        Map<String, Environment> readOnlyEnvironments = APIUtil.getReadOnlyEnvironments();
        List<String> gatewayLabelsForReadOnlyEnvironments = new ArrayList<>();
        for (Map.Entry<String, Environment> entry : readOnlyEnvironments.entrySet()) {
            Environment environment = entry.getValue();
            if (environment.getDataPlaneId().equalsIgnoreCase(dataPlaneId) &&
                    environment.getAccessibilityType().equalsIgnoreCase(gatewayAccessibilityType)) {
                gatewayLabelsForReadOnlyEnvironments.add(environment.getName());
            }
        }

        // Logging the API ID List
        log.debug("Getting runtime artifacts for the API ID List: " + apiUuids.toString());
        // Split apiId list into smaller list of size 25
        List<List<String>> apiIdsChunks = new ArrayList<>();
        int apiIdListSize = apiUuids.size();
        int apiIdArrayIndex = 0;
        int apiIdsChunkSize = SQLConstants.API_ID_CHUNK_SIZE;
        while (apiIdArrayIndex < apiIdListSize) {
            apiIdsChunks.add(apiUuids.subList(apiIdArrayIndex, Math.min(apiIdArrayIndex + apiIdsChunkSize, apiIdListSize)));
            apiIdArrayIndex += apiIdsChunkSize;
        }

        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();

        // Retrieving artifacts for dynamic environments
        for (List<String> apiIdList: apiIdsChunks) {
            String query = SQLConstants.RETRIEVE_ALL_ARTIFACTS_BY_DATA_PLANE_ID_AND_API_UUIDS;
            query = query.replaceAll(SQLConstants.API_ID_REGEX, String.join(",",Collections.nCopies(apiIdList.size(), "?")));
            try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
                 PreparedStatement preparedStatementForDynamicEnvs = connection.prepareStatement(query)) {
                preparedStatementForDynamicEnvs.setString(1, dataPlaneId);
                preparedStatementForDynamicEnvs.setString(2, gatewayAccessibilityType);
                int index = 3;
                for (String apiId: apiIdList) {
                    preparedStatementForDynamicEnvs.setString(index, apiId);
                    index++;
                }
                try (ResultSet resultSet = preparedStatementForDynamicEnvs.executeQuery()) {
                    while (resultSet.next()) {
                        String apiId = resultSet.getString("API_ID");
                        String label = resultSet.getString("LABEL");
                        try {
                            APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                            apiRuntimeArtifactDto.setApiId(apiId);
                            String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                                    resultSet.getString("VHOST"));
                            apiRuntimeArtifactDto.setLabel(label);
                            apiRuntimeArtifactDto.setVhost(resolvedVhost);
                            apiRuntimeArtifactDto.setName(resultSet.getString("API_NAME"));
                            apiRuntimeArtifactDto.setVersion(resultSet.getString("API_VERSION"));
                            apiRuntimeArtifactDto.setProvider(resultSet.getString("API_PROVIDER"));
                            apiRuntimeArtifactDto.setRevision(resultSet.getString("REVISION_ID"));
                            apiRuntimeArtifactDto.setType(resultSet.getString("API_TYPE"));
                            apiRuntimeArtifactDto.setContext(resultSet.getString("CONTEXT"));
                            InputStream artifact = resultSet.getBinaryStream("ARTIFACT");
                            if (artifact != null) {
                                byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                                try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                                    apiRuntimeArtifactDto.setArtifact(newArtifact);
                                }
                            }
                            apiRuntimeArtifactDto.setFile(true);
                            apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                        } catch (APIManagementException e) {
                            // handle exception inside the loop and continue with other API artifacts
                            log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_APIM_ERROR,
                                    apiId, label, e.getMessage()).toString());
                        } catch (IOException e) {
                            // handle exception inside the loop and continue with other API artifacts
                            log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_IO_ERROR,
                                    apiId, label, e.getMessage()).toString());
                        } catch (SQLException e) {
                            // handle exception inside the loop and continue with other API artifacts
                            log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_SQL_ERROR,
                                    apiId, label, e.getMessage()).toString());
                        }
                    }
                }
                // Retrieving artifacts for read-only environments
                if (gatewayLabelsForReadOnlyEnvironments.size() == 0) {
                    continue;
                }
                PreparedStatement preparedStatementForReadOnlyEnvironments = connection.prepareStatement(
                        SQLConstants.RETRIEVE_ALL_ARTIFACTS_BY_MULTIPLE_APIIDs_AND_LABEL
                                .replaceAll(SQLConstants.API_ID_REGEX,
                                        String.join(",",
                                                Collections.nCopies(apiIdList.size(), "?")
                                        )
                                )
                                .replaceAll(SQLConstants.GATEWAY_LABEL_REGEX,
                                        String.join(",",
                                                Collections.nCopies(gatewayLabelsForReadOnlyEnvironments.size(), "?")
                                        )
                                )
                );
                index = 1;
                for (String apiId: apiIdList) {
                    preparedStatementForReadOnlyEnvironments.setString(index, apiId);
                    index++;
                }
                for (String gatewayLabel : gatewayLabelsForReadOnlyEnvironments) {
                    preparedStatementForReadOnlyEnvironments.setString(index, gatewayLabel);
                    index++;
                }
                ResultSet resultSetForReadOnlyEnvironments = preparedStatementForReadOnlyEnvironments.executeQuery();
                while (resultSetForReadOnlyEnvironments.next()) {
                    String apiId = resultSetForReadOnlyEnvironments.getString("API_ID");
                    String label = resultSetForReadOnlyEnvironments.getString("LABEL");
                    try {
                        APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                        apiRuntimeArtifactDto.setApiId(apiId);
                        String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                                resultSetForReadOnlyEnvironments.getString("VHOST"));
                        apiRuntimeArtifactDto.setLabel(label);
                        apiRuntimeArtifactDto.setVhost(resolvedVhost);
                        apiRuntimeArtifactDto.setName(resultSetForReadOnlyEnvironments.getString("API_NAME"));
                        apiRuntimeArtifactDto.setVersion(resultSetForReadOnlyEnvironments.getString("API_VERSION"));
                        apiRuntimeArtifactDto.setProvider(resultSetForReadOnlyEnvironments.getString("API_PROVIDER"));
                        apiRuntimeArtifactDto.setRevision(resultSetForReadOnlyEnvironments.getString("REVISION_ID"));
                        apiRuntimeArtifactDto.setType(resultSetForReadOnlyEnvironments.getString("API_TYPE"));
                        apiRuntimeArtifactDto.setContext(resultSetForReadOnlyEnvironments.getString("CONTEXT"));
                        InputStream artifact = resultSetForReadOnlyEnvironments.getBinaryStream("ARTIFACT");
                        if (artifact != null) {
                            byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                            try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                                apiRuntimeArtifactDto.setArtifact(newArtifact);
                            }
                        }
                        apiRuntimeArtifactDto.setFile(true);
                        apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                    } catch (APIManagementException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Error resolving vhost while retrieving runtime artifact for API %s, "
                                + "gateway environment \"%s\". Skipping runtime artifact for the API.", apiId, label), e);
                    } catch (IOException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Error occurred retrieving input stream from byte array of " +
                                "API: %s, gateway environment \"%s\".", apiId, label), e);
                    } catch (SQLException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Failed to retrieve Gateway Artifact of API: %s, " +
                                "gateway environment \"%s\".", apiId, label), e);
                    }
                }
            } catch (SQLException e) {
                throw new APIManagementException("Failed to retrieve Gateway Artifact for DataPlaneId : "
                        + StringUtils.join(",", dataPlaneId), e);
            }
        }

        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveGatewayArtifactsByDataPlaneAndAPIIDs(String organization,
                                                                                    String dataPlaneId,
                                                                                    String gatewayAccessibilityType,
                                                                                    List<String> apiUuids)
            throws APIManagementException {
        Map<String, Environment> readOnlyEnvironments = APIUtil.getReadOnlyEnvironments();
        List<String> gatewayLabelsForReadOnlyEnvironments = new ArrayList<>();
        for (Map.Entry<String, Environment> entry : readOnlyEnvironments.entrySet()) {
            Environment environment = entry.getValue();
            if (environment.getDataPlaneId().equalsIgnoreCase(dataPlaneId) &&
                    environment.getAccessibilityType().equalsIgnoreCase(gatewayAccessibilityType)) {
                gatewayLabelsForReadOnlyEnvironments.add(environment.getName());
            }
        }

        // Logging the API ID List
        log.debug("Getting runtime artifacts for the API ID List: " + apiUuids.toString());
        // Split apiId list into smaller list of size 25
        List<List<String>> apiIdsChunks = new ArrayList<>();
        int apiIdListSize = apiUuids.size();
        int apiIdArrayIndex = 0;
        int apiIdsChunkSize = SQLConstants.API_ID_CHUNK_SIZE;
        while (apiIdArrayIndex < apiIdListSize) {
            apiIdsChunks.add(apiUuids.subList(apiIdArrayIndex, Math.min(apiIdArrayIndex + apiIdsChunkSize, apiIdListSize)));
            apiIdArrayIndex += apiIdsChunkSize;
        }

        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();

        // Retrieving artifacts for dynamic environments
        for (List<String> apiIdList: apiIdsChunks) {
            String query = SQLConstants.RETRIEVE_ARTIFACTS_BY_DATA_PLANE_ID_AND_API_UUIDS;
            query = query.replaceAll(SQLConstants.API_ID_REGEX, String.join(",",Collections.nCopies(apiIdList.size(), "?")));
            try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
                 PreparedStatement preparedStatementForDynamicEnvs = connection.prepareStatement(query)) {
                preparedStatementForDynamicEnvs.setString(1, organization);
                preparedStatementForDynamicEnvs.setString(2, dataPlaneId);
                preparedStatementForDynamicEnvs.setString(3, gatewayAccessibilityType);
                int index = 4;
                for (String apiId: apiIdList) {
                    preparedStatementForDynamicEnvs.setString(index, apiId);
                    index++;
                }
                try (ResultSet resultSetForDynamicEnvs = preparedStatementForDynamicEnvs.executeQuery()) {
                    while (resultSetForDynamicEnvs.next()) {
                        String apiId = resultSetForDynamicEnvs.getString("API_ID");
                        String label = resultSetForDynamicEnvs.getString("LABEL");
                        try {
                            APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                            apiRuntimeArtifactDto.setApiId(apiId);
                            String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                                    resultSetForDynamicEnvs.getString("VHOST"));
                            apiRuntimeArtifactDto.setLabel(label);
                            apiRuntimeArtifactDto.setVhost(resolvedVhost);
                            apiRuntimeArtifactDto.setName(resultSetForDynamicEnvs.getString("API_NAME"));
                            apiRuntimeArtifactDto.setVersion(resultSetForDynamicEnvs.getString("API_VERSION"));
                            apiRuntimeArtifactDto.setProvider(resultSetForDynamicEnvs.getString("API_PROVIDER"));
                            apiRuntimeArtifactDto.setRevision(resultSetForDynamicEnvs.getString("REVISION_ID"));
                            apiRuntimeArtifactDto.setType(resultSetForDynamicEnvs.getString("API_TYPE"));
                            apiRuntimeArtifactDto.setContext(resultSetForDynamicEnvs.getString("CONTEXT"));
                            InputStream artifact = resultSetForDynamicEnvs.getBinaryStream("ARTIFACT");
                            if (artifact != null) {
                                byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                                try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                                    apiRuntimeArtifactDto.setArtifact(newArtifact);
                                }
                            }
                            apiRuntimeArtifactDto.setFile(true);
                            apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                        } catch (APIManagementException e) {
                            // handle exception inside the loop and continue with other API artifacts
                            log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_APIM_ERROR,
                                    apiId, label, e.getMessage()).toString());
                        } catch (IOException e) {
                            // handle exception inside the loop and continue with other API artifacts
                            log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_IO_ERROR,
                                    apiId, label, e.getMessage()).toString());
                        } catch (SQLException e) {
                            // handle exception inside the loop and continue with other API artifacts
                            log.error(ExceptionCodes.from(ExceptionCodes.CANNOT_RETRIEVE_RUNTIME_ARTIFACT_SQL_ERROR,
                                    apiId, label, e.getMessage()).toString());
                        }
                    }
                }
                // Retrieving artifacts for read-only environments
                if (gatewayLabelsForReadOnlyEnvironments.size() == 0) {
                    continue;
                }
                PreparedStatement preparedStatementForReadOnlyEnvironments = connection.prepareStatement(
                        SQLConstants.RETRIEVE_ARTIFACTS_BY_MULTIPLE_APIIDs_AND_LABEL
                                .replaceAll(SQLConstants.API_ID_REGEX,
                                        String.join(",",
                                                Collections.nCopies(apiIdList.size(), "?")
                                        )
                                )
                                .replaceAll(SQLConstants.GATEWAY_LABEL_REGEX,
                                        String.join(",",
                                                Collections.nCopies(gatewayLabelsForReadOnlyEnvironments.size(), "?")
                                        )
                                )
                );
                index = 1;
                for (String apiId: apiIdList) {
                    preparedStatementForReadOnlyEnvironments.setString(index, apiId);
                    index++;
                }
                for (String gatewayLabel : gatewayLabelsForReadOnlyEnvironments) {
                    preparedStatementForReadOnlyEnvironments.setString(index, gatewayLabel);
                    index++;
                }
                preparedStatementForReadOnlyEnvironments.setString(index, organization);
                ResultSet resultSetForReadOnlyEnvironments = preparedStatementForReadOnlyEnvironments.executeQuery();
                while (resultSetForReadOnlyEnvironments.next()) {
                    String apiId = resultSetForReadOnlyEnvironments.getString("API_ID");
                    String label = resultSetForReadOnlyEnvironments.getString("LABEL");
                    try {
                        APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                        apiRuntimeArtifactDto.setApiId(apiId);
                        String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                                resultSetForReadOnlyEnvironments.getString("VHOST"));
                        apiRuntimeArtifactDto.setLabel(label);
                        apiRuntimeArtifactDto.setVhost(resolvedVhost);
                        apiRuntimeArtifactDto.setName(resultSetForReadOnlyEnvironments.getString("API_NAME"));
                        apiRuntimeArtifactDto.setVersion(resultSetForReadOnlyEnvironments.getString("API_VERSION"));
                        apiRuntimeArtifactDto.setProvider(resultSetForReadOnlyEnvironments.getString("API_PROVIDER"));
                        apiRuntimeArtifactDto.setRevision(resultSetForReadOnlyEnvironments.getString("REVISION_ID"));
                        apiRuntimeArtifactDto.setType(resultSetForReadOnlyEnvironments.getString("API_TYPE"));
                        apiRuntimeArtifactDto.setContext(resultSetForReadOnlyEnvironments.getString("CONTEXT"));
                        InputStream artifact = resultSetForReadOnlyEnvironments.getBinaryStream("ARTIFACT");
                        if (artifact != null) {
                            byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                            try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                                apiRuntimeArtifactDto.setArtifact(newArtifact);
                            }
                        }
                        apiRuntimeArtifactDto.setFile(true);
                        apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                    } catch (APIManagementException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Error resolving vhost while retrieving runtime artifact for API %s, "
                                + "gateway environment \"%s\". Skipping runtime artifact for the API.", apiId, label), e);
                    } catch (IOException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Error occurred retrieving input stream from byte array of " +
                                "API: %s, gateway environment \"%s\".", apiId, label), e);
                    } catch (SQLException e) {
                        // handle exception inside the loop and continue with other API artifacts
                        log.error(String.format("Failed to retrieve Gateway Artifact of API: %s, " +
                                "gateway environment \"%s\".", apiId, label), e);
                    }
                }
            } catch (SQLException e) {
                throw new APIManagementException("Failed to retrieve Gateway Artifact for DataPlaneId : "
                        + StringUtils.join(",", dataPlaneId), e);
            }
        }

        return apiRuntimeArtifactDtoList;
    }
    public List<APIRuntimeArtifactDto> retrieveAllGatewayArtifactsByDataPlaneId(String[] dataPlaneIds,
                                                                                String gatewayAccessibilityType)
            throws APIManagementException {

        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try {
            Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();

            // Retrieving artifacts for dynamic environments
            PreparedStatement preparedStatement = connection.prepareStatement(
                    SQLConstants.RETRIEVE_ALL_ARTIFACTS_BY_DATA_PLANE_IDS_AND_GATEWAY_ACCESSIBILITY_TYPE
                            .replaceAll(SQLConstants.DATA_PLANE_IDS_REGEX,
                                    String.join(",", Collections.nCopies(dataPlaneIds.length, "?"))));
            int index = 1;
            for (String dataPlaneId : dataPlaneIds) {
                preparedStatement.setString(index, dataPlaneId);
                index++;
            }
            preparedStatement.setString(index, gatewayAccessibilityType);
            ResultSet resultSetForDynamicEnvironments = preparedStatement.executeQuery();
            while (resultSetForDynamicEnvironments.next()) {
                String apiId = resultSetForDynamicEnvironments.getString("API_ID");
                String label = resultSetForDynamicEnvironments.getString("LABEL");
                String dataPlaneId = resultSetForDynamicEnvironments.getString("DATA_PLANE_ID");
                try {
                    APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                    apiRuntimeArtifactDto.setApiId(apiId);
                    String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                            resultSetForDynamicEnvironments.getString("VHOST"));
                    apiRuntimeArtifactDto.setLabel(label);
                    apiRuntimeArtifactDto.setVhost(resolvedVhost);
                    apiRuntimeArtifactDto.setName(resultSetForDynamicEnvironments.getString("API_NAME"));
                    apiRuntimeArtifactDto.setVersion(resultSetForDynamicEnvironments.getString("API_VERSION"));
                    apiRuntimeArtifactDto.setProvider(resultSetForDynamicEnvironments.getString("API_PROVIDER"));
                    apiRuntimeArtifactDto.setRevision(resultSetForDynamicEnvironments.getString("REVISION_ID"));
                    apiRuntimeArtifactDto.setType(resultSetForDynamicEnvironments.getString("API_TYPE"));
                    apiRuntimeArtifactDto.setContext(resultSetForDynamicEnvironments.getString("CONTEXT"));
                    InputStream artifact = resultSetForDynamicEnvironments.getBinaryStream("ARTIFACT");
                    if (artifact != null) {
                        byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                        try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                            apiRuntimeArtifactDto.setArtifact(newArtifact);
                        }
                    }
                    apiRuntimeArtifactDto.setFile(true);
                    apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                } catch (APIManagementException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Error resolving vhost while retrieving runtime artifact for API %s, "
                            + "gateway environment \"%s\", dataPlaneId: %s." +
                            "Skipping runtime artifact for the API.", apiId, label, dataPlaneId), e);
                } catch (IOException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Error occurred retrieving input stream from byte array of " +
                            "API: %s, gateway environment \"%s\", dataPlaneId: %s.", apiId, label, dataPlaneId), e);
                } catch (SQLException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Failed to retrieve Gateway Artifact of API: %s, " +
                            "gateway environment \"%s\", dataPlaneId: %s.", apiId, label, dataPlaneId), e);
                }
            }

            // Retrieving artifacts for read only environments
            Map<String, Environment> readOnlyEnvironments = APIUtil.getReadOnlyEnvironments();
            List<String> gatewayLabelsForReadOnlyEnvironments = new ArrayList<>();
            for (Map.Entry<String, Environment> entry : readOnlyEnvironments.entrySet()) {
                Environment environment = entry.getValue();
                if (Arrays.stream(dataPlaneIds).anyMatch(environment.getDataPlaneId()::equalsIgnoreCase) &&
                        environment.getAccessibilityType().equalsIgnoreCase(gatewayAccessibilityType)) {
                    gatewayLabelsForReadOnlyEnvironments.add(environment.getName());
                }
            }
            if (gatewayLabelsForReadOnlyEnvironments.size() == 0) {
                return apiRuntimeArtifactDtoList;
            }
            PreparedStatement preparedStatementForReadOnlyEnvironments = connection.prepareStatement(
                    SQLConstants.RETRIEVE_ALL_ARTIFACTS_BY_LABEL.replaceAll(SQLConstants.GATEWAY_LABEL_REGEX,
                            String.join(",", Collections.nCopies(gatewayLabelsForReadOnlyEnvironments.size(), "?"))));
            index = 1;
            for (String gatewayLabel : gatewayLabelsForReadOnlyEnvironments) {
                preparedStatementForReadOnlyEnvironments.setString(index, gatewayLabel);
                index++;
            }
            ResultSet resultSetForReadOnlyEnvironments = preparedStatementForReadOnlyEnvironments.executeQuery();
            while (resultSetForReadOnlyEnvironments.next()) {
                String apiId = resultSetForReadOnlyEnvironments.getString("API_ID");
                String label = resultSetForReadOnlyEnvironments.getString("LABEL");
                try {
                    APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                    apiRuntimeArtifactDto.setApiId(apiId);
                    String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                            resultSetForReadOnlyEnvironments.getString("VHOST"));
                    apiRuntimeArtifactDto.setLabel(label);
                    apiRuntimeArtifactDto.setVhost(resolvedVhost);
                    apiRuntimeArtifactDto.setName(resultSetForReadOnlyEnvironments.getString("API_NAME"));
                    apiRuntimeArtifactDto.setVersion(resultSetForReadOnlyEnvironments.getString("API_VERSION"));
                    apiRuntimeArtifactDto.setProvider(resultSetForReadOnlyEnvironments.getString("API_PROVIDER"));
                    apiRuntimeArtifactDto.setRevision(resultSetForReadOnlyEnvironments.getString("REVISION_ID"));
                    apiRuntimeArtifactDto.setType(resultSetForReadOnlyEnvironments.getString("API_TYPE"));
                    apiRuntimeArtifactDto.setContext(resultSetForReadOnlyEnvironments.getString("CONTEXT"));
                    InputStream artifact = resultSetForReadOnlyEnvironments.getBinaryStream("ARTIFACT");
                    if (artifact != null) {
                        byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                        try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                            apiRuntimeArtifactDto.setArtifact(newArtifact);
                        }
                    }
                    apiRuntimeArtifactDto.setFile(true);
                    apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                } catch (APIManagementException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Error resolving vhost while retrieving runtime artifact for API %s, "
                            + "gateway environment \"%s\". Skipping runtime artifact for the API.", apiId, label), e);
                } catch (IOException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Error occurred retrieving input stream from byte array of " +
                            "API: %s, gateway environment \"%s\".", apiId, label), e);
                } catch (SQLException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Failed to retrieve Gateway Artifact of API: %s, " +
                            "gateway environment \"%s\".", apiId, label), e);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve Gateway Artifacts for data-planes : " +
                    StringUtils.join(",", dataPlaneIds), e);
        }

        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveAllGatewayArtifactsByDataPlaneId(String organization, String[] dataPlaneIds)
            throws APIManagementException {

        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try {
            Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();

            // Retrieving artifacts for dynamic environments
            PreparedStatement preparedStatement = connection.prepareStatement(
                    SQLConstants.RETRIEVE_ALL_ARTIFACTS_BY_ORGANIZATION_AND_DATA_PLANE_IDS.replaceAll(
                            SQLConstants.DATA_PLANE_IDS_REGEX,
                            String.join(",",Collections.nCopies(dataPlaneIds.length, "?"))));
            int index = 1;
            for (String dataPlaneId : dataPlaneIds) {
                preparedStatement.setString(index, dataPlaneId);
                index++;
            }
            preparedStatement.setString(index, organization);
            ResultSet resultSetForDynamicEnvironments = preparedStatement.executeQuery();
            while (resultSetForDynamicEnvironments.next()) {
                String apiId = resultSetForDynamicEnvironments.getString("API_ID");
                String label = resultSetForDynamicEnvironments.getString("LABEL");
                String dataPlaneId = resultSetForDynamicEnvironments.getString("DATA_PLANE_ID");
                try {
                    APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                    apiRuntimeArtifactDto.setApiId(apiId);
                    String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                            resultSetForDynamicEnvironments.getString("VHOST"));
                    apiRuntimeArtifactDto.setLabel(label);
                    apiRuntimeArtifactDto.setVhost(resolvedVhost);
                    apiRuntimeArtifactDto.setName(resultSetForDynamicEnvironments.getString("API_NAME"));
                    apiRuntimeArtifactDto.setVersion(resultSetForDynamicEnvironments.getString("API_VERSION"));
                    apiRuntimeArtifactDto.setProvider(resultSetForDynamicEnvironments.getString("API_PROVIDER"));
                    apiRuntimeArtifactDto.setRevision(resultSetForDynamicEnvironments.getString("REVISION_ID"));
                    apiRuntimeArtifactDto.setType(resultSetForDynamicEnvironments.getString("API_TYPE"));
                    apiRuntimeArtifactDto.setContext(resultSetForDynamicEnvironments.getString("CONTEXT"));
                    InputStream artifact = resultSetForDynamicEnvironments.getBinaryStream("ARTIFACT");
                    if (artifact != null) {
                        byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                        try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                            apiRuntimeArtifactDto.setArtifact(newArtifact);
                        }
                    }
                    apiRuntimeArtifactDto.setFile(true);
                    apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                } catch (APIManagementException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Error resolving vhost while retrieving runtime artifact for API %s, "
                            + "gateway environment \"%s\", dataPlaneId: %s." +
                            "Skipping runtime artifact for the API.", apiId, label, dataPlaneId), e);
                } catch (IOException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Error occurred retrieving input stream from byte array of " +
                            "API: %s, gateway environment \"%s\", dataPlaneId: %s.", apiId, label, dataPlaneId), e);
                } catch (SQLException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Failed to retrieve Gateway Artifact of API: %s, " +
                            "gateway environment \"%s\", dataPlaneId: %s.", apiId, label, dataPlaneId), e);
                }
            }

            // Retrieving artifacts for read only environments
            Map<String, Environment> readOnlyEnvironments = APIUtil.getReadOnlyEnvironments();
            List<String> gatewayLabelsForReadOnlyEnvironments = new ArrayList<>();
            for (Map.Entry<String, Environment> entry : readOnlyEnvironments.entrySet()) {
                Environment environment = entry.getValue();
                if (Arrays.stream(dataPlaneIds).anyMatch(environment.getDataPlaneId()::equalsIgnoreCase)) {
                    gatewayLabelsForReadOnlyEnvironments.add(environment.getName());
                }
            }
            if (gatewayLabelsForReadOnlyEnvironments.size() == 0) {
                return apiRuntimeArtifactDtoList;
            }
            PreparedStatement preparedStatementForReadOnlyEnvironments = connection.prepareStatement(
                    SQLConstants.RETRIEVE_ARTIFACTS_BY_LABEL.replaceAll(SQLConstants.GATEWAY_LABEL_REGEX,
                            String.join(",", Collections.nCopies(gatewayLabelsForReadOnlyEnvironments.size(), "?"))));
            index = 1;
            for (String gatewayLabel : gatewayLabelsForReadOnlyEnvironments) {
                preparedStatementForReadOnlyEnvironments.setString(index, gatewayLabel);
                index++;
            }
            preparedStatementForReadOnlyEnvironments.setString(index, organization);
            ResultSet resultSetForReadOnlyEnvironments = preparedStatementForReadOnlyEnvironments.executeQuery();
            while (resultSetForReadOnlyEnvironments.next()) {
                String apiId = resultSetForReadOnlyEnvironments.getString("API_ID");
                String label = resultSetForReadOnlyEnvironments.getString("LABEL");
                try {
                    APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                    apiRuntimeArtifactDto.setApiId(apiId);
                    String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                            resultSetForReadOnlyEnvironments.getString("VHOST"));
                    apiRuntimeArtifactDto.setLabel(label);
                    apiRuntimeArtifactDto.setVhost(resolvedVhost);
                    apiRuntimeArtifactDto.setName(resultSetForReadOnlyEnvironments.getString("API_NAME"));
                    apiRuntimeArtifactDto.setVersion(resultSetForReadOnlyEnvironments.getString("API_VERSION"));
                    apiRuntimeArtifactDto.setProvider(resultSetForReadOnlyEnvironments.getString("API_PROVIDER"));
                    apiRuntimeArtifactDto.setRevision(resultSetForReadOnlyEnvironments.getString("REVISION_ID"));
                    apiRuntimeArtifactDto.setType(resultSetForReadOnlyEnvironments.getString("API_TYPE"));
                    apiRuntimeArtifactDto.setContext(resultSetForReadOnlyEnvironments.getString("CONTEXT"));
                    InputStream artifact = resultSetForReadOnlyEnvironments.getBinaryStream("ARTIFACT");
                    if (artifact != null) {
                        byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                        try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                            apiRuntimeArtifactDto.setArtifact(newArtifact);
                        }
                    }
                    apiRuntimeArtifactDto.setFile(true);
                    apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                } catch (APIManagementException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Error resolving vhost while retrieving runtime artifact for API %s, "
                            + "gateway environment \"%s\". Skipping runtime artifact for the API.", apiId, label), e);
                } catch (IOException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Error occurred retrieving input stream from byte array of " +
                            "API: %s, gateway environment \"%s\".", apiId, label), e);
                } catch (SQLException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Failed to retrieve Gateway Artifact of API: %s, " +
                            "gateway environment \"%s\".", apiId, label), e);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve Gateway Artifacts for data-planes : " +
                    StringUtils.join(",", dataPlaneIds), e);
        }

        return apiRuntimeArtifactDtoList;
    }

    public List<APIRuntimeArtifactDto> retrieveAllGatewayArtifactsByDataPlaneId(String organization,
                                                                                String[] dataPlaneIds,
                                                                                String gatewayAccessibilityType)
            throws APIManagementException {

        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try {
            Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();

            // Retrieving artifacts for dynamic environments
            PreparedStatement preparedStatement = connection.prepareStatement(
                    SQLConstants.RETRIEVE_ALL_ARTIFACTS_BY_ORGANIZATION_DATA_PLANE_IDS_AND_GATEWAY_ACCESSIBILITY_TYPE
                            .replaceAll(
                                    SQLConstants.DATA_PLANE_IDS_REGEX,
                                    String.join(",",Collections.nCopies(dataPlaneIds.length, "?"))));
            int index = 1;
            for (String dataPlaneId : dataPlaneIds) {
                preparedStatement.setString(index, dataPlaneId);
                index++;
            }
            preparedStatement.setString(index, gatewayAccessibilityType);
            preparedStatement.setString(index + 1, organization);
            ResultSet resultSetForDynamicEnvironments = preparedStatement.executeQuery();
            while (resultSetForDynamicEnvironments.next()) {
                String apiId = resultSetForDynamicEnvironments.getString("API_ID");
                String label = resultSetForDynamicEnvironments.getString("LABEL");
                String dataPlaneId = resultSetForDynamicEnvironments.getString("DATA_PLANE_ID");
                try {
                    APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                    apiRuntimeArtifactDto.setApiId(apiId);
                    String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                            resultSetForDynamicEnvironments.getString("VHOST"));
                    apiRuntimeArtifactDto.setLabel(label);
                    apiRuntimeArtifactDto.setVhost(resolvedVhost);
                    apiRuntimeArtifactDto.setName(resultSetForDynamicEnvironments.getString("API_NAME"));
                    apiRuntimeArtifactDto.setVersion(resultSetForDynamicEnvironments.getString("API_VERSION"));
                    apiRuntimeArtifactDto.setProvider(resultSetForDynamicEnvironments.getString("API_PROVIDER"));
                    apiRuntimeArtifactDto.setRevision(resultSetForDynamicEnvironments.getString("REVISION_ID"));
                    apiRuntimeArtifactDto.setType(resultSetForDynamicEnvironments.getString("API_TYPE"));
                    apiRuntimeArtifactDto.setContext(resultSetForDynamicEnvironments.getString("CONTEXT"));
                    InputStream artifact = resultSetForDynamicEnvironments.getBinaryStream("ARTIFACT");
                    if (artifact != null) {
                        byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                        try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                            apiRuntimeArtifactDto.setArtifact(newArtifact);
                        }
                    }
                    apiRuntimeArtifactDto.setFile(true);
                    apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                } catch (APIManagementException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Error resolving vhost while retrieving runtime artifact for API %s, "
                            + "gateway environment \"%s\", dataPlaneId: %s." +
                            "Skipping runtime artifact for the API.", apiId, label, dataPlaneId), e);
                } catch (IOException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Error occurred retrieving input stream from byte array of " +
                            "API: %s, gateway environment \"%s\", dataPlaneId: %s.", apiId, label, dataPlaneId), e);
                } catch (SQLException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Failed to retrieve Gateway Artifact of API: %s, " +
                            "gateway environment \"%s\", dataPlaneId: %s.", apiId, label, dataPlaneId), e);
                }
            }

            // Retrieving artifacts for read only environments
            Map<String, Environment> readOnlyEnvironments = APIUtil.getReadOnlyEnvironments();
            List<String> gatewayLabelsForReadOnlyEnvironments = new ArrayList<>();
            for (Map.Entry<String, Environment> entry : readOnlyEnvironments.entrySet()) {
                Environment environment = entry.getValue();
                if (Arrays.stream(dataPlaneIds).anyMatch(environment.getDataPlaneId()::equalsIgnoreCase) &&
                        environment.getAccessibilityType().equalsIgnoreCase(gatewayAccessibilityType)) {
                    gatewayLabelsForReadOnlyEnvironments.add(environment.getName());
                }
            }
            if (gatewayLabelsForReadOnlyEnvironments.size() == 0) {
                return apiRuntimeArtifactDtoList;
            }
            PreparedStatement preparedStatementForReadOnlyEnvironments = connection.prepareStatement(
                    SQLConstants.RETRIEVE_ARTIFACTS_BY_LABEL.replaceAll(SQLConstants.GATEWAY_LABEL_REGEX,
                            String.join(",", Collections.nCopies(gatewayLabelsForReadOnlyEnvironments.size(), "?"))));
            index = 1;
            for (String gatewayLabel : gatewayLabelsForReadOnlyEnvironments) {
                preparedStatementForReadOnlyEnvironments.setString(index, gatewayLabel);
                index++;
            }
            preparedStatementForReadOnlyEnvironments.setString(index, organization);
            ResultSet resultSetForReadOnlyEnvironments = preparedStatementForReadOnlyEnvironments.executeQuery();
            while (resultSetForReadOnlyEnvironments.next()) {
                String apiId = resultSetForReadOnlyEnvironments.getString("API_ID");
                String label = resultSetForReadOnlyEnvironments.getString("LABEL");
                try {
                    APIRuntimeArtifactDto apiRuntimeArtifactDto = new APIRuntimeArtifactDto();
                    apiRuntimeArtifactDto.setApiId(apiId);
                    String resolvedVhost = VHostUtils.resolveIfNullToDefaultVhost(label,
                            resultSetForReadOnlyEnvironments.getString("VHOST"));
                    apiRuntimeArtifactDto.setLabel(label);
                    apiRuntimeArtifactDto.setVhost(resolvedVhost);
                    apiRuntimeArtifactDto.setName(resultSetForReadOnlyEnvironments.getString("API_NAME"));
                    apiRuntimeArtifactDto.setVersion(resultSetForReadOnlyEnvironments.getString("API_VERSION"));
                    apiRuntimeArtifactDto.setProvider(resultSetForReadOnlyEnvironments.getString("API_PROVIDER"));
                    apiRuntimeArtifactDto.setRevision(resultSetForReadOnlyEnvironments.getString("REVISION_ID"));
                    apiRuntimeArtifactDto.setType(resultSetForReadOnlyEnvironments.getString("API_TYPE"));
                    apiRuntimeArtifactDto.setContext(resultSetForReadOnlyEnvironments.getString("CONTEXT"));
                    InputStream artifact = resultSetForReadOnlyEnvironments.getBinaryStream("ARTIFACT");
                    if (artifact != null) {
                        byte[] artifactByte = APIMgtDBUtil.getBytesFromInputStream(artifact);
                        try (InputStream newArtifact = new ByteArrayInputStream(artifactByte)) {
                            apiRuntimeArtifactDto.setArtifact(newArtifact);
                        }
                    }
                    apiRuntimeArtifactDto.setFile(true);
                    apiRuntimeArtifactDtoList.add(apiRuntimeArtifactDto);
                } catch (APIManagementException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Error resolving vhost while retrieving runtime artifact for API %s, "
                            + "gateway environment \"%s\". Skipping runtime artifact for the API.", apiId, label), e);
                } catch (IOException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Error occurred retrieving input stream from byte array of " +
                            "API: %s, gateway environment \"%s\".", apiId, label), e);
                } catch (SQLException e) {
                    // handle exception inside the loop and continue with other API artifacts
                    log.error(String.format("Failed to retrieve Gateway Artifact of API: %s, " +
                            "gateway environment \"%s\".", apiId, label), e);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve Gateway Artifacts for data-planes : " +
                    StringUtils.join(",", dataPlaneIds), e);
        }

        return apiRuntimeArtifactDtoList;
    }

    /**
     * Common method to handle exceptions within DAO
     *
     * @param msg error message
     * @param t   throwable exception
     * @throws APIManagementException
     */
    private static void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }
}
