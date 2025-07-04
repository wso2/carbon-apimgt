package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.NotifyGatewayPayloadDTO;
import org.wso2.carbon.apimgt.internal.service.dto.NotifyGatewayStatusResponseDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class NotifyGatewayApiServiceImpl implements NotifyGatewayApiService {

    public Response notifyGatewayPost(NotifyGatewayPayloadDTO notifyGatewayPayloadDTO, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((int) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setFields("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }
}
