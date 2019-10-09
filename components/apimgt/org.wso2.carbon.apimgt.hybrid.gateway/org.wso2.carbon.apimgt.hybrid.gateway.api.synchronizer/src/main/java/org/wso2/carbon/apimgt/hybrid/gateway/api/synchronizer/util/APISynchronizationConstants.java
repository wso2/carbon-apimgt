/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.util;

/**
 * Constants related to API synchronization
 */
public class APISynchronizationConstants {
    public static final String EMPTY_STRING = "";
    public static final String API_MEDIATION_POLICY_VIEW_SCOPE = "apim:mediation_policy_view";
    public static final String API_VIEW_SCOPE = "apim:api_view";
    public static final String API_PUBLISHER_URL_PROPERTY = "api.publisher.url";
    public static final String API_VERSION_PROPERTY = "rest.api.version";
    public static final String DEFAULT_API_PUBLISHER_URL = "https://localhost:9443";
    public static final String DEFAULT_API_UPDATE_URL_PROPERTY = "api.lifecycle.event.publisher.url";
    public static final String API_VIEW_PATH = "/api/am/publisher/{version}/apis";
    public static final String API_VIEW_MEDIATION_POLICY_PATH = "/policies/mediation";
    public static final String API_VIEW_GLOBAL_MEDIATION_POLICY_PATH = "/api/am/publisher/{version}/policies/mediation";
    public static final String DEFAULT_API_UPDATE_SERVICE_URL = "https://localhost:9443/micro-gateway/v0.9/updated-apis";
    public static final String API_NAME = "name";
    public static final String API_SEQUENCE = "sequence";
    public static final String API_VERSION_PARAM = "{version}";
    public static final String API_DEFAULT_VERSION = "v0.15";
    public static final String URL_PATH_SEPARATOR = "/";
    public static final String CLOUD_API = "cloud";
    public static final String QUESTION_MARK = "?";
    public static final String OFFSET_PREFIX = "offset=";
    public static final String API_SEARCH_LABEL_QUERY_PREFIX = "query=label:";
    public static final String AMPERSAND = "&";
    public static final String PAGINATION_LIMIT_PREFIX = "limit=";
    public static final String PAGINATION_LIMIT = "500";
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String SEQUENCE_NAME = "name";
}
