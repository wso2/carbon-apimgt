package org.wso2.carbon.apimgt.rest.api.store.v1.common.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.webhooks.Subscription;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.mappings.AsyncAPIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WebhookSubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.Set;

/**
 * This class has Webhooks ApiService related Implementation
 */
public class WebhookServiceImpl {

    private static final Log log = LogFactory.getLog(WebhookServiceImpl.class);

    private WebhookServiceImpl() {
    }

    /**
     *
     * @param applicationId
     * @param apiId
     * @return
     */
    public static WebhookSubscriptionListDTO getWebhooksSubscriptions(String applicationId, String apiId) {

        if (StringUtils.isNotEmpty(applicationId)) {
            String username = RestApiCommonUtil.getLoggedInUsername();
            try {
                APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
                WebhookSubscriptionListDTO webhookSubscriptionListDTO;
                Set<Subscription> subscriptionSet = apiConsumer.getTopicSubscriptions(applicationId, apiId);
                webhookSubscriptionListDTO = AsyncAPIMappingUtil.fromSubscriptionListToDTO(subscriptionSet);
                return webhookSubscriptionListDTO;
            } catch (APIManagementException e) {
                if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
                } else {
                    RestApiUtil.handleInternalServerError("Failed to get topic subscriptions of Async API " + apiId, e,
                            log);
                }
            }
        } else {
            RestApiUtil.handleBadRequest("Application Id cannot be empty", log);
        }
        return null;
    }
}
