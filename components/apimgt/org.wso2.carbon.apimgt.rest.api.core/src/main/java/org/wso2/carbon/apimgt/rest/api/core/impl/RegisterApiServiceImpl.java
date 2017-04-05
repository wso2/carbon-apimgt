package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;

import java.util.HashMap;
import java.util.List;

import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;

import java.io.InputStream;

import org.wso2.carbon.apimgt.rest.api.core.RegisterApiService;
import org.wso2.carbon.apimgt.rest.api.core.dto.RegistrationDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.RegistrationSummaryDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.MappingUtil;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class RegisterApiServiceImpl extends RegisterApiService {

    private static final Logger log = LoggerFactory.getLogger(RegisterApiServiceImpl.class);

    /**
     * Register gateway
     *
     * @param body        RegistrationDTO
     * @param contentType Content-Type header value
     * @return Registration summary details
     * @throws NotFoundException If failed to register gateway
     */
    @Override
    public Response registerPost(RegistrationDTO body, String contentType) throws NotFoundException {

        try {
            APIMgtAdminService adminService = RestApiUtil.getAPIMgtAdminService();
            String overwriteLabels = body.getLabelInfo().getOverwriteLabels();
            List<Label> labels = MappingUtil.convertToLabels(body.getLabelInfo().getLabelList());
            adminService.registerGatewayLabels(labels, overwriteLabels);
            //TODO : Add registration summary details based on the sharing values
            RegistrationSummaryDTO registrationSummaryDTO = new RegistrationSummaryDTO();
            return Response.ok().entity(registrationSummaryDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while registering the gateway";
            HashMap<String, String> paramList = new HashMap<String, String>();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }

    }
}
