package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APICategoryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APICategoryListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ApiCategoriesApiService {
      public Response apiCategoriesApiCategoryIdDelete(String apiCategoryId, MessageContext messageContext) throws APIManagementException;
      public Response apiCategoriesApiCategoryIdPut(String apiCategoryId, APICategoryDTO apICategoryDTO, MessageContext messageContext) throws APIManagementException;
      public Response apiCategoriesGet(MessageContext messageContext) throws APIManagementException;
      public Response apiCategoriesPost(APICategoryDTO apICategoryDTO, MessageContext messageContext) throws APIManagementException;
}
