package org.wso2.carbon.apimgt.rest.api.utils.mappings;

import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.rest.api.dto.SubscriptionDTO;

public class SubscriptionMappingUtil {
    public static SubscriptionDTO fromSubscriptiontoDTO(SubscribedAPI subscription) {
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setSubscriptionId(subscription.getUUID());
        subscriptionDTO.setApiId(subscription.getApiId().toString());
        subscriptionDTO.setApplicationId(subscription.getApplication().getUUID());
        subscriptionDTO.setStatus(subscription.getSubStatus());
        subscriptionDTO.setTier(subscription.getTier().getName());
        return subscriptionDTO;
    }
}