/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.notification;

/**
 * Contains the Constraints used by notification feature
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
    public final static String SUBSCRIBERS_PER_API = "subscriber_list";
    public final static String SUBSCRIBERS_PER_PROVIDER = "subscriber_list_for_provider";


    public static final String API_TENANT_CONF = "tenant-conf.json";
    public static final String APIMGT_REGISTRY_LOCATION = "/apimgt";
    public static final String API_APPLICATION_DATA_LOCATION = APIMGT_REGISTRY_LOCATION +"/applicationdata";
    public static final String API_TENANT_CONF_LOCATION = API_APPLICATION_DATA_LOCATION + "/" + API_TENANT_CONF;

    public final static String TYPE_KEY = "Type";
    public final static String TITLE_KEY = "Title";
    public final static String TEMPLATE_KEY = "Template";
    public final static String CLASS_KEY = "Notifiers";
    public final static String NOTIFIERS_KEY = "Notifiers";
    public final static String Notification_KEY = "notification";
    public final static String Notifications_KEY = "Notifications";

    public final static String NOTIFICATION_TYPE_NEW_VERSION = "new_api_version";
    public final static String NOTIFICATIONS_ENABLED = "NotificationsEnabled";
    public final static String CLAIMS_RETRIEVER_IMPL_CLASS = "ClaimsRetrieverImplClass";
}
