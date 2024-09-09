package org.wso2.carbon.apimgt.impl.keymgt;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.eventing.EventPublisherEvent;
import org.wso2.carbon.apimgt.eventing.EventPublisherType;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.databridge.commons.Event;

import java.util.Collections;

public class KeyMgtNotificationSender {

    public void notify(KeyManagerConfigurationDTO keyManagerConfigurationDTO,String action) {

        String keyManagerConfiguration = new Gson().toJson(keyManagerConfigurationDTO);
        String encodedString = new String(Base64.encodeBase64(keyManagerConfiguration.getBytes()));
        Object[] objects = new Object[]{APIConstants.KeyManager.KeyManagerEvent.KEY_MANAGER_CONFIGURATION, action,
                keyManagerConfigurationDTO.getName(), keyManagerConfigurationDTO.getType(),
                keyManagerConfigurationDTO.isEnabled(), encodedString,
                keyManagerConfigurationDTO.getOrganization(), keyManagerConfigurationDTO.getTokenType()};
            Event keyManagerEvent = new Event(APIConstants.KeyManager.KeyManagerEvent.KEY_MANAGER_STREAM_ID,
                System.currentTimeMillis(),
                null, null, objects);
        EventHubConfigurationDto eventHubConfigurationDto =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                        .getEventHubConfigurationDto();

        if (eventHubConfigurationDto.isEnabled()) {
            EventPublisherEvent notificationEvent =
                    new EventPublisherEvent(APIConstants.KeyManager.KeyManagerEvent.KEY_MANAGER_STREAM_ID,
                    System.currentTimeMillis(), objects, keyManagerEvent.toString());
            APIUtil.publishEvent(EventPublisherType.KEYMGT_EVENT, notificationEvent, keyManagerEvent.toString());
        }
    }
}
