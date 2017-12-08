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
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListPaginationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
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
     * @param xWSO2Tenant requested tenant domain for cross tenant invocations
     * @param query search condition
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @return matched APIs for the given search condition
     */
    @Override
    @SuppressWarnings("unchecked")
    public Response apisGet(Integer limit, Integer offset, String xWSO2Tenant, String query, String accept,
            String ifNoneMatch) {
        Map<String, Object> apisMap;
        int size = 0;
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        query = query == null ? "" : query;

        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        APIListDTO apiListDTO = new APIListDTO();
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);

            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }

            //if query parameter is not specified, This will search by name
            String searchType = APIConstants.API_NAME;
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
                    RestApiUtil.handleBadRequest("Provided query parameter '" + query + "' is invalid", log);
                }
            }

            if (searchType.equalsIgnoreCase(APIConstants.API_STATUS) && 
                    searchContent.equalsIgnoreCase(APIConstants.PROTOTYPED)) {
                apisMap = apiConsumer.getAllPaginatedAPIsByStatus(requestedTenantDomain, offset, limit,
                        APIConstants.PROTOTYPED, false);
            } else {
                apisMap = apiConsumer
                        .searchPaginatedAPIs(searchContent, searchType, requestedTenantDomain, offset, limit, true);
            }

            Object apisResult = apisMap.get(APIConstants.API_DATA_APIS);
            //APIConstants.API_DATA_LENGTH is returned by executing searchPaginatedAPIs()
            if (apisMap.containsKey(APIConstants.API_DATA_LENGTH)) {
                size = (int) apisMap.get(APIConstants.API_DATA_LENGTH);
            //APIConstants.API_DATA_TOT_LENGTH is returned by executing getAllPaginatedAPIsByStatus()
            } else if (apisMap.containsKey(APIConstants.API_DATA_TOT_LENGTH)) {
                size = (int) apisMap.get(APIConstants.API_DATA_TOT_LENGTH);
            } else {
                log.warn("Size could not be determined from apis GET result for query " + query);
            }

            if (apisResult != null) {
                Set<API> apiSet = (Set)apisResult;
                apiListDTO = APIMappingUtil.fromAPISetToDTO(apiSet);
                APIMappingUtil.setPaginationParams(apiListDTO, query, offset, limit, size);

                APIListPaginationDTO paginationDTO = new APIListPaginationDTO();
                paginationDTO.setOffset(offset);
                paginationDTO.setLimit(limit);
                paginationDTO.setTotal(size);
                apiListDTO.setPagination(paginationDTO);
            }

            return Response.ok().entity(apiListDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.rootCauseMessageMatches(e, "start index seems to be greater than the limit count")) {
                //this is not an error of the user as he does not know the total number of apis available. Thus sends 
                //  an empty response
                apiListDTO.setCount(0);
                apiListDTO.setNext("");
                apiListDTO.setPrevious("");
                return Response.ok().entity(apiListDTO).build();
            } else {
                String errorMessage = "Error while retrieving APIs";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
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
     * @param xWSO2Tenant requested tenant domain for cross tenant invocations
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
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
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
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     *  Returns all the documents of the given API identifier that matches to the search condition
     *  
     * @param apiId API identifier
     * @param limit max number of records returned
     * @param offset starting index
     * @param xWSO2Tenant requested tenant domain for cross tenant invocations
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @return matched documents as a list if DocumentDTOs
     */
    @Override
    public Response apisApiIdDocumentsGet(String apiId, Integer limit, Integer offset, String xWSO2Tenant,
            String accept, String ifNoneMatch) {
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);

            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }

            //this will fail if user doesn't have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, requestedTenantDomain);

            List<Documentation> documentationList = apiConsumer.getAllDocumentation(apiIdentifier, username);
            DocumentListDTO documentListDTO = DocumentationMappingUtil
                    .fromDocumentationListToDTO(documentationList, offset, limit);
            DocumentationMappingUtil
                    .setPaginationParams(documentListDTO, apiId, offset, limit, documentationList.size());
            return Response.ok().entity(documentListDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while getting API " + apiId, e, log);
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Returns a specific document by identifier that is belong to the given API identifier
     * 
     * @param apiId API identifier
     * @param documentId document identifier
     * @param xWSO2Tenant requested tenant domain for cross tenant invocations
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
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }

            if (!RestAPIStoreUtils.isUserAccessAllowedForAPI(apiId, requestedTenantDomain)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, log);
            }

            documentation = apiConsumer.getDocumentation(documentId, requestedTenantDomain);
            if (null != documentation) {
                DocumentDTO documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
                return Response.ok().entity(documentDTO).build();
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while getting API " + apiId, e, log);
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieves the content of a document
     *
     * @param apiId API identifier
     * @param documentId document identifier
     * @param xWSO2Tenant requested tenant domain for cross tenant invocations
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return Content of the document/ either inline/file or source url as a redirection
     */
    @Override
    public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId, String xWSO2Tenant,
            String accept, String ifNoneMatch, String ifModifiedSince) {

        Documentation documentation;
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();

            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }

            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier  = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, requestedTenantDomain);

            documentation = apiConsumer.getDocumentation(documentId, requestedTenantDomain);
            if (documentation == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
                return null;
            }

            if (documentation.getSourceType().equals(Documentation.DocumentSourceType.FILE)) {
                String resource = documentation.getFilePath();
                Map<String, Object> docResourceMap = APIUtil.getDocument(username, resource, requestedTenantDomain);
                Object fileDataStream = docResourceMap.get(APIConstants.DOCUMENTATION_RESOURCE_MAP_DATA);
                Object contentType = docResourceMap.get(APIConstants.DOCUMENTATION_RESOURCE_MAP_CONTENT_TYPE);
                contentType = contentType == null ? RestApiConstants.APPLICATION_OCTET_STREAM : contentType;
                String name = docResourceMap.get(APIConstants.DOCUMENTATION_RESOURCE_MAP_NAME).toString();
                return Response.ok(fileDataStream)
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, contentType)
                        .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                        .build();
            } else if (documentation.getSourceType().equals(Documentation.DocumentSourceType.INLINE)) {
                String content = apiConsumer.getDocumentationContent(apiIdentifier, documentation.getName());
                return Response.ok(content)
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, APIConstants.DOCUMENTATION_INLINE_CONTENT_TYPE)
                        .build();
            } else if (documentation.getSourceType().equals(Documentation.DocumentSourceType.URL)) {
                String sourceUrl = documentation.getSourceUrl();
                return Response.seeOther(new URI(sourceUrl)).build();
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving document " + documentId + " of the API " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving source URI location of " + documentId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieves the swagger document of an API
     * 
     * @param apiId API identifier
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param xWSO2Tenant requested tenant domain for cross tenant invocations
     * @return Swagger document of the API
     */
    @Override 
    public Response apisApiIdSwaggerGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince,
            String xWSO2Tenant) {
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();

            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }

            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier  = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, requestedTenantDomain);

            String apiSwagger = apiConsumer.getSwagger20Definition(apiIdentifier);
            return Response.ok().entity(apiSwagger).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

}
