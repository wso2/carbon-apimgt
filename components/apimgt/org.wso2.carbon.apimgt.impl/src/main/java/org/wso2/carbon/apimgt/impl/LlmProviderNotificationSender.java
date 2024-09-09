package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.notifier.events.LlmProviderEvent;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.UUID;

public class LlmProviderNotificationSender {

    /**
     * Sends a notification about an LLM Provider event with the specified details.
     *
     * @param name The name of the LLM Provider.
     * @param apiVersion The API version of the LLM Provider.
     * @param organization The organization associated with the LLM Provider.
     * @param configurations The LLM Provider configurations.
     * @param action The action related to the LLM Provider (e.g., create, update).
     * @throws APIManagementException If an error occurs while sending the notification.
     */
    public void notify(String name, String apiVersion, String organization, String configurations, String action) throws APIManagementException {

        int tenantId = APIUtil.getInternalOrganizationId(organization);
        LlmProviderEvent llmProviderEvent = new LlmProviderEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), action, tenantId, organization, name, apiVersion, configurations);
        APIUtil.sendNotification(llmProviderEvent, APIConstants.NotifierType.LLM_PROVIDER.name());
    }
}
