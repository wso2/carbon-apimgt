/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.apk.apimgt.rest.api.backoffice.v1.common.impl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.api.APIDefinition;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIProvider;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.FaultGatewaysException;
import org.wso2.apk.apimgt.api.model.API;
import org.wso2.apk.apimgt.api.model.APIIdentifier;
import org.wso2.apk.apimgt.api.model.ResourcePath;
import org.wso2.apk.apimgt.api.model.SwaggerData;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.definitions.OAS3Parser;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.BackofficeAPIUtils;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.mappings.APIMappingUtil;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.mappings.PublisherCommonUtils;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.FileInfoDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.ResourcePathListDTO;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;

import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Util class for ApisApiService related operations
 */
public class ApisApiCommonImpl {

    public static final String MESSAGE = "message";
    public static final String ERROR_WHILE_UPDATING_API = "Error while updating API : ";

    private ApisApiCommonImpl() {

    }

    private static final Log log = LogFactory.getLog(ApisApiCommonImpl.class);

    public static String getAllAPIs(Integer limit, Integer offset, String sortBy, String sortOrder, String query,
                                    String organization) throws APIManagementException {

        List<API> allMatchedApis = new ArrayList<>();
        Object apiListDTO;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        sortBy = sortBy != null ? sortBy : RestApiConstants.DEFAULT_SORT_CRITERION;
        sortOrder = sortOrder != null ? sortOrder : RestApiConstants.DESCENDING_SORT_ORDER;

        //revert content search back to normal search by name to avoid doc result complexity and to comply with
        // REST api practices
        if (query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":")) {
            query = query
                    .replace(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":",
                            APIConstants.NAME_TYPE_PREFIX + ":");
        }

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        Map<String, Object> result;

        result = apiProvider.searchPaginatedAPIs(query, organization, offset, limit, sortBy, sortOrder);

        Set<API> apis = (Set<API>) result.get("apis");
        allMatchedApis.addAll(apis);

        apiListDTO = APIMappingUtil.fromAPIListToDTO(allMatchedApis);

        //Add pagination section in the response
        Object totalLength = result.get("length");
        int length = 0;
        if (totalLength != null) {
            length = (Integer) totalLength;
        }

        APIMappingUtil.setPaginationParams(apiListDTO, query, offset, limit, length);
        return BackofficeAPIUtils.getJsonFromDTO(apiListDTO);
    }

    public static String getAPI(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        return BackofficeAPIUtils.getJsonFromDTO(getAPIByID(apiId, apiProvider, organization));
    }

    public static String updateAPI(String apiId, String json, String[] tokenScopes, String organization)
            throws APIManagementException {

        APIDTO body = BackofficeAPIUtils.getDTOFromJson(json, APIDTO.class);

        String username = RestApiCommonUtil.getLoggedInUsername();

        //validate if api exists
        RestApiCommonUtil.validateAPIExistence(apiId);

        APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
        API originalAPI = apiProvider.getAPIbyUUID(apiId, organization);
        originalAPI.setOrganization(organization);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(originalAPI.getStatus(), tokenScopes);
        API updatedApi;
        try {
            updatedApi = PublisherCommonUtils.updateApi(originalAPI, body, apiProvider, tokenScopes);
        } catch (FaultGatewaysException e) {
            String errorMessage = ERROR_WHILE_UPDATING_API + apiId;
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
        return BackofficeAPIUtils.getJsonFromDTO(APIMappingUtil.fromAPItoDTO(updatedApi));
    }

    private static void validateAPIOperationsPerLC(String status, String[] tokenScopes) throws APIManagementException {

        boolean updatePermittedForPublishedDeprecated = false;

        for (String scope : tokenScopes) {
            if (RestApiConstants.PUBLISHER_SCOPE.equals(scope)
                    || RestApiConstants.API_IMPORT_EXPORT_SCOPE.equals(scope)
                    || RestApiConstants.API_MANAGE_SCOPE.equals(scope)
                    || RestApiConstants.ADMIN_SCOPE.equals(scope)) {
                updatePermittedForPublishedDeprecated = true;
                break;
            }
        }
        if (!updatePermittedForPublishedDeprecated && (
                APIConstants.PUBLISHED.equals(status)
                        || APIConstants.DEPRECATED.equals(status))) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.API_UPDATE_FORBIDDEN_PER_LC, status));
        }
    }

    public static String updateAPIThumbnail(String apiId, InputStream fileInputStream, String organization,
                                            String fileName, String fileDetailContentType)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String extension = FilenameUtils.getExtension(fileName);
        if (!RestApiConstants.ALLOWED_THUMBNAIL_EXTENSIONS.contains(extension.toLowerCase())) {
            String errorMessage = "Unsupported Thumbnail File Extension. Supported extensions are .jpg, .png, "
                    + ".jpeg, .svg, and .gif";
            throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }
        String fileContentType = URLConnection.guessContentTypeFromName(fileName);
        if (StringUtils.isBlank(fileContentType)) {
            fileContentType = fileDetailContentType;
        }
        PublisherCommonUtils.updateThumbnail(fileInputStream, fileContentType, apiProvider, apiId, organization);
        FileInfoDTO infoDTO = new FileInfoDTO();
        infoDTO.setMediaType(fileContentType);
        return BackofficeAPIUtils.getJsonFromDTO(infoDTO);
    }

    public static String getAPIThumbnail(String apiId, APIProvider apiProvider, String organization)
            throws APIManagementException {

        //this will fail if user does not have access to the API or the API does not exist
        RestApiCommonUtil.validateAPIExistence(apiId);
        return BackofficeAPIUtils.getJsonFromDTO(apiProvider.getIcon(apiId, organization));
    }

    public static String getAPIResourcePaths(String apiId, Integer limit, Integer offset)
            throws APIManagementException {

        RestApiCommonUtil.validateAPIExistence(apiId);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        List<ResourcePath> apiResourcePaths = apiProvider.getResourcePathsOfAPI(apiIdentifier);

        ResourcePathListDTO dto = APIMappingUtil.fromResourcePathListToDTO(apiResourcePaths, limit, offset);
        APIMappingUtil.setPaginationParamsForAPIResourcePathList(dto, offset, limit, apiResourcePaths.size());
        return BackofficeAPIUtils.getJsonFromDTO(dto);
    }

    /**
     * @param api API
     * @return API definition
     * @throws APIManagementException If any error occurred in generating API definition from swagger data
     */
    public static String getApiDefinition(API api) throws APIManagementException {

        APIDefinition parser = new OAS3Parser();
        SwaggerData swaggerData = new SwaggerData(api);
        return parser.generateAPIDefinition(swaggerData);
    }

    private static APIDTO getAPIByID(String apiId, APIProvider apiProvider, String organization)
            throws APIManagementException {

        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        return APIMappingUtil.fromAPItoDTO(api, apiProvider);
    }
}
