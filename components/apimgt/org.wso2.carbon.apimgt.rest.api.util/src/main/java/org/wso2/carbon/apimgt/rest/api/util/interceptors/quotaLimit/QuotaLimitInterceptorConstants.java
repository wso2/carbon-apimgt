/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.util.interceptors.quotaLimit;

public class QuotaLimitInterceptorConstants {
    public static final String APPLICATION_KEY = "application";
    public static final String PATH_TO_MATCH_SLASH = "path_to_match_slash";
    public static final String HTTP_POST = "POST";
    public static final String API_FROM_SCRATCH_PATH = "/apis";
    public static final String API_TYPE_REGULAR = "regular";
    public static final String NEW_API_VERSION_PATH = "/apis/copy-api";
    public static final String IMPORT_OPENAPI_PATH = "/apis/import-openapi";
    public static final String QUERY_PARAM_STRING = "org.apache.cxf.message.Message.QUERY_STRING";
    public static final String QUERY_PARAM_ORGANIZATION_ID = "organizationid";
    public static final String QUOTA_LIMIT_RESOURCE_TYPE = "api:regular";
    public static final String QUOTA_LIMIT_USERID = "";
}
