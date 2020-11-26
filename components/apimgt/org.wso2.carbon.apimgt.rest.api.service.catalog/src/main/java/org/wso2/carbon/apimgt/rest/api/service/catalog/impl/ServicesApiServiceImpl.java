package org.wso2.carbon.apimgt.rest.api.service.catalog.impl;

import org.wso2.carbon.apimgt.rest.api.service.catalog.*;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class ServicesApiServiceImpl implements ServicesApiService {

    public Response checkServiceExistence(String name, String version, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status  = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response createService(ServiceDTO catalogEntry, InputStream definitionFileInputStream, Attachment definitionFileDetail, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status  = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response deleteService(String serviceId, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status  = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response exportService(String name, String version, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status  = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response getServiceById(String serviceId, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status  = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response getServiceDefinition(String serviceId, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status  = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response importService(String serviceId, String ifMatch, Boolean overwrite, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status  = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response searchServices(String name, String version, String definitionType, String displayName, String sortBy, String sortOrder, Integer limit, Integer offset, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status  = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response updateService(String serviceId, ServiceDTO catalogEntry, InputStream definitionFileInputStream, Attachment definitionFileDetail, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status  = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }
}
