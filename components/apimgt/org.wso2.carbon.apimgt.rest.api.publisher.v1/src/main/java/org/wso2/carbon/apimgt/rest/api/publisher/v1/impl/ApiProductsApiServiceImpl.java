/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.GZIPUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.RestApiPublisherUtils;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO.StateEnum;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductListDTO;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public class ApiProductsApiServiceImpl implements ApiProductsApiService {
    private static final Log log = LogFactory.getLog(ApiProductsApiServiceImpl.class);

    @Override public Response apiProductsApiProductIdDelete(String apiProductId, String ifMatch,
            MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
            if (log.isDebugEnabled()) {
                log.debug("Delete API Product request: Id " +apiProductId + " by " + username);
            }
            APIProduct apiProduct = apiProvider.getAPIProductbyUUID(apiProductId, tenantDomain);
            if (apiProduct == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, log);
            }
            apiProvider.deleteAPIProduct(apiProduct.getId());
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting API Product : " + apiProductId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override public Response apiProductsApiProductIdDocumentsDocumentIdContentGet(String apiProductId,
            String documentId, String accept, String ifNoneMatch, MessageContext messageContext) {
        return null;
    }

    @Override public Response apiProductsApiProductIdDocumentsDocumentIdContentPost(String apiProductId,
            String documentId, InputStream fileInputStream, Attachment fileDetail, String inlineContent, String ifMatch,
            MessageContext messageContext) {
        return null;
    }

    @Override public Response apiProductsApiProductIdDocumentsDocumentIdDelete(String apiProductId, String documentId,
            String ifMatch, MessageContext messageContext) {
        return null;
    }

    @Override public Response apiProductsApiProductIdDocumentsDocumentIdGet(String apiProductId, String documentId,
            String accept, String ifNoneMatch, MessageContext messageContext) {
        return null;
    }

    @Override public Response apiProductsApiProductIdDocumentsDocumentIdPut(String apiProductId, String documentId,
            DocumentDTO body, String ifMatch, MessageContext messageContext) {
        return null;
    }

    @Override public Response apiProductsApiProductIdDocumentsGet(String apiProductId, Integer limit, Integer offset,
            String accept, String ifNoneMatch, MessageContext messageContext) {
        return null;
    }

    @Override public Response apiProductsApiProductIdDocumentsPost(String apiProductId, DocumentDTO body,
            MessageContext messageContext) {
        return null;
    }

    @Override public Response apiProductsApiProductIdGet(String apiProductId, String accept, String ifNoneMatch,
            MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
            if (log.isDebugEnabled()) {
                log.debug("API Product request: Id " +apiProductId + " by " + username);
            }
            APIProduct apiProduct = apiProvider.getAPIProductbyUUID(apiProductId, tenantDomain);
            if (apiProduct == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, log);
            }

            APIProductDTO createdApiProductDTO = APIMappingUtil.fromAPIProducttoDTO(apiProduct);
            return Response.ok().entity(createdApiProductDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API Product from Id  : " + apiProductId ;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response apiProductsApiProductIdPut(String apiProductId, APIProductDTO body, String ifMatch,
            MessageContext messageContext) {
        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            APIProduct retrievedProduct = apiProvider.getAPIProductbyUUID(apiProductId, tenantDomain);
            if (retrievedProduct == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, log);
            }
            //validation for tiers
            List<String> tiersFromDTO = body.getPolicies();
            if (tiersFromDTO == null || tiersFromDTO.isEmpty()) {
                RestApiUtil.handleBadRequest("No tier defined for the API Product", log);
            }

            //check whether the added API Products's tiers are all valid
            Set<Tier> definedTiers = apiProvider.getTiers();
            List<String> invalidTiers = RestApiUtil.getInvalidTierNames(definedTiers, tiersFromDTO);
            if (!invalidTiers.isEmpty()) {
                RestApiUtil.handleBadRequest(
                        "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid", log);
            }
            if (body.getAdditionalProperties() != null) {
                String errorMessage = RestApiPublisherUtils
                        .validateAdditionalProperties(body.getAdditionalProperties());
                if (!errorMessage.isEmpty()) {
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
            }

            //only publish api product if tiers are defined
            if(StateEnum.PUBLISHED.equals(body.getState())) {
                //if the already created API product does not have tiers defined and the update request also doesn't
                //have tiers defined, then the product should not moved to PUBLISHED state.
                if (retrievedProduct.getAvailableTiers() == null && body.getPolicies() == null) {
                    RestApiUtil.handleBadRequest("Policy needs to be defined before publishing the API Product", log);
                }
            }


            APIProduct product = APIMappingUtil.fromDTOtoAPIProduct(body, username);
            //We do not allow to modify provider,name,version  and uuid. Set the origial value
            APIProductIdentifier productIdentifier = retrievedProduct.getId();
            product.setID(productIdentifier);
            product.setUuid(apiProductId);

            apiProvider.updateAPIProduct(product, username);
            APIProduct updatedProduct = apiProvider.getAPIProduct(productIdentifier);
            APIProductDTO updatedProductDTO = APIMappingUtil.fromAPIProducttoDTO(updatedProduct);
            return Response.ok().entity(updatedProductDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating API Product : " + apiProductId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override public Response apiProductsApiProductIdSwaggerGet(String apiProductId, String accept, String ifNoneMatch,
            MessageContext messageContext) {
        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            APIProduct retrievedProduct = apiProvider.getAPIProduct(apiProductId, tenantDomain);
            if (retrievedProduct == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, log);
            }
            APIProduct productWithSwagger = apiProvider.getAPIDefinitionOfAPIProduct(apiProductId);
            //Implement visibility related tasks using the retrieved product if needed

            String apiSwagger = "";
            if (!StringUtils.isEmpty(productWithSwagger.getDefinition())) {
                apiSwagger = productWithSwagger.getDefinition();
            }
            return Response.ok().entity(apiSwagger).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API Product from Id  : " + apiProductId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override public Response apiProductsApiProductIdSwaggerPut(String apiProductId, String apiDefinition,
            String ifMatch, MessageContext messageContext) {
        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            APIProduct retrievedProduct = apiProvider.getAPIProduct(apiProductId, tenantDomain);
            if (retrievedProduct == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, log);
            }
            //Implement visibility related tasks using the retrieved product if needed
            apiProvider.updateAPIDefinitionToAPIProduct(apiDefinition, apiProductId);
            APIProduct productWithSwagger = apiProvider.getAPIDefinitionOfAPIProduct(apiProductId);

            String apiSwagger = "";
            if (!StringUtils.isEmpty(productWithSwagger.getDefinition())) {
                apiSwagger = productWithSwagger.getDefinition();
            }
            return Response.ok().entity(apiSwagger).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API Product from Id  : " + apiProductId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override public Response apiProductsApiProductIdThumbnailGet(String apiProductId, String accept,
            String ifNoneMatch, MessageContext messageContext) {
        return null;
    }

    @Override public Response apiProductsApiProductIdThumbnailPost(String apiProductId, InputStream fileInputStream,
            Attachment fileDetail, String ifMatch, MessageContext messageContext) {
        return null;
    }

    @Override
    public Response apiProductsGet(Integer limit, Integer offset, String query, String accept,
            String ifNoneMatch, MessageContext messageContext) {

        List<APIProduct> allMatchedProducts = new ArrayList<>();
        APIProductListDTO apiProductListDTO;

        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;

        try {
            //for now one criterea is supported
            String searchQuery = StringUtils.replace(query, ":", "=");

            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
            if (log.isDebugEnabled()) {
                log.debug("API Product list request by " + username);
            }
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            Map<String, Object> result = apiProvider.searchPaginatedAPIProducts(searchQuery, tenantDomain, offset, limit);

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
            String errorMessage = "Error while retrieving API Products ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override public Response apiProductsPost(APIProductDTO body, MessageContext messageContext) {
        String provider = null;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            String username = RestApiUtil.getLoggedInUsername();
            // if not add product
            provider = body.getProvider();
            if (!StringUtils.isBlank(provider) && !provider.equals(username)) {
                if (!APIUtil.hasPermission(username, APIConstants.Permissions.APIM_ADMIN)) {
                    if (log.isDebugEnabled()) {
                        log.debug("User " + username + " does not have admin permission ("
                                + APIConstants.Permissions.APIM_ADMIN + ") hence provider (" + provider
                                + ") overridden with current user (" + username + ")");
                    }
                    provider = username;
                }
            } else {
                // Set username in case provider is null or empty
                provider = username;
            }

            if (apiProvider.isProductExist(body.getName(), provider, tenantDomain)) {
                RestApiUtil.handleBadRequest(
                        "Product with name " + body.getName() + " for provider " + provider + " already exists", log);
            }
            List<String> tiersFromDTO = body.getPolicies();
            Set<Tier> definedTiers = apiProvider.getTiers();
            List<String> invalidTiers = RestApiUtil.getInvalidTierNames(definedTiers, tiersFromDTO);
            if (!invalidTiers.isEmpty()) {
                RestApiUtil.handleBadRequest(
                        "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid", log);
            }
            if (body.getAdditionalProperties() != null) {
                String errorMessage = RestApiPublisherUtils
                        .validateAdditionalProperties(body.getAdditionalProperties());
                if (!errorMessage.isEmpty()) {
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
            }

            APIProduct productToBeAdded = APIMappingUtil.fromDTOtoAPIProduct(body, provider);
            String uuid = apiProvider.addAPIProduct(productToBeAdded);
            APIProductIdentifier createdAPIProductIdentifier = productToBeAdded.getId();
            APIProduct createdProduct = apiProvider.getAPIProduct(createdAPIProductIdentifier);

            APIProductDTO createdApiProductDTO = APIMappingUtil.fromAPIProducttoDTO(createdProduct);
            URI createdApiProductUri = new URI(
                    RestApiConstants.RESOURCE_PATH_API_PRODUCTS + "/" + createdApiProductDTO.getId());
            return Response.created(createdApiProductUri).entity(createdApiProductDTO).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API Product : " + provider + "-" + body.getName()
                    + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API Product location : " + provider + "-"
                    + body.getName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
