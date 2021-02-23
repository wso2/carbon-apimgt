package org.wso2.carbon.apimgt.gateway.handlers.streaming.sse.analytics;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.common.gateway.analytics.publishers.dto.Operation;

public class SseSubscriptionEventDataProvider extends AsyncAnalyticsDataProvider {

    private static final String SUBSCRIPTION_EVENT_PREFIX = "subscription:";

    public SseSubscriptionEventDataProvider(MessageContext messageContext) {
        super(messageContext);
    }

    @Override
    public Operation getOperation() {

        Operation operation = super.getOperation();
        operation.setApiResourceTemplate(SUBSCRIPTION_EVENT_PREFIX + operation.getApiResourceTemplate());
        return operation;
    }
}
