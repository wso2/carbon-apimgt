package org.wso2.carbon.apimgt.impl.Notification;

/**
 * Contains the Constraints used by Notification feature
 */
public class NotifierConstants {

    public final static String ADAPTER_NAME="email";
    public final static String EMAIL_FORMAT_TEXT ="text/plain";
    public final static String EMAIL_FORMAT_HTML ="text/html";
    public final static String EMAIL_CLAIM="http://wso2.org/claims/emailaddress";
    public final static String EMAIL_SUBJECT_KEY = "email.subject";
    public final static String EMAIL_ADDRESS_KEY = "email.address";
    public final static String EMAIL_LIST_KEY = "email.list";
    public final static String EMAIL_ADAPTER_TYPE = "email";
    public final static String EMAIL_TYPE_KEY = "email.type";
    public final static String API_KEY = "api";
    public final static String NEW_API_KEY = "new_api";


    public static final String API_TENANT_CONF = "tenant-conf.json";
    public static final String APIMGT_REGISTRY_LOCATION = "/apimgt";
    public static final String API_APPLICATION_DATA_LOCATION = APIMGT_REGISTRY_LOCATION +"/applicationdata";
    public static final String API_TENANT_CONF_LOCATION = API_APPLICATION_DATA_LOCATION + "/" + API_TENANT_CONF;

    public final static String TYPE_KEY = "Type";
    public final static String TITLE_KEY = "Title";
    public final static String MESSAGE_KEY = "Message";
    public final static String Notification_KEY = "Notification";
    public final static String Notifications_KEY = "Notifications";

    public final static String NOTIFICATION_TYPE_NEW_VERSION = "new_version";
    public final static String NOTIFIER_CLASS = "Notification.NotifcationClass";
    public final static String NOTIFICATION_ENABLED = "Notification.Enabled";

}
