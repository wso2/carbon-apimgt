package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.core.api.APIConsumer;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.store.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.store.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.dto.Application;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyGenerateRequest;
import org.wso2.carbon.apimgt.rest.api.store.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.store.utils.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.utils.RestApiUtil;

import javax.ws.rs.core.Response;


@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-24T10:59:23.111+05:30")
public class ApplicationsApiServiceImpl extends ApplicationsApiService {
    private static final Log log = LogFactory.getLog(ApplicationsApiServiceImpl.class);

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
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
