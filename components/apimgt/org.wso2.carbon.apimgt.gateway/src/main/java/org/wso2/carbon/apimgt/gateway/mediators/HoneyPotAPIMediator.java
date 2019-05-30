package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.gateway.throttling.HoneyAPIDataPublisher.HoneyAPIDataPublisher;

import java.util.Arrays;
import java.util.Map;

public class HoneyPotAPIMediator extends AbstractMediator {
    private static final Log log = LogFactory.getLog(HoneyPotAPIMediator.class);
    private static final String HEADER_X_FORWARDED_FOR = "X-FORWARDED-FOR";
    private static volatile HoneyAPIDataPublisher honeyAPIDataPublisher = new HoneyAPIDataPublisher();

    @Override
    public boolean mediate(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext msgContext = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        long currentTime = System.currentTimeMillis();
        String messageId = messageContext.getMessageID();
        String apiMethod = (String) msgContext.getProperty("HTTP_METHOD");
        String clientIP = getClientIp(messageContext);
        String messageBody = String.valueOf(msgContext.getEnvelope().getBody().getFirstOMChild());
        if (messageBody.equals("null")) {
            messageBody = "No message body passed";
        }
        String headerSet = getPassedHeaderSet(messageContext);


        log.info("Detected Anonymous User | MessageId :" + messageId + "|" + " Request Method :" + apiMethod + "|" +
                " Message Body : " + messageBody + "|" + " client Ip :" + clientIP + "|" + "Headers set :" + headerSet);

        honeyAPIDataPublisher.publishEvent(currentTime, messageId, apiMethod, headerSet, messageBody, clientIP);
        return true;

    }

    /**
     * Get clientIP from the message context.
     */
    private String getClientIp(MessageContext messageContext) {
        String clientIp;

        org.apache.axis2.context.MessageContext axis2MsgContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map headers =
                (Map) (axis2MsgContext).getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String xForwardForHeader = (String) headers.get(HEADER_X_FORWARDED_FOR);
        if (!StringUtils.isEmpty(xForwardForHeader)) {
            clientIp = xForwardForHeader;
            int idx = xForwardForHeader.indexOf(',');
            if (idx > -1) {
                clientIp = clientIp.substring(0, idx);
            }
        } else {
            clientIp = (String) axis2MsgContext.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        }
        return clientIp;
    }

    private String getPassedHeaderSet(MessageContext messageContext) {
        String headerSet;
        org.apache.axis2.context.MessageContext axis2MsgContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map headers =
                (Map) (axis2MsgContext).getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Object[] headersArray = headers.entrySet().toArray();
        headerSet = Arrays.toString(headersArray);
        return headerSet;
    }
}
