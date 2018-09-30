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

package org.wso2.carbon.apimgt.keymgt.service;

public class TracingConstants {

    public static final String VALIDATE_MAIN = "API:Validate_Main";
    public static final String REQUEST_ID = "request-id";
    public static final String GET_ACCESS_TOKEN_CACHE_KEY = "API:Get_Access_Token_Cache_key()";
    public static final String FETCHING_API_KEY_VAL_INFO_DTO_FROM_CACHE = "API:Fetching_API_iNFO_DTO_FROM_CACHE()";
    public static final String VALIDATE_TOKEN = "API:Validate_Token()";
    public static final String VALIDATE_SUBSCRIPTION = "API:Validate_Subscription()";
    public static final String VALIDATE_SCOPES = "API:Validate_Scopes()";
    public static final String GENERATE_JWT = "API:Generate_JWT";
    public static final String WRITE_TO_KEY_MANAGER_CACHE = "API:Write_To_Key_Manager_Cache()";
    public static final String PUBLISHING_KEY_VALIDATION_RESPONSE = "API:Publishing_Key_Validation_Response";
    public static final String TRACING_ENABLED = "OpenTracer.Enabled";
}