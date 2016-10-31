package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIConsumer;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.util.Constants;
import org.wso2.carbon.apimgt.rest.api.store.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.store.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.dto.Application;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyGenerateRequest;
import org.wso2.carbon.apimgt.rest.api.store.dto.Tier;
import org.wso2.carbon.apimgt.rest.api.store.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.store.utils.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-24T10:59:23.111+05:30")
public class ApplicationsApiServiceImpl extends ApplicationsApiService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationsApiServiceImpl.class);

    @Override
    public Response applicationsApplicationIdDelete(String applicationId, String ifMatch, String ifUnmodifiedSince ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            org.wso2.carbon.apimgt.core.models.Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application != null) {
                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    apiConsumer.removeApplication(application);
                    return Response.ok().build();
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while deleting application " + applicationId, e, log);
        }
       return null;
    }
    @Override
    public Response applicationsApplicationIdGet(String applicationId, String accept, String ifNoneMatch, String ifModifiedSince ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response applicationsApplicationIdPut(String applicationId, Application body, String contentType, String ifMatch, String ifUnmodifiedSince ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response applicationsGenerateKeysPost(String applicationId, ApplicationKeyGenerateRequest body, String contentType, String ifMatch, String ifUnmodifiedSince ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response applicationsGet(String groupId, String query, Integer limit, Integer offset, String accept, String ifNoneMatch ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response applicationsPost(Application body, String contentType ) throws NotFoundException {
//        String username = RestApiUtil.getLoggedInUsername();
//        try {
//            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
//            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
//
//            //validate the tier specified for the application
//            String tierName = body.getThrottlingTier();
//            if (tierName != null) {
//                Map<String, Tier> appTierMap = APIUtil.getTiers(Constants.TIER_APPLICATION_TYPE, tenantDomain);
//                if (appTierMap == null || RestApiUtil.findTier(appTierMap.values(), tierName) == null) {
//                    RestApiUtil.handleBadRequest("Specified tier " + tierName + " is invalid", log);
//                }
//            } else {
//                RestApiUtil.handleBadRequest("Throttling tier cannot be null", log);
//            }
//
//            //subscriber field of the body is not honored. It is taken from the context
//            org.wso2.carbon.apimgt.core.models.Application application = ApplicationMappingUtil.fromDTOtoApplication(body, username);
//
//            //setting the proper groupId. This is not honored for now.
//            // Later we can honor it by checking admin privileges of the user.
//            String groupId = RestApiUtil.getLoggedInUserGroupId();
//            application.setGroupID(groupId);
//            int applicationId = apiConsumer.addApplication(application, username);
//
//            //retrieves the created application and send as the response
//            org.wso2.carbon.apimgt.core.models.Application createdApplication = apiConsumer.getApplicationById(applicationId);
//            Application createdApplicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(createdApplication);
//
//            //to be set as the Location header
//            URI location = new URI(RestApiConstants.RESOURCE_PATH_APPLICATIONS + "/" +
//                    createdApplicationDTO.getApplicationId());
//            return Response.created(location).entity(createdApplicationDTO).build();
//        } catch (APIManagementException | URISyntaxException e) {
//            if (RestApiUtil.isDueToResourceAlreadyExists(e)) {
//                RestApiUtil.handleResourceAlreadyExistsError(
//                        "An application already exists with name " + body.getName(), e,
//                        log);
//            } else {
//                RestApiUtil.handleInternalServerError("Error while adding a new application for the user " + username,
//                        e, log);
//            }
//        }
         return null;
    }
}
