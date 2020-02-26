package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DeploymentsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.DeploymentsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class DeploymentsApiServiceImpl implements DeploymentsApiService {

    private static final Log log = LogFactory.getLog(DeploymentsApiServiceImpl.class);


      public Response deploymentsGet(MessageContext messageContext) {
      try{
          DeploymentsMappingUtil deploymentsMappingUtil = new DeploymentsMappingUtil();
//          List<DeploymentsDTO> deploymentsDTOS = deploymentsMappingUtil.fromTenantConftoDTO();
         DeploymentListDTO deploymentListDTO = deploymentsMappingUtil.fromTenantConftoDTO();
          return Response.ok().entity(deploymentListDTO).build();
      }catch (APIManagementException e) {
          String errorMessage = "Error while retrieving Deployments details form tenant-conf.json";
          RestApiUtil.handleInternalServerError(errorMessage, e, log);
      }
          return null;

  }
}
