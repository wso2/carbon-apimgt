/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.api.model;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents an evaluation status of a request
 */
public enum ComplianceEvaluationStatus {
    PENDING,
    PROCESSING;

    private static final Map<String, ComplianceEvaluationStatus> STRING_TO_ENUM =
            Stream.of(values())
                    .collect(Collectors.toMap(status -> status.name().toLowerCase(Locale.ENGLISH), status -> status));

    public static ComplianceEvaluationStatus fromString(String statusString) {
        return STRING_TO_ENUM.getOrDefault(
                statusString != null ? statusString.toLowerCase(Locale.ENGLISH) : "",
                PENDING
        );
    }
}
