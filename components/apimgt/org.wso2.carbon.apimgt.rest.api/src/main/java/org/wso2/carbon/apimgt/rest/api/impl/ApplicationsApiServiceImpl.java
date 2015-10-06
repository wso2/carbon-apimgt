package org.wso2.carbon.apimgt.rest.api.impl;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.rest.api.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.utils.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.utils.mappings.ApplicationMappingUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

public class ApplicationsApiServiceImpl extends ApplicationsApiService {
    @Override
    public Response applicationsGet(String subscriber, String groupId, String limit, String offset, String accept,
            String ifNoneMatch) {
        String username = RestApiUtil.getLoggedInUsername();
        if (groupId == null) {
            groupId = "";
        }
        if (subscriber == null) {
            subscriber = username;
        }

        List<ApplicationDTO> applicationDTOList = new ArrayList<>();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application[] applications = apiConsumer.getApplications(new Subscriber(subscriber), groupId);
            for (Application application : applications) {
                applicationDTOList.add(ApplicationMappingUtil.fromApplicationtoDTO(application));
            }
            return Response.ok().entity(applicationDTOList).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response applicationsPost(ApplicationDTO body, String contentType) {
        String username = RestApiUtil.getLoggedInUsername();
        String subscriber = body.getSubscriber();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = ApplicationMappingUtil.fromDTOtoApplication(body);
            int applicationId = apiConsumer.addApplication(application, subscriber);

            //retrieves the created application and send as the response
            Application createdApplication = apiConsumer.getApplicationById(applicationId);
            ApplicationDTO createdApplicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(createdApplication);

            //to be set as the Location header
            URI location = new URI(RestApiConstants.RESOURCE_PATH_APPLICATIONS + "/" +
                    createdApplicationDTO.getApplicationId());
            return Response.created(location).entity(createdApplicationDTO).build();
        } catch (APIManagementException | URISyntaxException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response applicationsApplicationIdGet(String applicationId, String accept, String ifNoneMatch,
            String ifModifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            ApplicationDTO applicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(application);
            return Response.ok().entity(applicationDTO).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response applicationsApplicationIdPut(String applicationId, ApplicationDTO body, String contentType, 
            String ifMatch, String ifUnmodifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        Application application = ApplicationMappingUtil.fromDTOtoApplication(body);
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            apiConsumer.updateApplicationByUUID(application);

            //retrieves the updated application and send as the response
            Application updatedApplication = apiConsumer.getApplicationByUUID(applicationId);
            ApplicationDTO updatedApplicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(updatedApplication);
            return Response.ok().entity(updatedApplicationDTO).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response applicationsApplicationIdDelete(String applicationId, String ifMatch,
            String ifUnmodifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            apiConsumer.removeApplicationByUUID(applicationId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }
    @Override
    public Response applicationsApplicationIdGenerateKeysPost(String applicationId,ApplicationDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
