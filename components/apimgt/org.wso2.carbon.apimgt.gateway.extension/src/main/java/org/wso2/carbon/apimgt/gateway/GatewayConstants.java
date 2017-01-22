package org.wso2.carbon.apimgt.gateway;
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

/**
 * Defining common constant value used in the gateway
 */
public class GatewayConstants {
    public static final String EVENT_DTO_PROPERTY_NAME = "statDto";

    //Subscription data related constants
    public static final String ACTION = "ACTION";
    public static final String ACTION_NEW = "NEW";
    public static final String ACTION_REMOVED = "REMOVED";
    public static final String API_CONTEXT = "API_CONTEXT";
    public static final String API_VERSION = "API_VERSION";
    public static final String API_PROVIDER = "API_PROVIDER";
    public static final String CONSUMER_KEY = "CONSUMER_KEY";
    public static final String SUBSCRIPTION_POLICY = "SUBSCRIPTION_POLICY";
    public static final String APPLICATION_NAME = "APPLICATION_NAME";
    public static final String APPLICATION_OWNER = "APPLICATION_OWNER";
    public static final String KEY_ENV_TYPE = "KEY_ENV_TYPE";

    public static final String SUBSCRIPTION_TOPIC_NAME = "APISubscriptionTopic";
    public static final String APPLICATION_ID = "api.ut.application.id";
    public static final String END_USER_NAME = "api.ut.userName";
    public static final String REQUEST_RECEIVED_TIME = "wso2statistics.request.received.time";
    public static final String REST_FULL_REQUEST_PATH = "REST_FULL_REQUEST_PATH";
}
