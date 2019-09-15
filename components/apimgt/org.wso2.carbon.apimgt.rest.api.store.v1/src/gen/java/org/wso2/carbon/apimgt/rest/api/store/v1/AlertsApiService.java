package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertConfigListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertTypesListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertsInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface AlertsApiService {
      public Response addAlertConfig(AlertConfigListDTO body, MessageContext messageContext) throws APIManagementException;
      public Response deleteAlertConfig(AlertConfigListDTO body, MessageContext messageContext) throws APIManagementException;
      public Response getAlertConfigs(MessageContext messageContext) throws APIManagementException;
      public Response getStoreAlertTypes(MessageContext messageContext) throws APIManagementException;
      public Response getSubscribedAlertTypes(MessageContext messageContext) throws APIManagementException;
      public Response subscribeToAlerts(AlertsInfoDTO body, MessageContext messageContext) throws APIManagementException;
      public Response unsubscribeAllAlerts(MessageContext messageContext) throws APIManagementException;
}
