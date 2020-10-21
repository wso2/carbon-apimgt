package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIMonetizationUsageDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WorkflowResponseDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface SubscriptionsApiService {
      public Response subscriptionsGet(String apiId, String applicationId, String groupId, String xWSO2Tenant, Integer offset, Integer limit, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response subscriptionsMultiplePost(List<SubscriptionDTO> body, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response subscriptionsPost(SubscriptionDTO body, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response subscriptionsSubscriptionIdDelete(String subscriptionId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response subscriptionsSubscriptionIdGet(String subscriptionId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response subscriptionsSubscriptionIdPut(SubscriptionDTO body, String subscriptionId, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response subscriptionsSubscriptionIdUsageGet(String subscriptionId, MessageContext messageContext) throws APIManagementException;
}
