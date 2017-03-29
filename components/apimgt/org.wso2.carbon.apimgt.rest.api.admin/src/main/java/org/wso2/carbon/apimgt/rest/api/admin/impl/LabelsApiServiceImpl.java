package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ErrorHandler;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;


import java.util.HashMap;
import java.util.List;

import org.wso2.carbon.apimgt.rest.api.admin.LabelsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;

import java.io.InputStream;

import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class LabelsApiServiceImpl extends LabelsApiService {

    private static final Logger log = LoggerFactory.getLogger(LabelsApiServiceImpl.class);

    /**
     * Delete label by label id
     *
     * @param labelId           Id of the label
     * @param ifMatch           if-Match header value
     * @param ifUnmodifiedSince if-Unmodified-Since header value
     * @param minorVersion      Minor ersion header value
     * @return 200 OK if the operation is successful
     * @throws NotFoundException If failed to find the particular resource
     */
    @Override
    public Response labelsLabelIdDelete(String labelId, String ifMatch, String ifUnmodifiedSince, String minorVersion
    ) throws NotFoundException {

        try {
            if (labelId != null) {
                APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
                apiMgtAdminService.deleteLabel(labelId);
            } else {
                //mandatory parameters not provided
                String errorMessage = "Label Id parameter should be provided";
                ErrorHandler errorHandler = ExceptionCodes.PARAMETER_NOT_PROVIDED;
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorHandler);
                log.error(errorMessage);
                return Response.status(errorHandler.getHttpStatusCode()).entity(errorDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while deleting the label [labelId] " + labelId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }

        return Response.ok().build();
    }
}
