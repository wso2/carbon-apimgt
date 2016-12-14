package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.common.ApplicationConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:48:55.078+05:30")
public class ApplicationsApiServiceImpl extends ApplicationsApiService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationsApiServiceImpl.class);

    @Override
    public
    Response applicationsApplicationIdDelete(String applicationId, String ifMatch, String ifUnmodifiedSince)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = RestApiUtil.getConsumer(username);
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

    @Override
    public Response applicationsApplicationIdGet(String applicationId, String accept, String ifNoneMatch,
            String ifModifiedSince) throws NotFoundException {
        ApplicationDTO applicationDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = RestApiUtil.getConsumer(username);
            Application application = apiConsumer.getApplication(applicationId, username, null);
            if (application != null) {
                applicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(application);
            } else {
                String errorMessage = "Application not found: " + applicationId;
                APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(
                        errorMessage, ExceptionCodes.APPLICATION_NOT_FOUND);
                HashMap<String, String> paramList = new HashMap<String, String>();
                paramList.put(APIMgtConstants.ExceptionsConstants.APPLICATION_ID, applicationId);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                log.error(errorMessage,e);
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
        return Response.ok().entity(applicationDTO).build();
    }

    @Override
    public Response applicationsApplicationIdPut(String applicationId, ApplicationDTO body, String contentType,
            String ifMatch, String ifUnmodifiedSince) throws NotFoundException {
        ApplicationDTO updatedApplicationDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = RestApiUtil.getConsumer(username);
            Application application = ApplicationMappingUtil.fromDTOtoApplication(body, username);
            apiConsumer.updateApplication(applicationId, application);

            //retrieves the updated application and send as the response
            Application updatedApplication = apiConsumer.getApplication(applicationId, username, null);
            updatedApplicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(updatedApplication);

        } catch (APIManagementException e) {
            String errorMessage = "Error while updating application: " + body.getName();
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.APPLICATION_NAME, body.getName());
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
        return Response.ok().entity(updatedApplicationDTO).build();
    }

    @Override
    public Response applicationsGenerateKeysPost(String applicationId, ApplicationKeyGenerateRequestDTO body,
            String contentType, String ifMatch, String ifUnmodifiedSince) throws NotFoundException {
        ApplicationKeyDTO applicationKeyDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = RestApiUtil.getConsumer(username);
            Application application = apiConsumer.getApplication(applicationId, username, null);
            if (application != null) {
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
            ApplicationMappingUtil.setPaginationParams(applicationListDTO, groupId, limit, offset,
                    allMatchedApps.size());
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

    @Override public Response applicationsPost(ApplicationDTO body, String contentType) throws NotFoundException {
        URI location = null;
        ApplicationDTO createdApplicationDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiConsumer = RestApiUtil.getConsumer(username);
            Application application = ApplicationMappingUtil.fromDTOtoApplication(body, username);
            String groupId = RestApiUtil.getLoggedInUserGroupId();
            application.setGroupId(groupId);
            String applicationUUID = apiConsumer.addApplication(application);

            Application createdApplication = apiConsumer.getApplication(applicationUUID, username, groupId);
            createdApplicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(createdApplication);
            //MSf4j support needed
//            location = new URI(RestApiConstants.RESOURCE_PATH_APPLICATIONS + "/" +
//                    createdApplicationDTO.getApplicationId());
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new application : " + body.getName();
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.APPLICATION_NAME, body.getName());
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }

//        return Response.created(location).entity(createdApplicationDTO).build();
          return Response.status(Response.Status.CREATED).entity(createdApplicationDTO).build();
    }
}
