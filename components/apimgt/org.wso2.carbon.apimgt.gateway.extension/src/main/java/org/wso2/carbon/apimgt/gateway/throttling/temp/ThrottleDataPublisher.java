package org.wso2.carbon.apimgt.gateway.throttling.temp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.gateway.throttling.dto.AuthenticationContextDTO;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * temproray file should be deleted once the C5 datapublishing work is done
 */
public class ThrottleDataPublisher {

    private static final Logger log = LoggerFactory.getLogger(ThrottleDataPublisher.class);

    /**
     * This method used to pass message context and let it run within separate thread.
     *
     * @param messageContext is message context object that holds
     */
    public void publishNonThrottledEvent(
            String applicationLevelThrottleKey, String applicationLevelTier,
            String apiLevelThrottleKey, String apiLevelTier,
            String subscriptionLevelThrottleKey, String subscriptionLevelTier,
            String resourceLevelThrottleKey, String resourceLevelTier,
            String authorizedUser, String apiContext, String apiVersion, String appTenant, String apiTenant,
            String appId, CarbonMessage messageContext,
            AuthenticationContextDTO authenticationContext) {
        try {
            log.info("Throttling event publisher yet to be implemented");
        } catch (Exception e) {
            log.error("Error while publishing throttling events to global policy server", e);
        }
    }
}
