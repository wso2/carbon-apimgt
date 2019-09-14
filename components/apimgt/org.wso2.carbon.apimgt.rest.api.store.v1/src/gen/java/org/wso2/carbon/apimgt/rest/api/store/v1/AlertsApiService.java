package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertConfigDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertConfigListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertTypesListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertsInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface AlertsApiService {
      public Response configureAlertUser(String userName, AlertConfigDTO body, MessageContext messageContext) throws APIManagementException;
      public Response deleteAlertConfigUser(String userName, AlertConfigDTO body, MessageContext messageContext) throws APIManagementException;
      public Response getAlertConfigsUser(String userName, MessageContext messageContext) throws APIManagementException;
      public Response getStoreAlertTypes(MessageContext messageContext) throws APIManagementException;
      public Response getSubscribedAlertTypesByUser(String userName, MessageContext messageContext) throws APIManagementException;
      public Response subscribeToAlertsByUser(String userName, AlertsInfoDTO body, MessageContext messageContext) throws APIManagementException;
      public Response unsubscribeAllAlerts(String userName, MessageContext messageContext) throws APIManagementException;
}
