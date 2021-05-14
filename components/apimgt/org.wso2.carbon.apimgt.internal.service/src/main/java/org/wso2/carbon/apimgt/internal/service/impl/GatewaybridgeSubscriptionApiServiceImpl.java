package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.EventSubscriptionDTO;
import org.wso2.carbon.apimgt.gatewaybridge.webhooks.ExternalGatewayWebhookSubscriptionService;
import org.wso2.carbon.apimgt.gatewaybridge.webhooks.ExternalGatewayWebhookSubscriptionServiceImpl;
import org.wso2.carbon.apimgt.gatewaybridge.dto.WebhookSubscriptionDTO;
import org.wso2.carbon.apimgt.internal.service.GatewaybridgeSubscriptionApiService;
import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class GatewaybridgeSubscriptionApiServiceImpl implements GatewaybridgeSubscriptionApiService {

    private static final Log log = LogFactory.getLog(GatewaybridgeSubscriptionApiServiceImpl.class);
    private final ExternalGatewayWebhookSubscriptionService externalGatewayWebhookSubscription =
            new ExternalGatewayWebhookSubscriptionServiceImpl();


    @Override
    public Response gatewaybridgeSubscriptionPost(EventSubscriptionDTO subscription, MessageContext messageContext) {
        try {
            externalGatewayWebhookSubscription.addExternalGatewaySubscription(
                    new WebhookSubscriptionDTO(subscription.getSubscriberName(),
                            subscription.getCallbackUrl(), subscription.getTopic()));

            return Response.ok().build();
        } catch (APIManagementException e) {

            log.error("Error while processing request", e);
            JSONObject responseObj = new JSONObject();
            responseObj.put("Message", e.getMessage());
            String responseStringObj = String.valueOf(responseObj);

            return Response.serverError().entity(responseStringObj).build();
        }
    }
}
