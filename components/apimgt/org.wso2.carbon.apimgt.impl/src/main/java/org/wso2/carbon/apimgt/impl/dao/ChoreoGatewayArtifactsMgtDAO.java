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
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
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
import java.util.List;

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

    private void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    public List<APIRuntimeArtifactDto> retrieveAllGatewayArtifactsByOrganizationAndDataPlaneId(String organization,
                                                                                               String dataPlaneId)
            throws APIManagementException {

        String query = SQLConstants.RETRIEVE_ALL_ARTIFACTS_BY_ORGANIZATION_AND_DATA_PLANE_ID;
        List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList = new ArrayList<>();
        try (Connection connection = GatewayArtifactsMgtDBUtil.getArtifactSynchronizerConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, dataPlaneId);
            preparedStatement.setString(2, organization);
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
                        log.error(String.format("Error resolving vhost while retrieving runtime artifact for API %s, "
                                + "gateway environment \"%s\"." +
                                "Skipping runtime artifact for the API.", apiId, label), e);
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
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve Gateway Artifact for DataPlaneId : "
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
                        log.error(String.format("Error resolving vhost while retrieving runtime artifact for API %s, "
                                + "gateway environment \"%s\"." +
                                "Skipping runtime artifact for the API.", apiId, label), e);
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
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve Gateway Artifact for DataPlaneId : "
                    + StringUtils.join(",", dataPlaneId), e);
        }
        return apiRuntimeArtifactDtoList;
    }
}
