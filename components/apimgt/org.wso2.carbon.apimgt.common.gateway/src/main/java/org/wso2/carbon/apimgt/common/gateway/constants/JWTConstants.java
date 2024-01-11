/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.common.gateway.constants;

/**
 * Constants related to jwt generation.
 */
public class JWTConstants {
    public static final String EXPIRY_TIME = "exp";
    public static final String ISSUED_TIME = "iat";

    public static final String SUPER_TENANT_DOMAIN = "carbon.super";
    public static final String TENANT_DOMAIN_COMBINER = "@";

    public static final String API_NAME = "name";
    public static final String SUBSCRIPTION_TIER = "subscriptionTier";
    public static final String SUBSCRIBER_TENANT_DOMAIN = "subscriberTenantDomain";

    public static final String APPLICATION = "application";
    public static final String APPLICATION_ID = "id";
    public static final String APPLICATION_NAME = "name";
    public static final String APPLICATION_TIER = "tier";
    public static final String APPLICATION_OWNER = "owner";
    public static final String AUTH_APPLICATION_USER_LEVEL_TOKEN = "Application_User";

    public static final String REST_API_CONTEXT = "REST_API_CONTEXT";
    public static final String SYNAPSE_REST_API_VERSION = "SYNAPSE_REST_API_VERSION";

    public static final String CONSUMER_KEY = "consumerKey";
    public static final String AUTHORIZED_PARTY = "azp";
    public static final String SCOPE = "scope";
    public static final String SCOPE_DELIMITER = " ";
    public static final String OAUTH2_DEFAULT_SCOPE = "default";
    public static final String AUTHORIZED_USER_TYPE = "aut";

    public static final String SUB = "sub";
    public static final String ORGANIZATIONS = "organizations";
    public static final String GATEWAY_JWKS_API_CONTEXT = "/jwks";
    public static final String GATEWAY_JWKS_API_NAME = "_JwksEndpoint_";
}
