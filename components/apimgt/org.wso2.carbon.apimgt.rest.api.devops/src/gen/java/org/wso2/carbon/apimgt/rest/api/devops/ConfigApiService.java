package org.wso2.carbon.apimgt.rest.api.devops;

import org.wso2.carbon.apimgt.rest.api.devops.*;
import org.wso2.carbon.apimgt.rest.api.devops.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.devops.dto.CorrelationComponentsListDTO;
import org.wso2.carbon.apimgt.rest.api.devops.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ConfigApiService {
      public Response configCorrelationGet(MessageContext messageContext) throws APIManagementException;
      public Response configCorrelationPut(CorrelationComponentsListDTO correlationComponentsListDTO, MessageContext messageContext) throws APIManagementException;
}
