package org.wso2.carbon.apimgt.usage.publisher;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.usage.publisher.dto.ThrottlePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/*
* This is the class mediator which will handle publishing events upon throttle out events
*/
public class APIMgtThrottleUsageHandler extends AbstractMediator {

    private boolean enabled = UsageComponent.getApiMgtConfigReaderService().isEnabled();

    private volatile APIMgtUsageDataPublisher publisher;

    public APIMgtThrottleUsageHandler() {

        if (!enabled) {
            return;
        }

        if (publisher == null) {
            synchronized (this) {
                if (publisher == null) {
                    String publisherClass = UsageComponent.getApiMgtConfigReaderService().getPublisherClass();
                    try {
                        log.debug("Instantiating Data Publisher");
                        publisher = (APIMgtUsageDataPublisher) Class.forName(publisherClass).
                                newInstance();
                        publisher.init();
                    } catch (ClassNotFoundException e) {
                        log.error("Class not found " + publisherClass);
                    } catch (InstantiationException e) {
                        log.error("Error instantiating " + publisherClass);
                    } catch (IllegalAccessException e) {
                        log.error("Illegal access to " + publisherClass);
                    }
                }
            }
        }
    }

    public boolean mediate(MessageContext messageContext) {

        try {
            if (!enabled) {
                return true;
            }
            // gets the access token and username
            AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext
                    (messageContext);
            if (authContext != null) {
                long currentTime = System.currentTimeMillis();
                ThrottlePublisherDTO throttlePublisherDTO = new ThrottlePublisherDTO();
                throttlePublisherDTO.setAccessToken(authContext.getApiKey());
                String username = (String) messageContext.getProperty(
                        APIMgtUsagePublisherConstants.USER_ID);
                throttlePublisherDTO.setUsername(username);
                throttlePublisherDTO.setTenantDomain(MultitenantUtils.getTenantDomain(username));
                throttlePublisherDTO.setApiname((String) messageContext.getProperty(
                        APIMgtUsagePublisherConstants.API));
                throttlePublisherDTO.setVersion((String) messageContext.getProperty(
                        APIMgtUsagePublisherConstants.API_VERSION));
                throttlePublisherDTO.setContext((String) messageContext.getProperty(
                        APIMgtUsagePublisherConstants.CONTEXT));
                throttlePublisherDTO.setProvider((String) messageContext.getProperty(
                        APIMgtUsagePublisherConstants.API_PUBLISHER));
                throttlePublisherDTO.setApplicationName((String) messageContext.getProperty(
                        APIMgtUsagePublisherConstants.APPLICATION_NAME));
                throttlePublisherDTO.setApplicationId((String) messageContext.getProperty(
                        APIMgtUsagePublisherConstants.APPLICATION_ID));
                throttlePublisherDTO.setThrottledTime(currentTime);
                publisher.publishEvent(throttlePublisherDTO);


            }


        } catch (Throwable e) {
            log.error("Cannot publish throttling event. " + e.getMessage(), e);
        }
        return true; // Should never stop the message flow
    }

    public boolean isContentAware() {
        return false;
    }
}

