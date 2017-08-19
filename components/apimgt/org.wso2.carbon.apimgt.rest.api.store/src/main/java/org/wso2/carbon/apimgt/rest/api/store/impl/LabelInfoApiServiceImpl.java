package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ErrorHandler;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.dto.LabelListDTO;
import org.wso2.carbon.apimgt.rest.api.store.LabelInfoApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.mappings.LabelMappingUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.Response;
import org.wso2.msf4j.Request;

/**
 * Implementation of Label info resource
 */
public class LabelInfoApiServiceImpl extends LabelInfoApiService {

    private static final Logger log = LoggerFactory.getLogger(LabelInfoApiServiceImpl.class);

    /**
     * Get label information for labels provided.
     *
     * @param labels          List of labels
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return Label List
     * @throws NotFoundException If failed to get the label values
     */
    @Override
    public Response labelInfoGet(String labels, String ifNoneMatch, String ifModifiedSince, Request request)
            throws NotFoundException {

        String username = RestApiUtil.getLoggedInUsername(request);
        LabelListDTO labelListDTO;
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            if (labels != null) {
                List<String> labelNames = Arrays.asList(labels.split(","));
                List<Label> labelList = apiStore.getLabelInfo(labelNames, username);
                labelListDTO = LabelMappingUtil.toLabelListDTO(labelList);
            } else {
                //mandatory parameters not provided
                String errorMessage = "Labels parameter should be provided";
                ErrorHandler errorHandler = ExceptionCodes.PARAMETER_NOT_PROVIDED;
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorHandler);
                log.error(errorMessage);
                return Response.status(errorHandler.getHttpStatusCode()).entity(errorDTO).build();
            }

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving label information";
            HashMap<String, String> paramList = new HashMap<String, String>();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
        return Response.ok().entity(labelListDTO).build();
    }
}
