package org.wso2.carbon.apimgt.impl.Notification;

import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

/**
 * This class is used to save all the notification related fields
 */
public class NotificationDTO {

    private String title;
    private String message;
    private String type;
    private Properties properties;
    private Map<String,ArrayList<String>> notifierMap;

    public NotificationDTO(Properties properties, String type) {
        this.properties = properties;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String subject) {
        this.title = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Map<String, ArrayList<String>> getNotifierMap() {
        return notifierMap;
    }

    public void setNotifierMap(Map<String, ArrayList<String>> notifierMap) {
        this.notifierMap = notifierMap;
    }
}
