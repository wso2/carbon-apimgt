package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APICategoryListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ApiCategoriesApiService {
      public Response apiCategoriesGet(String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
}
