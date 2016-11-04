package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.store.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.util.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.store.mappings.ApplicationMappingUtil;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:48:55.078+05:30")
public class ApplicationsApiServiceImpl extends ApplicationsApiService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationsApiServiceImpl.class);

    @Override
    public Response applicationsApplicationIdDelete(String applicationId, String ifMatch, String ifUnmodifiedSince)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
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
    public Response applicationsApplicationIdGet(String applicationId, String accept, String ifNoneMatch,
            String ifModifiedSince) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application != null) {
                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    ApplicationDTO applicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(application);
                    return Response.ok().entity(applicationDTO).build();
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving application " + applicationId, e, log);
        }
        return null;
    }

    @Override public Response applicationsApplicationIdPut(String applicationId, ApplicationDTO body,
            String contentType, String ifMatch, String ifUnmodifiedSince) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application oldApplication = apiConsumer.getApplicationByUUID(applicationId);
            if (oldApplication != null) {
                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(oldApplication)) {
                    //we do not honor the subscriber coming from the request body as we can't change the subscriber of the application
                    Application application = ApplicationMappingUtil.fromDTOtoApplication(body, username);
                    //groupId of the request body is not honored for now.
                    // Later we can improve by checking admin privileges of the user.
                    application.setGroupId(oldApplication.getGroupId());
                    //we do not honor the application id which is sent via the request body
                    application.setUUID(oldApplication.getUUID());

                    apiConsumer.updateApplication(application);

                    //retrieves the updated application and send as the response
                    Application updatedApplication = apiConsumer.getApplicationByUUID(applicationId);
                    ApplicationDTO updatedApplicationDTO = ApplicationMappingUtil
                            .fromApplicationtoDTO(updatedApplication);
                    return Response.ok().entity(updatedApplicationDTO).build();
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while updating application " + applicationId, e, log);
        }
        return null;
    }

    @Override
    public Response applicationsGenerateKeysPost(String applicationId, ApplicationKeyGenerateRequestDTO body,
            String contentType, String ifMatch, String ifUnmodifiedSince) throws NotFoundException {
//        String username = RestApiUtil.getLoggedInUsername();
//        try {
//            APIStore apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
//            Application application = apiConsumer.getApplicationByUUID(applicationId);
//            if (application != null) {
//                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
//                    String[] accessAllowDomainsArray = body.getAccessAllowDomains().toArray(new String[1]);
//                    JSONObject jsonParamObj = new JSONObject();
//                    jsonParamObj.put(Constants.OAUTH_CLIENT_USERNAME, username);
//                    String jsonParams = jsonParamObj.toString();
//                    String tokenScopes = StringUtils.join(body.getScopes(), " ");
//
//                    Map<String, Object> keyDetails = apiConsumer.requestApprovalForApplicationRegistration(
//                            username, application.getName(), body.getKeyType().toString(), body.getCallbackUrl(),
//                            accessAllowDomainsArray, body.getValidityTime(), tokenScopes, application.getGroupId(),
//                            jsonParams);
//                    ApplicationKeyDTO applicationKeyDTO =
//                            ApplicationKeyMappingUtil.fromApplicationKeyToDTO(keyDetails, body.getKeyType().toString());
//
//                    return Response.ok().entity(applicationKeyDTO).build();
//                } else {
//                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
//                }
//            } else {
//                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
//            }
//        } catch (APIManagementException e) {
//            if (RestApiUtil.rootCauseMessageMatches(e, "primary key violation")) {
//                RestApiUtil
//                        .handleResourceAlreadyExistsError("Keys already generated for the application " + applicationId,
//                                e,
//                                log);
//            } else {
//                RestApiUtil.handleInternalServerError("Error while generating keys for application " + applicationId, e,
//                        log);
//            }
//        }
        return null;
    }

    @Override public Response applicationsGet(String query, Integer limit, Integer offset, String accept,
            String ifNoneMatch) throws NotFoundException {
        // do some magic!

        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override public Response applicationsPost(ApplicationDTO body, String contentType) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
