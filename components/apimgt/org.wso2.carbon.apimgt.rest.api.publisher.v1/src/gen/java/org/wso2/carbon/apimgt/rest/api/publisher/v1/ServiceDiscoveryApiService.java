package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ServiceDiscoveriesInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ServiceDiscoverySystemTypeListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ServiceDiscoveryApiService {
      public Response serviceDiscoveryEndpointsGet(Integer limit, Integer offset, MessageContext messageContext) throws APIManagementException;
      public Response serviceDiscoveryEndpointsTypeGet(String type, Integer limit, Integer offset, MessageContext messageContext) throws APIManagementException;
}
