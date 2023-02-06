/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.common.gateway.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constants related to GraphQL operations.
 */
public class GraphQLConstants {

    public static final String QUERY_ANALYSIS_COMPLEXITY = "complexity";
    public static final List<String> QUERY_COMPLEXITY_SLICING_ARGS = Collections.unmodifiableList(
            Arrays.asList("first", "last", "limit"));
    public static final String GRAPHQL_QUERY = "Query";
    public static final String GRAPHQL_MUTATION = "Mutation";
    public static final String GRAPHQL_SUBSCRIPTION = "Subscription";

    public static final int GRAPHQL_QUERY_TOO_DEEP = 900820;
    public static final String GRAPHQL_QUERY_TOO_DEEP_MESSAGE = "QUERY TOO DEEP";

    public static final int GRAPHQL_QUERY_TOO_COMPLEX = 900821;
    public static final String GRAPHQL_QUERY_TOO_COMPLEX_MESSAGE = "QUERY TOO COMPLEX";

    public static final int GRAPHQL_INVALID_QUERY = 900422;
    public static final String GRAPHQL_API_FAILURE_HANDLER = "_graphql_failure_handler";
    public static final String GRAPHQL_INVALID_QUERY_MESSAGE = "INVALID QUERY";

    public static final String MAXIMUM_QUERY_COMPLEXITY = "max_query_complexity";
    public static final String MAXIMUM_QUERY_DEPTH = "max_query_depth";
    public static final String GRAPHQL_MAX_DEPTH = "graphQLMaxDepth";
    public static final String GRAPHQL_MAX_COMPLEXITY = "graphQLMaxComplexity";

    /**
     * GraphQL Constants related to GraphQL Subscription operations
     */
    public static class SubscriptionConstants {
        public static final String HTTP_METHOD_NAME = "SUBSCRIPTION";
        public static final String PAYLOAD_FIELD_NAME_TYPE = "type";
        public static final List<String> PAYLOAD_FIELD_NAME_ARRAY_FOR_SUBSCRIBE =
                Collections.unmodifiableList(Arrays.asList("start", "subscribe"));
        public static final List<String> PAYLOAD_FIELD_NAME_ARRAY_FOR_DATA =
                Collections.unmodifiableList(Arrays.asList("data", "next"));
        public static final String PAYLOAD_FIELD_NAME_PAYLOAD = "payload";
        public static final String PAYLOAD_FIELD_NAME_QUERY = "query";
        public static final String PAYLOAD_FIELD_NAME_ID = "id";
        public static final String PAYLOAD_FIELD_TYPE_ERROR = "error";
    }

    /**
     * Constants related to WebSocket/GraphQL frame errors.
     */
    public static class FrameErrorConstants {
        public static final int BLOCKED_REQUEST = 4006;
        public static final String BLOCKED_REQUEST_MESSAGE = "Blocked from accessing the API";
    }
}
