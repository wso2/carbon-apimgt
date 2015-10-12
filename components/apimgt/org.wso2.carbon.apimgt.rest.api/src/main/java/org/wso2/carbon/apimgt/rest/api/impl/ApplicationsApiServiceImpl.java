package org.wso2.carbon.apimgt.rest.api.impl;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;

import org.wso2.carbon.apimgt.rest.api.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.exception.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.utils.mappings.ApplicationMappingUtil;
import org.wso2.carbon.context.CarbonContext;

import java.util.ArrayList;
import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;

public class ApplicationsApiServiceImpl extends ApplicationsApiService {
    @Override
    public Response applicationsGet(String subscriber, String groupId, String limit, String offset, String accept,
            String ifNoneMatch) {
        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (groupId == null) {
            groupId = "";
        }
        if (subscriber == null) { //todo: implement permission
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
        //todo: validation, need to be moved
        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isEmpty(body.getName())) {
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Name is empty"))
                    .build();
        }
        if (StringUtils.isEmpty(body.getThrottlingTier())) {
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Tier is empty"))
                    .build();
        }
        String subscriber = body.getSubscriber(); //todo: implement permission
        if (subscriber == null) { 
            subscriber = username;
        }

        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = ApplicationMappingUtil.fromDTOtoApplication(body, new Subscriber(subscriber));
            String status = apiConsumer.addApplication(application, subscriber);  //todo: use "status" properly
            return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, status)).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response applicationsApplicationIdGet(String applicationId, String subscriber, String accept,
            String ifNoneMatch, String ifModifiedSince) {
        //todo: need to be improved. Should avoid iteration through all applications.       
        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (subscriber == null) { //todo: implement permission
            subscriber = username;
        }
        String groupId = "";  //todo: add to model
        int applicationID = Integer.parseInt(applicationId);
        ApplicationDTO applicationDTO = null;
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application[] applications = apiConsumer.getApplications(new Subscriber(subscriber), groupId);
            for (Application application : applications) {
                if (application.getId() == applicationID) {
                    applicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(application);
                }
            }
            if (applicationDTO != null) {
                return Response.ok().entity(applicationDTO).build();
            } else {
                throw new NotFoundException();
            }
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response applicationsApplicationIdPut(String applicationId, ApplicationDTO body, String contentType, 
            String ifMatch, String ifUnmodifiedSince) {
        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String subscriber = body.getSubscriber(); //todo: implement permission
        if (subscriber == null) {
            subscriber = username;
        }
        body.setApplicationId(Integer.parseInt(applicationId));
        Application application = ApplicationMappingUtil.fromDTOtoApplication(body, new Subscriber(subscriber));
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            apiConsumer.updateApplication(application);
            return Response.ok().build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response applicationsApplicationIdDelete(String applicationId, String subscriber, String ifMatch,
            String ifUnmodifiedSince) {
        //todo: need to be improved. Should avoid iteration through all applications.

        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (subscriber == null) { //todo: implement permission
            subscriber = username;
        }
        String groupId = "";
        int applicationID = Integer.parseInt(applicationId);
        boolean removed = false;
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application[] applications = apiConsumer.getApplications(new Subscriber(subscriber), groupId);
            for (Application application : applications) {
                if (application.getId() == applicationID) {
                    apiConsumer.removeApplication(application);
                    removed = true;
                    break;
                }
            }
            if (removed) {
                return Response.ok().build();
            } else {
                throw new NotFoundException();
            }
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
