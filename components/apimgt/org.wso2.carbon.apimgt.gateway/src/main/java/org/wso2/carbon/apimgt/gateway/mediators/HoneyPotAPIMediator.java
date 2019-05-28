package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.gateway.throttling.HoneyAPIDataPublisher.HoneyAPIDataPublisher;

import java.util.Arrays;
import java.util.Map;

public class HoneyPotAPIMediator extends APIMgtCommonExecutionPublisher {
    private static final Log log = LogFactory.getLog(HoneyPotAPIMediator.class);
    private static final String HEADER_X_FORWARDED_FOR = "X-FORWARDED-FOR";
    //private static volatile HoneyPotAPIDataPublisher honeyPotAPIDataPublisher = null;
    private static volatile HoneyAPIDataPublisher honeyAPIDataPublisher = null;


    @Override
    public boolean mediate(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext msgContext = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        long currentTime = System.currentTimeMillis();
        String messageId = messageContext.getMessageID();
        String apiName = (String) messageContext.getProperty("SYNAPSE_REST_API");
        String apiVersion = (String) messageContext.getProperty("SYNAPSE_REST_API_VERSION");
        String apiContext = (String) messageContext.getProperty("REST_API_CONTEXT");
        String fullRequestPath = (String) messageContext.getProperty("REST_FULL_REQUEST_PATH");
        String apiMethod = (String) msgContext.getProperty("HTTP_METHOD");
        String headerName = "";
        String headerValue = "";
        String messageBody = String.valueOf(msgContext.getEnvelope().getBody().getFirstOMChild());
        String headerSet = "";
        String clientIp = "";

        if (messageBody.equals("null")) {
            messageBody = "No message body passed";
        }

        Map<String, String> headers =
                (Map) msgContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        String xForwardForHeader = (String) headers.get(HEADER_X_FORWARDED_FOR);
        if (!StringUtils.isEmpty(xForwardForHeader)) {
            clientIp = xForwardForHeader;
            int idx = xForwardForHeader.indexOf(',');
            if (idx > -1) {
                clientIp = clientIp.substring(0, idx);
            }
        } else {
            clientIp = (String) msgContext.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        }

        if (log.isDebugEnabled()) {
            log.debug("Detected Anonymous User : MessageId :" + messageId + " API Name :" + apiName +
                    " API Version :" + apiVersion + " API Context : " + apiContext + " full request path :"
                    + fullRequestPath + " Request Method :" + apiMethod + " Message Body : " + messageBody +
                    " client Ip :" + clientIp);
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            headerName = entry.getKey();
            headerValue = entry.getValue();
            log.debug("Headers Set of " + apiName + " api invocation :" + headerName + ": " + headerValue);
        }
        Object[] objectArray = headers.entrySet().toArray();
        if (objectArray == null) {
            headerSet = "No passed Header";
        } else {
            headerSet = Arrays.toString(objectArray);
        }


        if (honeyAPIDataPublisher == null) {
            synchronized (this) {
                honeyAPIDataPublisher = new HoneyAPIDataPublisher();
            }
        }
        log.info("Started to publish data");
        honeyAPIDataPublisher.publishEvent(currentTime,messageId, apiMethod, headerSet, messageBody, clientIp);
        log.info("End to publish data");
        return true;

    }

}
