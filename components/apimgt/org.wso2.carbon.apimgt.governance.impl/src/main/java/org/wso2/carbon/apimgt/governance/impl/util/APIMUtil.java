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
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.impl.GovernanceConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.LabelsDAO;
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
     * Get the API name
     *
     * @param apiId API ID
     * @return API name
     * @throws GovernanceException If an error occurs while getting the API name and version
     */
    public static String getAPIName(String apiId) throws GovernanceException {

        try {
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            return apiIdentifier.getApiName();
        } catch (APIManagementException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_API_INFO,
                    apiId, e);
        }
    }

    /**
     * Get the API version
     *
     * @param apiId API ID
     * @return API version
     * @throws GovernanceException If an error occurs while getting the API name and version
     */
    public static String getAPIVersion(String apiId) throws GovernanceException {

        try {
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            return apiIdentifier.getVersion();
        } catch (APIManagementException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_API_INFO,
                    apiId, e);
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
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_LC_STATUS_OF_API,
                    apiId, e);
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
     * @param revisionNo   Revision number, if empty latest revision will be exported
     * @param organization Organization
     * @return API project zip as a byte array
     * @throws GovernanceException If an error occurs while getting the API project
     */
    public static byte[] getAPIProject(String apiId, String revisionNo, String organization)
            throws GovernanceException {
        synchronized (apiId.intern()) {
            try {
                APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
                String userName = apiIdentifier.getProviderName();
                APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(userName);
                if (revisionNo != null) {
                    apiId = apiProvider.getAPIRevisionUUID(revisionNo, apiId);
                }

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
                throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_APIM_PROJECT,
                        apiId, e);
            }
        }
    }

    /**
     * Check if the API is governable based on the status, deployment status and governable states
     *
     * @param status           API status
     * @param isDeployed       API deployment status
     * @param governableStates List of governable states
     * @return True if the API is governable
     */
    public static boolean isAPIGovernable(String status, boolean isDeployed, List<GovernableState> governableStates) {

        // If API is in any state we need to run created and update policies
        boolean isGovernable = governableStates.contains(GovernableState.API_CREATE)
                || governableStates.contains(GovernableState.API_UPDATE);

        // If the API is deployed, we need to run deploy policies
        if (isDeployed) {
            isGovernable |= governableStates.contains(GovernableState.API_DEPLOY);
        }

        // If the API is in published, deprecated or blocked state, we need to run publish policies
        if (APIStatus.PUBLISHED.equals(APIStatus.valueOf(status)) ||
                APIStatus.DEPRECATED.equals(APIStatus.valueOf(status)) ||
                APIStatus.BLOCKED.equals(APIStatus.valueOf(status))) {
            isGovernable |= governableStates.contains(GovernableState.API_PUBLISH);
        }

        return isGovernable;
    }

    /**
     * Extracts and maps API project content from a ZIP file.
     *
     * @param apiProjectZip Byte array representing the API project ZIP file.
     * @return A map of API project contents.
     * @throws GovernanceException if errors occur while extracting content.
     */
    public static Map<RuleType, String> extractAPIProjectContent(byte[] apiProjectZip)
            throws GovernanceException {

        Map<RuleType, String> apiProjectContentMap = new HashMap<>();

        String apiMetadata = extractAPIMetadata(apiProjectZip);
        String apiDefinition = extractAPIDefinition(apiProjectZip);

        if (apiMetadata == null) {
            throw new GovernanceException(GovernanceExceptionCodes.API_DETAILS_NOT_FOUND);
        } else {
            apiProjectContentMap.put(RuleType.API_METADATA, apiMetadata);
        }
        if (apiDefinition == null) {
            throw new GovernanceException(GovernanceExceptionCodes.API_DEFINITION_NOT_FOUND);
        } else {
            apiProjectContentMap.put(RuleType.API_DEFINITION, apiDefinition);
        }

        return apiProjectContentMap;
    }

    /**
     * Extracts API metadata from the project ZIP file.
     *
     * @param apiProjectZip Byte array representing the API project ZIP file.
     * @return The extracted API metadata as a string.
     * @throws GovernanceException if an error occurs while extracting metadata content.
     */
    public static String extractAPIMetadata(byte[] apiProjectZip) throws GovernanceException {
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
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_EXTRACTING_API_METADATA);
        }
        return null; // Return null if no matching metadata is found
    }


    /**
     * Extracts API definition from the project ZIP file.
     *
     * @param apiProjectZip Byte array representing the API project ZIP file.
     * @return The extracted API definition as a string.
     * @throws GovernanceException if an error occurs while extracting swagger content.
     */
    public static String extractAPIDefinition(byte[] apiProjectZip)
            throws GovernanceException {
        String defContent;
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
                    defContent = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
                    return defContent;
                } else if (entry.getName().contains(GovernanceConstants.DEFINITIONS_FOLDER +
                        GovernanceConstants.ASYNC_API_FILE_NAME)) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zipInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                    }
                    defContent = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
                    return defContent;
                }
            }
        } catch (IOException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_EXTRACTING_API_DEFINITION);
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
    public static List<String> getLabelsForAPI(String apiId) throws GovernanceException {
        try {
            return LabelsDAO.getInstance().getMappedLabelIDsForApi(apiId);
        } catch (APIManagementException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_LABELS_FOR_API, apiId, e);
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
        List<String> apiIds = new ArrayList<>();
        try {
            List<ApiResult> apis = LabelsDAO.getInstance().getMappedApisForLabel(labelId);

            for (ApiResult api : apis) {
                apiIds.add(api.getId());
            }
            return apiIds;

        } catch (APIManagementException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_APIS_FOR_LABEL, labelId, e);
        }
    }

    /**
     * Get all APIs for the organization
     *
     * @param organization Organization
     * @return List of API IDs
     * @throws GovernanceException If an error occurs while getting the APIs for the organization
     */
    public static List<String> getAllAPIs(String organization) throws GovernanceException {

        List<String> apiIds = new ArrayList<>();
        List<ApiResult> apis;
        try {
            apis = ApiMgtDAO.getInstance().getAllAPIs(organization);
            for (ApiResult api : apis) {
                apiIds.add(api.getId());
            }
            return apiIds;
        } catch (APIManagementException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_API_LIST, organization, e);
        }

    }

    /**
     * Get the API UUID
     *
     * @param apiName      API name
     * @param apiVersion   API version
     * @param organization Organization
     * @return API UUID
     * @throws GovernanceException
     */
    public static String getApiUUID(String apiName, String apiVersion, String organization)
            throws GovernanceException {

        try {
            ApiMgtDAO apimgtDAO = ApiMgtDAO.getInstance();
            String apiProvider = apimgtDAO.getAPIProviderByNameAndOrganization(apiName, organization);
            APIIdentifier apiIdentifier = new APIIdentifier(apiProvider, apiName, apiVersion);
            return apimgtDAO.getUUIDFromIdentifier(apiIdentifier);
        } catch (APIManagementException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_API_UUID_WITH_NAME_VERSION,
                    apiName, apiVersion, e);
        }
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
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_GETTING_API_TYPE, apiId, e);
        }
    }


    /**
     * Convert from API Manager api type to governance api type
     * TODO: We need to complete this list to match API Type to ExtendedArtifactType
     *
     * @param apiType API type
     * @return ExtendedArtifactType API type
     */
    public static ExtendedArtifactType getExtendedArtifactTypeForAPI(String apiType) {
        if ("rest".equalsIgnoreCase(apiType) || "http".equalsIgnoreCase(apiType)) {
            return ExtendedArtifactType.REST_API;
        } else if ("soap".equalsIgnoreCase(apiType)) {
            return ExtendedArtifactType.SOAP_API;
        } else if ("graphql".equalsIgnoreCase(apiType)) {
            return ExtendedArtifactType.GRAPHQL_API;
        } else if ("async".equalsIgnoreCase(apiType) || "ws".equalsIgnoreCase(apiType)) {
            return ExtendedArtifactType.ASYNC_API;
        }
        return null;
    }

}
