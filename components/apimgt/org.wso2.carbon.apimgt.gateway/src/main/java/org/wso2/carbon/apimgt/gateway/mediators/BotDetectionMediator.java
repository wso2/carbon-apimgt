package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.dto.BotDataDTO;

import javax.cache.Cache;
import java.util.Arrays;
import java.util.Map;

/**
 * This @BotDetectionMediator mediator refers at the OpenService api
 */
public class BotDetectionMediator extends APIMgtCommonExecutionPublisher {
    private static final Log log = LogFactory.getLog(BotDetectionMediator.class);
    private static final String BOT_ACCESS_COUNT_CACHE = "BOT_ACCESS_CACHE";
    private final Cache botAccessCountCache;
    private int throttleLimit = 2;

    /**
     *
     */
    public BotDetectionMediator() {
        super();
        botAccessCountCache = APIUtil.getCache(APIConstants.API_MANAGER_CACHE_MANAGER, BOT_ACCESS_COUNT_CACHE, 60, 60);
    }

    @Override
    public boolean mediate(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext msgContext = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        String clientIP = DataPublisherUtil.getClientIp(msgContext);
        if(isThrottledOut(clientIP)){
            messageContext.setProperty("BOT_THROTTLED_OUT",true);
            messageContext.setProperty("BOT_IP",clientIP);
            return true;
        }
        String messageBody;
        long currentTime = System.currentTimeMillis();
        String messageId = messageContext.getMessageID();
        String apiMethod = (String) msgContext.getProperty("HTTP_METHOD");
        try {
            RelayUtils.buildMessage(msgContext);
            SOAPEnvelope messageEnvelop = msgContext.getEnvelope();
            if (messageEnvelop != null && messageEnvelop.getBody() != null) {
                messageBody = String.valueOf(messageEnvelop.getBody());
            } else {
                messageBody = "Empty Message";
            }
        } catch (Exception e) {
            messageBody = "Malformed Message";
        }

        String headerSet = getPassedHeaderSet(msgContext);

        log.info(String.format("MessageId : %s | Request Method : %s | Message Body : %s | client Ip : %s | " +
                "Headers set : %s", messageId, apiMethod, messageBody, clientIP, headerSet));

        if (!enabled) {
            return true;
        }

        BotDataDTO botDataDTO = new BotDataDTO();
        botDataDTO.setCurrentTime(currentTime);
        botDataDTO.setMessageID(messageId);
        botDataDTO.setApiMethod(apiMethod);
        botDataDTO.setHeaderSet(headerSet);
        botDataDTO.setMessageBody(messageBody);
        botDataDTO.setClientIp(clientIP);
        publisher.publishEvent(botDataDTO);
        return true;
    }

    /**
     * @param msgContext get message context to get the header set
     * @return passed header set
     */
    private String getPassedHeaderSet(org.apache.axis2.context.MessageContext msgContext) {
        Map headers = (Map) (msgContext).getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Object[] headersArray = headers.entrySet().toArray();
        return Arrays.toString(headersArray);
    }

    /**
     * @param clientIp
     * @return
     */
    private boolean isThrottledOut(String clientIp){
        synchronized ("BOT_DETECTION_".concat(clientIp).intern()){
            int counter = 0;
            Object counterObject = botAccessCountCache.get(clientIp);
            if (counterObject != null){
                counter = (int) counterObject;
                if(counter > throttleLimit){
                    return true;
                }
            }
            botAccessCountCache.put(clientIp,++counter);
            return false;
        }
    }

    public int getThrottleLimit() {
        return throttleLimit;
    }

    public void setThrottleLimit(int throttleLimit) {
        this.throttleLimit = throttleLimit;
    }
}
