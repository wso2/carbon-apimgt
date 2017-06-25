package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.wso2.carbon.apimgt.core.api.WSDLProcessor;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtWSDLException;
import org.wso2.carbon.apimgt.core.impl.WSDLProcessFactory;
import org.wso2.carbon.apimgt.core.models.WSDLInfo;
import org.wso2.carbon.apimgt.core.util.APIMWSDLUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.WsdlsApiService;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.WSDLValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class WsdlsApiServiceImpl extends WsdlsApiService {
    @Override
    public Response wsdlsValidatePost(String wsdlUrl, Request request) throws NotFoundException {
        try {
            WSDLProcessor processor = WSDLProcessFactory.getInstance().getWSDLProcessor(wsdlUrl);
            WSDLInfo info = processor.getWsdlInfo();
            if (info != null) {
                WSDLValidationResponseDTO responseDTO = MappingUtil.toWSDLValidationResponseDTO(info);
                return Response.ok(responseDTO).build();
            }
        } catch (APIMgtWSDLException e) {
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
