package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.utils.APICategoryUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.ApiCategoriesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APICategoryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APICategoryListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.APICategoryMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.core.Response;

public class ApiCategoriesApiServiceImpl extends ApiCategoriesApiService {
    private static final Log log = LogFactory.getLog(ApiCategoriesApiServiceImpl.class);

    public Response apiCategoriesGet(){
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            int tenantID = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
            List<APICategory> categoryList = APICategoryUtil.getAllAPICategoriesOfTenant(tenantID);
            APICategoryListDTO categoryListDTO =
                    APICategoryMappingUtil.fromCategoryListToCategoryListDTO(categoryList);
            return Response.ok().entity(categoryListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API categories";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public Response apiCategoriesPost(APICategoryDTO body){
        APICategory apiCategory = null;
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            String userName = RestApiUtil.getLoggedInUsername();
            apiCategory = APICategoryMappingUtil.fromCategoryDTOToCategory(body);
            APICategoryDTO categoryDTO = APICategoryMappingUtil.
                    fromCategoryToCategoryDTO(apiAdmin.addCategory(userName, apiCategory));
            URI location = new URI(RestApiConstants.RESOURCE_PATH_CATEGORY + "/" + categoryDTO.getId());
            return Response.created(location).entity(categoryDTO).build();
        } catch (APIManagementException | URISyntaxException e) {
            String errorMessage = "Error while adding new API Category '" + body.getName() + "' - " + e.getMessage() ;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public Response apiCategoriesApiCategoryIdPut(String apiCategoryId, APICategoryDTO body) {
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            APICategory apiCategoryToUpdate = APICategoryMappingUtil.fromCategoryDTOToCategory(body);
            APICategory apiCategoryOriginal = APICategoryUtil.getAPICategoryByID(apiCategoryId);
            if (apiCategoryOriginal == null) {
                String errorMsg = "No api category with the given category ID exists :" + apiCategoryId;
                log.error(errorMsg);
                throw new APIManagementException(errorMsg);
            }

            //Override several properties as they are not allowed to be updated
            apiCategoryToUpdate.setName(apiCategoryOriginal.getName());
            apiCategoryToUpdate.setId(apiCategoryOriginal.getId());
            apiCategoryToUpdate.setTenantID(apiCategoryOriginal.getTenantID());

            apiAdmin.updateCategory(apiCategoryToUpdate);
            APICategory updatedAPICategory = APICategoryUtil.getAPICategoryByID(apiCategoryId);
            APICategoryDTO updatedAPICategoryDTO = APICategoryMappingUtil.fromCategoryToCategoryDTO(updatedAPICategory);
            return Response.ok().entity(updatedAPICategoryDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating API Category '" + body.getName() + "' - " + e.getMessage() ;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public Response apiCategoriesApiCategoryIdDelete(String apiCategoryId, String ifMatch,
            String ifUnmodifiedSince) {
        return null;
    }
}
