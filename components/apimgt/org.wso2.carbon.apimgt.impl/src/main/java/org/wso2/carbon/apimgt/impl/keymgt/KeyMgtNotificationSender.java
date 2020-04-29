package org.wso2.carbon.apimgt.impl.keymgt;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerConfigurationsDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.databridge.commons.Event;

import java.util.Collections;

public class KeyMgtNotificationSender {

    public void notify(KeyManagerConfigurationDTO keyManagerConfigurationDTO,String action) {
        String encodedString = "";
        if (keyManagerConfigurationDTO.getAdditionalProperties() != null){
            JSONObject jsonObject = new JSONObject();
            jsonObject.putAll(keyManagerConfigurationDTO.getAdditionalProperties());
            encodedString = new String(Base64.encodeBase64(jsonObject.toJSONString().getBytes()));
        }
        Object[] objects = new Object[]{APIConstants.KeyManager.KeyManagerEvent.KEY_MANAGER_CONFIGURATION, action,
                keyManagerConfigurationDTO.getName(), keyManagerConfigurationDTO.getType(),
                keyManagerConfigurationDTO.isEnabled(), encodedString,
                keyManagerConfigurationDTO.getTenantDomain()};
        Event keyManagerEvent = new Event(APIConstants.KeyManager.KeyManagerEvent.KEY_MANAGER_STREAM_ID,
                System.currentTimeMillis(),
                null, null, objects);
        KeyManagerConfigurationsDto keyManagerConfigurationsDto =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                        .getKeyManagerConfigurationsDto();

        if (keyManagerConfigurationsDto.isEnabled()) {
            APIUtil.publishEvent(APIConstants.BLOCKING_EVENT_PUBLISHER, Collections.EMPTY_MAP, keyManagerEvent);
        }
    }
}
