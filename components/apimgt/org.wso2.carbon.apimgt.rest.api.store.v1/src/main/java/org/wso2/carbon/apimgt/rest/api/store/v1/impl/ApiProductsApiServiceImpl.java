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

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.ApiProductsApiServiceImpl;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;


import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIProductListDTO;

import java.util.ArrayList;
import java.util.List;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public class ApiProductsApiServiceImpl implements ApiProductsApiService {
    private static final Log log = LogFactory.getLog(ApiProductsApiServiceImpl.class);


    @Override
    public Response apiProductsApiProductIdCommentsCommentIdDelete(String commentId, String apiProductId, String ifMatch, MessageContext messageContext) throws APIManagementException {
        return null;
    }

    @Override
    public Response apiProductsApiProductIdCommentsCommentIdGet(String commentId, String apiProductId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException {
        return null;
    }

    @Override
    public Response apiProductsApiProductIdCommentsCommentIdPut(String commentId, String apiProductId, CommentDTO body, String ifMatch, MessageContext messageContext) throws APIManagementException {
        return null;
    }

    @Override
    public Response apiProductsApiProductIdCommentsGet(String apiProductId, Integer limit, Integer offset, MessageContext messageContext) {
        return null;
    }

    @Override
    public Response apiProductsApiProductIdCommentsPost(String apiProductId, CommentDTO body, MessageContext messageContext) {
        return null;
    }

    @Override public Response apiProductsApiProductIdDocumentsDocumentIdContentGet(String apiProductId,
                                                                                   String documentId, String ifNoneMatch, MessageContext messageContext) {
        return null;
    }

    @Override public Response apiProductsApiProductIdDocumentsDocumentIdGet(String apiProductId, String documentId,
            String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) {
        return null;
    }

    @Override public Response apiProductsApiProductIdDocumentsGet(String apiProductId, Integer limit, Integer offset,
            String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) {
        return null;
    }

    @Override public Response apiProductsApiProductIdGet(String apiProductId, String ifNoneMatch, String xWSO2Tenant,
            MessageContext messageContext) {
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();

            if (!APIUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }

            APIProduct product = apiConsumer.getAPIProductbyUUID(apiProductId, requestedTenantDomain);
            if (product == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, log);
            }
            if(!RestAPIStoreUtils.isUserAccessAllowedForAPIProduct(product)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, log);
            }
            APIProductDTO productToReturn = APIMappingUtil.fromAPIProductToDTO(product, requestedTenantDomain);
            return Response.ok().entity(productToReturn).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API Product : " + apiProductId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response apiProductsApiProductIdRatingsGet(String apiProductId, Integer limit, Integer offset, MessageContext messageContext) {
        return null;
    }

    @Override
    public Response apiProductsApiProductIdRatingsRatingIdGet(String apiProductId, String ratingId, String ifNoneMatch, MessageContext messageContext) {
        return null;
    }

    @Override
    public Response apiProductsApiProductIdSubscriptionPoliciesGet(String apiProductId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) {
        return null;
    }

    @Override public Response apiProductsApiProductIdSwaggerGet(String apiProductId, String labelName,
                                                                String environmentName, String ifNoneMatch,
                                                                String xWSO2Tenant, MessageContext messageContext) {
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();

            if (org.apache.commons.lang3.StringUtils.isNotEmpty(labelName) &&
                    org.apache.commons.lang3.StringUtils.isNotEmpty(environmentName)) {
                RestApiUtil.handleBadRequest("Only one of 'labelName' or 'environmentName' can be provided", log);
            }

            if (!APIUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }

            APIProduct product = apiConsumer.getAPIProductbyUUID(apiProductId, requestedTenantDomain);
            if (product == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, log);
            }

            //gets the first available environment if neither label nor environment is not provided
            if (org.apache.commons.lang3.StringUtils.isEmpty(labelName) && org.apache.commons.lang3.StringUtils.isEmpty(environmentName)) {
                environmentName = product.getEnvironments().iterator().next();
            }

            if(!RestAPIStoreUtils.isUserAccessAllowedForAPIProduct(product)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, log);
            }

            String apiSwagger = null;
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(environmentName)) {
                apiSwagger = apiConsumer.getOpenAPIDefinitionForEnvironment(product.getId(), environmentName);
            } else if (org.apache.commons.lang3.StringUtils.isNotEmpty(labelName)) {
                apiSwagger = apiConsumer.getOpenAPIDefinitionForLabel(product.getId(), labelName);
            }
            
            if (StringUtils.isEmpty(apiSwagger)) {
                apiSwagger = "";
            }
            return Response.ok().entity(apiSwagger).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API Product : " + apiProductId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override public Response apiProductsApiProductIdThumbnailGet(String apiProductId, String xWSO2Tenant,
            String ifNoneMatch, MessageContext messageContext) {
        return null;
    }

    @Override
    public Response apiProductsApiProductIdUserRatingPut(String apiProductId, RatingDTO body, MessageContext messageContext) {
        return null;
    }

    @Override public Response apiProductsGet(Integer limit, Integer offset, String xWSO2Tenant, String query,
            String ifNoneMatch, MessageContext messageContext) {
        //TODO implement pagination
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        List<APIProduct> allMatchedProducts = new ArrayList<>();
        APIProductListDTO apiProductListDTO = new APIProductListDTO();

        try {
            //for now one criterea is supported
            String searchQuery = StringUtils.replace(query, ":", "=");

            String username = RestApiUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);

            if (!APIUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }
            Map<String, Object> result = apiConsumer.searchPaginatedAPIProducts(searchQuery, requestedTenantDomain, offset, limit);
            Set<APIProduct> apiProducts = (Set<APIProduct>) result.get("products");
            allMatchedProducts.addAll(apiProducts);
            apiProductListDTO = APIMappingUtil.fromAPIProductListtoDTO(allMatchedProducts);

            //Add pagination section in the response
            Object totalLength = result.get("length");
            Integer length = 0;
            if (totalLength != null) {
                length = (Integer) totalLength;
            }
            APIMappingUtil.setPaginationParams(apiProductListDTO, query, offset, limit, length);

            return Response.ok().entity(apiProductListDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.rootCauseMessageMatches(e, "start index seems to be greater than the limit count")) {
                // this is not an error of the user as he does not know the total number of apis available. Thus sends
                // an empty response
                apiProductListDTO.setCount(0);
                apiProductListDTO.setPagination(new PaginationDTO());
                return Response.ok().entity(apiProductListDTO).build();
            } else {
                String errorMessage = "Error while retrieving API Products";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
