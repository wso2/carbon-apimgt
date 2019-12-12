package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.ApiCategoriesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APICategoryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APICategoryListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.APICategoryMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ApiCategoriesApiServiceImpl extends ApiCategoriesApiService {
    private static final Log log = LogFactory.getLog(ApiCategoriesApiServiceImpl.class);

    public Response apiCategoriesGet(){
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            int tenantID = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
            List<APICategory> categoryList = apiAdmin.getAllAPICategoriesOfTenant(tenantID);
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
            String userName = RestApiUtil.getLoggedInUsername();
            String tenantDomain = MultitenantUtils.getTenantDomain(userName);
            int tenantID = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
            APICategory apiCategoryToUpdate = APICategoryMappingUtil.fromCategoryDTOToCategory(body);
            APICategory apiCategoryOriginal = apiAdmin.getAPICategoryByID(apiCategoryId);
            if (apiCategoryOriginal == null) {
                String errorMsg = "No api category with the given category ID exists :" + apiCategoryId;
                log.error(errorMsg);
                throw new APIManagementException(errorMsg);
            }

            //Override several properties as they are not allowed to be updated
            apiCategoryToUpdate.setId(apiCategoryOriginal.getId());
            apiCategoryToUpdate.setTenantID(apiCategoryOriginal.getTenantID());

            //We allow to update API Category name given that the new category name is not taken yet
            String oldName = apiCategoryOriginal.getName();
            String updatedName = apiCategoryToUpdate.getName();
            if (!oldName.equals(updatedName) && apiAdmin.isCategoryNameExists(updatedName, apiCategoryId, tenantID)) {
                String errorMsg = "An API category already exists by the new API category name :" + updatedName;
                log.error(errorMsg);
                throw new APIManagementException(errorMsg);
            }

            apiAdmin.updateCategory(apiCategoryToUpdate, userName);
            APICategory updatedAPICategory = apiAdmin.getAPICategoryByID(apiCategoryId);
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
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            apiAdmin.deleteCategory(apiCategoryId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting API Category '" + apiCategoryId + "' - " + e.getMessage() ;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public Response apiCategoriesApiCategoryIdThumbnailGet(String apiCategoryId,String ifNoneMatch){
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            APICategory category = apiAdmin.getAPICategoryByID(apiCategoryId);
            ResourceFile thumbnailResource = apiProvider.getCategoryIcon(category.getName(), userName);

            if (thumbnailResource != null) {
                return Response
                        .ok(thumbnailResource.getContent(), MediaType.valueOf(thumbnailResource.getContentType()))
                        .build();
            } else {
                return Response.noContent().build();
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_CATEGORY, apiCategoryId, e, log);
            } else {
                String errorMessage = "Error while retrieving thumbnail of API category : " + apiCategoryId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    public Response apiCategoriesApiCategoryIdThumbnailPut(String apiCategoryId, InputStream fileInputStream,
            Attachment fileDetail, String ifMatch) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            APIAdmin apiAdmin = new APIAdminImpl();
            String userName = RestApiUtil.getLoggedInUsername();
            String fileName = fileDetail.getDataHandler().getName();
            String fileContentType = URLConnection.guessContentTypeFromName(fileName);
            if (StringUtils.isBlank(fileContentType)) {
                fileContentType = fileDetail.getContentType().toString();
            }

            APICategory apiCategory = apiAdmin.getAPICategoryByID(apiCategoryId);
            ResourceFile categoryImage = new ResourceFile(fileInputStream, fileContentType);
            String thumbPath = APIUtil.getCategoryIconPath(apiCategory.getName());
            String thumbnailUrl = apiProvider.addResourceFile(null, thumbPath, categoryImage);
            apiCategory.setThumbnailUrl(APIUtil.prependTenantPrefix(thumbnailUrl, userName));

            apiAdmin.updateCategory(apiCategory, userName);

            String uriString = RestApiConstants.RESOURCE_PATH_CATEGORY_THUMBNAIL
                    .replace(RestApiConstants.APICATEGORYID_PARAM, apiCategoryId);
            URI uri = new URI(uriString);
            FileInfoDTO infoDTO = new FileInfoDTO();
            infoDTO.setRelativePath(uriString);
            infoDTO.setMediaType(categoryImage.getContentType());
            return Response.created(uri).entity(infoDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_CATEGORY, apiCategoryId, e, log);
            } else {
                String errorMessage = "Error while adding thumbnail of API Category: " + apiCategoryId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while updating thumbnail of API: " + apiCategoryId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
        return null;
    }

}
