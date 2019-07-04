package org.wso2.carbon.apimgt.gateway.throttling.HoneyAPIDataPublisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataBridgeDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.dto.*;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.util.List;

public class BotDataPublisher implements APIMgtUsageDataPublisher {
    private static final Log log   = LogFactory.getLog(BotDataPublisher.class);

    protected DataPublisher dataPublisher;
    private static DataPublisher dataPublisherStatics;

    private static DataPublisher getDataPublisher() {

        //If a DataPublisher had not been registered for the tenant.
        if (dataPublisherStatics == null
                && DataPublisherUtil.getApiManagerAnalyticsConfiguration().getDasReceiverUrlGroups() != null) {

            String serverUser = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getDasReceiverServerUser();
            String serverPassword = DataPublisherUtil.getApiManagerAnalyticsConfiguration()
                    .getDasReceiverServerPassword();
            String serverURL = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getDasReceiverUrlGroups();
            String serverAuthURL = DataPublisherUtil.getApiManagerAnalyticsConfiguration()
                    .getDasReceiverAuthUrlGroups();

            try {
                //Create new DataPublisher for the tenant.
                synchronized (APIMgtUsageDataBridgeDataPublisher.class) {

                    if (dataPublisherStatics == null) {
                        dataPublisherStatics = new DataPublisher(null, serverURL, serverAuthURL, serverUser,
                                serverPassword);
                    }
                }
            }  catch (DataEndpointConfigurationException e) {
                log.error("Error while creating data publisher", e);
            } catch (DataEndpointException e) {
                log.error("Error while creating data publisher", e);
            } catch (DataEndpointAgentConfigurationException e) {
                log.error("Error while creating data publisher", e);
            } catch (TransportException e) {
                log.error("Error while creating data publisher", e);
            } catch (DataEndpointAuthenticationException e) {
                log.error("Error while creating data publisher", e);
            }
        }

        return dataPublisherStatics;
    }

    @Override
    public void init() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing APIMgtUsageDataBridgeDataPublisher");
            }

            this.dataPublisher = getDataPublisher();

        } catch (Exception e) {
            log.error("Error initializing APIMgtUsageDataBridgeDataPublisher", e);
        }
    }

    @Override
    public void publishEvent(FaultPublisherDTO faultPublisherDTO) {

    }

    @Override
    public void publishEvent(ThrottlePublisherDTO throttlePublisherDTO) {

    }

    @Override
    public void publishEvent(AlertTypeDTO alertTypeDTO) throws APIManagementException {

    }

    @Override
    public void publishEvent(RequestResponseStreamDTO requestStream) {

    }

    @Override
    public void publishEvent(BotDataDTO botDataDTO) {


    }
}
