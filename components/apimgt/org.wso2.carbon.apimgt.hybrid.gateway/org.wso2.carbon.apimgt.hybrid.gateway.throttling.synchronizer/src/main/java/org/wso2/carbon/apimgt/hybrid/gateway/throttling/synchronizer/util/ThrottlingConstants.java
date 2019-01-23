/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.util;

/**
 * Constants
 */
public class ThrottlingConstants {

    public static final String TOKEN_TIER_VIEW_SCOPE = "apim:tier_view";
    public static final String BLOCKING_CONDITION_VIEW_SCOPE = "apim:bl_view";

    public static final String SUBSCRIPTION_POLICIES_SUFFIX = "/api/am/admin/{version}/throttling/policies/subscription";
    public static final String APPLICATION_POLICIES_SUFFIX = "/api/am/admin/{version}/throttling/policies/application";
    public static final String ADVANCED_POLICIES_SUFFIX = "/api/am/admin/{version}/throttling/policies/advanced";
    public static final String BLOCKING_POLICIES_SUFFIX = "/api/am/admin/{version}/throttling/blacklist";
    public static final String DEFAULT_API_ADMIN_URL = "https://localhost:9443";

    public static final String API_VERSION_PROPERTY = "rest.api.version";
    public static final String API_VERSION_PARAM = "{version}";
    public static final String CLOUD_API = "cloud";
    public static final String API_DEFAULT_VERSION = "v0.14";
    public static final String URL_PATH_SEPARATOR = "/";
    public static final String EMPTY_STRING = "";

}
