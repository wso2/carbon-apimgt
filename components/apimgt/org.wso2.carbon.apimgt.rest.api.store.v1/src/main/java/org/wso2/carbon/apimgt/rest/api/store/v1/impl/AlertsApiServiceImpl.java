package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertConfigDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertConfigListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertTypesListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertsInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class AlertsApiServiceImpl implements AlertsApiService {
      public Response configureAlertUser(String userName, AlertConfigDTO body, MessageContext messageContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response deleteAlertConfigUser(String userName, AlertConfigDTO body, MessageContext messageContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response getAlertConfigsUser(String userName, MessageContext messageContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response getStoreAlertTypes(MessageContext messageContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response getSubscribedAlertTypesByUser(String userName, MessageContext messageContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response subscribeToAlertsByUser(String userName, AlertsInfoDTO body, MessageContext messageContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response unsubscribeAllAlerts(String userName, MessageContext messageContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
}
