package org.wso2.carbon.apimgt.impl.portalNotifications;

public interface PortalNotificationService<T> {

    void sendPortalNotifications(T notificationObject, String tenantDomain);
    
}
