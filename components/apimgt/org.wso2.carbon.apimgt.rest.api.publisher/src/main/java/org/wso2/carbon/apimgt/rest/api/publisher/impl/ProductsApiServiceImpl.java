/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductStatus;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.*;


import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIProductListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIProductDTO;

import java.util.Arrays;
import java.util.List;

import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.APIProductMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * This is the service implementation class for Publisher API related operations
 */
public class ProductsApiServiceImpl extends ProductsApiService {

    private static final Log log = LogFactory.getLog(ProductsApiServiceImpl.class);

    /**
     * Retrieves API Products qualifying under given search condition
     *
     * @param limit maximum number of API Products returned
     * @param offset starting index
     * @param query search condition
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @return matched API Products for the given search condition
     */
    @Override
    public Response productsGet(Integer limit,Integer offset,String query,String accept,String ifNoneMatch){
        List<APIProduct> allMatchedApiProducts;
        APIProductListDTO apiProductListDTO;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            //if query parameter is not specified, This will search by name
            String searchType = APIConstants.API_PRODUCT_NAME;
            String searchContent = "";
            if (!StringUtils.isBlank(query)) {
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

            //We should send null as the provider, Otherwise searchAPIs will return all APIs of the provider
            // instead of looking at type and query
            allMatchedApiProducts = apiProvider.searchAPIProducts(searchContent, searchType, null);
            apiProductListDTO = APIProductMappingUtil.fromAPIProductListToDTO(allMatchedApiProducts, offset, limit);
            APIProductMappingUtil.setPaginationParams(apiProductListDTO, query, offset, limit,
                                                                                        allMatchedApiProducts.size());
            return Response.ok().entity(apiProductListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving APIs";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }

        return null;
    }

    /**
     * Create new APIProduct
     *
     * @param body DTO model of new APIProduct to be created
     * @param contentType content type of the payload
     * @return created APIProduct
     */
    @Override
    public Response productsPost(APIProductDTO body,String contentType){
        URI createdApiUri;
        APIProductDTO  createdApiProductDTO;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String username = RestApiUtil.getLoggedInUsername();

            List<String> tiersFromDTO = body.getThrottlingTier();
            //If tiers are not defined, the api should be a PROTOTYPED one,
            if (tiersFromDTO == null || tiersFromDTO.isEmpty()) {
                RestApiUtil.handleBadRequest("No tier defined for the API", log);
            }
            //check whether the added APIProduct's tiers are all valid
            Set<Tier> definedTiers = apiProvider.getTiers();
            List<String> invalidTiers = RestApiUtil.getInvalidTierNames(definedTiers, tiersFromDTO);
            if (invalidTiers.size() > 0) {
                RestApiUtil.handleBadRequest(
                        "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid", log);
            }

            //check if the user has admin permission before applying a different provider than the current user
            String provider = body.getProvider();
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
                //set username in case provider is null or empty
                provider = username;
            }
            APIProduct apiProductToAdd = APIProductMappingUtil.fromDTOtoAPIProduct(body, provider);

            if (apiProvider.isAPIProductAvailable(apiProductToAdd.getId())) {
                RestApiUtil.handleResourceAlreadyExistsError(
                        "Error occurred while adding the APIProduct. A duplicate APIProduct already exists for " + apiProductToAdd.getId()
                                .getApiProductName() + "-" + apiProductToAdd.getId().getVersion(), log);
            }
            //Overriding some properties:
            //only allow CREATED as the stating state for the new api
            apiProductToAdd.setStatus(APIProductStatus.CREATED);

            //we are setting the api owner as the logged in user until we support checking admin privileges and assigning
            //  the owner as a different user
            apiProductToAdd.setApiProductOwner(provider);

            //adding the api
            apiProvider.addAPIProduct(apiProductToAdd);
            APIProductIdentifier createdApiId = apiProductToAdd.getId();
            //Retrieve the newly added API to send in the response payload
            APIProduct apiProduct = apiProvider.getAPIProduct(createdApiId);
            createdApiProductDTO = APIProductMappingUtil.fromAPIProducttoDTO(apiProduct);
            //This URI used to set the location header of the POST response
            createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiProductDTO.getId());
            return Response.created(createdApiUri).entity(createdApiProductDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response productsChangeProductLifecyclePost(String action,String productId,String lifecycleChecklist,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response productsCopyProductPost(String newVersion,String productId){

        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }


    @Override
    public Response productsProductIdDelete(String productId,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response productsProductIdGet(String productId,String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response productsProductIdPut(String productId,APIProductDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
