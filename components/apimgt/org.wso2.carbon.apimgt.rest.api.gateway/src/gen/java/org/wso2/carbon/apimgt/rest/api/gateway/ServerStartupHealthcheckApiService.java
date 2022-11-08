package org.wso2.carbon.apimgt.rest.api.gateway;

import org.wso2.carbon.apimgt.rest.api.gateway.*;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;


import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ServerStartupHealthcheckApiService {
      public Response serverStartupHealthcheckGet(MessageContext messageContext) throws APIManagementException;
}
