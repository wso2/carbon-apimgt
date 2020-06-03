package org.wso2.carbon.apimgt.jms.listener.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupObserver;

/**
 * This Class used to properly start and Close JMS listeners
 */
public class JMSListenerStartupShutdownListener implements ServerStartupObserver, ServerShutdownHandler {

    private Log log = LogFactory.getLog(JMSListenerStartupShutdownListener.class);
    private JMSTransportHandler jmsTransportHandler;

    public JMSListenerStartupShutdownListener() {

        this.jmsTransportHandler = new JMSTransportHandler();
    }

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {

        jmsTransportHandler.subscribeForJmsEvents();
    }

    @Override
    public void invoke() {

        if (jmsTransportHandler != null) {
            // This method will make shutdown the Listener.
            log.debug("Unsubscribe from JMS Events...");
            jmsTransportHandler.unSubscribeFromEvents();
        }
    }
}
