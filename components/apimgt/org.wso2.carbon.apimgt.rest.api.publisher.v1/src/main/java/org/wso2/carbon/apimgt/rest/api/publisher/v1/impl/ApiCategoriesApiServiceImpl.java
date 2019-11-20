package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.impl.utils.APICategoryUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APICategoryListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.APICategoryMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class ApiCategoriesApiServiceImpl implements ApiCategoriesApiService {
    private static final Log log = LogFactory.getLog(ApiCategoriesApiServiceImpl.class);
    
    public Response apiCategoriesGet(MessageContext messageContext) {
          try {
              String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
              int tenantID = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
              List<APICategory> categoryList = APICategoryUtil.getAllAPICategoriesOfTenant(tenantID);
              APICategoryListDTO categoryListDTO =
                      APICategoryMappingUtil.fromCategoryListToCategoryListDTO(categoryList);
              return Response.ok().entity(categoryListDTO).build();
          } catch (APIManagementException e) {
              String errorMessage = "Error while retrieving API categories";
              RestApiUtil.handleInternalServerError(errorMessage, e, log);
          }
          return null;
    }
}
