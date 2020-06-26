/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.impl.alertmgt;

import java.util.HashMap;
import java.util.Map;

public class AlertMgtConstants {

    public static final String PUBLISHER_AGENT = "publisher";
    public static final String ADMIN_DASHBOARD_AGENT = "admin-dashboard";
    public static final String APIM_STAKEHOLDER_ALERT_APP = "APIM_STAKEHOLDER_ALERT";
    public static final String APIM_ALERT_CONFIG_APP = "APIM_CONFIGURATION_ALERT";
    public static final String APIM_ALERT_BOT_DETECTION_APP = "APIM_ALERT_BOT_DETECTION_EMAIL";
    public static final String API_NAME_KEY = "apiName";
    public static final String API_CREATOR_KEY = "apiCreator";
    public static final String API_VERSION_KEY = "apiVersion";
    public static final String CONFIG_PROPERTY_KEY = "configProperty";
    public static final String CONFIG_VALUE_KEY = "configValue";
    public static final String API_CREATOR_TENANT_DOMAIN_KEY = "apiCreatorTenantDomain";
    public static final String ABNORMAL_RESPONSE_TIME_ALERT = "AbnormalResponseTime";
    public static final String ABNORMAL_BACKEND_TIME_ALERT = "AbnormalBackendTime";

    public static final String STORE_AGENT = "subscriber";
    public static final String APPLICATION_ID_KEY = "applicationId";
    public static final String REQUEST_COUNT_KEY = "requestCount";

    public static Map<String, String> alertTypeConfigMap;
    static {
        alertTypeConfigMap = new HashMap<>();
        alertTypeConfigMap.put("AbnormalRequestsPerMin", "thresholdRequestCountPerMin");
        alertTypeConfigMap.put("AbnormalResponseTime", "thresholdResponseTime");
        alertTypeConfigMap.put("AbnormalBackendTime", "thresholdBackendTime");
    }
}
