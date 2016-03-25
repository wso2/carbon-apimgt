package org.wso2.carbon.apimgt.impl.Notification;

import org.wso2.carbon.apimgt.api.model.Subscriber;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * This class will be the default class set in the apimanager-config.xml
 * This class will not send any notifcations
 */
public class SimpleNotifier extends Notifier {
    @Override
    public void sendNotifications(NotificationDTO notification) {
    }

    @Override
    public Map<String, ArrayList<String>> getNotifierMap(Set<Subscriber> subscriberList) {
        return null;
    }

}
