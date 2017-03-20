package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.util.ETagUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:47:43.416+05:30")
public class ApplicationsApiServiceImpl extends ApplicationsApiService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationsApiServiceImpl.class);

    @Override
    public Response applicationsApplicationIdGet(String applicationId
            , String accept
            , String ifNoneMatch
            , String ifModifiedSince
            , String minorVersion
    ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String existingFingerprint = applicationsApplicationIdGetFingerprint(applicationId, accept, ifNoneMatch,
                    ifModifiedSince, minorVersion);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }

            Application application = apiPublisher.getApplication(applicationId, username, null);

            if (application == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }

            ApplicationDTO applicationDTO = MappingUtil.toApplicationDto(application);
            return Response.ok().header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"").entity(applicationDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving application " + applicationId, e, log);
            return null;
        }

    }

    public String applicationsApplicationIdGetFingerprint(String applicationId, String accept, String ifNoneMatch,
            String ifModifiedSince, String minorVersion) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            String lastUpdatedTime = RestAPIPublisherUtil.getApiPublisher(username)
                    .getLastUpdatedTimeOfApplication(applicationId);
            return ETagUtils.generateETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            //gives a warning and let it continue the execution
            String errorMessage = "Error while retrieving last updated time of application " + applicationId;
            log.error(errorMessage, e);
            return null;
        }
    }
}
