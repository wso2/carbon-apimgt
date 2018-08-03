package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.RegistrationSummary;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.GatewaysApiService;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;

import java.util.HashMap;
import java.util.List;

import java.io.InputStream;

import org.wso2.carbon.apimgt.rest.api.core.dto.LabelInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.RegistrationDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.RegistrationSummaryDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.MappingUtil;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class GatewaysApiServiceImpl extends GatewaysApiService {

    private APIMgtAdminService adminService;

    private static final Logger log = LoggerFactory.getLogger(GatewaysApiServiceImpl.class);

    public GatewaysApiServiceImpl(APIMgtAdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Register gateway
     *
     * @param body        RegistrationDTO
     * @param contentType Content-Type header value
     * @return Registration summary details
     * @throws NotFoundException If failed to register gateway
     */
    @Override
    public Response gatewaysRegisterPost(RegistrationDTO body, String contentType, Request request)
            throws NotFoundException {

        try {
            LabelInfoDTO labelInfoDTO = body.getLabelInfo();

            if (labelInfoDTO != null) {
                String overwriteLabels = labelInfoDTO.getOverwriteLabels();
                List<Label> labels = MappingUtil.convertToLabels(labelInfoDTO.getLabelList());
                adminService.registerGatewayLabels(labels, overwriteLabels);
                RegistrationSummary registrationSummary = adminService.getRegistrationSummary();
                return Response.ok().entity(MappingUtil.toRegistrationSummaryDTO(registrationSummary)).build();
            } else {
                String errorMessage = "Label information cannot be null";
                APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.LABEL_INFORMATION_CANNOT_BE_NULL);
                HashMap<String, String> paramList = new HashMap<String, String>();
                org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                log.error(errorMessage, e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
            }

        } catch (APIManagementException e) {
            String errorMessage = "Error while registering the gateway";
            HashMap<String, String> paramList = new HashMap<String, String>();
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
