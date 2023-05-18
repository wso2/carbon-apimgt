package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.UUIDListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface RetrieveRuntimeArtifactsApiService {
      public Response retrieveRuntimeArtifactsGet(String type, String dataPlaneId, String gatewayAccessibilityType, MessageContext messageContext) throws APIManagementException;
      public Response retrieveRuntimeArtifactsPost(String type, String dataPlaneId, String gatewayAccessibilityType, UUIDListDTO uuidList, MessageContext messageContext) throws APIManagementException;
}
