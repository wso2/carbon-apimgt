/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.store.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** This is the service implementation class for Store API related operations 
 *
 */
public class ApisApiServiceImpl extends ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);

    /** Retrieves APIs qualifying under given search condition 
     *
     * @param limit maximum number of APIs returns
     * @param offset starting index
     * @param query search condition
     * @param type value for the search condition
     * @param sort sort parameter
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @return matched APIs for the given search condition
     */
    @Override
    @SuppressWarnings("unchecked")
    public Response apisGet(Integer limit, Integer offset, String xWSO2Tenant, String query, String type,
            String sort, String accept, String ifNoneMatch) {
        Map<String, Object> apisMap;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);

            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                throw RestApiUtil.buildBadRequestException("Provided tenant domain " + xWSO2Tenant + " is invalid");
            }

            //if query parameter is not specified, This will search by name
            String searchType = "Name";
            String searchContent = "*";
            if (query != null) {
                String[] querySplit = query.split(":");
                if (querySplit.length == 2 && StringUtils.isNotBlank(querySplit[0]) && StringUtils
                        .isNotBlank(querySplit[1])) {
                    searchType = querySplit[0];
                    searchContent = querySplit[1];
                } else if (querySplit.length == 1) {
                    searchContent = query;
                } else {
                    throw RestApiUtil.buildBadRequestException("Provided query parameter " + query + " is invalid");
                }
            }

            apisMap = apiConsumer.searchPaginatedAPIs(searchContent, searchType, requestedTenantDomain, offset, limit, true);
            APIListDTO apiListDTO = new APIListDTO();
            Object apisResult = apisMap.get(APIConstants.API_DATA_APIS);
            int size = (int)apisMap.get(APIConstants.API_DATA_LENGTH);
            if (apisResult != null) {
                Set<API> apiSet = (Set)apisResult;
                apiListDTO = APIMappingUtil.fromAPISetToDTO(apiSet);
                APIMappingUtil.setPaginationParams(apiListDTO, query, type, offset, limit, size);
            }

            return Response.ok().entity(apiListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving APIs";
            handleException(errorMessage, e);
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            handleException(errorMessage, e);
        }
        return null;
    }

    /**
     * Get API of given ID
     *
     * @param apiId  API ID
     * @param accept accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return API of the given ID
     */
    @Override
    public Response apisApiIdGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince,
            String xWSO2Tenant) {
        APIDTO apiToReturn;
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();

            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                throw RestApiUtil.buildBadRequestException("Provided tenant domain " + xWSO2Tenant + " is invalid");
            }

            API api;
            if (RestApiUtil.isUUID(apiId)) {
                api = apiConsumer.getAPIbyUUID(apiId, requestedTenantDomain);
            } else {
                APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiId(apiId);
                api = apiConsumer.getAPI(apiIdentifier);
            }
            apiToReturn = APIMappingUtil.fromAPItoDTO(api);
            return Response.ok().entity(apiToReturn).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                throw RestApiUtil.buildForbiddenException(RestApiConstants.RESOURCE_API, apiId);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                handleException(errorMessage, e);
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            handleException(errorMessage, e);
        }
        return null;
    }

    /**
     *  Returns all the documents of the given API identifier that matches to the search condition
     *  
     * @param apiId API identifier
     * @param limit max number of records returned
     * @param offset starting index
     * @param query document search condition
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @return matched documents as a list if DocumentDTOs
     */
    @Override
    public Response apisApiIdDocumentsGet(String apiId, Integer limit, Integer offset, String xWSO2Tenant,
            String query, String accept, String ifNoneMatch) {

        //todo : implement document search. Search conditions can be found at store-api.yaml

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        if (query == null) {
            query = "";
        }
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);

            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                throw RestApiUtil.buildBadRequestException("Provided tenant domain " + xWSO2Tenant + " is invalid");
            }

            //this will fail if user doesn't have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, requestedTenantDomain);

            List<Documentation> documentationList = apiConsumer.getAllDocumentation(apiIdentifier, username);
            DocumentListDTO documentListDTO = DocumentationMappingUtil
                    .fromDocumentationListToDTO(documentationList, offset, limit);
            DocumentationMappingUtil
                    .setPaginationParams(documentListDTO, query, apiId, offset, limit, documentationList.size());
            return Response.ok().entity(documentListDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                throw RestApiUtil.buildForbiddenException(RestApiConstants.RESOURCE_API, apiId);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                handleException("Error while getting API " + apiId, e);
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            handleException(errorMessage, e);
        }
        return null;
    }

    /**
     * Returns a specific document by identifier that is belong to the given API identifier
     * 
     * @param apiId API identifier
     * @param documentId document identifer
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return returns the matched document
     */
    @Override
    public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String xWSO2Tenant,
            String accept, String ifNoneMatch, String ifModifiedSince) {
        Documentation documentation;
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);

            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                throw RestApiUtil.buildBadRequestException("Provided tenant domain " + xWSO2Tenant + " is invalid");
            }

            if (!RestAPIStoreUtils.isUserAccessAllowedForAPI(apiId, requestedTenantDomain)) {
                throw RestApiUtil.buildForbiddenException(RestApiConstants.RESOURCE_API, apiId);
            }

            documentation = apiConsumer.getDocumentation(documentId, requestedTenantDomain);
            if (null != documentation) {
                DocumentDTO documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
                return Response.ok().entity(documentDTO).build();
            } else {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_DOCUMENTATION, documentId);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                handleException("Error while getting API " + apiId, e);
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            handleException(errorMessage, e);
        }
        return null;
    }

    private void handleException(String msg, Throwable t) throws InternalServerErrorException {
        log.error(msg, t);
        throw new InternalServerErrorException(t);
    }

}
