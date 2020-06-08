package org.wso2.carbon.apimgt.impl.notifier;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

public class DeployAPIInGatewayNotifier implements Notifier {

    @Override
    public boolean publishEvent(Event event) throws NotifierException {

        try {
            DeployAPIInGatewayEvent deployAPIInGatewayEvent = (DeployAPIInGatewayEvent) event;
            byte[] bytesEncoded = Base64.encodeBase64(new Gson().toJson(deployAPIInGatewayEvent).getBytes());
            Object[] objects = new Object[]{
                    deployAPIInGatewayEvent.getType(), deployAPIInGatewayEvent.getTimeStamp(), new String(bytesEncoded)};
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
