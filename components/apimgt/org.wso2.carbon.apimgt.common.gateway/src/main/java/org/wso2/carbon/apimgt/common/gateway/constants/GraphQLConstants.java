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
}
