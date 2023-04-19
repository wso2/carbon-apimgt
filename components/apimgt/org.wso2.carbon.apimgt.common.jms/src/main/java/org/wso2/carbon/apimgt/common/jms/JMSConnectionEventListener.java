package org.wso2.carbon.apimgt.common.jms;

public interface JMSConnectionEventListener {

    void onReconnect();

    void onDisconnect();
}
