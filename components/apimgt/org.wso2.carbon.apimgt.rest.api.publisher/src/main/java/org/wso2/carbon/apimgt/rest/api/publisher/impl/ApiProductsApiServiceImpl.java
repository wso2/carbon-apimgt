package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;


import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIProductDetailedDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIProductListDTO;

import java.util.List;

import java.io.InputStream;
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
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apiProductsApiProductIdGet(String apiProductId,String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apiProductsGet(Integer limit,Integer offset,String query,String accept,String ifNoneMatch,Boolean expand,String tenantDomain){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apiProductsPost(APIProductDetailedDTO body,String contentType){
        // do some magic!
        // Check if product exists
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
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

            APIProduct product = APIMappingUtil.fromDTOtoAPIProduct(body, provider);
            
            return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API Product : " + body.getProvider() + "-" + body.getName()
                    + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
