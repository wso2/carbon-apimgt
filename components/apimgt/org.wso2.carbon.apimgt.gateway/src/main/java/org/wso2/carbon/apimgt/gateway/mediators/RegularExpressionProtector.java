package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.http.HttpStatus;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;

import java.util.Map;
import java.util.regex.Pattern;

public class RegularExpressionProtector extends AbstractMediator {
    org.apache.axis2.context.MessageContext a2mc;
    private static Pattern pattern;

    @Override
    public boolean mediate(MessageContext messageContext) {
        a2mc  = ((Axis2MessageContext)messageContext).getAxis2MessageContext();
        regexCompile(messageContext);
        String queryParams = getQueryParams(a2mc);
        Map transportHeaders = getTransportHeaders(a2mc);
        String payload = getPayloadString(a2mc);

        if (queryParams != null && pattern.matcher(queryParams).find()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Threat detected in query parameters [ %s ] by regex [ %s ]",
                        queryParams, pattern));
            }
            handleThreat(messageContext,
                    APIMgtGatewayConstants.QPARAM_THREAT_CODE,
                    APIMgtGatewayConstants.QPARAM_THREAT_MSG,
                    APIMgtGatewayConstants.QPARAM_THREAT_DESC);
            return false;
        }
        if (transportHeaders != null && pattern.matcher(transportHeaders.toString()).find()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Threat detected in Transport headers [ %s ] by regex [ %s ]",
                        transportHeaders, pattern));
            }
            handleThreat(messageContext,
                    APIMgtGatewayConstants.HTTP_HEADER_THREAT_CODE,
                    APIMgtGatewayConstants.HTTP_HEADER_THREAT_MSG,
                    APIMgtGatewayConstants.HTTP_HEADER_THREAT_DESC);
            return false;
        }
        if (payload != null && pattern.matcher(payload).find()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Threat detected in request payload [ %s ] by regex [ %s ]))",
                          payload, pattern));
            }
            handleThreat(messageContext,
                         APIMgtGatewayConstants.PAYLOAD_THREAT_CODE,
                         APIMgtGatewayConstants.PAYLOAD_THREAT_MSG,
                         APIMgtGatewayConstants.PAYLOAD_THREAT_DESC);
        }
        return true;

    }

    //get query parameters
    private String getQueryParams(org.apache.axis2.context.MessageContext a2mc) {
        return (String) a2mc.getProperty(NhttpConstants.REST_URL_POSTFIX);
    }

    // get headers
    private Map getTransportHeaders(org.apache.axis2.context.MessageContext a2mc) {
        return (Map) a2mc.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    //get payload
    private String getPayloadString(org.apache.axis2.context.MessageContext a2mc) {
        return JsonUtil.jsonPayloadToString(a2mc);
    }

    private void regexCompile(MessageContext messageContext) {
        pattern = Pattern.compile((String)messageContext.getProperty("regex"), Pattern.CASE_INSENSITIVE);

    }

    private void handleThreat(MessageContext messageContext, String threatCode, String threatMsg, String threatDesc) {
        messageContext.setProperty(APIMgtGatewayConstants.THREAT_FOUND, true);
        messageContext.setProperty(APIMgtGatewayConstants.THREAT_CODE, threatCode);
        messageContext.setProperty(APIMgtGatewayConstants.THREAT_MSG, threatMsg);
        messageContext.setProperty(APIMgtGatewayConstants.THREAT_DESC, threatDesc);
        Mediator sequence = messageContext.getSequence(APIMgtGatewayConstants.THREAT_FAILURE_HANDLER);

        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return;
        }

        // By default we send a 401 response back
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        // This property need to be set to avoid sending the content in pass-through pipe (request message)
        // as the response.
        axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
        try {
            RelayUtils.consumeAndDiscardMessage(axis2MC);
        } catch (AxisFault axisFault) {
            //In case of an error it is logged and the process is continued because we're setting a fault message in the payload.
            log.error("Error occurred while consuming and discarding the message", axisFault);
        }
        axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/soap+xml");

        if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, getFaultPayload(threatCode, threatMsg, threatDesc));
        } else {
            Utils.setSOAPFault(messageContext, "Client", threatMsg, threatDesc);
        }
        Utils.sendFault(messageContext, HttpStatus.SC_BAD_REQUEST);
    }

    private OMElement getFaultPayload(String threatStatus, String threatMsg, String threatDescription) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APISecurityConstants.API_SECURITY_NS,
                APISecurityConstants.API_SECURITY_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(String.valueOf(threatStatus));
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText(threatMsg);
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(threatDescription);

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }
}
