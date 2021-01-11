/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.DeploymentsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class DeploymentsApiServiceImpl implements DeploymentsApiService {

    private static final Log log = LogFactory.getLog(DeploymentsApiServiceImpl.class);


      public Response deploymentsGet(MessageContext messageContext) {
      try{
          DeploymentsMappingUtil deploymentsMappingUtil = new DeploymentsMappingUtil();
         DeploymentListDTO deploymentListDTO = deploymentsMappingUtil.fromTenantConftoDTO();
          return Response.ok().entity(deploymentListDTO).build();
      }catch (APIManagementException e) {
          String errorMessage = "Error while retrieving Deployments details form tenant-conf.json";
          RestApiUtil.handleInternalServerError(errorMessage, e, log);
      }
          return null;

  }
}
