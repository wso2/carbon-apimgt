package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import java.io.File;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.wso2.carbon.apimgt.rest.api.admin.v1.ExportApi.*;


public interface ExportApiService {
      public Response exportApiGet(String name, String version, String providerName, FormatEnum format, Boolean preserveStatus, MessageContext messageContext) throws APIManagementException;
      public Response exportApiProductGet(String name, String version, String providerName, FileFormatEnum fileFormat, Boolean preserveStatus, MessageContext messageContext) throws APIManagementException;
      public Response exportApplicationsGet(String appName, String appOwner, Boolean withKeys, MessageContext messageContext) throws APIManagementException;
}
