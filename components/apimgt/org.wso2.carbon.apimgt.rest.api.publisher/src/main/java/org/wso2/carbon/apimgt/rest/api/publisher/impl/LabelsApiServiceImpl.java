package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import java.util.HashMap;
import java.util.List;

import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:47:43.416+05:30")
public class LabelsApiServiceImpl extends LabelsApiService {

    private static final Logger log = LoggerFactory.getLogger(LabelsApiServiceImpl.class);

    /**
     * Get all the labels.
     *
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param minorVersion    Minor ersion header value
     * @return Lable List
     * @throws NotFoundException If failed to get the label values
     */
    @Override
    public Response labelsGet(String accept
            , String ifNoneMatch
            , String ifModifiedSince
            , String minorVersion
    ) throws NotFoundException {

        String username = RestApiUtil.getLoggedInUsername();

        try {
            List<Label> labels = RestAPIPublisherUtil.getApiPublisher(username).getAllLabels();
            LabelListDTO labelListDTO = MappingUtil.toLabelListDTO(labels);
            return Response.ok().entity(labelListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving Labels";
            HashMap<String, String> paramList = new HashMap<String, String>();
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }

    }
}
