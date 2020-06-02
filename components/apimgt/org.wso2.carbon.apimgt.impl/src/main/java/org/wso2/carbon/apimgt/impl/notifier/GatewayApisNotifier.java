package org.wso2.carbon.apimgt.impl.notifier;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.notifier.events.APIGatewayEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

public class GatewayApisNotifier implements Notifier {

    @Override
    public boolean publishEvent(Event event) throws NotifierException {

        try {
            APIGatewayEvent apiGatewayEvent = (APIGatewayEvent) event;
            byte[] bytesEncoded = Base64.encodeBase64(new Gson().toJson(apiGatewayEvent).getBytes());
            Object[] objects = new Object[]{apiGatewayEvent.getType(), apiGatewayEvent.getTimeStamp(), new String(bytesEncoded)};
            org.wso2.carbon.databridge.commons.Event payload = new org.wso2.carbon.databridge.commons.Event(
                    APIConstants.NOTIFICATION_STREAM_ID, System.currentTimeMillis(),
                    null, null, objects);
            APIUtil.publishEvent(APIConstants.NOTIFICATION_EVENT_PUBLISHER, null, payload);
            return true;
        } catch (Exception e) {
            throw new NotifierException(e);
        }
    }

    @Override
    public String getType() {
        return APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name();
    }
}
