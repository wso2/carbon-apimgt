package org.wso2.carbon.apimgt.gatewaybridge.webhooks;


import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gatewaybridge.dto.WebhookSubscriptionDTO;

/**
 * Subscribe the external gateway to receive events.
 */
public interface ExternalGatewayWebhookSubscriptionService {

    /**
     * Invoke the services needed to
     * store gateway subscriptions in the database.
     * @param webhookSubscriptionDTO the DTO object contains the subscription details
     */
     void addExternalGatewaySubscription(WebhookSubscriptionDTO webhookSubscriptionDTO)
            throws APIManagementException;
}
