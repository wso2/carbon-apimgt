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

package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.GZIPUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.List;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.json.JSONException;

import javax.ws.rs.core.Response;

public class ApiProductsApiServiceImpl extends ApiProductsApiService {
    private static final Log log = LogFactory.getLog(ApiProductsApiServiceImpl.class);
    @Override
    public Response apiProductsApiProductIdDelete(String apiProductId,String ifMatch,String ifUnmodifiedSince){
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
            if (log.isDebugEnabled()) {
                log.debug("Delete API Product request: Id " +apiProductId + " by " + username);
            }
            apiProvider.deleteAPIProduct(apiProductId, tenantDomain);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting API Product : " + apiProductId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
    @Override
    public Response apiProductsApiProductIdGet(String apiProductId,String accept,String ifNoneMatch,String ifModifiedSince){
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String username = RestApiUtil.getLoggedInUsername();
            if (log.isDebugEnabled()) {
                log.debug("API Product request: Id " +apiProductId + " by " + username);
            }
            APIProduct apiProduct = apiProvider.getAPIProduct(apiProductId, username);
            if (apiProduct == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, log);
            }

            APIProductDetailedDTO createdApiProductDTO = APIMappingUtil.fromAPIProducttoDTO(apiProduct);
            return Response.ok().entity(createdApiProductDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API Product from Id  : " + apiProductId ;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
    @Override
    public Response apiProductsGet(Integer limit,Integer offset,String query,String accept,String ifNoneMatch,Boolean expand,String targetTenantDomain){

        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
            if (log.isDebugEnabled()) {
                log.debug("API Product list request by " + username);
            }
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            List<APIProduct> productList = apiProvider.getAPIProducts(tenantDomain, username);

            APIProductListDTO apiProductListDTO = APIMappingUtil.fromAPIProductListtoDTO(productList);

            //TODO implement pagination
            if (APIConstants.APPLICATION_GZIP.equals(accept)) {
                try {
                    File zippedResponse = GZIPUtils.constructZippedResponse(apiProductListDTO);
                    return Response.ok().entity(zippedResponse).header("Content-Disposition", "attachment")
                            .header("Content-Encoding", "gzip").build();
                } catch (APIManagementException e) {
                    RestApiUtil.handleInternalServerError(e.getMessage(), e, log);
                }
            } else {
                return Response.ok().entity(apiProductListDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API Products ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
    @Override
    public Response apiProductsPost(APIProductDetailedDTO body, String contentType) {

        // Check if product exists
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            String username = RestApiUtil.getLoggedInUsername();
            // if not add product
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
                // Set username in case provider is null or empty
                provider = username;
            }

            List<String> tiersFromDTO = body.getTiers();
            //If tiers are not defined, 
            if (tiersFromDTO == null || tiersFromDTO.isEmpty()) {
                RestApiUtil.handleBadRequest("No tier defined for the API Product", log);
            }
            APIProduct product = APIMappingUtil.fromDTOtoAPIProduct(body, provider);
            String uuid = apiProvider.createAPIProduct(product, tenantDomain);
            APIProduct createdProduct = apiProvider.getAPIProduct(uuid, provider);

            APIProductDetailedDTO createdApiProductDTO = APIMappingUtil.fromAPIProducttoDTO(createdProduct);
            URI createdApiProductUri = new URI(
                    RestApiConstants.RESOURCE_PATH_API_PRODUCTS + "/" + createdApiProductDTO.getId());
            return Response.created(createdApiProductUri).entity(createdApiProductDTO).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API Product : " + body.getProvider() + "-" + body.getName()
                    + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API Product location : " + body.getProvider() + "-"
                    + body.getName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
    @Override
    public Response apiProductsApiProductIdDocumentsDocumentIdContentGet(String apiProductId, String documentId,
            String accept, String ifNoneMatch, String ifModifiedSince) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Response apiProductsApiProductIdDocumentsDocumentIdContentPost(String apiProductId, String documentId,
            String contentType, InputStream fileInputStream, Attachment fileDetail, String inlineContent,
            String ifMatch, String ifUnmodifiedSince) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Response apiProductsApiProductIdDocumentsDocumentIdDelete(String apiProductId, String documentId,
            String ifMatch, String ifUnmodifiedSince) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Response apiProductsApiProductIdDocumentsDocumentIdGet(String apiProductId, String documentId, String accept,
            String ifNoneMatch, String ifModifiedSince) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Response apiProductsApiProductIdDocumentsDocumentIdPut(String apiProductId, String documentId,
            DocumentDTO body, String contentType, String ifMatch, String ifUnmodifiedSince) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Response apiProductsApiProductIdDocumentsGet(String apiProductId, Integer limit, Integer offset,
            String accept, String ifNoneMatch) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Response apiProductsApiProductIdDocumentsPost(String apiProductId, DocumentDTO body, String contentType) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Response apiProductsApiProductIdPut(String apiProductId, APIProductDetailedDTO body, String contentType,
            String ifMatch, String ifUnmodifiedSince) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Response apiProductsApiProductIdSwaggerGet(String apiProductId, String accept, String ifNoneMatch,
            String ifModifiedSince) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Response apiProductsApiProductIdSwaggerPut(String apiProductId, String apiDefinition, String contentType,
            String ifMatch, String ifUnmodifiedSince) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Response apiProductsApiProductIdThumbnailGet(String apiProductId, String accept, String ifNoneMatch,
            String ifModifiedSince) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Response apiProductsApiProductIdThumbnailPost(String apiProductId, InputStream fileInputStream,
            Attachment fileDetail, String contentType, String ifMatch, String ifUnmodifiedSince) {
        // TODO Auto-generated method stub
        return null;
    }
}
