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

package org.wso2.carbon.apimgt.governance.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.ApiResult;
import org.wso2.carbon.apimgt.governance.api.ArtifactGovernanceHandler;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
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
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class implements the ArtifactGovernanceHandler interface to provide the governance capabilities for the APIs
 */
public class APIGovernanceHandler implements ArtifactGovernanceHandler {

    /**
     * This method is used to get all the apis of a given type in a given organization
     *
     * @param organization organization name
     * @return List of api ids
     * @throws APIMGovernanceException if an error occurs while getting the apis
     */
    @Override
    public List<String> getAllArtifacts(String organization) throws APIMGovernanceException {
        List<String> apiIds = new ArrayList<>();
        List<ApiResult> apis;
        try {
            apis = ApiMgtDAO.getInstance().getAllAPIs(organization);
            for (ApiResult api : apis) {
                apiIds.add(api.getId());
            }
            return apiIds;
        } catch (APIManagementException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_API_LIST, e, organization);
        }

    }

    /**
     * This method is used to get all the artifacts visible to a given user in a given organization
     *
     * @param username     username of logged-in user
     * @param organization organization name
     * @return List of artifact ids
     * @throws APIMGovernanceException if an error occurs while getting the artifacts
     */
    @Override
    public List<String> getAllArtifacts(String username, String organization) throws APIMGovernanceException {
        List<String> apiIds = new ArrayList<>();
        try {
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(username);
            List<API> apis = apiProvider.getAllAPIs();
            for (API api : apis) {
                apiIds.add(api.getUuid());
            }
            return apiIds;
        } catch (APIManagementException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_API_LIST, e, organization);
        }
    }

    /**
     * This method is used to get all the apis attached to a given label in a given organization
     *
     * @param label label id
     * @return List of api ids
     * @throws APIMGovernanceException if an error occurs while getting the apis
     */
    @Override
    public List<String> getArtifactsByLabel(String label) throws APIMGovernanceException {
        List<String> apiIds = new ArrayList<>();
        try {
            List<ApiResult> apis = LabelsDAO.getInstance().getMappedApisForLabel(label);

            for (ApiResult api : apis) {
                apiIds.add(api.getId());
            }
            return apiIds;

        } catch (APIManagementException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_APIS_FOR_LABEL, e, label);
        }
    }

    /**
     * This method is used to get all the labels attached to a given api in a given organization
     *
     * @param apiId api uuid
     * @return List of label ids
     * @throws APIMGovernanceException if an error occurs while getting the labels
     */
    @Override
    public List<String> getLabelsForArtifact(String apiId) throws APIMGovernanceException {
        try {
            return LabelsDAO.getInstance().getMappedLabelIDsForApi(apiId);
        } catch (APIManagementException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_LABELS_FOR_API, e, apiId);
        }
    }

    /**
     * This method checks whether an api is available in the given organization
     *
     * @param apiId        api uuid
     * @param organization organization name
     * @return true if the api is available, false otherwise
     * @throws APIMGovernanceException if an error occurs while checking the availability
     */
    @Override
    public boolean isArtifactAvailable(String apiId, String organization) throws APIMGovernanceException {
        try {
            APIIdentifier apiIdentifier = ApiMgtDAO.getInstance().getAPIIdentifierFromUUID(apiId);
            if (apiIdentifier != null) {
                return true;
            }
        } catch (APIManagementException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_CHECKING_API_AVAILABILITY, e,
                    apiId);
        }
        return false;
    }

    /**
     * This method checks whether an artifact is visible to a given user in the given organization
     *
     * @param artifactRefId artifact reference id (uuid on APIM side)
     * @param username      username of logged-in user
     * @param organization  organization name
     * @return true if the artifact is visible, false otherwise
     * @throws APIMGovernanceException if an error occurs while checking the visibility
     */
    @Override
    public boolean isArtifactVisibleToUser(String artifactRefId, String username,
                                           String organization) throws APIMGovernanceException {

        APIProvider apiProvider;
        try {
            apiProvider = APIManagerFactory.getInstance().getAPIProvider(username);
            API api = apiProvider.getAPIbyUUID(artifactRefId, organization);
            return api != null;
        } catch (APIManagementException e) {
            // Provider will throw unauthorized error if the user is not authorized to view the API.
            // Hence, catching the exception and returning false.
            if (ExceptionCodes.UN_AUTHORIZED_TO_VIEW_MODIFY_API.getErrorCode() ==
                    (e.getErrorHandler().getErrorCode())) {
                return false;
            }
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_CHECKING_API_VISIBILITY, e,
                    artifactRefId);
        }
    }

    /**
     * Given a list of governable states, this method checks whether the api is governable considering
     * apis current state on the APIM side
     *
     * @param apiId            api uuid
     * @param governableStates list of governable states
     * @return true if the api is governable, false otherwise
     * @throws APIMGovernanceException if an error occurs while checking the governability
     */
    @Override
    public boolean isArtifactGovernable(String apiId, List<APIMGovernableState> governableStates)
            throws APIMGovernanceException {

        String lcStatus = getAPIStatus(apiId);
        boolean isDeployed = isAPIDeployed(apiId);

        // If API is in any state we need to run created and update policies
        boolean isGovernable = governableStates.contains(APIMGovernableState.API_CREATE)
                || governableStates.contains(APIMGovernableState.API_UPDATE);

        // If the API is deployed, we need to run deploy policies
        if (isDeployed) {
            isGovernable |= governableStates.contains(APIMGovernableState.API_DEPLOY);
        }

        // If the API is in published, deprecated or blocked state, we need to run publish policies
        if (APIStatus.PUBLISHED.equals(APIStatus.valueOf(lcStatus)) ||
                APIStatus.DEPRECATED.equals(APIStatus.valueOf(lcStatus)) ||
                APIStatus.BLOCKED.equals(APIStatus.valueOf(lcStatus))) {
            isGovernable |= governableStates.contains(APIMGovernableState.API_PUBLISH);
        }

        return isGovernable;
    }


    /**
     * Get the lifecycle status of an API
     *
     * @param apiId API ID
     * @return API status
     * @throws APIMGovernanceException If an error occurs while getting the status of the API
     */
    private String getAPIStatus(String apiId) throws APIMGovernanceException {

        try {
            return ApiMgtDAO.getInstance().getAPIStatusFromAPIUUID(apiId);
        } catch (APIManagementException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_LC_STATUS_OF_API, e,
                    apiId);
        }
    }

    /**
     * Check if the API is deployed
     *
     * @param apiId API ID
     * @return True if the API is deployed
     */
    private boolean isAPIDeployed(String apiId) throws APIMGovernanceException {
        try {
            List<APIRevisionDeployment> deployedAPIRevisionList =
                    ApiMgtDAO.getInstance().getAPIRevisionDeploymentByApiUUID(apiId);
            if (deployedAPIRevisionList != null && !deployedAPIRevisionList.isEmpty()) {
                return true;
            }
        } catch (APIManagementException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes
                    .ERROR_WHILE_CHECKING_API_DEPLOYMENT_STATUS, e, apiId);
        }
        return false;
    }

    /**
     * This method checks whether the api is governable considering the apis type
     * For now, we do not support governance for SOAP and GraphQL APIs. Hence, we return false for those types.
     *
     * @param apiId api uuid
     * @return true if the api is governable, false otherwise
     * @throws APIMGovernanceException if an error occurs while checking the governability
     */
    @Override
    public boolean isArtifactGovernable(String apiId) throws APIMGovernanceException {
        ExtendedArtifactType extendedArtifactType = getExtendedArtifactType(apiId);
        return extendedArtifactType != null;
    }

    /**
     * This method is used to get the name of an api
     *
     * @param apiId        api uuid
     * @param organization organization name
     * @return name of the api
     * @throws APIMGovernanceException if an error occurs while getting the name
     */
    @Override
    public String getName(String apiId, String organization) throws APIMGovernanceException {
        try {
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            return apiIdentifier.getApiName();
        } catch (APIManagementException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_API_INFO, e,
                    apiId);
        }
    }

    /**
     * This method is used to get the version of an api
     *
     * @param apiId        api uuid
     * @param organization organization name
     * @return version of the api
     * @throws APIMGovernanceException if an error occurs while getting the version
     */
    @Override
    public String getVersion(String apiId, String organization) throws APIMGovernanceException {
        try {
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            return apiIdentifier.getVersion();
        } catch (APIManagementException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_API_INFO, e,
                    apiId);
        }
    }

    /**
     * This method is used to get the owner of an api
     *
     * @param apiId        api uuid
     * @param organization organization name
     * @return owner of the api
     * @throws APIMGovernanceException if an error occurs while getting the owner
     */
    @Override
    public String getOwner(String apiId, String organization) throws APIMGovernanceException {
        try {
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            APIProvider apiProvider = APIManagerFactory.getInstance()
                    .getAPIProvider(apiIdentifier.getProviderName());
            API api = apiProvider.getAPIbyUUID(apiId, organization);
            String techOwner = api.getTechnicalOwnerEmail();
            String apiOwner = api.getApiOwner();
            return techOwner != null ? techOwner : apiOwner;
        } catch (APIManagementException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_API_INFO, e,
                    apiId);
        }
    }

    /**
     * This method is used to convert the apis type on APIM side to the ExtendedArtifactType
     * for the operations in the governance side
     *
     * @param apiId api uuid
     * @return ExtendedArtifactType
     * @throws APIMGovernanceException if an error occurs while getting the extended api type
     */
    @Override
    public ExtendedArtifactType getExtendedArtifactType(String apiId) throws APIMGovernanceException {
        String apiType;
        try {
            apiType = ApiMgtDAO.getInstance().getAPITypeFromUUID(apiId);
        } catch (APIManagementException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_API_TYPE, e, apiId);
        }

        return getExtendedArtifactTypeFromAPIType(apiType);
    }

    /**
     * This method is used to convert the apis type on APIM side to the ExtendedArtifactType
     *
     * @param apiType api type
     * @return ExtendedArtifactType
     */
    private ExtendedArtifactType getExtendedArtifactTypeFromAPIType(String apiType) {
        switch (apiType.toUpperCase(Locale.ENGLISH)) {
            case "REST":
            case "HTTP":
                return ExtendedArtifactType.REST_API;
            case "WS":
            case "SSE":
            case "WEBSUB":
            case "WEBHOOK":
            case "ASYNC":
                return ExtendedArtifactType.ASYNC_API;
            default:
                return null;
        }
    }

    /**
     * This method is used to get the api project zip
     *
     * @param apiId        api uuid
     * @param revisionId   revision number of the api
     * @param organization organization name
     * @return api content
     * @throws APIMGovernanceException if an error occurs while getting the api zip
     */
    @Override
    public byte[] getArtifactProject(String apiId, String revisionId, String organization)
            throws APIMGovernanceException {
        synchronized (apiId.intern()) {
            try {
                APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
                String userName = apiIdentifier.getProviderName();
                APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(userName);
                if (revisionId != null) {
                    apiId = revisionId;
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
                throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_APIM_PROJECT, e,
                        apiId);
            }
        }
    }

    /**
     * This method is used to extract the api project in to a map of RuleType and the content for governance operations
     *
     * @param apiProject api project zip
     * @return Map of RuleType and the content
     * @throws APIMGovernanceException if an error occurs while extracting the api project
     */
    @Override
    public Map<RuleType, String> extractArtifactProject(byte[] apiProject) throws APIMGovernanceException {
        Map<RuleType, String> apiProjectContentMap = new HashMap<>();

        String apiMetadata = extractAPIMetadata(apiProject);
        ExtendedArtifactType extendedArtifactType = getExtendedArtifactTypeFromAPIMetadata(apiMetadata);
        String apiDefinition = extractAPIDefinition(apiProject, extendedArtifactType);
        String docData = extractDocData(apiProject);

        if (apiMetadata == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.API_DETAILS_NOT_FOUND);
        } else {
            apiProjectContentMap.put(RuleType.API_METADATA, apiMetadata);
        }
        if (apiDefinition == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.API_DEFINITION_NOT_FOUND);
        } else {
            apiProjectContentMap.put(RuleType.API_DEFINITION, apiDefinition);
        }
        if (docData == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.API_DOCUMENT_DATA_NOT_FOUND);
        } else {
            apiProjectContentMap.put(RuleType.API_DOCUMENTATION, docData);
        }

        return apiProjectContentMap;
    }

    /**
     * Extracts API metadata from the project ZIP file.
     *
     * @param apiProjectZip Byte array representing the API project ZIP file.
     * @return The extracted API metadata as a string.
     * @throws APIMGovernanceException if an error occurs while extracting metadata content.
     */
    public static String extractAPIMetadata(byte[] apiProjectZip) throws APIMGovernanceException {
        String apiMetadata;
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(apiProjectZip))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().contains(APIMGovernanceConstants.API_FILE_NAME)) {
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
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_EXTRACTING_API_METADATA, e);
        }
        return null; // Return null if no matching metadata is found
    }

    /**
     * This method is used to get the extended artifact type from the API metadata
     *
     * @param apiMetadata API metadata
     * @return ExtendedArtifactType
     * @throws APIMGovernanceException if an error occurs while getting the extended artifact type
     */
    private ExtendedArtifactType getExtendedArtifactTypeFromAPIMetadata(String apiMetadata)
            throws APIMGovernanceException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        JsonNode rootNode;
        try {
            rootNode = yamlMapper.readTree(apiMetadata);
            JsonNode dataNode = rootNode.path("data"); // Get the 'data' node
            if (dataNode != null && dataNode.has("type")) {
                String type = dataNode.path("type").asText();
                return getExtendedArtifactTypeFromAPIType(type);
            }
        } catch (JsonProcessingException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_API_TYPE_FROM_PROJECT, e);
        }
        return null;
    }


    /**
     * Extracts API definition from the project ZIP file.
     *
     * @param apiProjectZip        Byte array representing the API project ZIP file.
     * @param extendedArtifactType Extended artifact type of the API
     * @return The extracted API definition as a string.
     * @throws APIMGovernanceException if an error occurs while extracting swagger content.
     */
    public static String extractAPIDefinition(byte[] apiProjectZip, ExtendedArtifactType extendedArtifactType)
            throws APIMGovernanceException {
        String rootFolder = APIMGovernanceConstants.DEFINITIONS_FOLDER;
        String swaggerPath = rootFolder + APIMGovernanceConstants.SWAGGER_FILE_NAME;
        String asyncAPIPath = rootFolder + APIMGovernanceConstants.ASYNC_API_FILE_NAME;
        String defContent;

        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(apiProjectZip))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if ((entry.getName().contains(swaggerPath)
                        && ExtendedArtifactType.REST_API.equals(extendedArtifactType))
                        || (entry.getName().contains(asyncAPIPath) &&
                        ExtendedArtifactType.ASYNC_API.equals(extendedArtifactType))) {
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
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_EXTRACTING_API_DEFINITION, e);
        }
        return null; // Return null if no matching swagger content is found
    }

    /**
     * Extracts the document data from the API project ZIP file.
     *
     * @param apiProjectZip API project ZIP file as a byte array
     * @return Document data as a YAML string
     * @throws APIMGovernanceException If an error occurs while extracting the document data
     */
    public static String extractDocData(byte[] apiProjectZip) throws APIMGovernanceException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

        String rootFolder = APIMGovernanceConstants.DOCS_FOLDER + File.separator;
        String docMetadataFile = APIMGovernanceConstants.DOC_META_DATA_FILE_NAME;
        List<Object> docsList = new ArrayList<>();
        int count = 0;

        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(apiProjectZip))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().contains(rootFolder) && entry.getName().endsWith(docMetadataFile)) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zipInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                    }
                    String yamlContent = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
                    Object parsedYamlContent = yamlMapper.readTree(yamlContent);
                    if (parsedYamlContent != null) {
                        count++;
                        docsList.add(parsedYamlContent);
                    }
                }
            }
            // Create the final YAML structure with a root element "docs", "count"
            HashMap<String, Object> root = new HashMap<>();
            root.put("count", count);
            if (docsList.size() > 0) {
                root.put("docs", docsList);
            }
            return yamlMapper.writeValueAsString(root);
        } catch (IOException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_EXTRACTING_DOC_DATA, e);
        }
    }

    /**
     * This method is used to get the extended artifact type from the artifact project
     *
     * @param artifactProject artifact project zip
     * @return ExtendedArtifactType
     * @throws APIMGovernanceException if an error occurs while getting the extended artifact type
     */
    @Override
    public ExtendedArtifactType getExtendedArtifactTypeFromProject(byte[] artifactProject)
            throws APIMGovernanceException {
        String apiMetadata = extractAPIMetadata(artifactProject);
        return getExtendedArtifactTypeFromAPIMetadata(apiMetadata);
    }
}
