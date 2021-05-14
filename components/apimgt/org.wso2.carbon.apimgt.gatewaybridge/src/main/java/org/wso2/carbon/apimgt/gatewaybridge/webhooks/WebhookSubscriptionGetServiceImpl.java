package org.wso2.carbon.apimgt.gatewaybridge.webhooks;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gatewaybridge.dao.WebhooksDAO;
import org.wso2.carbon.apimgt.gatewaybridge.dto.WebhookSubscriptionDTO;

import java.util.List;
/**
 * Get the subscriptions for the topic from database.
 */
public class WebhookSubscriptionGetServiceImpl implements WebhookSubscriptionGetService {

    /**
     *Invoke the services to retrieve
     * subscription list.
     * @param topic the subscribed topic.
     * @return a list of webhook subscription.
     */
    @Override
    public List<WebhookSubscriptionDTO> getWebhookSubscription(String topic) throws APIManagementException {
        WebhooksDAO webhooksDAO = new WebhooksDAO();
        return webhooksDAO.getSubscriptionsList(topic);
    }
}
