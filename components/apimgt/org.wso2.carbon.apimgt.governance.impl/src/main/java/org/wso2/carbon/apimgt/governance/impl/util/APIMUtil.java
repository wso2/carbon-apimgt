/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.impl.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.ApiResult;
import org.wso2.carbon.apimgt.api.model.DeployedAPIRevision;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.impl.GovernanceConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.LabelsDAO;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ExportUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class represents the API Manager utility for governance
 */
public class APIMUtil {

    private static final Log log = LogFactory.getLog(APIMUtil.class);

    /**
     * Check if the API exists
     *
     * @param apiId API ID
     * @return True if the API exists
     */
    public static boolean isAPIExist(String apiId) {
        try {
            APIIdentifier apiIdentifier = ApiMgtDAO.getInstance().getAPIIdentifierFromUUID(apiId);
            if (apiIdentifier != null) {
                return true;
            }
        } catch (APIManagementException e) {
            log.error("Error while checking the existence of the API with ID: " + apiId, e);
        }
        return false;
    }

    /**
     * Get the API name combined with the version
     *
     * @param apiId API ID
     * @return API name combined with the version
     * @throws GovernanceException If an error occurs while getting the API name and version
     */
    public static String getAPINameCombinedWithVersion(String apiId) throws GovernanceException {
        try {
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            return apiIdentifier.getApiName() + " " + apiIdentifier.getVersion();
        } catch (APIManagementException e) {
            throw new GovernanceException("Error while getting the API name and version with ID: " + apiId, e);
        }
    }

    /**
     * Get the status of the API
     *
     * @param apiId API ID
     * @return API status
     * @throws GovernanceException If an error occurs while getting the status of the API
     */
    public static String getAPIStatus(String apiId) throws GovernanceException {
        try {
            return ApiMgtDAO.getInstance().getAPIStatusFromAPIUUID(apiId);
        } catch (APIManagementException e) {
            throw new GovernanceException("Error while getting the status of the API with ID: " + apiId, e);
        }
    }

    /**
     * Check if the API is deployed
     *
     * @param apiId API ID
     * @return True if the API is deployed
     */
    public static boolean isAPIDeployed(String apiId) {
        try {
            List<DeployedAPIRevision> deployedAPIRevisionList =
                    ApiMgtDAO.getInstance().getDeployedAPIRevisionByApiUUID(apiId);
            if (deployedAPIRevisionList != null && !deployedAPIRevisionList.isEmpty()) {
                return true;
            }
        } catch (APIManagementException e) {
            log.error("Error while checking the deployment status of the API with ID: " + apiId, e);
        }
        return false;
    }


    /**
     * Get the API project
     *
     * @param apiId        API ID
     * @param organization Organization
     * @return API project zip as a byte array
     * @throws GovernanceException If an error occurs while getting the API project
     */
    public static byte[] getAPIProject(String apiId, String organization) throws GovernanceException {
        synchronized (apiId.intern()) {
            try {
                APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
                String userName = apiIdentifier.getProviderName();
                APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(userName);

                API api = apiProvider.getAPIbyUUID(apiId, organization);
                api.setUuid(apiId);
                apiIdentifier.setUuid(apiId);
                APIDTO apiDtoToReturn = APIMappingUtil.fromAPItoDTO(api, true, apiProvider);
                File apiProject = ExportUtils.exportApi(
                        apiProvider, apiIdentifier, apiDtoToReturn, api, userName,
                        ExportFormat.YAML, true, true, StringUtils.EMPTY, organization
                ); // returns zip file
                return Files.readAllBytes(apiProject.toPath());
            } catch (APIManagementException | APIImportExportException | IOException e) {
                throw new GovernanceException("Error while getting the API project with ID: " + apiId, e);
            }
        }
    }


    /**
     * Get the corresponding API statuses for governable states
     * That is the states on APIM side can be mapped to the states on the governance side
     *
     * @param governableStates List of governable states
     * @return List of corresponding API statuses
     */
    public static List<String> getCorrespondingAPIStatusesForGovernableStates(List<GovernableState> governableStates) {
        List<String> apiStatuses = new ArrayList<>();
        for (GovernableState governableState : governableStates) {
            switch (governableState) {
                case API_CREATE:
                    apiStatuses.add(String.valueOf(APIStatus.CREATED));
                    break;
                case API_UPDATE:
                case API_DEPLOY:
                    apiStatuses.add(String.valueOf(APIStatus.CREATED));
                    apiStatuses.add(String.valueOf(APIStatus.PROTOTYPED));
                    break;
                case API_PUBLISH:
                    apiStatuses.add(String.valueOf(APIStatus.CREATED));
                    apiStatuses.add(String.valueOf(APIStatus.PUBLISHED));
                    apiStatuses.add(String.valueOf(APIStatus.PROTOTYPED));
                    apiStatuses.add(String.valueOf(APIStatus.DEPRECATED));
                    apiStatuses.add(String.valueOf(APIStatus.BLOCKED));
                    break;
                default:
                    break;
            }
        }
        return apiStatuses;
    }

    /**
     * Extracts and maps API project content from a ZIP file.
     *
     * @param apiProjectZip Byte array representing the API project ZIP file.
     * @param apiId         The ID of the API.
     * @return A map of API project contents.
     * @throws GovernanceException if errors occur while extracting content.
     */
    public static Map<RuleType, String> extractAPIProjectContent(byte[] apiProjectZip, String apiId)
            throws GovernanceException {
        Map<RuleType, String> apiProjectContentMap = new HashMap<>();

        apiProjectContentMap.put(RuleType.API_METADATA, extractAPIMetadata(apiProjectZip, apiId));
        apiProjectContentMap.put(RuleType.API_DEFINITION, extractAPIDefinition(apiProjectZip, apiId));

        return apiProjectContentMap;
    }

    /**
     * Extracts API metadata from the project ZIP file.
     *
     * @param apiProjectZip Byte array representing the API project ZIP file.
     * @param apiId         The ID of the API.
     * @return The extracted API metadata as a string.
     * @throws GovernanceException if an error occurs while extracting metadata content.
     */
    public static String extractAPIMetadata(byte[] apiProjectZip, String apiId) throws GovernanceException {
        String apiMetadata;
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(apiProjectZip))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().contains(GovernanceConstants.API_FILE_NAME)) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zipInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                    }
                    apiMetadata = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
                    return apiMetadata;
                }
            }
        } catch (IOException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_EXTRACTING_API_METADATA, apiId);
        }
        return null; // Return null if no matching metadata is found
    }


    /**
     * Extracts API definition from the project ZIP file.
     *
     * @param apiProjectZip Byte array representing the API project ZIP file.
     * @param apiId         The ID of the API.
     * @return The extracted API definition as a string.
     * @throws GovernanceException if an error occurs while extracting swagger content.
     */
    public static String extractAPIDefinition(byte[] apiProjectZip, String apiId) throws GovernanceException {
        String swaggerContent;
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(apiProjectZip))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().contains(GovernanceConstants.DEFINITIONS_FOLDER +
                        GovernanceConstants.SWAGGER_FILE_NAME)) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zipInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                    }
                    swaggerContent = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
                    return swaggerContent;
                }
            }
        } catch (IOException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_EXTRACTING_API_DEFINITION, apiId);
        }
        return null; // Return null if no matching swagger content is found
    }

    /**
     * Get the labels for the API
     *
     * @param apiId API ID
     * @return List of labels IDs
     * @throws GovernanceException If an error occurs while getting the labels for the API
     */
    public static List<String> getLabelIDsForAPI(String apiId) throws GovernanceException {
        try {
            return LabelsDAO.getInstance().getMappedLabelIDsForApi(apiId);
        } catch (APIManagementException e) {
            throw new GovernanceException("Error while getting the labels for the API with ID: " + apiId, e);
        }
    }

    /**
     * Get the APIs for the label
     *
     * @param labelId Label ID
     * @return List of API IDs
     * @throws GovernanceException If an error occurs while getting the APIs for the label
     */
    public static List<String> getAPIsByLabel(String labelId) throws GovernanceException {
        try {
            List<ApiResult> apiResults = LabelsDAO.getInstance().getMappedApisForLabel(labelId);

            return apiResults.stream()
                    .map(ApiResult::getId)
                    .collect(Collectors.toList());
        } catch (APIManagementException e) {
            throw new GovernanceException("Error while getting the APIs for the label with ID: " + labelId, e);
        }
    }

    /**
     * Get all APIs for the organization
     *
     * @param organization Organization
     * @return List of API IDs
     */
    public static List<String> getAllAPIs(String organization) {

        List<String> apiIds = new ArrayList<>();
        List<org.wso2.carbon.apimgt.api.model.subscription.API> apis =
                new SubscriptionValidationDAO().getAllApis(organization, false);
        for (org.wso2.carbon.apimgt.api.model.subscription.API api : apis) {
            apiIds.add(api.getApiUUID());
        }
        return apiIds;
    }

    /**
     * Get all APIs for the organization with pagination
     *
     * @param organization Organization
     * @param limit        Limit
     * @param offset       Offset
     * @return List of API IDs
     */
    public static List<String> getPaginatedAPIs(String organization, int limit, int offset) {
        List<String> apiIds = getAllAPIs(organization);
        return apiIds.subList(offset, Math.min(offset + limit, apiIds.size()));
    }

    /**
     * Get the API type
     *
     * @param apiId API ID
     * @return API type
     * @throws GovernanceException If an error occurs while getting the API type
     */
    public static String getAPIType(String apiId) throws GovernanceException {
        try {
            return ApiMgtDAO.getInstance().getAPITypeFromUUID(apiId);
        } catch (APIManagementException e) {
            throw new GovernanceException("Error while getting the API type for the API with ID: " + apiId, e);
        }
    }

}
