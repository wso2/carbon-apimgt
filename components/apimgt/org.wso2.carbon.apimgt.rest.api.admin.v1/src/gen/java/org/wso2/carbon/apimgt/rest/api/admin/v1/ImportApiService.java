package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APIInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApplicationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import java.io.File;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ImportApiService {
      public Response importApiPost(InputStream fileInputStream, Attachment fileDetail, Boolean preserveProvider, Boolean overwrite, MessageContext messageContext) throws APIManagementException;
      public Response importApiProductPost(InputStream fileInputStream, Attachment fileDetail, Boolean preserveProvider, Boolean importAPIs, Boolean overwriteAPIProduct, Boolean overwriteAPIs, MessageContext messageContext) throws APIManagementException;
      public Response importApplicationsPost(InputStream fileInputStream, Attachment fileDetail, Boolean preserveOwner, Boolean skipSubscriptions, String appOwner, Boolean skipApplicationKeys, Boolean update, MessageContext messageContext) throws APIManagementException;
}
