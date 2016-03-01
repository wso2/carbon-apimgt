package org.wso2.carbon.apimgt.usage.publisher;


import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.dto.ExecutionTimePublisherDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.publisher.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.Map;

import static org.wso2.carbon.apimgt.gateway.handlers.Utils.publishExecutionTime;

public class APIMgtCommonExecutionPublisher extends AbstractMediator {
    protected boolean enabled;

    protected boolean skipEventReceiverConnection;

    protected volatile APIMgtUsageDataPublisher publisher;

    public APIMgtCommonExecutionPublisher() {
        if (ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService() != null) {
            this.initializeDataPublisher();
        }

    }

    @Override
    public boolean mediate(MessageContext messageContext) {
        if (DataPublisherUtil.getApiManagerAnalyticsConfiguration().isAnalyticsEnabled()
                && !skipEventReceiverConnection) {
            Object totalTimeObject = messageContext.getProperty(APIMgtGatewayConstants
                    .REQUEST_EXECUTION_START_TIME);
            long totalTime = 0;
            if (totalTimeObject != null) {
                totalTime = Long.parseLong((String) totalTimeObject);
            }
            publishExecutionTime(messageContext, totalTime, "Total Time");
            Object executionTimeMapObject = messageContext.getProperty("api.execution.time");
            long eventTime = System.currentTimeMillis();
            Map<String, ExecutionTimePublisherDTO> executionTimePublisherDTOMap;
            if (executionTimeMapObject != null && executionTimeMapObject instanceof Map) {
                executionTimePublisherDTOMap = (Map<String, ExecutionTimePublisherDTO>) executionTimeMapObject;
                for (ExecutionTimePublisherDTO executionTimePublisherDTO : executionTimePublisherDTOMap.values()) {
                    if (publisher == null) {
                        initializeDataPublisher();
                    }
                    executionTimePublisherDTO.setEventTime(eventTime);
                    publisher.publishEvent(executionTimePublisherDTO);
                }
                messageContext.setProperty("api.execution.time", null);
            }
        }
        return true;
    }

    protected void initializeDataPublisher() {

        enabled = DataPublisherUtil.getApiManagerAnalyticsConfiguration().isAnalyticsEnabled();
        skipEventReceiverConnection = DataPublisherUtil.getApiManagerAnalyticsConfiguration().
                isSkipEventReceiverConnection();
        if (!enabled || skipEventReceiverConnection) {
            return;
        }
        if (publisher == null) {
            synchronized (this) {
                if (publisher == null) {
                    String publisherClass = DataPublisherUtil.getApiManagerAnalyticsConfiguration()
                            .getPublisherClass();
                    try {
                        log.debug("Instantiating Data Publisher");
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().
                                setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                        APIMgtUsageDataPublisher tempPublisher = (APIMgtUsageDataPublisher) APIUtil.getClassForName
                                (publisherClass).newInstance();
                        tempPublisher.init();
                        publisher = tempPublisher;
                    } catch (ClassNotFoundException e) {
                        log.error("Class not found " + publisherClass, e);
                    } catch (InstantiationException e) {
                        log.error("Error instantiating " + publisherClass, e);
                    } catch (IllegalAccessException e) {
                        log.error("Illegal access to " + publisherClass, e);
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
            }
        }
    }
}
