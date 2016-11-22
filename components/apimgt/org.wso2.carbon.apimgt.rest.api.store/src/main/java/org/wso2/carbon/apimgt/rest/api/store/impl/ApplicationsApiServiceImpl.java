package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.Policy;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.rest.api.common.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.ApplicationConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.ApplicationKeyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.mappings.ApplicationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.util.RestAPIStoreUtils;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Map;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:48:55.078+05:30")
public class ApplicationsApiServiceImpl extends ApplicationsApiService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationsApiServiceImpl.class);

    @Override
    public
    Response applicationsApplicationIdDelete(String applicationId, String ifMatch, String ifUnmodifiedSince)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application != null) {
                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    apiConsumer.deleteApplication(application);
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while deleting application " + applicationId, e, log);
        }
        return Response.ok().build();
    }

    @Override
    public
    Response applicationsApplicationIdGet(String applicationId, String accept, String ifNoneMatch,
            String ifModifiedSince) throws NotFoundException {
        ApplicationDTO applicationDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application != null) {
                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    applicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(application);
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving application " + applicationId, e, log);
        }
        return Response.ok().entity(applicationDTO).build();
    }

    @Override
    public Response applicationsApplicationIdPut(String applicationId, ApplicationDTO body,
            String contentType, String ifMatch, String ifUnmodifiedSince) throws NotFoundException {
        ApplicationDTO updatedApplicationDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application oldApplication = apiConsumer.getApplicationByUUID(applicationId);
            if (oldApplication != null) {
                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(oldApplication)) {
                    Application application = ApplicationMappingUtil.fromDTOtoApplication(body, username);
                    application.setGroupId(oldApplication.getGroupId());
                    application.setUuid(oldApplication.getUuid());
                    apiConsumer.updateApplication(oldApplication.getUuid(), application);

                    //retrieves the updated application and send as the response
                    Application updatedApplication = apiConsumer.getApplicationByUUID(applicationId);
                    updatedApplicationDTO = ApplicationMappingUtil
                            .fromApplicationtoDTO(updatedApplication);
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while updating application " + applicationId, e, log);
        }
        return Response.ok().entity(updatedApplicationDTO).build();
    }

    @Override
    public Response applicationsGenerateKeysPost(String applicationId, ApplicationKeyGenerateRequestDTO body,
            String contentType, String ifMatch, String ifUnmodifiedSince) throws NotFoundException {
        ApplicationKeyDTO applicationKeyDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application != null) {
                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    String[] accessAllowDomainsArray = body.getAccessAllowDomains().toArray(new String[1]);
                    JSONObject jsonParamObj = new JSONObject();
                    jsonParamObj.put(ApplicationConstants.OAUTH_CLIENT_USERNAME, username);
                    String jsonParams = jsonParamObj.toString();
                    String tokenScopes = StringUtils.join(body.getScopes(), " ");

                    Map<String, Object> keyDetails = apiConsumer
                            .requestApprovalForApplicationRegistration(username, application.getName(),
                                    body.getKeyType().toString(), body.getCallbackUrl(), accessAllowDomainsArray,
                                    body.getValidityTime(), tokenScopes, application.getGroupId(), jsonParams);
                    applicationKeyDTO = ApplicationKeyMappingUtil
                            .fromApplicationKeyToDTO(keyDetails, body.getKeyType().toString());
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.rootCauseMessageMatches(e, "primary key violation")) {
                RestApiUtil
                        .handleResourceAlreadyExistsError("Keys already generated for the application " + applicationId,
                                e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while generating keys for application " + applicationId, e,
                        log);
            }
        }
        return Response.ok().entity(applicationKeyDTO).build();
    }

    @Override public Response applicationsGet(String query, Integer limit, Integer offset, String accept,
            String ifNoneMatch) throws NotFoundException {

        ApplicationListDTO applicationListDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        String groupId = RestApiUtil.getLoggedInUserGroupId();

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        try {
            APIStore apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application[] allMatchedApps = new Application[0];
            if (StringUtils.isBlank(query)) {
                allMatchedApps = apiConsumer.getApplications(username, groupId);
            } else {
                Application application = apiConsumer.getApplicationByName(username, query, groupId);
                if (application != null) {
                    allMatchedApps = new Application[1];
                    allMatchedApps[0] = application;
                }
            }

            //allMatchedApps are already sorted to application name
            applicationListDTO = ApplicationMappingUtil.fromApplicationsToDTO(allMatchedApps, limit, offset);
            ApplicationMappingUtil.setPaginationParams(applicationListDTO, groupId, limit, offset,
                    allMatchedApps.length);
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Error while retrieving applications of the user " + username, e, log);
        }
        return Response.ok().entity(applicationListDTO).build();
    }

    @Override public Response applicationsPost(ApplicationDTO body, String contentType) throws NotFoundException {
        URI location = null;
        ApplicationDTO createdApplicationDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            //validate the tier specified for the application
            String tierName = body.getThrottlingTier();
            if (tierName != null) {
                Map<String, Policy> appTierMap = APIUtils.getPolicies(APIConstants.POLICY_APPLICATION_TYPE);
                if (appTierMap == null || RestApiUtil.findPolicy(appTierMap.values(), tierName) == null) {
                    RestApiUtil.handleBadRequest("Specified tier " + tierName + " is invalid", log);
                }
            } else {
                RestApiUtil.handleBadRequest("Throttling tier cannot be null", log);
            }

            Application application = ApplicationMappingUtil.fromDTOtoApplication(body, username);
            String groupId = RestApiUtil.getLoggedInUserGroupId();
            application.setGroupId(groupId);
            application.setCreatedTime(LocalDateTime.now());
            String applicationUUID = apiConsumer.addApplication(application);

            //retrieves the created application and send as the response
            Application createdApplication = apiConsumer.getApplicationByUUID(applicationUUID);
            createdApplicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(createdApplication);

            //to be set as the Location header
            location = new URI(RestApiConstants.RESOURCE_PATH_APPLICATIONS + "/" +
                    createdApplicationDTO.getApplicationId());

        } catch (APIManagementException | URISyntaxException e) {
            if (RestApiUtil.isDueToResourceAlreadyExists(e)) {
                RestApiUtil.handleResourceAlreadyExistsError(
                        "An application already exists with name " + body.getName(), e,
                        log);
            } else {
                RestApiUtil.handleInternalServerError("Error while adding a new application for the user " + "fazlan",
                        e, log);
            }
        }
        return Response.created(location).entity(createdApplicationDTO).build();
    }
}
