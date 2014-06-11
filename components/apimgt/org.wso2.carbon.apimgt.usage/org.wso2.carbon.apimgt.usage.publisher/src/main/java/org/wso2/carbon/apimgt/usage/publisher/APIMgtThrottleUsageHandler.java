package org.wso2.carbon.apimgt.usage.publisher;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.usage.publisher.dto.FaultPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ThrottlePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class APIMgtThrottleUsageHandler extends AbstractMediator{

    private boolean enabled = UsageComponent.getApiMgtConfigReaderService().isEnabled();

    private volatile APIMgtUsageDataPublisher publisher;

    private String publisherClass = UsageComponent.getApiMgtConfigReaderService().getPublisherClass();

    public APIMgtThrottleUsageHandler(){

        if (!enabled) {
            return;
        }

        if (publisher == null) {
            synchronized (this){
                if (publisher == null) {
                    try {
                        log.debug("Instantiating Data Publisher");
                        publisher = (APIMgtUsageDataPublisher)Class.forName(publisherClass).newInstance();
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

        try{
            if (!enabled) {
                return true;
            }
            // gets the access token and username
            AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(messageContext);
            long currentTime = System.currentTimeMillis();
            if(authContext!=null){
                ThrottlePublisherDTO throttlePublisherDTO = new ThrottlePublisherDTO();
                throttlePublisherDTO.setAccessToken(authContext.getApiKey());
                String username=(String) messageContext.getProperty("END_USER_NAME");
                throttlePublisherDTO.setUsername(username);
                throttlePublisherDTO.setTenantDomain(MultitenantUtils.getTenantDomain(username));
                throttlePublisherDTO.setApiname((String) messageContext.getProperty("API_NAME"));
                throttlePublisherDTO.setVersion((String) messageContext.getProperty("SYNAPSE_REST_API_VERSION"));
                throttlePublisherDTO.setContext((String) messageContext.getProperty("REST_API_CONTEXT"));
                throttlePublisherDTO.setProvider((String) messageContext.getProperty("API_PUBLISHER"));
                throttlePublisherDTO.setAccessLevel(authContext.getTier());
                throttlePublisherDTO.setRequestTime(currentTime);
                publisher.publishEvent(throttlePublisherDTO);


            }


        }catch (Throwable e){
            log.error("Cannot publish event. " + e.getMessage(), e);
        }
        return true; // Should never stop the message flow
    }

    public boolean isContentAware(){
        return false;
    }
}
