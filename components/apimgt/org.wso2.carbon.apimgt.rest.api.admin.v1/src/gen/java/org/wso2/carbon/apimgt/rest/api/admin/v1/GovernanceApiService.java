package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoveredAPIDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoveredAPIListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoverySummaryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.UntraffickedListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface GovernanceApiService {
      public Response getDiscoveredAPIById(String discoveredApiId, MessageContext messageContext) throws APIManagementException;
      public Response getDiscoveredAPIs(String classification, String service, String internal, Integer limit, Integer offset, MessageContext messageContext) throws APIManagementException;
      public Response getDiscoverySummary(MessageContext messageContext) throws APIManagementException;
      public Response getUntrafficked(MessageContext messageContext) throws APIManagementException;
}
