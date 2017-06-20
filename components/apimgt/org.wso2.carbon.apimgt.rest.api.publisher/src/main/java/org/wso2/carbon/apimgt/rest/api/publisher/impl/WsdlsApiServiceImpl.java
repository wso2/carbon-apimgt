package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.WSDLInfo;
import org.wso2.carbon.apimgt.core.util.APIMWSDLUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.WsdlsApiService;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.WSDLValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public class WsdlsApiServiceImpl extends WsdlsApiService {
    @Override
    public Response wsdlsValidatePost(String wsdlUrl, Request request) throws NotFoundException {
        try {
            WSDLInfo info = APIMWSDLUtils.getWSDLInfo(wsdlUrl);
            if (info != null) {
                WSDLValidationResponseDTO responseDTO = MappingUtil.toWSDLValidationResponseDTO(info);
                return Response.ok(responseDTO).build();
            }
        } catch (APIManagementException e) {
            e.printStackTrace();
        }
        return Response.ok().entity(buildInvalidWSDLResponseDTO()).build();
    }

    private WSDLValidationResponseDTO buildInvalidWSDLResponseDTO() {
        WSDLValidationResponseDTO responseDTO = new WSDLValidationResponseDTO();
        responseDTO.isValid(false);
        return responseDTO;
    }
}
