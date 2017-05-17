package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ErrorHandler;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.APIKey;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.ApplicationCreationResponse;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.ApplicationStatus;
import org.wso2.carbon.apimgt.core.util.ApplicationUtils;
import org.wso2.carbon.apimgt.core.util.ETagUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.ApplicationKeyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.mappings.ApplicationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.mappings.MiscMappingUtil;
import org.wso2.msf4j.Request;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date =
        "2016-11-01T13:48:55.078+05:30")
public class ApplicationsApiServiceImpl
        extends ApplicationsApiService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationsApiServiceImpl.class);

    /**
     * Deletes an existing application
     *
     * @param applicationId     ID of the application
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return 200 response if the deletion was successful
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response applicationsApplicationIdDelete(String applicationId, String ifMatch, String ifUnmodifiedSince,
                                                    Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = RestApiUtil.getConsumer(username);
            String existingFingerprint = applicationsApplicationIdGetFingerprint(applicationId, null, null, null,
                    request);
            if (!StringUtils.isEmpty(ifMatch) && !StringUtils.isEmpty(existingFingerprint) && !ifMatch
                    .contains(existingFingerprint)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }
            apiConsumer.deleteApplication(applicationId);
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting application: " + applicationId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.APPLICATION_ID, applicationId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
        return Response.ok().build();
    }

    /**
     * Retrives an existing application
     *
     * @param applicationId   application Id
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return Requested application detials as the response
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response applicationsApplicationIdGet(String applicationId, String accept, String ifNoneMatch,
                                                 String ifModifiedSince, Request request) throws NotFoundException {
        ApplicationDTO applicationDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = RestApiUtil.getConsumer(username);
            String existingFingerprint = applicationsApplicationIdGetFingerprint(applicationId, accept, ifNoneMatch,
                    ifModifiedSince, request);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }

            Application application = apiConsumer.getApplication(applicationId, username, null);
            if (application != null) {
                if (application.getKeys() != null) {
                    //TODO : this logic needs to be wriiten properly ones the Identity server DCR endpoint is
                    // available to get oauth app details by providing consumer
                    KeyManager keyManager = APIManagerFactory.getInstance().getIdentityProvider();
                    for (APIKey apiKey : application.getKeys()) {
                        try {
                            OAuthApplicationInfo oAuthApplicationInfo = keyManager
                                    .retrieveApplication(apiKey.getConsumerKey());
                            apiKey.setConsumerSecret(oAuthApplicationInfo.getClientSecret());
                            AccessTokenInfo accessTokenInfo = keyManager.getNewApplicationAccessToken(
                                    ApplicationUtils.createAccessTokenRequest(oAuthApplicationInfo));
                            apiKey.setAccessToken(accessTokenInfo.getAccessToken());
                            apiKey.setValidityPeriod(accessTokenInfo.getValidityPeriod());
                            //TODO : When showing keys in the application view page , we need to get the access token
                            // as well
                        } catch (KeyManagementException e) {
                            // This is exception is not thrown intentionally because key manager mock servie does not
                            // have keys once the server is started. We need to re write this properly once the key
                            // manager is available.
                            log.error("Error while getting keys fo application : " + applicationId, e);
                        }
                    }

                }
                applicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(application);
                return Response.ok().entity(applicationDTO).header(HttpHeaders.ETAG,
                        "\"" + existingFingerprint + "\"").build();
            } else {
                String errorMessage = "Application not found: " + applicationId;
                APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.APPLICATION_NOT_FOUND);
                HashMap<String, String> paramList = new HashMap<String, String>();
                paramList.put(APIMgtConstants.ExceptionsConstants.APPLICATION_ID, applicationId);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                log.error(errorMessage, e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving application: " + applicationId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.APPLICATION_ID, applicationId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrieves the fingerprint of a application given its UUID
     *
     * @param applicationId   application Id
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return Retrieves the fingerprint of a application
     */
    public String applicationsApplicationIdGetFingerprint(String applicationId, String accept, String ifNoneMatch,
                                                          String ifModifiedSince, Request request) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            String lastUpdatedTime = RestApiUtil.getConsumer(username).getLastUpdatedTimeOfApplication(applicationId);
            return ETagUtils.generateETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            //gives a warning and let it continue the execution
            String errorMessage = "Error while retrieving last updated time of application " + applicationId;
            log.error(errorMessage, e);
            return null;
        }
    }

    /**
     * Updates an existing application
     *
     * @param applicationId     application Id
     * @param body              Application details to be updated
     * @param contentType       Content-Type header
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Updated application details as the response
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response applicationsApplicationIdPut(String applicationId, ApplicationDTO body,
                                                 String contentType, String ifMatch, String ifUnmodifiedSince,
                                                 Request request) throws NotFoundException {
        ApplicationDTO updatedApplicationDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = RestApiUtil.getConsumer(username);
            String existingFingerprint = applicationsApplicationIdGetFingerprint(applicationId, null, null, null,
                    request);
            if (!StringUtils.isEmpty(ifMatch) && !StringUtils.isEmpty(existingFingerprint) && !ifMatch
                    .contains(existingFingerprint)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }

            Application application = ApplicationMappingUtil.fromDTOtoApplication(body, username);
            if (!ApplicationStatus.APPLICATION_APPROVED.equals(application.getStatus())) {
                String errorMessage = "Application " + applicationId + " is not active";
                ExceptionCodes exceptionCode = ExceptionCodes.APPLICATION_INACTIVE;
                APIManagementException e = new APIManagementException(errorMessage, exceptionCode);
                Map<String, String> paramList = new HashMap<>();
                paramList.put(APIMgtConstants.ExceptionsConstants.APPLICATION_ID, applicationId);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                log.error(errorMessage, e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
                
            }
            WorkflowResponse updateResponse = apiConsumer.updateApplication(applicationId, application);
            if (WorkflowStatus.REJECTED == updateResponse.getWorkflowStatus()) {
                String errorMessage = "Update request for application " + applicationId + " is rejected";
                ExceptionCodes exceptionCode = ExceptionCodes.WORKFLOW_REJCECTED;
                APIManagementException e = new APIManagementException(errorMessage, exceptionCode);
                Map<String, String> paramList = new HashMap<>();
                paramList.put(APIMgtConstants.ExceptionsConstants.APPLICATION_ID, applicationId);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
            }

            //retrieves the updated application and send as the response
            Application updatedApplication = apiConsumer.getApplication(applicationId, username, null);
            updatedApplicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(updatedApplication);
            String newFingerprint = applicationsApplicationIdGetFingerprint(applicationId, null, null, null,
                    request);
            
            
            //if workflow is in pending state or if the executor sends any httpworklfowresponse (workflow state can 
            //be in either pending or approved state) send back the workflow response 
            if (ApplicationStatus.APPLICATION_ONHOLD.equals(updatedApplication.getStatus())) {
                
                WorkflowResponseDTO workflowResponse = MiscMappingUtil
                        .fromWorkflowResponsetoDTO(updateResponse);
                URI location = new URI(RestApiConstants.RESOURCE_PATH_APPLICATIONS + "/" + applicationId);
                return Response.status(Response.Status.ACCEPTED).header(RestApiConstants.LOCATION_HEADER, location)
                        .entity(workflowResponse).build();
            }    
            return Response.ok().entity(updatedApplicationDTO).header(HttpHeaders.ETAG, "\"" + newFingerprint + "\"")
                    .build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating application: " + body.getName();
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.APPLICATION_NAME, body.getName());
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while adding location header in response for application : " + body.getName();
            Map<String, String> paramList = new HashMap<>();
            paramList.put(APIMgtConstants.ExceptionsConstants.APPLICATION_NAME, body.getName());
            ErrorHandler errorHandler = ExceptionCodes.LOCATION_HEADER_INCORRECT;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorHandler, paramList);
            log.error(errorMessage, e);
            return Response.status(errorHandler.getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Generates keys for the application
     *
     * @param applicationId     application Id
     * @param body              Key generation request details
     * @param contentType       Content-Type header
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Generated application key details
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response applicationsGenerateKeysPost(String applicationId, ApplicationKeyGenerateRequestDTO body,
                                                 String contentType, String ifMatch, String ifUnmodifiedSince,
                                                 Request request) throws NotFoundException {
        ApplicationKeyDTO applicationKeyDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = RestApiUtil.getConsumer(username);
            Application application = apiConsumer.getApplication(applicationId, username, null);
            if (application != null && !ApplicationStatus.APPLICATION_APPROVED.equals(application.getStatus())) {
                String errorMessage = "Application " + applicationId + " is not active";
                ExceptionCodes exceptionCode = ExceptionCodes.APPLICATION_INACTIVE;
                APIManagementException e = new APIManagementException(errorMessage, exceptionCode);
                HashMap<String, String> paramList = new HashMap<String, String>();
                paramList.put(APIMgtConstants.ExceptionsConstants.APPLICATION_ID, applicationId);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                log.error(errorMessage, e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
                
            }
            if (application != null) {
                String[] accessAllowDomainsArray = body.getAccessAllowDomains().toArray(new String[1]);
                String tokenScopes = StringUtils.join(body.getScopes(), " ");

                Map<String, Object> keyDetails = apiConsumer
                        .generateApplicationKeys(username, application.getName(), applicationId, body.getKeyType()
                                        .toString(),
                                body.getCallbackUrl(), accessAllowDomainsArray, body.getValidityTime(), tokenScopes,
                                application.getGroupId());
                applicationKeyDTO = ApplicationKeyMappingUtil
                        .fromApplicationKeyToDTO(keyDetails, body.getKeyType().toString());
            } else {
                String errorMessage = "Application not found for key geneation: " + applicationId;
                APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.APPLICATION_NOT_FOUND);
                HashMap<String, String> paramList = new HashMap<String, String>();
                paramList.put(APIMgtConstants.ExceptionsConstants.APPLICATION_ID, applicationId);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                log.error(errorMessage, e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while generating keys for application";
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.APPLICATION_ID, applicationId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
        return Response.ok().entity(applicationKeyDTO).build();
    }

    /**
     * Retrieves all applications that qualifies for the search query
     *
     * @param query       Search query
     * @param limit       Max number of applications to return
     * @param offset      Starting position of pagination
     * @param accept      Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param request     msf4j request object
     * @return A list of qualifying application DTOs as the response
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response applicationsGet(String query, Integer limit, Integer offset, String accept,
                                    String ifNoneMatch, Request request) throws NotFoundException {

        ApplicationListDTO applicationListDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        String groupId = RestApiUtil.getLoggedInUserGroupId();

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        try {
            APIStore apiConsumer = RestApiUtil.getConsumer(username);
            List<Application> allMatchedApps = new ArrayList<>();
            if (StringUtils.isBlank(query)) {
                allMatchedApps = apiConsumer.getApplications(username, groupId);
            } else {
                Application application = apiConsumer.getApplicationByName(username, query, groupId);
                if (application != null) {
                    allMatchedApps = new ArrayList<>();
                    allMatchedApps.add(application);
                }
            }

            //allMatchedApps are already sorted to application name
            applicationListDTO = ApplicationMappingUtil.fromApplicationsToDTO(allMatchedApps, limit, offset);
            ApplicationMappingUtil
                    .setPaginationParams(applicationListDTO, groupId, limit, offset, allMatchedApps.size());
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving applications";
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.APPLICATION_NAME, query);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
        return Response.ok().entity(applicationListDTO).build();
    }

    /**
     * Adds a new application
     *
     * @param body        Application details to be added
     * @param contentType Content-Type header
     * @param request     msf4j request object
     * @return 201 Response if successful
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response applicationsPost(ApplicationDTO body, String contentType, Request request)
            throws NotFoundException {
        URI location = null;
        ApplicationDTO createdApplicationDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = RestApiUtil.getConsumer(username);
            Application application = ApplicationMappingUtil.fromDTOtoApplication(body, username);
            String groupId = RestApiUtil.getLoggedInUserGroupId();
            application.setGroupId(groupId);
            ApplicationCreationResponse applicationResponse = apiConsumer.addApplication(application);
            String applicationUUID = applicationResponse.getApplicationUUID();
            Application createdApplication = apiConsumer.getApplication(applicationUUID, username, groupId);
            createdApplicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(createdApplication);
           
            location = new URI(RestApiConstants.RESOURCE_PATH_APPLICATIONS + "/" +
                    createdApplicationDTO.getApplicationId());

            //if workflow is in pending state or if the executor sends any httpworklfowresponse (workflow state can 
            //be in either pending or approved state) send back the workflow response 
            if (ApplicationStatus.APPLICATION_ONHOLD.equals(createdApplication.getStatus())) {
                
                WorkflowResponseDTO workflowResponse = MiscMappingUtil
                        .fromWorkflowResponsetoDTO(applicationResponse.getWorkflowResponse());
                return Response.status(Response.Status.ACCEPTED).header(RestApiConstants.LOCATION_HEADER, location)
                        .entity(workflowResponse).build();
            }    

        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new application : " + body.getName();
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.APPLICATION_NAME, body.getName());
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while adding location header in response for application : " + body.getName();
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.APPLICATION_NAME, body.getName());
            ErrorHandler errorHandler = ExceptionCodes.LOCATION_HEADER_INCORRECT;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorHandler, paramList);
            log.error(errorMessage, e);
            return Response.status(errorHandler.getHttpStatusCode()).entity(errorDTO).build();
        }
        // TODO: set location header once msf4j is updates : Response.created(location).entity(createdApplicationDTO)
        // .build()
        return Response.status(Response.Status.CREATED).header(RestApiConstants.LOCATION_HEADER, location)
                .entity(createdApplicationDTO).build();
    }
}
