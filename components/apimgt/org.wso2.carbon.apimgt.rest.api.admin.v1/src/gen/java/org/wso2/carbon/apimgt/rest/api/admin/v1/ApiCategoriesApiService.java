package org.wso2.carbon.apimgt.rest.api.admin.v1;


import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APICategoryDTO;


import javax.ws.rs.core.Response;


public interface ApiCategoriesApiService {
      public Response apiCategoriesApiCategoryIdDelete(String apiCategoryId, MessageContext messageContext) throws APIManagementException;
      public Response apiCategoriesApiCategoryIdPut(String apiCategoryId, APICategoryDTO apICategoryDTO, MessageContext messageContext) throws APIManagementException;
      public Response apiCategoriesGet(MessageContext messageContext) throws APIManagementException;
      public Response apiCategoriesPost(APICategoryDTO apICategoryDTO, MessageContext messageContext) throws APIManagementException;
}
