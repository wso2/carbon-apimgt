package org.wso2.carbon.apimgt.impl.keymgt;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.eventing.EventPublisherEvent;
import org.wso2.carbon.apimgt.eventing.EventPublisherType;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.KeyManagerEvent;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.UUID;

public class KeyMgtNotificationSender {

    public void notify(KeyManagerConfigurationDTO keyManagerConfigurationDTO,String action) {
        // TODO: (VirajSalaka) remove env flag
        if (!"true".equalsIgnoreCase(System.getenv("EXTERNAL_IDP_ENABLED"))) {
            if (KeyManagerConfiguration.TokenType.EXTERNAL.toString()
                    .equals(keyManagerConfigurationDTO.getTokenType())) {
                return;
            }
        }
        String encodedString = "";
        if (keyManagerConfigurationDTO.getAdditionalProperties() != null){
            String additionalProperties = new Gson().toJson(keyManagerConfigurationDTO.getAdditionalProperties());
            encodedString = new String(Base64.encodeBase64(additionalProperties.getBytes()));
        }
        //handle only super tenant mode in choreo
        KeyManagerEvent keyManagerEvent = new KeyManagerEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                MultitenantConstants.SUPER_TENANT_ID, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, action,
                keyManagerConfigurationDTO.getName(), keyManagerConfigurationDTO.getType(),
                keyManagerConfigurationDTO.isEnabled(), encodedString,
                keyManagerConfigurationDTO.getOrganization(), keyManagerConfigurationDTO.getTokenType());

        byte[] bytesEncoded = Base64.encodeBase64(new Gson().toJson(keyManagerEvent).getBytes());
        Object[] eventObjects = new Object[]{APIConstants.EventType.KEY_MANAGER_CONFIGURATION.name(),
                keyManagerEvent.getTimeStamp(), new String(bytesEncoded)};
        EventHubConfigurationDto eventHubConfigurationDto =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                        .getEventHubConfigurationDto();

        // if the Token Type is EXCHANGED, there is no need to send the key manager event to the gateway
        if (eventHubConfigurationDto.isEnabled()
                && !KeyManagerConfiguration.TokenType.EXCHANGED.toString().equals(
                keyManagerConfigurationDTO.getTokenType())) {
            EventPublisherEvent notificationEvent =
                    new EventPublisherEvent(APIConstants.NOTIFICATION_STREAM_ID,
                    System.currentTimeMillis(), eventObjects, keyManagerEvent.toString());
            APIUtil.publishEvent(EventPublisherType.NOTIFICATION, notificationEvent, keyManagerEvent.toString());
        }
    }
}
