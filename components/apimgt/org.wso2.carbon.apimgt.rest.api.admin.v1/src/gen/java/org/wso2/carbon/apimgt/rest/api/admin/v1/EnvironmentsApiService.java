package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface EnvironmentsApiService {
      public Response environmentsEnvironmentIdDelete(String environmentId, MessageContext messageContext) throws APIManagementException;
      public Response environmentsEnvironmentIdPut(String environmentId, EnvironmentDTO environmentDTO, MessageContext messageContext) throws APIManagementException;
      public Response environmentsGet(MessageContext messageContext) throws APIManagementException;
      public Response environmentsPost(EnvironmentDTO environmentDTO, MessageContext messageContext) throws APIManagementException;
}
