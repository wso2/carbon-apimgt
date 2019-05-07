/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApisApiService;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingDTO;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.core.Response;

public class ApisApiServiceImpl extends ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);

    @Override
    public Response apisGet(Integer limit, Integer offset, String xWSO2Tenant, String query, String ifNoneMatch) {
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
            String newSearchQuery = APIUtil.constructNewSearchQuery(query);

            //revert content search back to normal search by name to avoid doc result complexity and to comply with REST api practices
            if (newSearchQuery.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + "=")) {
                newSearchQuery = newSearchQuery
                        .replace(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + "=", APIConstants.NAME_TYPE_PREFIX + "=");
            }
            // Append LC state query criteria if the search is not doc or subcontext
            // based
            if (!APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX_WITH_EQUALS.startsWith(newSearchQuery) &&
                    !APIConstants.SUBCONTEXT_SEARCH_TYPE_PREFIX.startsWith(newSearchQuery)) {
                boolean displayAPIsWithMultipleStatus = APIUtil.isAllowDisplayAPIsWithMultipleStatus();

                String[] statusList = { APIConstants.PUBLISHED, APIConstants.PROTOTYPED };
                if (displayAPIsWithMultipleStatus) {
                    statusList = new String[] { APIConstants.PUBLISHED, APIConstants.PROTOTYPED,
                            APIConstants.DEPRECATED };
                }

                String lcCriteria = APIConstants.LCSTATE_SEARCH_TYPE_KEY;
                lcCriteria = lcCriteria + APIUtil.getORBasedSearchCriteria(statusList);

                newSearchQuery = newSearchQuery + APIConstants.SEARCH_AND_TAG + lcCriteria;
            }

            Map allMatchedApisMap = apiConsumer
                    .searchPaginatedAPIs(newSearchQuery, requestedTenantDomain, offset, limit, false);
            Set<API> sortedSet = (Set<API>) allMatchedApisMap.get("apis"); // This is a SortedSet
            ArrayList<API> allMatchedApis = new ArrayList<>(sortedSet);

            apiListDTO = APIMappingUtil.fromAPIListToDTO(allMatchedApis);
            APIMappingUtil.setPaginationParams(apiListDTO, query, offset, limit, allMatchedApis.size());

            //Add pagination section in the response
            Object totalLength = allMatchedApisMap.get("length");
            Integer length = 0;
            if (totalLength != null) {
                length = (Integer) totalLength;
            }
            PaginationDTO paginationDTO = new PaginationDTO();
            paginationDTO.setOffset(offset);
            paginationDTO.setLimit(limit);
            paginationDTO.setTotal(length);
            apiListDTO.setPagination(paginationDTO);

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

    @Override
    public Response apisApiIdGet(String apiId, String xWSO2Tenant, String ifNoneMatch) {
        APIDTO apiToReturn;
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();

            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }

            API api = apiConsumer.getAPIbyUUID(apiId, requestedTenantDomain);
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

    @Override
    public Response apisApiIdCommentsCommentIdDelete(String commentId, String apiId, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdCommentsCommentIdGet(String commentId, String apiId, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdCommentsCommentIdPut(String commentId, String apiId, CommentDTO body, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdCommentsGet(String apiId, Integer limit, Integer offset) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdCommentsPost(String apiId, CommentDTO body) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId, String ifNoneMatch,
            String ifModifiedSince) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String ifNoneMatch,
            String ifModifiedSince) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdDocumentsGet(String apiId, Integer limit, Integer offset, String xWSO2Tenant,
            String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdRatingsGet(String apiId, Integer limit, Integer offset) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdRatingsRatingIdGet(String apiId, String ratingId, String ifNoneMatch
    ) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdSdksLanguageGet(String apiId, String language) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    /**
     * Retrieves the swagger document of an API
     *
     * @param apiId           API identifier
     * @param ifNoneMatch     If-None-Match header value
     * @param xWSO2Tenant     requested tenant domain for cross tenant invocations
     * @return Swagger document of the API
     */
    @Override
    public Response apisApiIdSwaggerGet(String apiId, String ifNoneMatch, String xWSO2Tenant) {
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }

            //this will fail if user does not have access to the API or the API does not exist

            APIIdentifier apiIdentifier = RestAPIStoreUtils.getAPIIdentifierFromUUID(apiId, requestedTenantDomain);;
            String apiSwagger = apiConsumer.getOpenAPIDefinition(apiIdentifier);
            apiSwagger = APIUtil.removeXMediationScriptsFromSwagger(apiSwagger);

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

    @Override public Response apisApiIdThumbnailGet(String apiId, String xWSO2Tenant, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdUserRatingPut(String apiId, RatingDTO body) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdWsdlGet(String apiId, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

}
