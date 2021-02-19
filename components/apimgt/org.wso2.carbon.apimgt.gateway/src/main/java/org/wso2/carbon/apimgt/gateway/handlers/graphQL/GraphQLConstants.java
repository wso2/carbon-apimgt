/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.gateway.handlers.graphQL;

import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;

/**
 * GraphQL related gateway constants
 */
public class GraphQLConstants {
    public static final int GRAPHQL_QUERY_TOO_DEEP = APIThrottleConstants.GRAPHQL_QUERY_TOO_DEEP;
    public static final String GRAPHQL_QUERY_TOO_DEEP_MESSAGE = "QUERY TOO DEEP";

    public static final int GRAPHQL_QUERY_TOO_COMPLEX = APIThrottleConstants.GRAPHQL_QUERY_TOO_COMPLEX;
    public static final String GRAPHQL_QUERY_TOO_COMPLEX_MESSAGE = "QUERY TOO COMPLEX";

    public static final int GRAPHQL_INVALID_QUERY = 900422;
    public static final String GRAPHQL_API_FAILURE_HANDLER = "_graphql_failure_handler";
    public static final String GRAPHQL_INVALID_QUERY_MESSAGE= "INVALID QUERY";

}
